package com.kairos.persistence.model.organization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.kairos.enums.OrganizationLevel;
import com.kairos.enums.UnionState;
import com.kairos.enums.time_slot.TimeSlotMode;
import com.kairos.persistence.model.access_permission.AccessGroup;
import com.kairos.persistence.model.client.ContactAddress;
import com.kairos.persistence.model.client.ContactDetail;
import com.kairos.persistence.model.common.UserBaseEntity;
import com.kairos.persistence.model.country.*;
import com.kairos.persistence.model.country.default_data.BusinessType;
import com.kairos.persistence.model.country.default_data.CompanyCategory;
import com.kairos.persistence.model.country.default_data.ContractType;
import com.kairos.persistence.model.country.default_data.UnitType;
import com.kairos.persistence.model.country.default_data.account_type.AccountType;
import com.kairos.persistence.model.country.tag.Tag;
import com.kairos.persistence.model.organization.group.Group;
import com.kairos.persistence.model.organization.time_slot.TimeSlotSet;
import com.kairos.persistence.model.organization.union.Location;
import com.kairos.persistence.model.organization.union.Sector;
import com.kairos.persistence.model.staff.employment.Employment;
import com.kairos.persistence.model.user.department.Department;
import com.kairos.persistence.model.user.office_esources_and_metadata.OfficeResources;
import com.kairos.persistence.model.user.region.LocalAreaTag;
import com.kairos.persistence.model.user.region.ZipCode;
import com.kairos.persistence.model.user.resources.Resource;
import com.kairos.dto.user.organization.CompanyType;
import com.kairos.dto.user.organization.CompanyUnitType;
import com.kairos.utils.ZoneIdStringConverter;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.annotation.typeconversion.DateString;
import org.neo4j.ogm.annotation.typeconversion.EnumString;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.time.ZoneId;
import java.util.*;

import static com.kairos.enums.time_slot.TimeSlotMode.STANDARD;
import static com.kairos.persistence.model.constants.RelationshipConstants.*;
import static org.neo4j.ogm.annotation.Relationship.INCOMING;


/**
 * Organization Domain & it's properties
 */
//@JsonSerialize(using = OrganizationSerializer.class)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)

@NodeEntity
public class Organization extends UserBaseEntity {
    private boolean isEnable = true;

    @NotNull(message = "error.Organization.name.notnull")
    private String name;
    private String email;

    @Property(name = "organizationLevel")
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
    private Long clientSince;
    private String cvrNumber;
    private String pNumber;

    private boolean isKairosHub;
    @Relationship(type = KAIROS_STATUS)
    private KairosStatus kairosStatus;

    private TimeSlotMode timeSlotMode = STANDARD;
    private boolean isParentOrganization;
    private boolean isPrekairos;
    private boolean showPersonNames;
    private boolean isOneTimeSyncPerformed;

    private boolean isAutoGeneratedPerformed;

    // Relationships

    @Relationship(type = COUNTRY)
    private Country country;

    @Relationship(type = HAS_GROUP)
    private List<Group> groupList = new ArrayList<>();

    @Relationship(type = HAS_SUB_ORGANIZATION)
    private List<Organization> children = new ArrayList<>();

    @Relationship(type = BELONGS_TO_HUB)
    private Organization hub;


    @Relationship(type = TYPE_OF)
    private OrganizationType organizationType;

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

    @Relationship(type = ORGANIZATION_HAS_TAG)
    private List<Tag> tags;

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

    @Relationship(type = HAS_LEVEL)
    private Level level;

    @Relationship(type = HAS_TIME_SLOT_SET)
    private List<TimeSlotSet> timeSlotSets = new ArrayList<>();

    @Relationship(type = HAS_PAYMENT_SETTINGS)
    private PaymentSettings paymentSettings;

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    private String description;
    private String externalId; //timeCare External Id
    private String estimoteAppId;
    private String estimoteAppToken;
    private int endTimeDeduction = 5; //in percentage

    private String kmdExternalId; //kmd External Id

    private int dayShiftTimeDeduction = 4; //in percentage

    private int nightShiftTimeDeduction = 7; //in percentage
    private boolean phaseGenerated = true;
    private Boolean showCountryTags = true;
    @Convert(ZoneIdStringConverter.class)
    private ZoneId timeZone;
    @DateString("HH:mm")
    private Date nightStartTimeFrom;
    @DateString("HH:mm")
    private Date nightEndTimeTo;
    private boolean union;

