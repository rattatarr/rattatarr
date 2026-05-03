package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import java.io.Serializable;
import java.util.List;

public record YearRewindAvailableYearsWrapper(
        List<Integer> years
) implements Serializable {
    public static YearRewindAvailableYearsWrapper fromList(List<Integer> years) {
        return new YearRewindAvailableYearsWrapper(years);
    }
}
