package com.kairos.shiftplanningNewVersion.move.helper;

import com.kairos.shiftplanningNewVersion.entity.ALI;
import com.kairos.shiftplanningNewVersion.entity.Shift;
import com.kairos.shiftplanningNewVersion.solution.StaffingLevelSolution;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ALIChangeMoveHelper {
    public static final String SHIFT = "shift";
    public static final String FORMAT = "{} {{}} -> {{}}";
    private static final Logger LOGGER = LoggerFactory.getLogger(ALIChangeMoveHelper.class);

    /**
     *
     * @param scoreDirector
     * @param activityLineInterval gets assigned
     * @param shiftImp  to be assigned to
     */
    public static void assignActivityIntervalToShift(ScoreDirector<StaffingLevelSolution> scoreDirector, ALI activityLineInterval,
                                                     Shift shiftImp, List<ALI> exActivityLineIntervalsForThisShift, Shift exActivityLineIntervalShift){
        LOGGER.debug("**********************STARTS********************");
        if(CollectionUtils.isNotEmpty(exActivityLineIntervalsForThisShift)){
            for(ALI existingActivityLineInterval: exActivityLineIntervalsForThisShift){
                if(Objects.equals(existingActivityLineInterval.getShift(),exActivityLineIntervalShift)){
                    continue;
                }
                scoreDirector.beforeVariableChanged(existingActivityLineInterval, SHIFT);
                LOGGER.debug("X {} {{}} -> {{}}",existingActivityLineInterval,existingActivityLineInterval.getShift(),exActivityLineIntervalShift);
                existingActivityLineInterval.setShift(exActivityLineIntervalShift);
                scoreDirector.afterVariableChanged(existingActivityLineInterval, SHIFT);
            }
        }
        if(Objects.equals(activityLineInterval.getShift(), shiftImp)){
            return;
        }
        scoreDirector.beforeVariableChanged(activityLineInterval, SHIFT);
        LOGGER.debug(FORMAT,activityLineInterval,activityLineInterval.getShift(), shiftImp);
        activityLineInterval.setShift(shiftImp);
        scoreDirector.afterVariableChanged(activityLineInterval, SHIFT);
        LOGGER.debug("**********************END********************");
        //IRSV is triggered in beforeVariableChanged() so if exiting shift is null IRSV wont be triggered because shift would be null for it. it wont know what collection to add to
        //now, there is no need to worry as after the move is applied it'll trigger all listeners and then IRSV would also be trigger
        //but it IRSV would be triggered then then wwhy not here with scoreDirector.afterVariableChanged(activityLineInterval, "shift");??????????
    }

    public static void assignActivityIntervalToShift(ScoreDirector<StaffingLevelSolution> scoreDirector, ALI activityLineInterval,
                                                     Shift shiftImp) {
        if(Objects.equals(activityLineInterval.getShift(), shiftImp)){
            return;
        }
        scoreDirector.beforeVariableChanged(activityLineInterval, SHIFT);
        LOGGER.debug(FORMAT, activityLineInterval,activityLineInterval.getShift(), shiftImp);
        activityLineInterval.setShift(shiftImp);
        scoreDirector.afterVariableChanged(activityLineInterval, SHIFT);
    }
}
