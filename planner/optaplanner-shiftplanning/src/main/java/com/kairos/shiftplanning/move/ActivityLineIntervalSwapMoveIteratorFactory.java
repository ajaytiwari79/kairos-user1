package com.kairos.shiftplanning.move;

import com.kairos.shiftplanning.domain.activity.ActivityLineInterval;
import com.kairos.shiftplanning.solution.ShiftPlanningSolution;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveIteratorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ActivityLineIntervalSwapMoveIteratorFactory implements MoveIteratorFactory<ShiftPlanningSolution> {

    @Override
    public long getSize(ScoreDirector<ShiftPlanningSolution> scoreDirector) {
        ShiftPlanningSolution solution = scoreDirector.getWorkingSolution();
        int size = solution.getActivityLineIntervals().size();
        return solution.getShifts().size() * size;
    }

    @Override
    public Iterator<? extends Move<ShiftPlanningSolution>> createOriginalMoveIterator(ScoreDirector<ShiftPlanningSolution> scoreDirector) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ActivityLineIntervalSwapMoveIterator<? extends Move<ShiftPlanningSolution>> createRandomMoveIterator(ScoreDirector<ShiftPlanningSolution> scoreDirector, Random workingRandom) {
        ShiftPlanningSolution solution = scoreDirector.getWorkingSolution();
        List<ActivityLineInterval> activityLineIntervals= solution.getActivityLineIntervals();
        List<ActivityLineInterval> possibleActivityLineIntervals= new ArrayList<>();
        LocalDate date=solution.getWeekDates().get(workingRandom.nextInt(solution.getWeekDates().size()));
        for (ActivityLineInterval activityLineInterval:activityLineIntervals) {
            if(activityLineInterval.getStart().toLocalDate().equals(date)){
                possibleActivityLineIntervals.add(activityLineInterval);
            }
        }
        ActivityLineIntervalSwapMoveIterator activityLineIntervalChangeMoveIterator = new ActivityLineIntervalSwapMoveIterator(possibleActivityLineIntervals,workingRandom);
        return activityLineIntervalChangeMoveIterator;
    }



}
