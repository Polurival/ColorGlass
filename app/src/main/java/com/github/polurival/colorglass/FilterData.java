package com.github.polurival.colorglass;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Польщиков Юрий
 */
public enum FilterData {
    FILTER1("Фильтр 1", "filter1", Color.BLUE),
    FILTER2("Фильтр 2", "filter2", Color.GREEN),
    FILTER3("Фильтр 3", "filter3", Color.RED),
    FILTER4("Фильтр 4", "filter4", Color.MAGENTA);

    private String name;
    private String fileName;
    @ColorInt
    private int color;

    FilterData(String name, String fileName, int color) {
        this.name = name;
        this.fileName = fileName;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public int getColor() {
        return color;
    }

    @Nullable
    public static FilterData getFilterDataByName(@NonNull String name) {
        FilterData filterData = null;
        for (FilterData filter : FilterData.values()) {
            if (filter.name.equals(name)) {
                filterData = filter;
            }
        }
        return filterData;
    }

    @NonNull
    @Override
    public String toString() {
        return "FilterData{" + "name='" + name + '\'' +
                ", fileName='" + fileName + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
