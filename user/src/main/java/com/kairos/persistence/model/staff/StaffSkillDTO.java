package com.kairos.persistence.model.staff;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by prabjot on 3/4/17.
 */
@Getter
@Setter
public class StaffSkillDTO {

    private List<Long> removedSkillId;
    private boolean isSelected;
    private List<Long> assignedSkillIds;
}
