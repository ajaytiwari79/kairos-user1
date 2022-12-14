package com.kairos.dto.activity.auto_gap_fill_settings;

import com.kairos.dto.activity.common.UserInfo;
import com.kairos.dto.user.access_permission.AccessGroupRole;
import com.kairos.enums.auto_gap_fill_settings.AutoFillGapSettingsRule;
import com.kairos.enums.auto_gap_fill_settings.AutoGapFillingScenario;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedHashSet;

@Getter
@Setter
@NoArgsConstructor
public class AutoFillGapSettingsDTO implements Serializable {
    private static final long serialVersionUID = 3797387742395962545L;
    private BigInteger id;
    private Long countryId;
    private Long unitId;
    @NotNull(message = "message.start.date.not.null")
    private LocalDate startDate;
    private LocalDate endDate;
    @NotNull(message = "message.org.type.not.null")
    private Long organizationTypeId;
    @NotNull(message = "message.org.subtype.not.null")
    private Long organizationSubTypeId;
    @NotNull(message = "message.phase.not.null")
    private BigInteger phaseId;
    @NotNull(message = "message.gap.filling.scenario.not.null")
    private AutoGapFillingScenario autoGapFillingScenario;
    @NotEmpty(message = "message.gap.settings.rule.not.empty")
    private LinkedHashSet<AutoFillGapSettingsRule> selectedAutoFillGapSettingsRules;
    @NotNull(message = "message.gap.applicable.for.not.null")
    private AccessGroupRole gapApplicableFor;
    private Date updatedAt;
    private UserInfo lastModifiedBy;
    private boolean published;
    private BigInteger parentId;
    private boolean skipRules;
    private boolean checkedStaffingLevel; // filled the gap after checking staffing level
}
