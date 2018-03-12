package com.kairos.persistence.model.user.pay_group_area;

import com.kairos.persistence.model.common.UserBaseEntity;
import com.kairos.persistence.model.user.region.Municipality;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import static com.kairos.persistence.model.constants.RelationshipConstants.HAS_MUNICIPALITY;

/**
 * Created by vipul on 12/3/18.
 */
//@RelationshipEntity(type = HAS_MUNICIPALITY)
public class PayGroupAreaMunicipalityRelationship  {

    //  @StartNode
    private PayGroupArea payGroupArea;
    // @EndNode
    private Municipality municipality;
    private Long startDateMillis;
    private Long endDateMillis;

    public PayGroupAreaMunicipalityRelationship() {
        //default constructor
    }


    public PayGroupArea getPayGroupArea() {
        return payGroupArea;
    }

    public void setPayGroupArea(PayGroupArea payGroupArea) {
        this.payGroupArea = payGroupArea;
    }

    public Municipality getMunicipality() {
        return municipality;
    }

    public void setMunicipality(Municipality municipality) {
        this.municipality = municipality;
    }

    public Long getStartDateMillis() {
        return startDateMillis;
    }

    public void setStartDateMillis(Long startDateMillis) {
        this.startDateMillis = startDateMillis;
    }

    public Long getEndDateMillis() {
        return endDateMillis;
    }

    public void setEndDateMillis(Long endDateMillis) {
        this.endDateMillis = endDateMillis;
    }
}
