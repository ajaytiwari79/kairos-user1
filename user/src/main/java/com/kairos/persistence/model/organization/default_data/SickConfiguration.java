package com.kairos.persistence.model.organization.default_data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.persistence.model.common.UserBaseEntity;
import com.kairos.persistence.model.organization.Organization;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.math.BigInteger;
import java.util.Set;

import static com.kairos.persistence.model.constants.RelationshipConstants.IN_UNIT;

/**
 * CreatedBy vipulpandey on 29/8/18
 **/
@NodeEntity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SickConfiguration extends UserBaseEntity {

    private Set<BigInteger> timeTypes;

    @Relationship(type = IN_UNIT)
    private Organization organization;

    public SickConfiguration() {
        // DC
    }

    public Set<BigInteger> getTimeTypes() {
        return timeTypes;
    }

    public void setTimeTypes(Set<BigInteger> timeTypes) {
        this.timeTypes = timeTypes;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public SickConfiguration(Set<BigInteger> timeTypes, Organization organization) {
        this.timeTypes = timeTypes;
        this.organization = organization;
    }
}
