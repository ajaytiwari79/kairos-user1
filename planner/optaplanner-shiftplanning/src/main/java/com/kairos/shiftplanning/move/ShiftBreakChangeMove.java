package com.kairos.shiftplanning.move;

import com.kairos.shiftplanning.domain.activity.ActivityLineInterval;
import com.kairos.shiftplanning.domain.shift.ShiftBreak;
import com.kairos.shiftplanning.domain.shift.ShiftImp;
import com.kairos.shiftplanning.move.helper.ShiftBreakChangeMoveHelper;
import com.kairos.shiftplanning.solution.BreaksIndirectAndActivityPlanningSolution;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ShiftBreakChangeMove extends AbstractMove<BreaksIndirectAndActivityPlanningSolution> {
    private ShiftBreak shiftBreak;
    private ZonedDateTime breakTime;
    private List<ActivityLineInterval> activityLineIntervals;
    private ShiftImp shift;

    public ShiftBreakChangeMove(ShiftBreak shiftBreak, ZonedDateTime breakTime, List<ActivityLineInterval> activityLineIntervals, ShiftImp shift) {
        this.shiftBreak = shiftBreak;
        this.breakTime = breakTime;
        this.activityLineIntervals = activityLineIntervals;
        this.shift = shift;
    }

    @Override
    protected AbstractMove<BreaksIndirectAndActivityPlanningSolution> createUndoMove(ScoreDirector<BreaksIndirectAndActivityPlanningSolution> scoreDirector) {
        return new ShiftBreakChangeMove(shiftBreak,shiftBreak.getStartTime(),activityLineIntervals,shiftBreak.getShift());
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<BreaksIndirectAndActivityPlanningSolution> scoreDirector) {
        ShiftBreakChangeMoveHelper.assignShiftBreakToShift(scoreDirector,shiftBreak,breakTime,activityLineIntervals,shift);
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<BreaksIndirectAndActivityPlanningSolution> scoreDirector) {
        return true;
    }

    @Override
    public Collection<?> getPlanningEntities() {
        return Arrays.asList(shiftBreak);
    }

    @Override
    public Collection<?> getPlanningValues() {
        return Arrays.asList(breakTime);
    }

    @Override
    public String toString() {
        return "Shift:"+shiftBreak.getShift().getStartDate()+"["+shiftBreak.getDuration()+"]"+"{"+shiftBreak.getStartTime()+"}"+"->{"+breakTime+"}";
    }
}
