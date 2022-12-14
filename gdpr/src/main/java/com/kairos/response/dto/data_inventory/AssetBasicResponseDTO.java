package com.kairos.response.dto.data_inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.persistence.model.embeddables.ManagingOrganization;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class AssetBasicResponseDTO {

    private Long id;

    private String name;

    private String description;

    private String hostingLocation;

    private ManagingOrganization managingDepartment;

    private boolean active;

    private RelatedProcessingActivityResponseDTO processingActivity;

    // constructor used in query result to fetch asset and there related processing activity
    public AssetBasicResponseDTO(BigInteger id, String name,BigInteger processingActivityId,String processingActivityName, boolean subProcessingActivity,BigInteger parentProcessingActivityId ,String parentProcessingActivityName) {
        this.id =id.longValue();
        this.name = name;
        this.processingActivity = new RelatedProcessingActivityResponseDTO(processingActivityId.longValue(), processingActivityName, subProcessingActivity,parentProcessingActivityId,parentProcessingActivityName);
    }

    public AssetBasicResponseDTO(Long id, String name, String description, String hostingLocation, ManagingOrganization managingDepartment, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.hostingLocation = hostingLocation;
        this.managingDepartment = managingDepartment;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHostingLocation() {
        return hostingLocation;
    }

    public void setHostingLocation(String hostingLocation) {
        this.hostingLocation = hostingLocation;
    }

    public ManagingOrganization getManagingDepartment() {
        return managingDepartment;
    }

    public void setManagingDepartment(ManagingOrganization managingDepartment) {
        this.managingDepartment = managingDepartment;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public RelatedProcessingActivityResponseDTO getProcessingActivity() {
        return processingActivity;
    }

    public void setProcessingActivity(RelatedProcessingActivityResponseDTO processingActivity) {
        this.processingActivity = processingActivity;
    }

}
