package com.kairos.response.dto.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigInteger;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessingPurposeResponseDTO {


    private BigInteger id;

    private String name;

    private Long organizationId;

    public Long getOrganizationId() { return organizationId; }

    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public BigInteger getId() { return id; }

    public void setId(BigInteger id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }
}
