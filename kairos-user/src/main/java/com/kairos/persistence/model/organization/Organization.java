package com.kairos.persistence.model.organization;

import static com.kairos.persistence.model.constants.RelationshipConstants.BUSINESS_TYPE;
import static com.kairos.persistence.model.constants.RelationshipConstants.CONTACT_ADDRESS;
import static com.kairos.persistence.model.constants.RelationshipConstants.CONTACT_DETAIL;
import static com.kairos.persistence.model.constants.RelationshipConstants.CONTRACT_TYPE;
import static com.kairos.persistence.model.constants.RelationshipConstants.COUNTRY;
import static com.kairos.persistence.model.constants.RelationshipConstants.EMPLOYEE_LIMIT;
import static com.kairos.persistence.model.constants.RelationshipConstants.HAS_BILLING_ADDRESS;
import static com.kairos.persistence.model.constants.RelationshipConstants.HAS_EMPLOYMENTS;
import static com.kairos.persistence.model.constants.RelationshipConstants.HAS_GROUP;
import static com.kairos.persistence.model.constants.RelationshipConstants.HAS_LOCAL_AREA_TAGS;
import static com.kairos.persistence.model.constants.RelationshipConstants.HAS_PUBLIC_PHONE_NUMBER;
import static com.kairos.persistence.model.constants.RelationshipConstants.HAS_SETTING;
import static com.kairos.persistence.model.constants.RelationshipConstants.HAS_SUB_ORGANIZATION;
import static com.kairos.persistence.model.constants.RelationshipConstants.INDUSTRY_TYPE;
import static com.kairos.persistence.model.constants.RelationshipConstants.KAIROS_STATUS;
import static com.kairos.persistence.model.constants.RelationshipConstants.ORGANIZATION_HAS_ACCESS_GROUPS;
import static com.kairos.persistence.model.constants.RelationshipConstants.ORGANIZATION_HAS_DEPARTMENT;
import static com.kairos.persistence.model.constants.RelationshipConstants.ORGANIZATION_HAS_OFFICE_RESOURCE;
import static com.kairos.persistence.model.constants.RelationshipConstants.ORGANIZATION_HAS_RESOURCE;
import static com.kairos.persistence.model.constants.RelationshipConstants.OWNERSHIP_TYPE;
import static com.kairos.persistence.model.constants.RelationshipConstants.SUB_TYPE_OF;
import static com.kairos.persistence.model.constants.RelationshipConstants.TYPE_OF;
import static com.kairos.persistence.model.constants.RelationshipConstants.VAT_TYPE;
import static com.kairos.persistence.model.constants.RelationshipConstants.ZIP_CODE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.EnumString;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.kairos.persistence.model.common.UserBaseEntity;
import com.kairos.persistence.model.organization.enums.OrganizationLevel;
import com.kairos.persistence.model.organization.group.Group;
import com.kairos.persistence.model.user.access_permission.AccessGroup;
import com.kairos.persistence.model.user.client.ContactAddress;
import com.kairos.persistence.model.user.client.ContactDetail;
import com.kairos.persistence.model.user.country.BusinessType;
import com.kairos.persistence.model.user.country.ContractType;
import com.kairos.persistence.model.user.country.Country;
import com.kairos.persistence.model.user.country.EmployeeLimit;
import com.kairos.persistence.model.user.country.IndustryType;
import com.kairos.persistence.model.user.country.KairosStatus;
import com.kairos.persistence.model.user.country.OwnershipType;
import com.kairos.persistence.model.user.country.VatType;
import com.kairos.persistence.model.user.department.Department;
import com.kairos.persistence.model.user.office_esources_and_metadata.OfficeResources;
import com.kairos.persistence.model.user.region.LocalAreaTag;
import com.kairos.persistence.model.user.region.ZipCode;
import com.kairos.persistence.model.user.resources.Resource;
import com.kairos.persistence.model.user.staff.Employment;


/**
 * Organization Domain & it's properties
 */
//@JsonSerialize(using = OrganizationSerializer.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@NodeEntity
public class Organization extends UserBaseEntity {
    private boolean isEnable = true;

    @NotNull(message = "error.Organization.name.notnull")
    private String name;
    private String email;
    @Property(name="organizationLevel")
    @EnumString(OrganizationLevel.class)
    private OrganizationLevel organizationLevel = OrganizationLevel.CITY;
    private String childLevel;
    private String eanNumber;
    @NotNull(message = "error.Organization.formal.notnull")
    private String formalName;
    private String costCenterCode;
    private String costCenterName;
    private String shortName;
    private String webSiteUrl;
    private long clientSince;
    private String cvrNumber;
    private String pNumber;



    private boolean isKairosHub;
    @Relationship(type = KAIROS_STATUS)
    private KairosStatus kairosStatus;

