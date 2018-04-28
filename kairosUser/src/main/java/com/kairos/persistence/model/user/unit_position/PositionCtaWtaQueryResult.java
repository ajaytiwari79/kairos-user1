package com.kairos.persistence.model.user.unit_position;

import com.kairos.persistence.model.user.agreement.cta.CostTimeAgreement;
import com.kairos.persistence.model.user.expertise.Expertise;
import com.kairos.persistence.model.user.expertise.ExpertiseQueryResult;
import com.kairos.persistence.model.user.expertise.FunctionAndSeniorityLevelQueryResult;
import com.kairos.persistence.model.user.expertise.SeniorityLevel;
import com.kairos.response.dto.web.wta.WTAResponseDTO;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.List;

/**
 * Created by prabjot on 3/1/18.
 */
@QueryResult
public class PositionCtaWtaQueryResult {

    private List<CostTimeAgreement> cta;
    private List<WTAResponseDTO> wta;
    private Expertise expertise;
    private FunctionAndSeniorityLevelQueryResult applicableSeniorityLevel;

    public PositionCtaWtaQueryResult() {
    }

    public List<CostTimeAgreement> getCta() {
        return cta;
    }

    public void setCta(List<CostTimeAgreement> cta) {
        this.cta = cta;
    }

    public List<WTAResponseDTO> getWta() {
        return wta;
    }

    public void setWta(List<WTAResponseDTO> wta) {
        this.wta = wta;
    }

    public Expertise getExpertise() {
        return expertise;
    }

    public void setExpertise(Expertise expertise) {
        this.expertise = expertise;
    }

    public FunctionAndSeniorityLevelQueryResult getApplicableSeniorityLevel() {
        return applicableSeniorityLevel;
    }

    public void setApplicableSeniorityLevel(FunctionAndSeniorityLevelQueryResult applicableSeniorityLevel) {
        this.applicableSeniorityLevel = applicableSeniorityLevel;
    }
}