package com.kairos.dto.user.country.agreement.cta.cta_response;

import com.kairos.commons.utils.TranslationUtil;
import com.kairos.dto.TranslationInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

/**
 * Created by prerna on 22/3/18.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCategoryDTO implements Serializable {

    private static final long serialVersionUID = 7877279567029162429L;
    private String name;
    private BigInteger id;
    private Long countryId;
    private BigInteger timeTypeId;
    private Map<String, TranslationInfo> translations;

    public ActivityCategoryDTO(BigInteger id, String name) {
        this.name = name;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityCategoryDTO that = (ActivityCategoryDTO) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(id, that.id);
    }
    public String getName() {
        return TranslationUtil.getName(translations,name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }
}
