package com.kairos.dto.activity.wta.templates;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ActivityCutOffCount {
    private LocalDate startDate;
    private LocalDate endDate;
    private int count;
}
