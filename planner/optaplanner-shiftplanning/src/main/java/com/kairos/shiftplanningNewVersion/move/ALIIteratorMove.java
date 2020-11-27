package com.kairos.shiftplanningNewVersion.move;




import com.kairos.shiftplanningNewVersion.entity.ALI;
import com.kairos.shiftplanningNewVersion.entity.Shift;
import com.kairos.shiftplanningNewVersion.solution.StaffingLevelSolution;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.Arrays;
import java.util.Collection;

public class ALIIteratorMove extends AbstractMove<StaffingLevelSolution> {
    private final ALI activityLineInterval;
    private final Shift shiftImp;

    public ALIIteratorMove(ALI activityLineInterval, Shift shiftImp) {
        this.activityLineInterval = activityLineInterval;
        this.shiftImp = shiftImp;
    }

    @Override
    protected AbstractMove<StaffingLevelSolution> createUndoMove(ScoreDirector<StaffingLevelSolution> scoreDirector) {
        return new ALIIteratorMove(activityLineInterval,activityLineInterval.getShift());
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<StaffingLevelSolution> scoreDirector) {
        scoreDirector.beforeVariableChanged(activityLineInterval, "shift");
        activityLineInterval.setShift(shiftImp);
        scoreDirector.afterVariableChanged(activityLineInterval, "shift");
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<StaffingLevelSolution> scoreDirector) {
        return true;
    }

    @Override
    public Collection<?> getPlanningEntities() {
        return Arrays.asList(activityLineInterval);
    }

    @Override
    public Collection<?> getPlanningValues() {
        return Arrays.asList(shiftImp);
    }

    @Override
    public String toString() {
        return "ActivityLineIntervalIteratorMove{" +
                "activityLineInterval=" + activityLineInterval +" "+activityLineInterval.getShift()+
                "-> shiftImp=" + shiftImp +
                '}';
    }
}