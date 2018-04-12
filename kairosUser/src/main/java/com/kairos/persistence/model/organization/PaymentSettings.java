package com.kairos.persistence.model.organization;

import com.kairos.persistence.model.common.UserBaseEntity;
import com.kairos.response.dto.web.experties.PaidOutFrequencyEnum;
import org.neo4j.ogm.annotation.Labels;
import org.neo4j.ogm.annotation.NodeEntity;
import org.springframework.data.annotation.Version;

/**
 * Created by vipul on 12/4/18.
 */
@NodeEntity
public class PaymentSettings extends UserBaseEntity {

    private PaidOutFrequencyEnum type;
    private Long dateOfPayment;
    private Long monthOfPayment;
    @Version
    private  Long versionId;

    public PaymentSettings() {
        // default cons
    }

    public PaidOutFrequencyEnum getType() {
        return type;
    }

    public void setType(PaidOutFrequencyEnum type) {
        this.type = type;
    }

    public Long getDateOfPayment() {
        return dateOfPayment;
    }

    public void setDateOfPayment(Long dateOfPayment) {
        this.dateOfPayment = dateOfPayment;
    }

    public Long getMonthOfPayment() {
        return monthOfPayment;
    }

    public void setMonthOfPayment(Long monthOfPayment) {
        this.monthOfPayment = monthOfPayment;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public PaymentSettings(PaidOutFrequencyEnum type, Long dateOfPayment, Long monthOfPayment) {
        this.type = type;
        this.dateOfPayment = dateOfPayment;
        this.monthOfPayment = monthOfPayment;
    }

    public PaymentSettings(PaidOutFrequencyEnum type, Long dateOfPayment) {
        this.type = type;
        this.dateOfPayment = dateOfPayment;
    }
}
