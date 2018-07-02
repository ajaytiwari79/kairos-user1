package com.kairos.persistence.model.phase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kairos.persistence.model.common.MongoBaseEntity;
import com.kairos.enums.DurationType;
import com.kairos.enums.phase.PhaseType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by vipul on 25/9/17.
 */
@Document(collection = "phases")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Phase extends MongoBaseEntity {
    @NotNull(message = "error.phase.name.notnull")
    private String name;
    private String description;
    private int duration;
    private DurationType durationType;
    private int sequence;
    @Indexed
    private Long organizationId;
    private Long countryId;
    private BigInteger parentCountryPhaseId;
    private PhaseType phaseType;
    private List<PhaseStatus> status;

    public Phase() {
        //default constructor
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DurationType getDurationType() {
        return durationType;
    }

    public void setDurationType(DurationType durationType) {
        this.durationType = durationType;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public BigInteger getParentCountryPhaseId() {
        return parentCountryPhaseId;
    }

    public void setParentCountryPhaseId(BigInteger parentCountryPhaseId) {
        this.parentCountryPhaseId = parentCountryPhaseId;
    }

    public PhaseType getPhaseType() {
        return phaseType;
    }

    public void setPhaseType(PhaseType phaseType) {
        this.phaseType = phaseType;
    }

    public List<PhaseStatus> getStatus() {
        return status;
    }

    public void setStatus(List<PhaseStatus> status) {
        this.status = status;
    }

    public Phase(String name, String description, int duration, DurationType durationType, int sequence, Long countryId, Long organizationId, BigInteger parentCountryPhaseId, PhaseType phaseType, List<String> status) {
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.durationType = durationType;
        this.sequence = sequence;
        this.countryId = countryId;
        this.organizationId = organizationId;
        this.parentCountryPhaseId = parentCountryPhaseId;
        this.phaseType = phaseType;
        this.status = PhaseStatus.getListByValue(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Phase phase = (Phase) o;

        return new EqualsBuilder()
                .append(duration, phase.duration)
                .append(sequence, phase.sequence)
                .append(organizationId, phase.organizationId)
                .append(name, phase.name)
                .append(description, phase.description)
                .append(durationType, phase.durationType)
                .append(countryId, phase.countryId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(description)
                .append(duration)
                .append(durationType)
                .append(sequence)
                .append(organizationId)
                .append(countryId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Phase{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", duration=" + duration +
                ", durationType=" + durationType +
                ", sequence=" + sequence +
                ", organizationId=" + organizationId +
                ", countryId=" + countryId +
                '}';
    }

    public enum PhaseStatus {

        FIXED, LOCKED, APPROVED, PUBLISHED, PENDING, REQUESTED, VALIDATED, REJECTED;

        public String value;

        public static PhaseStatus getByValue(String value) {
            for (PhaseStatus status : PhaseStatus.values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            return null;
        }

        public static List<PhaseStatus> getListByValue(List<String> values) {
            if(Optional.ofNullable(values).isPresent()){
                return values.stream().map(PhaseStatus::valueOf)
                        .collect(Collectors.toList());
            }
            return null;

        }
    }

}
