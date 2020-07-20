package com.kairos.persistence.model.activity.tabs;

import com.kairos.annotations.KPermissionField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by vipul on 24/8/17.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityBonusSettings {
    @KPermissionField
    private String bonusHoursType;
    @KPermissionField
    private boolean overRuleCtaWta;

}