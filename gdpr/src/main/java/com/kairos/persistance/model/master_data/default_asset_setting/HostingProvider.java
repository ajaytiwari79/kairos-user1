package com.kairos.persistance.model.master_data.default_asset_setting;

import com.kairos.enums.SuggestedDataStatus;
import com.kairos.persistance.model.common.MongoBaseEntity;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;


@Document(collection = "hosting_provider")
public class HostingProvider extends MongoBaseEntity {

    @NotBlank(message = "Name can't be empty ")
    @Pattern(message = "Numbers and Special characters are not allowed for Name",regexp = "^[a-zA-Z\\s]+$")
    private String name;

    private Long countryId;

    private String suggestedDataStatus=SuggestedDataStatus.ACCEPTED.value;

    public String getSuggestedDataStatus() { return suggestedDataStatus; }

    public void setSuggestedDataStatus(String suggestedDataStatus) { this.suggestedDataStatus = suggestedDataStatus; }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public String getName() {
        return name.trim();
    }

    public void setName(String name) {
        this.name = name;
    }

    public HostingProvider(String name) {
        this.name = name;
    }

    public HostingProvider() {
    }
}
