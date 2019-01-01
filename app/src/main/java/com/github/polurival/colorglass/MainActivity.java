package com.github.polurival.colorglass;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.Series;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GraphView graphView;
    private CheckBox multiplyView;
    private EditText waveLengthView;
    private TextView bandpassView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // вью добавленных на график фильтров
        RecyclerView selectedFiltersView = findViewById(R.id.selected_filters_view);
        final SelectedFiltersAdapter selectedFiltersAdapter = new SelectedFiltersAdapter();
        selectedFiltersView.setAdapter(selectedFiltersAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(selectedFiltersAdapter));
        itemTouchHelper.attachToRecyclerView(selectedFiltersView);

        // вью выбора фильтра
        Spinner allFiltersView = findViewById(R.id.all_filters_view);
        final ArrayAdapter<String> allFiltersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        for (FilterData filter : FilterData.values()) {
            allFiltersAdapter.add(filter.getName());
        }
        allFiltersView.setAdapter(allFiltersAdapter);
        allFiltersView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFiltersAdapter.addFilter(FilterData.getFilterDataByName(allFiltersAdapter.getItem(position)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });

        // вью графика
        graphView = findViewById(R.id.graph_view);
        Viewport graphViewport = graphView.getViewport();
        graphViewport.setXAxisBoundsManual(true);
        graphViewport.setMinX(240.0);
        graphViewport.setMaxX(3000.0);
        graphViewport.setYAxisBoundsManual(true);
        graphViewport.setMinY(0.0);
        graphViewport.setMaxY(6.0);

        multiplyView = findViewById(R.id.multiply_view);
        multiplyView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                graphView.removeAllSeries();
                if (isChecked) {
                    graphView.addSeries(XlsFileConverter.getMultipliedLineGraphSeries(getApplicationContext(), selectedFiltersAdapter.getFilters()));
                } else {
                    for (FilterData filterData : selectedFiltersAdapter.getFilters()) {
                        graphView.addSeries(XlsFileConverter.getLineGraphSeries(getApplicationContext(), filterData));
                    }
                }
            }
        });

        // вью ввода длины волны для нахождения значения пропускания
        waveLengthView = findViewById(R.id.input_wave_length_view);
        waveLengthView.addTextChangedListener(new WaveLengthTextWatcher());
        bandpassView = findViewById(R.id.bandpass_view);
    }

    private final class SelectedFiltersAdapter extends RecyclerView.Adapter<SelectedFiltersAdapter.SimpleViewHolder> {

        List<FilterData> filters = new ArrayList<>();

        public void addFilter(@Nullable FilterData filterData) {
            if (filterData != null && !filters.contains(filterData)) {
                filters.add(filterData);
                notifyItemInserted(filters.size());

                if (multiplyView.isChecked()) {
                    graphView.removeAllSeries();
                    graphView.addSeries(XlsFileConverter.getMultipliedLineGraphSeries(getApplicationContext(), filters));
                } else {
                    graphView.addSeries(XlsFileConverter.getLineGraphSeries(getApplicationContext(), filterData));
                }
            }
        }

        public List<FilterData> getFilters() {
            return filters;
        }

        @NonNull
        @Override
        public SimpleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = new TextView(parent.getContext());
            return new SimpleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SimpleViewHolder viewHolder, int position) {
            viewHolder.textView.setText(filters.get(position).getName());
            viewHolder.textView.setTextColor(filters.get(position).getColor());
        }

        @Override
        public int getItemCount() {
            return filters.size();
        }

        public void removeAt(int position) {
            filters.remove(position);
            notifyItemRemoved(position);
        }

        public void removeAll() {
            filters.clear();
            notifyDataSetChanged();
        }

        class SimpleViewHolder extends RecyclerView.ViewHolder {
            private TextView textView;

            private SimpleViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView;
            }
        }
    }

    private class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

        SelectedFiltersAdapter adapter;

        public SwipeToDeleteCallback(SelectedFiltersAdapter adapter) {
            super(0, ItemTouchHelper.LEFT);
            this.adapter = adapter;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            if (multiplyView.isChecked()) {
                graphView.removeAllSeries();
                adapter.removeAll();
            } else {
                int adapterPosition = viewHolder.getAdapterPosition();
                graphView.removeSeries(graphView.getSeries().get(adapterPosition));
                adapter.removeAt(adapterPosition);
            }
        }
    }

    private class WaveLengthTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //do nothing
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //do nothing
        }

        @Override
        public void afterTextChanged(Editable text) {
            List<Series> series = graphView.getSeries();
            if (series != null && !series.isEmpty() && !TextUtils.isEmpty(text)) {
                Double waveLength = Double.valueOf(text.toString());
                Series graphic = series.get(0);
                Iterator bandPassesIterator = graphic.getValues(waveLength, waveLength);
                if (bandPassesIterator != null && bandPassesIterator.hasNext()) {
                    waveLengthView.setTextColor(graphic.getColor());
                    waveLengthView.setHintTextColor(graphic.getColor());

                    DataPoint point = (DataPoint) bandPassesIterator.next();
                    bandpassView.setText(String.format(getString(R.string.bandpass), point.getY()));
                    bandpassView.setTextColor(graphic.getColor());
                }
            }
        }
    }
}
