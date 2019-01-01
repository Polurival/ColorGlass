package com.github.polurival.colorglass;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.Objects;

/**
 * @author Польщиков Юрий
 */
public class FilterDataModel {

    private final List<Float> waveLengths;
    private final List<Float> bandPasses;

    public FilterDataModel(List<Float> waveLengths, List<Float> bandPasses) {
        if (waveLengths.size() != bandPasses.size()) {
            throw new IllegalArgumentException("list sizes are not equal");
        }
        this.waveLengths = waveLengths;
        this.bandPasses = bandPasses;
    }

    public List<Float> getWaveLengths() {
        return waveLengths;
    }

    public List<Float> getBandPasses() {
        return bandPasses;
    }

    public float getMinWaveLength() {
        return waveLengths.get(0);
    }

    public float getMaxWaveLength() {
        return waveLengths.get(waveLengths.size() - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FilterDataModel that = (FilterDataModel) o;
        return Objects.equals(waveLengths, that.waveLengths) &&
                Objects.equals(bandPasses, that.bandPasses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(waveLengths, bandPasses);
    }

    @NonNull
    @Override
    public String toString() {
        return "FilterDataModel{" + "waveLengths=" + waveLengths +
                ", bandPasses=" + bandPasses +
                '}';
    }
}
