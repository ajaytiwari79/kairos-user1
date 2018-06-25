package com.kairos.response.dto.web.staff;

import com.kairos.persistence.model.user.country.employment_type.EmploymentType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StaffEmploymentWrapper {
    private List<EmploymentType> employmentTypes= new ArrayList<>();
    private List<Map> staffList = new ArrayList<>();

    public StaffEmploymentWrapper() {
        // DC
    }

    public List<EmploymentType> getEmploymentTypes() {
        return employmentTypes;
    }

    public void setEmploymentTypes(List<EmploymentType> employmentTypes) {
        this.employmentTypes = employmentTypes;
    }

    public List<Map> getStaffList() {
        return staffList;
    }

    public void setStaffList(List<Map> staffList) {
        this.staffList = staffList;
    }
}
