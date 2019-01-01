package com.github.polurival.colorglass;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Польщиков Юрий
 */
public class XlsFileConverter {

    private static final String TAG = "XlsFileConverter";
    private static final double WAVE_LENGTH_STEP = 5.0;

    @SuppressWarnings("unchecked")
    @NonNull
    public static LineGraphSeries getLineGraphSeries(@NonNull Context context, @NonNull FilterData filterData) {
        FilterDataModel filterDataModel = convertXlsFileToPoints(context, filterData);
        SplineInterpolator monotoneCubicSpline =
                SplineInterpolator.createMonotoneCubicSpline(filterDataModel.getWaveLengths(), filterDataModel.getBandPasses());

        List<DataPoint> dataPoints = new ArrayList<>();
        for (float waveLength = filterDataModel.getMinWaveLength(); waveLength <= filterDataModel.getMaxWaveLength(); waveLength += WAVE_LENGTH_STEP) {
            float bandPass = monotoneCubicSpline.interpolate(waveLength);
            dataPoints.add(new DataPoint(waveLength, bandPass));
        }
        DataPoint[] dataPointsArray = new DataPoint[dataPoints.size()];
        LineGraphSeries lineGraphSeries = new LineGraphSeries(dataPoints.toArray(dataPointsArray));
        lineGraphSeries.setColor(filterData.getColor());
        return lineGraphSeries;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static LineGraphSeries getMultipliedLineGraphSeries(@NonNull Context context, @NonNull List<FilterData> filters) {
        List<Float> waveLengths = new ArrayList<>();
        List<Float> multipliedBandPasses = new ArrayList<>();

        Integer blendedColor = filters.get(0).getColor();
        boolean isFirstFilter = true;
        for (FilterData filterData : filters) {
            FilterDataModel filterDataModel = convertXlsFileToPoints(context, filterData);
            SplineInterpolator monotoneCubicSpline =
                    SplineInterpolator.createMonotoneCubicSpline(filterDataModel.getWaveLengths(), filterDataModel.getBandPasses());

            int position = 0;
            for (float waveLength = filterDataModel.getMinWaveLength(); waveLength <= filterDataModel.getMaxWaveLength(); waveLength += WAVE_LENGTH_STEP) {
                float bandPass = monotoneCubicSpline.interpolate(waveLength);
                if (isFirstFilter) {
                    waveLengths.add(waveLength);
                    multipliedBandPasses.add(bandPass);
                } else {
                    float multipliedBandPass = multipliedBandPasses.get(position) * bandPass;
                    multipliedBandPasses.set(position, multipliedBandPass);
                }
                position++;
            }
            isFirstFilter = false;
            blendedColor = ColorUtils.blendARGB(blendedColor, filterData.getColor(), 0.5F);
        }

        List<DataPoint> dataPoints = new ArrayList<>(waveLengths.size());
        for (int i = 0; i < waveLengths.size(); i++) {
            dataPoints.add(new DataPoint(waveLengths.get(i), multipliedBandPasses.get(i)));
        }
        DataPoint[] dataPointsArray = new DataPoint[dataPoints.size()];
        LineGraphSeries lineGraphSeries = new LineGraphSeries(dataPoints.toArray(dataPointsArray));
        if (blendedColor != null) {
            lineGraphSeries.setColor(blendedColor);
        }
        return lineGraphSeries;
    }

    private static FilterDataModel convertXlsFileToPoints(@NonNull Context context, @NonNull FilterData filterData) {
        List<Float> waveLengths = new ArrayList<>();
        List<Float> bandPasses = new ArrayList<>();

        AssetManager assetManager = context.getAssets();

        try (InputStream inputStream = assetManager.open(filterData.getFileName() + ".xls")) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            for (Iterator<Row> rowIterator = sheet.rowIterator(); rowIterator.hasNext(); ) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();

                Cell waveLengthCell = cellIterator.next();
                float waveLenght = (float) waveLengthCell.getNumericCellValue();
                waveLengths.add(waveLenght);

                Cell bandpassCell = cellIterator.next();
                float bandPass = (float) bandpassCell.getNumericCellValue();
                bandPasses.add(bandPass);
            }
        } catch (IOException | InvalidFormatException | NumberFormatException | IllegalStateException e) {
            Log.e(TAG, e.getMessage());
        }
        return new FilterDataModel(waveLengths, bandPasses);
    }
}