    private String desiredUrl;
    private String shortCompanyName;
    @Relationship(type = HAS_COMPANY_CATEGORY)
    private CompanyCategory companyCategory;
    private String kairosCompanyId;
    private CompanyType companyType;

    private String vatId;

    private boolean costCenter;
    private Integer costCenterId;
    private CompanyUnitType companyUnitType;

    private boolean boardingCompleted;
    private UnionState state;
    private boolean workcentre;
    private boolean gdprUnit;
    private BigInteger payRollTypeId;
    @Relationship(type = HAS_ACCOUNT_TYPE)
    private AccountType accountType;

    @Relationship(type = HAS_UNIT_TYPE)
    private UnitType unitType;

    @Relationship(type= HAS_LOCATION)
    private List<Location> locations = new ArrayList<>();

    @Relationship(type=HAS_SECTOR)
    private List<Sector> sectors = new ArrayList();


    //set o.nightStartTimeFrom="22:15",o.nightEndTimeTo="07:15"

    public Organization() {
    }

    //constructor for creating Union
    public Organization(String name,boolean union,Country country) {
        this.name = name;
        this.union = union;
        this.country= country;
    }
    public Organization(String name, List<Sector> sectors, ContactAddress contactAddress,boolean boardingCompleted,Country country,boolean union) {
        this.name = name;
        this.sectors = sectors;
        this.contactAddress = contactAddress;
        this.union = union;
        this.boardingCompleted=boardingCompleted;
        this.country = country;
    }

    public Organization(Long id, String name, String description, boolean isPrekairos, String desiredUrl, String shortCompanyName, String kairosCompanyId, CompanyType companyType,
                        String vatId, List<BusinessType> businessTypes, OrganizationType organizationType, List<OrganizationType> organizationSubTypes, CompanyUnitType companyUnitType,
                        CompanyCategory companyCategory, ZoneId timeZone, String childLevel, boolean isParentOrganization, Country country, AccountType accountType, boolean boardingCompleted,
                        List<Group> groupList, List<Organization> children, UnitType unitType,boolean workcentre) {
        this.name = name;
        this.description = description;
        this.isKairosHub = isPrekairos;
        this.desiredUrl = desiredUrl;
        this.shortCompanyName = shortCompanyName;
        this.kairosCompanyId = kairosCompanyId;
        this.vatId = vatId;
        this.businessTypes = businessTypes;
        this.organizationSubTypes = organizationSubTypes;
        this.organizationType = organizationType;
        this.companyType = companyType;
        this.companyCategory = companyCategory;
        this.companyUnitType = companyUnitType;
        this.timeZone = timeZone;
        this.childLevel = childLevel;
        this.isParentOrganization = isParentOrganization;
        this.country = country;
        this.id = id;
        this.accountType = accountType;
        this.companyType = companyType;
        this.boardingCompleted = boardingCompleted;
        this.workcentre = workcentre;
        this.groupList = groupList;
        this.children = children;
        this.unitType = unitType;

    }


    public void setOneTimeSyncPerformed(boolean oneTimeSyncPerformed) {
        isOneTimeSyncPerformed = oneTimeSyncPerformed;
    }

    public boolean isOneTimeSyncPerformed() {

        return isOneTimeSyncPerformed;
    }

    public boolean isUnion() {
        return union;
    }

