package com.kairos.persistence.model.master_data.data_category_element;

import com.kairos.persistence.model.common.MongoBaseEntity;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Document
public class DataCategory extends MongoBaseEntity {

    @NotBlank(message = "Name cannot be empty")
    @Pattern(message = "Numbers and Special characters are not allowed in Name",regexp = "^[a-zA-Z\\s]+$")
    private String name;

    // empty array to get rid of null pointer
    private List<BigInteger> dataElements=new ArrayList<>();

    private Long countryId;

    public List<BigInteger> getDataElements() {
        return dataElements;
    }

    public void setDataElements(List<BigInteger> dataElements) {
        this.dataElements = dataElements;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataCategory(String name, List<BigInteger> dataElements) {
        this.name = name;
        this.dataElements = dataElements;
    }



    public DataCategory() {
    }

    public DataCategory(String name) {
        this.name = name;
    }
}
