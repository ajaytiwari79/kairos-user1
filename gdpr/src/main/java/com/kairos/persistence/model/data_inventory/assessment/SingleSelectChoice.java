package com.kairos.persistence.model.data_inventory.assessment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embedded;
import javax.persistence.Entity;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SingleSelectChoice extends SelectedChoice {

    @Embedded
    private MetaDataVO selectedChoice;


}
