package com.kairos.persistence.model.user.region;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kairos.persistence.model.common.UserBaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

import static com.kairos.constants.UserMessagesConstants.*;
import static com.kairos.persistence.model.constants.RelationshipConstants.REGION;

/**
 * Created by oodles on 7/1/17.
 */
@NodeEntity
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Province extends UserBaseEntity {
    private static final long serialVersionUID = -6436738867715576005L;
    @NotBlank(message = ERROR_PROVINCE_NAME_NOTEMPTY)
    private String name;
    @NotBlank(message = ERROR_PROVINCE_GEOFENCE_NOTEMPTY)
    private String geoFence;
    @NotBlank(message = ERROR_PROVINCE_CODE_NOTEMPTY)
    private String code;

    private float latitude;
    private float longitude;

    @Relationship(type = REGION)
    private Region region;

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    private boolean isEnable = true;


    public Map<String,Object> retrieveDetails() {
        Map<String,Object> response = new HashMap<>(4);
        response.put("id",this.id);
        response.put("name",this.name);
        response.put("code",this.code);
        response.put("geoFence",this.geoFence);
        return  response;

    }
}
