package com.kairos.persistence.model.activity_stream;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.persistence.model.common.MongoBaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * Created by oodles on 27/1/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document
@Getter
@Setter
public class Notification extends MongoBaseEntity implements Serializable {
    private static final long serialVersionUID = -5097502586565568474L;
    private String name;
    private String message;
    private String source;
    private Boolean isRead = false;
    private Long userId;

    private Long  organizationId;


    public void setRead(Boolean read) {
        isRead = read;
    }
}
