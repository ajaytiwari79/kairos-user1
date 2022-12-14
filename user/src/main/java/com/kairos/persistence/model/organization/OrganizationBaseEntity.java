package com.kairos.persistence.model.organization;/*
 *Created By Pavan on 29/5/19
 *
 */

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.kairos.dto.user.organization.CompanyUnitType;
import com.kairos.enums.OrganizationLevel;
import com.kairos.persistence.model.access_permission.AccessGroup;
import com.kairos.persistence.model.client.ContactAddress;
import com.kairos.persistence.model.client.ContactDetail;
import com.kairos.persistence.model.common.UserBaseEntity;
import com.kairos.persistence.model.country.default_data.*;
import com.kairos.persistence.model.country.default_data.account_type.AccountType;
import com.kairos.persistence.model.country.tag.Tag;
import com.kairos.persistence.model.user.region.LocalAreaTag;
import com.kairos.utils.ZoneIdStringConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.annotation.typeconversion.EnumString;

import javax.validation.constraints.NotNull;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static com.kairos.constants.UserMessagesConstants.ERROR_ORGANIZATION_CONTACTADDRESS_NOTNULL;
import static com.kairos.constants.UserMessagesConstants.ERROR_ORGANIZATION_FORMAL_NOTNULL;
import static com.kairos.persistence.model.constants.RelationshipConstants.*;

@Getter
@Setter
@NoArgsConstructor
public class OrganizationBaseEntity extends UserBaseEntity {
    protected boolean isEnable = true;

    @NotNull(message = "error.Organization.name.notnull")
    protected String name;
    protected String email;
    protected String eanNumber;
    @NotNull(message = ERROR_ORGANIZATION_FORMAL_NOTNULL)
    protected String formalName;
    protected String costCenterCode;
    protected String costCenterName;
    protected String shortName;
    protected String webSiteUrl;
    //fixme review
    protected Long clientSince;
    protected String cvrNumber;
    protected String pNumber;
    @Relationship(type = KAIROS_STATUS)
    protected KairosStatus kairosStatus;

    @Relationship(type = TYPE_OF)
    protected OrganizationType organizationType;

    @Relationship(type = SUB_TYPE_OF)
    protected List<OrganizationType> organizationSubTypes;
    @Relationship(type = HAS_ACCOUNT_TYPE)
    protected AccountType accountType;

    @Relationship(type = ORGANIZATION_HAS_ACCESS_GROUPS)
    private List<AccessGroup> accessGroups = new ArrayList<>();


    protected String desiredUrl;
    protected String shortCompanyName;
    @Relationship(type = HAS_COMPANY_CATEGORY)
    protected CompanyCategory companyCategory;
    protected String kairosCompanyId;


    protected String vatId;
    protected CompanyUnitType companyUnitType;

    protected boolean boardingCompleted;

    protected String description;
    protected String externalId; //timeCare External Id
    //fixme move to task/shift planning
    protected String estimoteAppId;
    //fixme move to task/shift planning
    protected String estimoteAppToken;


    protected String kmdExternalId; //kmd External Id
    //fixme move to shift planning and use method to check
    protected boolean phaseGenerated = true;
    protected Boolean showCountryTags = true;
    @Convert(ZoneIdStringConverter.class)
    protected ZoneId timeZone;
    @Relationship(type = ORGANIZATION_HAS_TAG)
    protected List<Tag> tags;

    @Relationship(type = HAS_LOCAL_AREA_TAGS)
    protected List<LocalAreaTag> localAreaTags = new ArrayList<>();

    @Relationship(type = HAS_BILLING_ADDRESS)
    protected ContactAddress billingAddress;

    @NotNull(message = ERROR_ORGANIZATION_CONTACTADDRESS_NOTNULL)
    @Relationship(type = CONTACT_DETAIL)
    protected ContactDetail contactDetail;

    @Relationship(type = CONTACT_ADDRESS)
    protected ContactAddress contactAddress;

    @Relationship(type = BUSINESS_TYPE)
    protected List<BusinessType> businessTypes;
    //fixme review
    @Relationship(type = OWNERSHIP_TYPE)
    protected OwnershipType ownershipType;
    //fixme review
    @Relationship(type = INDUSTRY_TYPE)
    protected IndustryType industryType;
    //fixme review
    @Relationship(type = CONTRACT_TYPE)
    protected ContractType contractType;
    @Relationship(type = VAT_TYPE)
    protected VatType vatType;

    @Relationship(type = HAS_LEVEL)
    protected Level level;

    //fixme review
    @Relationship(type = HAS_UNIT_TYPE)
    private UnitType unitType;

    //fixme remove
    @Property(name = "organizationLevel")
    @EnumString(OrganizationLevel.class)
    private OrganizationLevel organizationLevel = OrganizationLevel.CITY;

    public List<BusinessType> getBusinessTypes() {
        return Optional.fromNullable(businessTypes).or(Lists.newArrayList());
    }

    public List<OrganizationType> getOrganizationSubTypes() {
        return Optional.fromNullable(organizationSubTypes).or(Lists.newArrayList());
    }


}
