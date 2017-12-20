package com.kairos.persistence.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Id;

import java.io.Serializable;

/**
 * Contains common fields of an entity
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)

public abstract class UserBaseEntity implements Serializable {

    //@GeneratedValue
    @GraphId protected Long id;
    protected boolean deleted;
    private Long creationDate;
    private Long lastModificationDate;
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public Long getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(Long lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
