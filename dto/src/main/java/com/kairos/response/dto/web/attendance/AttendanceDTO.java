package com.kairos.response.dto.web.attendance;

import com.kairos.user.organization.OrganizationBasicDTO;
import com.kairos.user.organization.OrganizationCommonDTO;
import com.kairos.user.reason_code.ReasonCodeDTO;

import java.util.List;
import java.util.Set;

public class AttendanceDTO {

    private AttendanceDurationDTO duration;
    private List<OrganizationCommonDTO> organizationIdAndNameResults;
    private Set<ReasonCodeDTO> reasonCode;
    public AttendanceDTO() {
    }

    public AttendanceDTO(AttendanceDurationDTO duration) {
        this.duration = duration;
    }

    public AttendanceDTO(List<OrganizationCommonDTO> organizationIdAndNameResults,Set<ReasonCodeDTO> reasonCode) {
        this.organizationIdAndNameResults = organizationIdAndNameResults;
        this.reasonCode=reasonCode;
    }


    public List<OrganizationCommonDTO> getOrganizationIdAndNameResults() {
        return organizationIdAndNameResults;
    }

    public void setOrganizationIdAndNameResults(List<OrganizationCommonDTO> organizationIdAndNameResults) {
        this.organizationIdAndNameResults = organizationIdAndNameResults;
    }

    public AttendanceDurationDTO getDuration() {
        return duration;
    }

    public void setDuration(AttendanceDurationDTO duration) {
        this.duration = duration;
    }

    public Set<ReasonCodeDTO> getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(Set<ReasonCodeDTO> reasonCode) {
        this.reasonCode = reasonCode;
    }
}