    private boolean standardTimeSlot = true;
    private boolean isParentOrganization;
    private boolean isPrekairos;
    private boolean showPersonNames;
    private boolean isOneTimeSyncPerformed;

    private boolean isAutoGeneratedPerformed;

    public void setOneTimeSyncPerformed(boolean oneTimeSyncPerformed) {
        isOneTimeSyncPerformed = oneTimeSyncPerformed;
    }

    public boolean isOneTimeSyncPerformed() {

        return isOneTimeSyncPerformed;
    }
    // Relationships

    @Relationship(type = COUNTRY)
    private Country country;

    @Relationship(type = HAS_GROUP)
    private List<Group> groupList = new ArrayList<>();

    @Relationship(type = HAS_SUB_ORGANIZATION)
    private List<Organization> children = new ArrayList<>();


    @Relationship(type = TYPE_OF)
    private List<OrganizationType> organizationTypes;

    @Relationship(type = SUB_TYPE_OF)
    private List<OrganizationType> organizationSubTypes;


    @Relationship(type = HAS_SETTING)
    private OrganizationSetting organizationSetting;


    @Relationship(type = ORGANIZATION_HAS_ACCESS_GROUPS)
    private List<AccessGroup> accessGroups = new ArrayList<>();

    @Relationship(type = ORGANIZATION_HAS_RESOURCE)
    private List<Resource> resourceList;


    @Relationship(type = ORGANIZATION_HAS_DEPARTMENT)
    private List<Department> departments;

    @Relationship(type = HAS_PUBLIC_PHONE_NUMBER)
    private List<PublicPhoneNumber> publicPhoneNumberList;

    @Relationship(type = ORGANIZATION_HAS_OFFICE_RESOURCE)
    private List<OfficeResources> officeResourcesList;


    @Relationship(type = HAS_EMPLOYMENTS)
    private List<Employment> employments = new ArrayList<>();

    @Relationship(type = HAS_LOCAL_AREA_TAGS)
    private List<LocalAreaTag> localAreaTags = new ArrayList<>();

    @Relationship(type = HAS_BILLING_ADDRESS)
    ContactAddress billingAddress;

    @NotNull(message = "error.Organization.ContactAddress.notnull")
    @Relationship(type = CONTACT_DETAIL)
    private ContactDetail contactDetail;

    @Relationship(type = CONTACT_ADDRESS)
    private ContactAddress contactAddress;

    @Relationship(type = ZIP_CODE)
    private ZipCode zipCode;
    @Relationship(type = BUSINESS_TYPE)
    private List<BusinessType> businessTypes;
    @Relationship(type = OWNERSHIP_TYPE)
    private OwnershipType ownershipType;
    @Relationship(type = INDUSTRY_TYPE)
    private IndustryType industryType;
    @Relationship(type = CONTRACT_TYPE)
    private ContractType contractType;
    @Relationship(type = EMPLOYEE_LIMIT)
    private EmployeeLimit employeeLimit;
    @Relationship(type = VAT_TYPE)
    private VatType vatType;


    private String description;
    private String externalId; //timeCare External Id
    private String estimoteAppId;
    private String estimoteAppToken;
    private int endTimeDeduction = 5; //in percentage

    private String kmdExternalId; //timeCare External Id

    private int dayShiftTimeDeduction = 4; //in percentage

    private int nightShiftTimeDeduction = 7; //in percentage



    public Organization(String name, List<Group> groupList, List<Organization> children) {
        this.name = name;
        this.groupList = groupList;
        this.children = children;
    }

    public Organization() {
    }

    public List<LocalAreaTag> getLocalAreaTags() {
        return localAreaTags;
    }

    public void setLocalAreaTags(List<LocalAreaTag> localAreaTags) {
        this.localAreaTags = localAreaTags;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public Organization(String name) {
        this.name = name;
    }

    public Organization(String name, OrganizationSetting organizationSetting, OrganizationLevel organizationLevel, List<Group> groupList) {
        this.name = name;

        this.organizationSetting = organizationSetting;
        this.organizationLevel = organizationLevel;
        this.groupList = groupList;
    }

    public Organization(String name, String email, ContactDetail contact, ContactAddress contactAddress, OrganizationLevel organizationLevel, Country country, String childLevel) {
        this.name = name;
        this.email = email;
        this.contactDetail = contact;
        this.contactAddress = contactAddress;
        this.organizationLevel = organizationLevel;
        this.country = country;
        this.childLevel = childLevel;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public List<Resource> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<Resource> resourceList) {
        this.resourceList = resourceList;
    }


    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public List<Group> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<Group> groupList) {
        this.groupList = groupList;
    }

    public List<Organization> getChildren() {
        return children;

    }

    public void setChildren(List<Organization> children) {
        this.children = children;
    }

    public ZipCode getZipCode() {
        return zipCode;
    }

