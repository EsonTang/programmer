package com.prize.statistics.model;

import java.util.List;

/**
 * longbaoxiu
 * 2016/12/19.11:05
 */

public class ExposureDataBlock {
    @Override
    public String toString() {
        return "ExposureDataBlock{" +
                "events=" + events +
                '}';
    }

    public List<ExposureEvent> events;

}
