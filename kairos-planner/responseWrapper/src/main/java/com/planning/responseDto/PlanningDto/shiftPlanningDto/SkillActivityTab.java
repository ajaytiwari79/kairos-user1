package com.planning.responseDto.PlanningDto.shiftPlanningDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vipul on 25/8/17.
 */
public class SkillActivityTab implements Serializable {

    private List<ActivitySkill> activitySkills = new ArrayList<>();

    public SkillActivityTab() {
    }

    public SkillActivityTab(List<ActivitySkill> activitySkills) {
        this.activitySkills = activitySkills;
    }

    public List<ActivitySkill> getActivitySkills() {
        return activitySkills;
    }

    public void setActivitySkills(List<ActivitySkill> activitySkills) {
        this.activitySkills = activitySkills;
    }
}