    public void setZipCode(ZipCode zipCode) {
        this.zipCode = zipCode;
    }

    public String getEanNumber() {
        return eanNumber;
    }

    public void setEanNumber(String eanNumber) {
        this.eanNumber = eanNumber;
    }

    public String getFormalName() {
        return formalName;
    }

    public void setFormalName(String formalName) {
        this.formalName = formalName;
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public int getEndTimeDeduction() {
        return endTimeDeduction;
    }

    public void setEndTimeDeduction(int endTimeDeduction) {
        this.endTimeDeduction = endTimeDeduction;
    }

    public OrganizationSetting getOrganizationSetting() {
        return organizationSetting;
    }

    public void setOrganizationSetting(OrganizationSetting organizationSetting) {
        this.organizationSetting = organizationSetting;
    }

    public void setOrganizationLevel(OrganizationLevel organizationLevel) {
        this.organizationLevel = organizationLevel;
    }

    public OrganizationLevel getOrganizationLevel() {
        return organizationLevel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ContactAddress getContactAddress() {
        return contactAddress;
    }

    public void setContactAddress(ContactAddress contactAddress) {
        this.contactAddress = contactAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChildLevel() {
        return childLevel;
    }

    public void setChildLevel(String childLevel) {
        this.childLevel = childLevel;
    }

    public List<AccessGroup> getAccessGroups() {
        return accessGroups;
    }

    public void setAccessGroups(List<AccessGroup> accessGroups) {
        this.accessGroups = accessGroups;
    }

    public ContactDetail getContactDetail() {
        return contactDetail;
    }

    public void setContactDetail(ContactDetail contactDetail) {
        this.contactDetail = contactDetail;
    }

    public List<Employment> getEmployments() {
        return employments;
    }

    public void setEmployments(List<Employment> employments) {
        this.employments = employments;
    }

    public List<PublicPhoneNumber> getPublicPhoneNumberList() {
        return publicPhoneNumberList;
    }

    public void setPublicPhoneNumberList(List<PublicPhoneNumber> publicPhoneNumberList) {
        this.publicPhoneNumberList = publicPhoneNumberList;
    }

    public String getEstimoteAppId() {
        return estimoteAppId;
    }

    public void setEstimoteAppId(String estimoteAppId) {
        this.estimoteAppId = estimoteAppId;
    }

    public String getEstimoteAppToken() {
        return estimoteAppToken;
    }

    public void setEstimoteAppToken(String estimoteAppToken) {
        this.estimoteAppToken = estimoteAppToken;
    }
    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public List<OfficeResources> getOfficeResourcesList() {
        return officeResourcesList;
    }

    public void setOfficeResourcesList(List<OfficeResources> officeResourcesList) {
        this.officeResourcesList = officeResourcesList;
    }




    public Map<String, Object> retrieveOrganizationUnitDetails() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", this.id);
        map.put("name", this.name);
        map.put("description", this.description);
        //    map.put("type", this.organizationType.getName());
        map.put("externalId", this.externalId);
        map.put("kairosStatus", this.kairosStatus);

        if (this.contactAddress!=null){
            map.put("type", this.contactAddress.getStreet1());
            map.put("type", this.contactAddress.getHouseNumber());
            //        map.put("zipCode", this.zipCode.getZipCode());
//        map.put("zipCodeName", this.zipCode.getZipCode());

        }

        return map;
    }



    public ContactAddress getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(ContactAddress billingAddress) {
        this.billingAddress = billingAddress;
    }

    public void setCostCenterName(String costCenterName) {
        this.costCenterName = costCenterName;
    }

    public void setWebSiteUrl(String webSiteUrl) {
        this.webSiteUrl = webSiteUrl;
    }

    public void setClientSince(long clientSince) {
        this.clientSince = clientSince;
    }

    public String getCostCenterName() {
        return costCenterName;
    }

    public String getWebSiteUrl() {
        return webSiteUrl;
    }

    public long getClientSince() {
        return clientSince;
    }

    public OwnershipType getOwnershipType() {
        return ownershipType;
    }

    public IndustryType getIndustryType() {
        return industryType;
    }

    public ContractType getContractType() {
        return contractType;
    }


    public void setOwnershipType(OwnershipType ownershipType) {
        this.ownershipType = ownershipType;
    }

    public void setIndustryType(IndustryType industryType) {
        this.industryType = industryType;
    }

    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
    }

    public EmployeeLimit getEmployeeLimit() {
        return employeeLimit;
    }

    public void setEmployeeLimit(EmployeeLimit employeeLimit) {
        this.employeeLimit = employeeLimit;
    }

    public VatType getVatType() {
        return vatType;
    }

    public void setVatType(VatType vatType) {
        this.vatType = vatType;
    }

    public String getCvrNumber() {
        return cvrNumber;
    }

    public String getpNumber() {
        return pNumber;
    }

    public void setCvrNumber(String cvrNumber) {
        this.cvrNumber = cvrNumber;
    }

    public void setpNumber(String pNumber) {
        this.pNumber = pNumber;
    }

    public boolean isKairosHub() {
        return isKairosHub;
    }

    public void setKairosHub(boolean kairosHub) {
        isKairosHub = kairosHub;
    }

    public KairosStatus getKairosStatus() {
        return kairosStatus;
    }

    public void setKairosStatus(KairosStatus kairosStatus) {
        this.kairosStatus = kairosStatus;
    }

    public boolean isStandardTimeSlot() {
        return standardTimeSlot;
    }

    public void setStandardTimeSlot(boolean standardTimeSlot) {
        this.standardTimeSlot = standardTimeSlot;
    }

    public boolean isParentOrganization() {
        return isParentOrganization;
    }

    public void setParentOrganization(boolean parentOrganization) {
        isParentOrganization = parentOrganization;
    }

    public boolean isPrekairos() {
        return isPrekairos;
    }

    public void setPrekairos(boolean prekairos) {
        isPrekairos = prekairos;
    }

    public boolean isShowPersonNames() {
        return showPersonNames;
    }

    public void setShowPersonNames(boolean showPersonNames) {
        this.showPersonNames = showPersonNames;
    }

    public List<OrganizationType> getOrganizationTypes() {
        return Optional.fromNullable(organizationTypes).or(Lists.newArrayList());
    }

    public List<BusinessType> getBusinessTypes() {
        return Optional.fromNullable(businessTypes).or(Lists.newArrayList());
    }

    public void setOrganizationTypes(List<OrganizationType> organizationTypes) {
        this.organizationTypes = organizationTypes;
    }

    public void setBusinessTypes(List<BusinessType> businessTypes) {
        this.businessTypes = businessTypes;
    }

    public List<OrganizationType> getOrganizationSubTypes() {
        return Optional.fromNullable(organizationSubTypes).or(Lists.newArrayList());
    }

    public void setOrganizationSubTypes(List<OrganizationType> organizationSubTypes) {
        this.organizationSubTypes = organizationSubTypes;
    }

    public String getKmdExternalId() {
        return kmdExternalId;
    }

    public void setKmdExternalId(String kmdExternalId) {
        this.kmdExternalId = kmdExternalId;
    }

    public int getDayShiftTimeDeduction() {
        return dayShiftTimeDeduction;
    }

    public void setDayShiftTimeDeduction(int dayShiftTimeDeduction) {
        this.dayShiftTimeDeduction = dayShiftTimeDeduction;
    }

    public int getNightShiftTimeDeduction() {
        return nightShiftTimeDeduction;
    }

    public void setNightShiftTimeDeduction(int nightShiftTimeDeduction) {
        this.nightShiftTimeDeduction = nightShiftTimeDeduction;
    }

    @Override
    public String toString() {
        return "{Organization={" +
                "isEnable=" + isEnable +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", childLevel='" + childLevel + '\'' +
                ", eanNumber='" + eanNumber + '\'' +
                ", formalName='" + formalName + '\'' +
                ", costCenterCode='" + costCenterCode + '\'' +
                ", costCenterName='" + costCenterName + '\'' +
                ", shortName='" + shortName + '\'' +
                ", webSiteUrl='" + webSiteUrl + '\'' +
                ", clientSince=" + clientSince +
                ", cvrNumber='" + cvrNumber + '\'' +
                ", pNumber='" + pNumber + '\'' +
                ", isKairosHub=" + isKairosHub +
                ", standardTimeSlot=" + standardTimeSlot +
                ", isParentOrganization=" + isParentOrganization +
                ", isPrekairos=" + isPrekairos +
                ", showPersonNames=" + showPersonNames +
                ", isOneTimeSyncPerformed=" + isOneTimeSyncPerformed +
                ", departments=" + departments +
                ", publicPhoneNumberList=" + publicPhoneNumberList +
                ", officeResourcesList=" + officeResourcesList +
                ", description='" + description + '\'' +
                ", externalId='" + externalId + '\'' +
                ", estimoteAppId='" + estimoteAppId + '\'' +
                ", estimoteAppToken='" + estimoteAppToken + '\'' +
                ", endTimeDeduction=" + endTimeDeduction +
                ", kmdExternalId='" + kmdExternalId + '\'' +
                ", dayShiftTimeDeduction=" + dayShiftTimeDeduction +
                ", nightShiftTimeDeduction=" + nightShiftTimeDeduction +
                '}'+
                '}';
    }

    public boolean isAutoGeneratedPerformed() {
        return isAutoGeneratedPerformed;
    }

    public void setAutoGeneratedPerformed(boolean autoGeneratedPerformed) {
        isAutoGeneratedPerformed = autoGeneratedPerformed;
    }
}
