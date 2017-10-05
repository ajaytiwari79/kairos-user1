package com.kairos.persistence.model.query_wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.List;

/**
 * Created by Jasgeet on 5/10/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@QueryResult
public class ClientContactPersonQueryResult {
    private Long primaryStaffId;
    private Long secondaryStaffId;
    private Long secondaryTwoStaffId;
    private Long secondaryThreeStaffId;
    private Long serviceId;
    private List<Long> households;

    public Long getPrimaryStaffId() {
        return primaryStaffId;
    }

    public void setPrimaryStaffId(Long primaryStaffId) {
        this.primaryStaffId = primaryStaffId;
    }

    public Long getSecondaryStaffId() {
        return secondaryStaffId;
    }

    public void setSecondaryStaffId(Long secondaryStaffId) {
        this.secondaryStaffId = secondaryStaffId;
    }

    public Long getSecondaryTwoStaffId() {
        return secondaryTwoStaffId;
    }

    public void setSecondaryTwoStaffId(Long secondaryTwoStaffId) {
        this.secondaryTwoStaffId = secondaryTwoStaffId;
    }

    public Long getSecondaryThreeStaffId() {
        return secondaryThreeStaffId;
    }

    public void setSecondaryThreeStaffId(Long secondaryThreeStaffId) {
        this.secondaryThreeStaffId = secondaryThreeStaffId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public List<Long> getHouseholds() {
        return households;
    }

    public void setHouseholds(List<Long> households) {
        this.households = households;
    }
}
