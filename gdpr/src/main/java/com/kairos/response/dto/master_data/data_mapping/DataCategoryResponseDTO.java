package com.kairos.response.dto.master_data.data_mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataCategoryResponseDTO {

    private Long id;

    private String name;

    private List<DataElementBasicResponseDTO> dataElements=new ArrayList<>();
}
