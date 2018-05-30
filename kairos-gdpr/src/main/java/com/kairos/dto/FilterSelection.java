package com.kairos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kairos.persistance.model.enums.FilterType;
import com.kairos.utils.custome_annotation.NotNullOrEmpty;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FilterSelection {

    @NotNullOrEmpty(message = "error.message.name.cannot.be.null.or.empty")
    private String name;
    private List<BigInteger> values;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BigInteger> getValues() {
        return values;
    }

    public void setValues(List<BigInteger> values) {
        this.values = values;
    }
}

