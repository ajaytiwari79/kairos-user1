package com.kairos.persistence.model.access_permission;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.dto.user.access_permission.AccessGroupRole;
import com.kairos.persistence.model.common.UserBaseEntity;
import com.kairos.persistence.model.country.default_data.account_type.AccountType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.EnumString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.kairos.constants.UserMessagesConstants.ERROR_NAME_NOTNULL;
import static com.kairos.constants.UserMessagesConstants.ERROR_STARTDATE_NOTNULL;
import static com.kairos.persistence.model.constants.RelationshipConstants.HAS_ACCOUNT_TYPE;
import static com.kairos.persistence.model.constants.RelationshipConstants.HAS_PARENT_ACCESS_GROUP;

/**
 * Created by prabjot on 9/27/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@NodeEntity
@Getter
@Setter
@NoArgsConstructor
public class AccessGroup extends UserBaseEntity {

    private static final long serialVersionUID = 5789054664231770197L;
    @NotBlank(message = ERROR_NAME_NOTNULL)
    private String name;
    private boolean enabled = true;
    private boolean typeOfTaskGiver;
    private String description;
    @Property(name = "role")
    @EnumString(AccessGroupRole.class)
    private AccessGroupRole role;
    @Relationship(type = HAS_ACCOUNT_TYPE)
    private List<AccountType> accountType;
    @NotNull(message = ERROR_STARTDATE_NOTNULL)
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean allowedDayTypes;
    private Set<BigInteger> dayTypeIds;
    @Relationship(type = HAS_PARENT_ACCESS_GROUP)
    private AccessGroup parentAccessGroup;

    public AccessGroup(@NotBlank(message = ERROR_NAME_NOTNULL) String name, String description, AccessGroupRole role) {
        this.name = name;
        this.description = description;
        this.role = role;
    }

    public AccessGroup(String name, String description, AccessGroupRole role, Set<BigInteger> dayTypeIds,LocalDate startDate,LocalDate endDate) {
        this.name = name;
        this.description = description;
        this.role = role;
        this.dayTypeIds=dayTypeIds;
        this.startDate=startDate;
        this.endDate=endDate;
    }

    public AccessGroup(@NotBlank(message = ERROR_NAME_NOTNULL) @NotNull(message = ERROR_NAME_NOTNULL) String name, String description, AccessGroupRole role, List<AccountType> accountType,Set<BigInteger> dayTypeIds,LocalDate startDate,LocalDate endDate) {
        this.name = name;
        this.description = description;
        this.role = role;
        this.accountType = accountType;
        this.dayTypeIds=dayTypeIds;
        this.startDate=startDate;
        this.endDate=endDate;
    }

    public Set<BigInteger> getDayTypeIds() {
        this.dayTypeIds=dayTypeIds==null?new HashSet<>():dayTypeIds;
        return dayTypeIds;
    }
}
