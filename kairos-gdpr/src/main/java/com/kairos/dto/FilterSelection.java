package com.kairos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kairos.enums.FilterType;
import com.kairos.utils.custome_annotation.NotNullOrEmpty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FilterSelection {

    @NotNullOrEmpty(message = "Filter Category name cannot be empty")
    private FilterType name;

    @NotEmpty(message = "Value cannotbe Empty")
    @NotNull(message = "Value cannot be Null")
    private List<Long> value;

    public FilterSelection() {
        //dv
    }

    public FilterType getName() {
        return name;
    }

    public void setName(FilterType name) {
        this.name = name;
    }

    public List<Long> getValue() {
        return value;
    }

    public void setValue(List<Long> value) {
        this.value = value;
    }
}

