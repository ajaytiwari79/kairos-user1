package com.kairos.shiftplanning.constraints.unitconstraint;

import com.kairos.shiftplanning.constraints.Constraint;
import com.kairos.shiftplanning.constraints.ScoreLevel;
import com.kairos.shiftplanning.domain.activity.Activity;
import com.kairos.shiftplanning.domain.shift.ShiftImp;
import com.kairos.shiftplanning.domain.tag.Tag;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
public class NoChangesToStaffWithCareBubble implements Constraint {
    private Long tagId;
    private ScoreLevel level;
    private int weight;

    @Override
    public int checkConstraints(Activity activity, ShiftImp shift) {
        return 0;
    }

    @Override
    public <T extends Constraint> int checkConstraints(T object, List<ShiftImp> shifts) {
        Map<Long,Tag> tagMap = new HashMap<>();
        int count = 0;
        for (ShiftImp shift : shifts) {
            Tag tag = tagMap.get(shift.getEmployee().getId());
            if(!tagMap.containsKey(shift.getEmployee().getId())) {
                Optional<Tag> tagOptional = shift.getEmployee().getTags().stream().filter(t -> t.getId().equals(tagId)).findFirst();
                if(tagOptional.isPresent()){
                    tag = tagOptional.get();
                }else {
                    continue;
                }
            }
            if(tag.isValidTag(shift.getStartDate()) && shift.isChanged(shift.getEmployee().getActualShiftsMap().get(shift.getId()))){
                count++;
            }
        }
        return count;
    }
}
