package com.kairos.persistence.model.counter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kairos.dto.TranslationInfo;
import com.kairos.dto.activity.common.UserInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.kairos.commons.utils.ObjectUtils.isMapNotEmpty;

/**
 * Created by oodles on 4/1/17.
 */
@Getter
@Setter
public abstract class MongoBaseEntity implements Serializable {

    private static final long serialVersionUID = 3991615936551701970L;
    @Id
    protected BigInteger id;
    @CreatedDate
    protected Date createdAt;

    @LastModifiedDate
    protected Date updatedAt;
    protected boolean deleted;
    protected UserInfo createdBy;
    protected UserInfo lastModifiedBy;
    protected Map<String, TranslationInfo> translations = new HashMap<>();

    public UserInfo getCreatedBy() {
        return createdBy;
    }

    public Map<String, TranslationInfo> getTranslations() {
        return isMapNotEmpty(translations) ? translations : new HashMap<>();
    }

    @JsonIgnore
    public void setCreatedBy(UserInfo createdBy) {
        this.createdBy = createdBy;
    }

    public UserInfo getLastModifiedBy() {
        return lastModifiedBy;
    }

    @JsonIgnore
    public void setLastModifiedBy(UserInfo lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }


    public Date getCreatedAt() {
        return createdAt;
    }

    @JsonIgnore
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    @JsonIgnore
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
