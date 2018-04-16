package com.kairos.shiftplanning.domain.activityConstraint;

import com.kairos.shiftplanning.domain.Activity;
import com.kairos.shiftplanning.domain.ActivityLineInterval;
import com.kairos.shiftplanning.domain.ShiftRequestPhase;
import com.kairos.shiftplanning.domain.constraints.ScoreLevel;
import com.kairos.shiftplanning.domain.wta.ConstraintHandler;
import org.joda.time.Interval;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScoreHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class MaxDiffrentActivity implements ConstraintHandler {

    //In minutes
    private int maxDiffrentActivity;

    public MaxDiffrentActivity() {
    }

    @Override
    public ScoreLevel getLevel() {
        return level;
    }

    public void setLevel(ScoreLevel level) {
        this.level = level;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    private ScoreLevel level;
    private int weight;

    public MaxDiffrentActivity(int maxDiffrentActivity, ScoreLevel level, int weight) {
        this.maxDiffrentActivity = maxDiffrentActivity;
        this.level = level;
        this.weight = weight;
    }

    public int checkConstraints(ShiftRequestPhase shift){
        Set<Activity> activities = new HashSet<>();
        for (ActivityLineInterval ali:shift.getActivityLineIntervals()) {
             activities.add(ali.getActivity());
        }
        return activities.size()>maxDiffrentActivity?activities.size()-maxDiffrentActivity:0;
    }


}
