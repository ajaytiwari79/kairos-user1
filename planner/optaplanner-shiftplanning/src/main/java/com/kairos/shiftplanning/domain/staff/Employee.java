package com.kairos.shiftplanning.domain.staff;

import com.kairos.enums.constraint.ConstraintSubType;
import com.kairos.enums.shift.PaidOutFrequencyEnum;
import com.kairos.shiftplanning.domain.shift.ShiftImp;
import com.kairos.shiftplanning.domain.skill.Skill;
import com.kairos.shiftplanning.domain.tag.Tag;
import com.kairos.shiftplanning.domain.unit.Unit;
import com.kairos.shiftplanning.domain.wta_ruletemplates.WTABaseRuleTemplate;
import com.kairos.shiftplanning.utils.ShiftPlanningUtility;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScoreHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XStreamAlias("Employee")
public class Employee {
    private static final Logger LOGGER = LoggerFactory.getLogger(Employee.class);
    private Long id;
    private BigDecimal baseCost;
    private Map<java.time.LocalDate,List<CTARuleTemplate>> localDateCTARuletemplateMap;
    private String name;
    private Set<Skill> skillSet;
    private PaidOutFrequencyEnum paidOutFrequencyEnum;
    private SeniorAndChildCareDays seniorAndChildCareDays;
    private List<StaffChildDetail> staffChildDetails;
    private Set<Tag> tags;
    private Set<Team> teams;
    private boolean nightWorker;
    private Employment employment;
    private ExpertiseNightWorkerSetting expertiseNightWorkerSetting;
    private Unit unit;
    private Map<LocalDate,Map<ConstraintSubType, WTABaseRuleTemplate>> wtaRuleTemplateMap;
    private BreakSettings breakSettings;
    @Builder.Default
    private Map<LocalDate,BigDecimal> functionalBonus = new HashMap<>();

    public Employee(Long id, String name, Set<Skill> skillSet) {
        super();
        this.id = id;
        this.name = name;
        this.skillSet = skillSet;
        this.functionalBonus = new HashMap<>();
    }


    public String toString() {
        return "E:" + id;
    }


    public int checkConstraints(Unit unit, ShiftImp shiftImp, List<ShiftImp> shiftImps,ConstraintSubType constraintSubType) {
        if(!this.wtaRuleTemplateMap.containsKey(constraintSubType)) return 0;
        return this.wtaRuleTemplateMap.get(shiftImp.getStartDate()).get(constraintSubType).checkConstraints(unit,shiftImp,shiftImps);
    }

    public int checkConstraints(ShiftImp shiftImp, List<ShiftImp> shiftImps,HardMediumSoftLongScoreHolder scoreHolder,RuleContext ruleContext) throws InterruptedException, ExecutionException {
        List<Callable<Boolean>> callables = new ArrayList<>();
        for (WTABaseRuleTemplate wtaBaseRuleTemplate : this.wtaRuleTemplateMap.get(shiftImp.getStartDate()).values()) {
            Callable<Boolean> callable = ()->{
                int constraintPenality = wtaBaseRuleTemplate.checkConstraints(unit,shiftImp,shiftImps);
                wtaBaseRuleTemplate.breakLevelConstraints(scoreHolder,ruleContext,constraintPenality);
                return true;
            };
            callables.add(callable);
        }
        List<Future<Boolean>> futures = ShiftPlanningUtility.executeAsynchronously(callables);
        for (Future<Boolean> future : futures) {
            future.get();
        }
        return 0;
    }

    public void breakContraints(ShiftImp shiftImp,HardMediumSoftLongScoreHolder scoreHolder, RuleContext kContext, int constraintPenality, ConstraintSubType constraintSubType) {
        this.wtaRuleTemplateMap.get(shiftImp.getStartDate()).get(constraintSubType).breakLevelConstraints(scoreHolder,kContext,constraintPenality);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Employee employee = (Employee) o;

        return new EqualsBuilder()
                .append(this.id, employee.id).append(this.employment.getId(),employee.getEmployment().getId())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id.hashCode()).append(this.getEmployment().getId()).hashCode();
    }

}