    public void setUnion(boolean union) {
        this.union = union;
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
        return java.util.Optional.ofNullable(resourceList).orElse(new ArrayList<>());
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
        return java.util.Optional.ofNullable(children).orElse(new ArrayList<>());

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
        return java.util.Optional.ofNullable(employments).orElse(new ArrayList<>());
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

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
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

    public void setClientSince(Long clientSince) {
        this.clientSince = clientSince;
    }

    public String getCostCenterName() {
        return costCenterName;
    }

    public String getWebSiteUrl() {
        return webSiteUrl;
    }

    public Long getClientSince() {
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

    public OrganizationType getOrganizationType() {
        return organizationType;
    }

    public List<BusinessType> getBusinessTypes() {
        return Optional.fromNullable(businessTypes).or(Lists.newArrayList());
    }

    public void setOrganizationType(OrganizationType organizationType) {
        this.organizationType = organizationType;
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

    public boolean isAutoGeneratedPerformed() {
        return isAutoGeneratedPerformed;
    }

    public void setAutoGeneratedPerformed(boolean autoGeneratedPerformed) {
        isAutoGeneratedPerformed = autoGeneratedPerformed;
    }

    public boolean isPhaseGenerated() {
        return phaseGenerated;
    }

    public void setPhaseGenerated(boolean phaseGenerated) {
        this.phaseGenerated = phaseGenerated;
    }

    public void addResource(Resource resource) {
        List<Resource> resourceList = this.getResourceList();
        resourceList.add(resource);
        this.resourceList = resourceList;
    }

    public Boolean isShowCountryTags() {
        return showCountryTags;
    }

    public void setShowCountryTags(Boolean showCountryTags) {
        this.showCountryTags = showCountryTags;
    }

    public List<TimeSlotSet> getTimeSlotSets() {
        return timeSlotSets;
    }

    public void setTimeSlotSets(List<TimeSlotSet> timeSlotSets) {
        this.timeSlotSets = timeSlotSets;
    }

    public TimeSlotMode getTimeSlotMode() {
        return timeSlotMode;
    }

    public void setTimeSlotMode(TimeSlotMode timeSlotMode) {
        this.timeSlotMode = timeSlotMode;
    }

    public ZoneId getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(ZoneId timeZone) {
        this.timeZone = timeZone;
    }

    public Boolean getShowCountryTags() {
        return showCountryTags;
    }

    public Date getNightStartTimeFrom() {
        return nightStartTimeFrom;
    }

    public void setNightStartTimeFrom(Date nightStartTimeFrom) {
        this.nightStartTimeFrom = nightStartTimeFrom;
    }

    public Date getNightEndTimeTo() {
        return nightEndTimeTo;
    }

    public void setNightEndTimeTo(Date nightEndTimeTo) {
        this.nightEndTimeTo = nightEndTimeTo;
    }

    public PaymentSettings getPaymentSettings() {
        return paymentSettings;
    }

    public void setPaymentSettings(PaymentSettings paymentSettings) {
        this.paymentSettings = paymentSettings;
    }

    public String getDesiredUrl() {
        return desiredUrl;
    }

    public void setDesiredUrl(String desiredUrl) {
        this.desiredUrl = desiredUrl;
    }

    public String getShortCompanyName() {
        return shortCompanyName;
    }

    public void setShortCompanyName(String shortCompanyName) {
        this.shortCompanyName = shortCompanyName;
    }

    public String getKairosCompanyId() {
        return kairosCompanyId;
    }

    public void setKairosCompanyId(String kairosCompanyId) {
        this.kairosCompanyId = kairosCompanyId;
    }

    public CompanyType getCompanyType() {
        return companyType;
    }

    public void setCompanyType(CompanyType companyType) {
        this.companyType = companyType;
    }

    public String getVatId() {
        return vatId;
    }

    public void setVatId(String vatId) {
        this.vatId = vatId;
    }

    public boolean isCostCenter() {
        return costCenter;
    }

    public void setCostCenter(boolean costCenter) {
        this.costCenter = costCenter;
    }

    public Integer getCostCenterId() {
        return costCenterId;
    }

    public void setCostCenterId(Integer costCenterId) {
        this.costCenterId = costCenterId;
    }

    public CompanyUnitType getCompanyUnitType() {
        return companyUnitType;
    }

    public void setCompanyUnitType(CompanyUnitType companyUnitType) {
        this.companyUnitType = companyUnitType;
    }

    public CompanyCategory getCompanyCategory() {
        return companyCategory;
    }

    public void setCompanyCategory(CompanyCategory companyCategory) {
        this.companyCategory = companyCategory;
    }

    public boolean isBoardingCompleted() {
        return boardingCompleted;
    }

    public void setBoardingCompleted(boolean boardingCompleted) {
        this.boardingCompleted = boardingCompleted;
    }

    public boolean isWorkcentre() {
        return workcentre;
    }

    public void setWorkcentre(boolean workcentre) {
        this.workcentre = workcentre;
    }

    public boolean isGdprUnit() {
        return gdprUnit;
    }

    public void setGdprUnit(boolean gdprUnit) {
        this.gdprUnit = gdprUnit;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public void setUnitType(UnitType unitType) {
        this.unitType = unitType;
    }

    public BigInteger getPayRollTypeId() {
        return payRollTypeId;
    }

    public void setPayRollTypeId(BigInteger payRollTypeId) {
        this.payRollTypeId = payRollTypeId;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public UnionState getState() {
        return state;
    }

    public void setState(UnionState state) {
        this.state = state;
    }

    public List<Sector> getSectors() {
        return sectors;
    }

    public void setSectors(List<Sector> sectors) {
        this.sectors = sectors;
    }

    public Organization getHub() {
        return hub;
    }

    public void setHub(Organization hub) {
        this.hub = hub;
    }
}