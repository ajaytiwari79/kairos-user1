package com.kairos.activity.wta.templates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.activity.wta.basic_details.WTABaseRuleTemplateDTO;
import com.kairos.enums.PartOfDay;
import com.kairos.enums.WTATemplateType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NoOfSequenceShiftWTATemplateDTO extends WTABaseRuleTemplateDTO
{
    private int sequence;
    private boolean restingTimeAllowed;
    private int restingTime;
    private PartOfDay sequenceShiftFrom;
    private PartOfDay sequenceShiftTo;

    private List<BigInteger> plannedTimeIds = new ArrayList<>();
    private List<BigInteger> timeTypeIds = new ArrayList<>();

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public boolean isRestingTimeAllowed() {
        return restingTimeAllowed;
    }

    public void setRestingTimeAllowed(boolean restingTimeAllowed) {
        this.restingTimeAllowed = restingTimeAllowed;
    }
    public int getRestingTime() {
        return restingTime;
    }

    public void setRestingTime(int restingTime) {
        this.restingTime = restingTime;
    }

    public PartOfDay getSequenceShiftFrom() {
        return sequenceShiftFrom;
    }

    public void setSequenceShiftFrom(PartOfDay sequenceShiftFrom) {
        this.sequenceShiftFrom = sequenceShiftFrom;
    }

    public PartOfDay getSequenceShiftTo() {
        return sequenceShiftTo;
    }

    public void setSequenceShiftTo(PartOfDay sequenceShiftTo) {
        this.sequenceShiftTo = sequenceShiftTo;
    }

    public WTATemplateType getWtaTemplateType() {
        return wtaTemplateType;
    }

    public NoOfSequenceShiftWTATemplateDTO() {
        wtaTemplateType= WTATemplateType.NO_OF_SEQUENCE_SHIFT;
    }

    public List<BigInteger> getPlannedTimeIds() {
        return plannedTimeIds;
    }

    public void setPlannedTimeIds(List<BigInteger> plannedTimeIds) {
        this.plannedTimeIds = plannedTimeIds;
    }

    public List<BigInteger> getTimeTypeIds() {
        return timeTypeIds;
    }

    public void setTimeTypeIds(List<BigInteger> timeTypeIds) {
        this.timeTypeIds = timeTypeIds;
    }
}
