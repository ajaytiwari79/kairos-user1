package com.kairos.response.dto.web.staff;

import com.kairos.response.dto.web.StaffDTO;

import java.util.ArrayList;
import java.util.List;

public class UnitStaffResponseDTO {

    private Long unitId;
    private List<StaffDTO> staffList = new ArrayList<>();

    public UnitStaffResponseDTO(){
        // default constructor
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public List<StaffDTO> getStaffList() {
        return staffList;
    }

    public void setStaffList(List<StaffDTO> staffList) {
        this.staffList = staffList;
    }
}
