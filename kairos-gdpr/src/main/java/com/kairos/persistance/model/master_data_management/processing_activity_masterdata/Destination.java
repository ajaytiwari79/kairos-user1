package com.kairos.persistance.model.master_data_management.processing_activity_masterdata;


import com.kairos.persistance.model.common.MongoBaseEntity;
import com.kairos.utils.custome_annotation.NotNullOrEmpty;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "destination")
public class Destination extends MongoBaseEntity {


    @NotNullOrEmpty(message = "error.message.name.cannot.be.null.or.empty")
    private String name;

    // @NotNull(message = "error.message.countryId.cannot.be.null")
    private Long countryId;

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

}
