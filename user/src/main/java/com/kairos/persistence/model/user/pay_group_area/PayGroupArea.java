package com.kairos.persistence.model.user.pay_group_area;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.persistence.model.common.UserBaseEntity;
import com.kairos.persistence.model.organization.Level;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import static com.kairos.persistence.model.constants.RelationshipConstants.IN_LEVEL;

/**
 * @Created by prabjot on 20/12/17.
 * @Modified by VIPUl for KP-2320 on 9-March-18
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NodeEntity
@Getter
@Setter
@NoArgsConstructor
public class PayGroupArea extends UserBaseEntity {
    private static final long serialVersionUID = 1900074878897740934L;
    private String name;
    private String description;
    @Relationship(type = IN_LEVEL)
    private Level level;

    public PayGroupArea(String name, String description, Level level) {
        this.name = name;
        this.description = description;
        this.level = level;
    }

    public PayGroupArea(Long id, String name) {
        this.name = name;
        this.id = id;
    }

}
