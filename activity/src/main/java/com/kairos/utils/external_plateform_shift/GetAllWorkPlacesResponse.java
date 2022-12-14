package com.kairos.utils.external_plateform_shift;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

/**
 * Created by oodles on 19/12/16.
 */
public class GetAllWorkPlacesResponse {
    @JacksonXmlProperty
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<GetAllWorkPlacesResult> GetAllWorkPlacesResult;

    public GetAllWorkPlacesResponse() {
        //Not in use
    }

    public List<GetAllWorkPlacesResult> getWorkPlaceList() {
        return GetAllWorkPlacesResult;
    }

    public void setWorkPlaceList(List<GetAllWorkPlacesResult> workPlaceList) {
        this.GetAllWorkPlacesResult = workPlaceList;
    }
}
