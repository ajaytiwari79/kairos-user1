package com.kairos.service.client;

import com.kairos.commons.custom_exception.DataNotFoundByIdException;
import com.kairos.commons.utils.CommonsExceptionUtil;
import com.kairos.commons.utils.DateUtils;
import com.kairos.config.env.EnvConfig;
import com.kairos.dto.user.organization.AddressDTO;
import com.kairos.enums.Gender;
import com.kairos.persistence.model.auth.User;
import com.kairos.persistence.model.client.*;
import com.kairos.persistence.model.client.relationships.ClientLanguageRelation;
import com.kairos.persistence.model.client.relationships.ClientNextToKinRelationship;
import com.kairos.persistence.model.client.relationships.ClientRelativeRelation;
import com.kairos.persistence.model.country.default_data.CitizenStatus;
import com.kairos.persistence.model.country.default_data.RelationType;
import com.kairos.persistence.model.user.language.Language;
import com.kairos.persistence.model.user.region.Municipality;
import com.kairos.persistence.model.user.region.ZipCode;
import com.kairos.persistence.repository.organization.UnitGraphRepository;
import com.kairos.persistence.repository.user.auth.UserGraphRepository;
import com.kairos.persistence.repository.user.client.*;
import com.kairos.persistence.repository.user.country.CitizenStatusGraphRepository;
import com.kairos.persistence.repository.user.country.CountryGraphRepository;
import com.kairos.persistence.repository.user.language.LanguageGraphRepository;
import com.kairos.persistence.repository.user.region.MunicipalityGraphRepository;
import com.kairos.persistence.repository.user.region.RegionGraphRepository;
import com.kairos.persistence.repository.user.region.ZipCodeGraphRepository;
import com.kairos.persistence.repository.user.staff.StaffGraphRepository;
import com.kairos.rest_client.TaskServiceRestClient;
import com.kairos.service.exception.ExceptionService;
import com.kairos.utils.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.File;
import java.util.*;

import static com.kairos.constants.AppConstants.FORWARD_SLASH;
import static com.kairos.constants.AppConstants.IMAGES_PATH;
import static com.kairos.constants.UserMessagesConstants.*;
import static com.kairos.persistence.model.constants.RelationshipConstants.HAS_HOME_ADDRESS;


/**
 * Created by Jasgeet on 22/5/17.
 */
@Service
@Transactional
public class ClientExtendedService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    private ClientGraphRepository clientGraphRepository;
    @Inject
    private UnitGraphRepository unitGraphRepository;
    @Inject
    private ClientService clientService;
    @Inject
    private AddressVerificationService addressVerificationService;
    @Inject
    private ClientOrganizationRelationService relationService;
    @Inject
    private ZipCodeGraphRepository zipCodeGraphRepository;
    @Inject
    private MunicipalityGraphRepository municipalityGraphRepository;
    @Inject
    private RegionGraphRepository regionGraphRepository;
    @Inject
    private ContactAddressGraphRepository contactAddressGraphRepository;
    @Inject
    private ClientAddressService clientAddressService;
    @Inject
    EnvConfig envConfig;
    @Inject
    private CitizenStatusGraphRepository citizenStatusGraphRepository;
    @Inject
    private ContactDetailsGraphRepository contactDetailsGraphRepository;
    @Inject
    private ClientOrganizationRelationGraphRepository relationGraphRepository;
    @Inject
    private ClientStaffRelationGraphRepository staffRelationGraphRepository;
    @Inject
    private StaffGraphRepository staffGraphRepository;
    @Inject
    private ClientLanguageRelationGraphRepository clientLanguageRelationGraphRepository;
    @Inject
    private LanguageGraphRepository languageGraphRepository;
    @Inject
    private UserGraphRepository userGraphRepository;
    @Inject
    private ClientRelativeGraphRepository clientRelativeGraphRepository;
    @Inject
    private ClientDiagnoseGraphRepository clientDiagnoseGraphRepository;
    @Inject
    private ClientAllergiesGraphRepository clientAllergiesGraphRepository;
    @Inject
    private AccessToLocationGraphRepository accessToLocationGraphRepository;
    @Inject
    private CountryGraphRepository countryGraphRepository;
    @Inject
    private ExceptionService exceptionService;
    @Inject
    private ClientNextToKinRelationshipRepository clientNextToKinRelationshipRepository;
    @Inject
    private ClientRelativeRelationshipRepository clientRelativeRelationshipRepository;
    @Inject
    private TaskServiceRestClient taskServiceRestClient;

    public NextToKinDTO saveNextToKin(Long unitId, Long clientId, NextToKinDTO nextToKinDTO) {
        Client client = clientGraphRepository.findOne(clientId);
        validateDetails(clientId, nextToKinDTO, client);
        Long homeAddressId = null;
        User nextToKin = validateCPRNumber(nextToKinDTO.getCprNumber());
        Client nextToKinClientObject;
        ContactDetail contactDetail = null;
        if (!Optional.ofNullable(nextToKin).isPresent()) {
            nextToKin = new User(nextToKinDTO.getCprNumber().trim(), nextToKinDTO.getFirstName(), nextToKinDTO.getLastName(), nextToKin.getEmail(), nextToKin.getUserName());
            nextToKin.setNickName(nextToKinDTO.getNickName());
            nextToKinClientObject = new Client();
            nextToKinClientObject.setCivilianStatus(getCivilianStatus(nextToKinDTO));
        } else {
            nextToKinClientObject = clientGraphRepository.getClientByUserId(nextToKin.getId());
            if (nextToKinClientObject.getId().equals(clientId)) {
                exceptionService.dataNotMatchedException(MESSAGE_CLIENT_NEXTTOKIN_NOTMATCH);
            }
            homeAddressId = clientGraphRepository.getIdOfHomeAddress(nextToKinClientObject.getId());
            contactDetail = clientGraphRepository.getContactDetailOfNextToKin(nextToKinClientObject.getId()).orElse(new ContactDetail());
        }
        ContactAddress homeAddress = homeAddressId == null ? ContactAddress.getInstance() : contactAddressGraphRepository.findOne(homeAddressId);
        setBasicDetailsInNextToKin(unitId, nextToKinDTO, nextToKin, nextToKinClientObject, contactDetail, homeAddress);
        CitizenStatus citizenStatus = getCivilianStatus(nextToKinDTO);
        nextToKinClientObject.setCivilianStatus(citizenStatus);
        nextToKinClientObject.setUser(nextToKin);
        clientGraphRepository.save(nextToKinClientObject);
        saveCitizenRelation(nextToKinDTO.getRelationTypeId(), unitId, nextToKinClientObject, client.getId());
        assignDataInNextToKin(unitId, clientId, client, nextToKinClientObject);
        return new NextToKinDTO().buildResponse(nextToKin, envConfig.getServerHost() + FORWARD_SLASH, nextToKinDTO.getRelationTypeId(), nextToKinDTO);
    }

    private void assignDataInNextToKin(Long unitId, Long clientId, Client client, Client nextToKinClientObject) {
        if (!hasAlreadyNextToKin(clientId, nextToKinClientObject.getId())) {
            createNextToKinRelationship(client, nextToKinClientObject);
        }
        if (!gettingServicesFromOrganization(nextToKinClientObject.getId(), unitId)) {
            assignOrganizationToNextToKin(nextToKinClientObject, unitId);
        }
    }

    private void setBasicDetailsInNextToKin(Long unitId, NextToKinDTO nextToKinDTO, User nextToKin, Client nextToKinClientObject, ContactDetail contactDetail, ContactAddress homeAddress) {
        nextToKin.setBasicDetail(nextToKinDTO);
        nextToKinClientObject.setProfilePic(nextToKinDTO.getProfilePic());
        nextToKinClientObject.saveContactDetail(nextToKinDTO, contactDetail);
        nextToKin.setContactDetail(contactDetail);
        homeAddress = verifyAndSaveAddressOfNextToKin(unitId, nextToKinDTO.getHomeAddress(), homeAddress);
        if (Optional.ofNullable(homeAddress).isPresent()) {
            nextToKin.setHomeAddress(homeAddress);
        }
    }

    private void validateDetails(Long clientId, NextToKinDTO nextToKinDTO, Client client) {
        if (client == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_CLIENT_ID_NOTFOUND, clientId);
        }
        if (clientGraphRepository.citizenInNextToKinList(clientId, nextToKinDTO.getCprNumber())) {
            exceptionService.duplicateDataException(MESSAGE_CLIENT_NEXTTOKIN_ALREADYEXIST);
        }
    }

    private User validateCPRNumber(String cprNumber) {
        return userGraphRepository.findUserByCprNumber(cprNumber.trim());
    }

    private boolean hasAlreadyNextToKin(Long clientId, Long nextToKinId) {
        return clientGraphRepository.hasAlreadyNextToKin(clientId, nextToKinId);
    }

    private boolean gettingServicesFromOrganization(Long clientId, Long unitId) {
        return relationService.checkClientOrganizationRelation(clientId, unitId) > 0;
    }

    private void createNextToKinRelationship(Client client, Client nextToKin) {
        ClientNextToKinRelationship clientNextToKinRelationship = new ClientNextToKinRelationship();
        clientNextToKinRelationship.setClient(client);
        clientNextToKinRelationship.setNextToKin(nextToKin);
        clientNextToKinRelationshipRepository.save(clientNextToKinRelationship);
    }

    private void assignOrganizationToNextToKin(Client nextToKin, long unitId) {
        relationGraphRepository.createClientRelationWithOrganization(nextToKin.getId(), unitId, new DateTime().getMillis(),
                UUID.randomUUID().toString().toUpperCase());
    }


    private ContactAddress verifyAndSaveAddressOfNextToKin(long unitId, AddressDTO addressDTO, ContactAddress contactAddressToSave) {
        Municipality municipality = municipalityGraphRepository.findById(addressDTO.getMunicipality().getId()).orElseThrow(()->new DataNotFoundByIdException(CommonsExceptionUtil.convertMessage(MESSAGE_MUNICIPALITY_NOTFOUND)));
        ZipCode zipCode;
        if (addressDTO.isVerifiedByGoogleMap()) {
            zipCode = zipCodeGraphRepository.findByZipCode(addressDTO.getZipCode().getZipCode());
            if (!Optional.ofNullable(zipCode).isPresent()) {
                exceptionService.dataNotFoundByIdException(MESSAGE_ZIPCODE_NOTFOUND);
            }
        } else {
            Map<String, Object> tomtomResponse = addressVerificationService.verifyAddress(addressDTO, unitId);
            if (!Optional.ofNullable(tomtomResponse).isPresent()) {
                logger.debug("Address not verified by TomTom ");
                return null;
            }
            contactAddressToSave.setLongitude(Float.valueOf(String.valueOf(tomtomResponse.get("yCoordinates"))));
            contactAddressToSave.setLatitude(Float.valueOf(String.valueOf(tomtomResponse.get("xCoordinates"))));
            zipCode = zipCodeGraphRepository.findOne(addressDTO.getZipCode().getId());
            if (zipCode == null) {
                logger.debug("ZipCode Not Found returning null");
                return null;
            }
        }
        Map<String, Object> geographyData = regionGraphRepository.getGeographicData(municipality.getId());
        if (geographyData == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_GEOGRAPHYDATA_NOTFOUND, municipality.getId());
        }
        setContactAddress(addressDTO, contactAddressToSave, municipality, zipCode, geographyData);
        return contactAddressToSave;
    }

    private void setContactAddress(AddressDTO addressDTO, ContactAddress contactAddressToSave, Municipality municipality, ZipCode zipCode, Map<String, Object> geographyData) {
        contactAddressToSave.setMunicipality(municipality);
        contactAddressToSave.setProvince(String.valueOf(geographyData.get("provinceName")));
        contactAddressToSave.setCountry(String.valueOf(geographyData.get("countryName")));
        contactAddressToSave.setRegionName(String.valueOf(geographyData.get("regionName")));
        contactAddressToSave.setCountry(String.valueOf(geographyData.get("countryName")));
        contactAddressToSave.setZipCode(zipCode);
        contactAddressToSave.setStreet(addressDTO.getStreet());
        contactAddressToSave.setHouseNumber(addressDTO.getHouseNumber());
        contactAddressToSave.setFloorNumber(addressDTO.getFloorNumber());
        contactAddressToSave.setCity(zipCode.getName());
    }

    private CitizenStatus getCivilianStatus(NextToKinDTO nextToKinDTO) {
        CitizenStatus citizenStatus = citizenStatusGraphRepository.findOne(nextToKinDTO.getCivilianStatusId(), 0);
        if (!Optional.ofNullable(citizenStatus).isPresent()) {
            logger.debug("Finding civilian status using id " + nextToKinDTO.getCivilianStatusId());
            exceptionService.dataNotFoundByIdException(MESSAGE_CLIENT_CITIZENSTATUS_ID_NOTFOUND, citizenStatus);

        }
        return citizenStatus;

    }


    private void saveCitizenRelation(Long relationTypeId, Long unitId, Client nextToKin, Long clientId) {

        Long countryId = countryGraphRepository.getCountryIdByUnitId(unitId);
        Client client = clientGraphRepository.findOne(clientId);
        if (Optional.ofNullable(relationTypeId).isPresent()) {
            RelationType relationType = countryGraphRepository.getRelationType(countryId, relationTypeId);
            ClientRelationType clientRelationTypeRelationship = clientGraphRepository.getClientRelationType(clientId, nextToKin.getId());
            clientGraphRepository.removeClientRelationType(clientId, nextToKin.getId());
            if (Optional.ofNullable(clientRelationTypeRelationship).isPresent())
                clientGraphRepository.removeClientRelationById(clientRelationTypeRelationship.getId());
            clientRelationTypeRelationship = new ClientRelationType();
            clientRelationTypeRelationship.setRelationType(relationType);
            clientRelationTypeRelationship.setNextToKin(nextToKin);
            client.addClientRelations(clientRelationTypeRelationship);
            clientGraphRepository.save(client);
        } else {
            exceptionService.dataNotFoundByIdException(ERROR_CLIENT_RELATIONTYPE_NOTEMPTY);

        }
    }

    // Add new home address of client after detaching all household members
    public ContactAddress addNewHomeAddress(AddressDTO addressDTO, Client client, long unitId, String type) {
        ContactAddress contactAddress = ContactAddress.getInstance();
        contactAddress = verifyAndSaveAddressOfNextToKin(unitId, addressDTO, contactAddress);
        if (contactAddress == null) {
            return null;
        }
        return addressVerificationService.saveAndUpdateClientAddress(client, contactAddress, type);
    }

    public NextToKinDTO updateNextToKinDetail(long unitId, long nextToKinId, NextToKinDTO nextToKinDTO, long clientId) {
        User nextToKin = userGraphRepository.findById(nextToKinId).orElseThrow(() -> new DataNotFoundByIdException(CommonsExceptionUtil.convertMessage(MESSAGE_CLIENT_NEXTTOKIN_ID_NOTFOUND, nextToKinId)));
        nextToKin.setBasicDetail(nextToKinDTO);
        Client nextToKinClient = clientGraphRepository.getClientByUserId(nextToKinId);
        Long homeAddressId = clientGraphRepository.getIdOfHomeAddress(nextToKinId);
        if (!Optional.ofNullable(homeAddressId).isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_CLIENT_HOMEADDRESS_NOTFOUND);
        }
        ContactAddress homeAddress = contactAddressGraphRepository.findOne(homeAddressId);
        // Add new address for nextToKin of client if household adress are not being updated
        homeAddress = nextToKinDTO.isUpdateHouseholdAddress() ? verifyAndSaveAddressOfNextToKin(unitId, nextToKinDTO.getHomeAddress(), homeAddress) : addNewHomeAddress(nextToKinDTO.getHomeAddress(), nextToKinClient, unitId, HAS_HOME_ADDRESS);
        if (!Optional.ofNullable(homeAddress).isPresent()) {
            return null;
        }
        nextToKin.setHomeAddress(homeAddress);
        ContactDetail contactDetail = clientGraphRepository.getContactDetailOfNextToKin(nextToKinId).orElseThrow(() -> new DataNotFoundByIdException(CommonsExceptionUtil.convertMessage(MESSAGE_CLIENT_CONTACTDETAIL_NOTFOUND)));
        nextToKinClient.saveContactDetail(nextToKinDTO, contactDetail);
        nextToKin.setContactDetail(contactDetail);
        CitizenStatus citizenStatus = getCivilianStatus(nextToKinDTO);
        nextToKinClient.setCivilianStatus(citizenStatus);
        saveCitizenRelation(nextToKinDTO.getRelationTypeId(), unitId, nextToKinClient, clientId);
        logger.debug("Preparing response");
        userGraphRepository.save(nextToKin);
        return new NextToKinDTO().buildResponse(nextToKin, envConfig.getServerHost() + FORWARD_SLASH,
                nextToKinDTO.getRelationTypeId(), nextToKinDTO);
    }

    public NextToKinQueryResult getNextToKinByCprNumber(String cprNumber) {
        if (StringUtils.isEmpty(cprNumber) || cprNumber.length() < 10) {
            logger.error("Cpr number is incorrect {}", cprNumber);
        }
        return clientGraphRepository.getNextToKinByCprNumber(cprNumber, envConfig.getServerHost() + FORWARD_SLASH);

    }

    public Map<String, Object> setTransportationDetails(Client client) {
        Client currentClient = clientGraphRepository.findOne(client.getId());
        if (currentClient != null) {
            //Update Transport Details
            setClientInfo(client, currentClient);
            List<Language> languages = client.getLanguageUnderstands();
            List<Language> languageList = new ArrayList<>();
            logger.debug("Language Understands: {}" , client.getLanguageUnderstands().size());
            languageGraphRepository.removeAllLanguagesFromClient(currentClient.getId());
            for (Language lang : languages) {
                Language language = languageGraphRepository.findOne(lang.getId());
                ClientLanguageRelation languageRelation;
                if (language != null) {
                    languageRelation = new ClientLanguageRelation(currentClient, language);
                    logger.debug("Adding Language to list: {}" , language.getName());
                    languageList.add(language);
                    clientLanguageRelationGraphRepository.save(languageRelation);
                }
            }
            logger.debug("Adding Language Understand: {}" , languageList.size());
            currentClient.setDoRequireTranslationAssistance(client.isDoRequireTranslationAssistance());
            currentClient.setRequire2peopleForTransport(client.isRequire2peopleForTransport());
            currentClient.setRequireOxygenUnderTransport(client.isRequireOxygenUnderTransport());
            clientGraphRepository.save(currentClient);
            return getTransportationDetails(currentClient.getId());
        }
        return null;
    }

    private void setClientInfo(Client client, Client currentClient) {
        currentClient.setDriverLicenseNumber(client.getDriverLicenseNumber());
        currentClient.setUseWheelChair(client.isUseWheelChair());
        currentClient.setLiftBus(client.isLiftBus());
        currentClient.setRequiredEquipmentsList(client.getRequiredEquipmentsList());
        currentClient.setUseOwnVehicle(client.isUseOwnVehicle());
        currentClient.setWantToUserOwnVehicle(client.isWantToUserOwnVehicle());
    }


    public Map<String, Object> getTransportationDetails(Long clientId) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> transportDetails = clientGraphRepository.findOne(clientId).retrieveTransportationDetails();

        if (transportDetails != null) {
            List<Long> languageDetails = clientLanguageRelationGraphRepository.findClientLanguagesIds(clientId);
            response.put("transportDetails", transportDetails == null ? Collections.EMPTY_MAP : transportDetails);
            response.put("languageUnderstands", languageDetails == null ? Collections.EMPTY_LIST : languageDetails.toArray());
            return response;
        }
        return null;
    }

    public List<Map<String, Object>> getRelativeDetails(Long id) {
        return clientGraphRepository.getRelativesListByClientId(id);
    }


    public ClientRelativeRelation setRelativeDetails(Map<String, Object> relativeProperties, Long relativeId) {
        User relative = userGraphRepository.findById(relativeId).orElseThrow(()->new DataNotFoundByIdException(CommonsExceptionUtil.convertMessage(MESSAGE_CLIENT_ID_NOTFOUND, relativeId)));
        Long relationId = Long.valueOf(((String.valueOf(relativeProperties.get("relationId")))));
        ClientRelativeRelation clientRelativeRelation = clientRelativeGraphRepository.findById(relationId).orElseThrow(()->new DataNotFoundByIdException(CommonsExceptionUtil.convertMessage(MESSAGE_CLIENT_ID_NOTFOUND, relationId)));
            // First update UserDetails
            relative.setFirstName(String.valueOf(relativeProperties.get("firstName")));
            relative.setLastName(String.valueOf(relativeProperties.get("lastName")));
            // AddressDTO
            Long addressId = Long.valueOf((Integer) relativeProperties.get("addressId"));
            ContactAddress contactAddress = contactAddressGraphRepository.findOne(addressId);
            if (contactAddress != null) {
                logger.debug("AddressDTO found: Updating...");
                setContactAddressInfo(relativeProperties, contactAddress);
            }

            // Contact
            Long contactId = Long.valueOf(((String.valueOf(relativeProperties.get("contactId")))));
            ContactDetail contactDetails = contactDetailsGraphRepository.findOne(contactId);
            if (contactDetails != null) {
                logger.debug("Contact Details: Updating...");
                setContactDetails(relativeProperties, contactDetails);
            }
            relative.setHomeAddress(contactAddress);
            relative.setContactDetail(contactDetails);
            userGraphRepository.save(relative);
            // Relation
            setRelativesDetails(relativeProperties, clientRelativeRelation);
            return clientRelativeRelationshipRepository.save(clientRelativeRelation);
    }

    private void setContactAddressInfo(Map<String, Object> relativeProperties, ContactAddress contactAddress) {
        contactAddress.setStreet((String) relativeProperties.get("street1"));
        contactAddress.setCity((String) relativeProperties.get("city"));
        contactAddress.setCountry((String) relativeProperties.get("country"));
        contactAddress.setLongitude(Float.valueOf((Integer) relativeProperties.get("longitude")));
        contactAddress.setLatitude(Float.valueOf((Integer) relativeProperties.get("latitude")));
    }

    private void setContactDetails(Map<String, Object> relativeProperties, ContactDetail contactDetails) {
        contactDetails.setWorkEmail((String) relativeProperties.get("workEmail"));
        contactDetails.setWorkPhone((String) relativeProperties.get("workPhone"));
        contactDetails.setMobilePhone((String) relativeProperties.get("mobilePhone"));
        contactDetails.setPrivateEmail((String) relativeProperties.get("privateEmail"));
        contactDetails.setPrivatePhone((String) relativeProperties.get("privatePhone"));
        contactDetails.setTwitterAccount((String) relativeProperties.get("twitterAccount"));
        contactDetails.setFacebookAccount((String) relativeProperties.get("facebookAccount"));
    }


    //TODO create relative details

    public ClientRelativeRelation setNewRelativeDetails(Map<String, Object> relativeProperties) {
        User relative = new User();
        ClientRelativeRelation clientRelativeRelation = new ClientRelativeRelation();
        // First update UserDetails
        relative.setFirstName(String.valueOf(relativeProperties.get("firstName")));
        relative.setLastName(String.valueOf(relativeProperties.get("lastName")));
        // AddressDTO
        Long addressId = Long.valueOf((Integer) relativeProperties.get("addressId"));
        ContactAddress contactAddress = contactAddressGraphRepository.findOne(addressId);
        if (contactAddress != null) {
            logger.debug("AddressDTO found: Updating...");
            setContactAddressInfo(relativeProperties, contactAddress);
        }
        // Contact
        ContactDetail contactDetails = getContactDetail(relativeProperties);
        relative.setHomeAddress(contactAddress);
        relative.setContactDetail(contactDetails);
        userGraphRepository.save(relative);
        // Relation
        setRelativesDetails(relativeProperties, clientRelativeRelation);
        return clientRelativeRelationshipRepository.save(clientRelativeRelation);
    }

    private void setRelativesDetails(Map<String, Object> relativeProperties, ClientRelativeRelation clientRelativeRelation) {
        clientRelativeRelation.setCanUpdateOnPublicPortal((Boolean) relativeProperties.get("canUpdateOnPublicPortal"));
        clientRelativeRelation.setFullGuardian((Boolean) relativeProperties.get("isFullGuardian"));
        clientRelativeRelation.setDistanceToRelative(String.valueOf(relativeProperties.get("distanceToRelative")));
        clientRelativeRelation.setRemarks((String) relativeProperties.get("remarks"));
        clientRelativeRelation.setRelation((String) relativeProperties.get("relation"));
        clientRelativeRelation.setPriority((String) relativeProperties.get("priority"));
    }

    private ContactDetail getContactDetail(Map<String, Object> relativeProperties) {
        Long contactId = Long.valueOf(((String.valueOf(relativeProperties.get("contactId")))));
        ContactDetail contactDetails = contactDetailsGraphRepository.findOne(contactId);
        if (contactDetails != null) {
            logger.debug("Contact Details: Updating...");
            setContactDetails(relativeProperties, contactDetails);
        }
        return contactDetails;
    }

    public ClientDoctor setMedicalDetails(Long clientId, ClientDoctor clientDoctor) {
        Client currentClient = clientGraphRepository.findOne(clientId);

        if (currentClient.getClientDoctorList() == null) {
            currentClient.setClientDoctorList(Arrays.asList(clientDoctor));
            clientGraphRepository.save(currentClient);
            return clientDoctor;
        }
        List<ClientDoctor> clientDoctorList = currentClient.getClientDoctorList();
        clientDoctorList.add(clientDoctor);
        clientGraphRepository.save(currentClient);
        return clientDoctor;
    }


    public ClientAllergies setHealthDetails(ClientAllergies clientAllergies, Long clientId) {
        Client currentClient = clientGraphRepository.findOne(clientId);
        if (currentClient.getClientAllergiesList() == null) {
            currentClient.setClientAllergiesList(Arrays.asList(clientAllergies));
            clientGraphRepository.save(currentClient);
            return clientAllergies;
        }
        List<ClientAllergies> clientAllergiesList = currentClient.getClientAllergiesList();
        clientAllergiesList.add(clientAllergies);
        clientGraphRepository.save(currentClient);
        return clientAllergies;
    }


    public ContactDetail setSocialMediaDetails(Long clientId, ContactDetailSocialDTO socialMediaDetail) {
        // Client Social Media
        Client currentClient = clientGraphRepository.findOne(clientId);
        if (currentClient != null) {
            ContactDetail detail = currentClient.getContactDetail();
            if (detail == null) {
                detail = new ContactDetail();
            }
            detail.setFacebookAccount(String.valueOf(socialMediaDetail.getFacebookAccount()));
            detail.setTwitterAccount(String.valueOf(socialMediaDetail.getTwitterAccount()));
            detail.setLinkedInAccount(String.valueOf(socialMediaDetail.getLinkedInAccount()));
            detail.setMessenger(String.valueOf(socialMediaDetail.getMessenger()));
            detail.setHideMobilePhone(socialMediaDetail.isHideMobilePhone());
            detail.setHideWorkPhone(socialMediaDetail.isHideWorkPhone());
            detail.setHidePrivatePhone(socialMediaDetail.isHidePrivatePhone());
            detail.setMobilePhone(String.valueOf(socialMediaDetail.getMobilePhone()));
            detail.setWorkPhone(String.valueOf(socialMediaDetail.getWorkPhone()));
            detail.setPrivatePhone(String.valueOf(socialMediaDetail.getPrivatePhone()));
            detail.setPrivateEmail(String.valueOf(socialMediaDetail.getWorkEmail()));
            detail.setEmergencyPhone(String.valueOf(socialMediaDetail.getEmergencyPhone()));
            detail.setHideEmergencyPhone(socialMediaDetail.isHideEmergencyPhone());
            currentClient.setContactDetail(contactDetailsGraphRepository.save(detail));
            // try saving with native repo of Node
            clientGraphRepository.save(currentClient);
            return detail;
        }
        return null;
    }


    public Map<String, Object> getMedicalDetails(Long clientId) {
        return clientGraphRepository.findOne(clientId).retrieveMedicalDetails();
        // TODO: 24/10/16 type of doctor enum
    }

    public List<ClientAllergies> getHealthDetails(Long clientId) {
        return clientGraphRepository.findOne(clientId).getClientAllergiesList();
    }

    public ClientDiagnose addDiagnoseToMedicalInformation(Long clientId, ClientDiagnose clientDiagnose) {
        Client currentClient = clientGraphRepository.findOne(clientId);
        if (currentClient.getClientDiagnoseList() == null) {
            currentClient.setClientDiagnoseList(Arrays.asList(clientDiagnose));
            clientGraphRepository.save(currentClient);
            return clientDiagnose;
        }
        List<ClientDiagnose> clientDiagnoseList = currentClient.getClientDiagnoseList();
        clientDiagnoseList.add(clientDiagnose);
        currentClient.setClientDiagnoseList(clientDiagnoseList);
        clientGraphRepository.save(currentClient);
        return clientDiagnose;

    }


    public ClientAllergies updateClientAllergy(ClientAllergies clientAllergies) {
        ClientAllergies allergies = clientAllergiesGraphRepository.findOne(clientAllergies.getId());
        allergies.setAvoidance(clientAllergies.getAvoidance());
        allergies.setAllergyValidated(clientAllergies.isAllergyValidated());
        allergies.setAllergyType(clientAllergies.getAllergyType());
        allergies.setAllergyName(clientAllergies.getAllergyName());
        return clientAllergiesGraphRepository.save(clientAllergies);
    }


    public boolean deleteMedicalDiagnose(Long diagnoseId) {
        if (clientDiagnoseGraphRepository.existsById(diagnoseId)) {
            logger.debug("diagnose exist");
        }
        clientDiagnoseGraphRepository.deleteById(diagnoseId);
        boolean result = clientDiagnoseGraphRepository.existsById(diagnoseId);
        if (!result) {
            logger.debug("deleted diagnose not exist ");
        }
        return result;
    }

    public String uploadAccessToLocationImage(Long accessToLocationId, MultipartFile multipartFile) {
        AccessToLocation accessToLocation = accessToLocationGraphRepository.findOne(accessToLocationId);
        if (accessToLocation == null) {
            return null;
        }
        String fileName = DateUtils.getDate().getTime() + multipartFile.getOriginalFilename();
        createDirectory(IMAGES_PATH);
        final String path = IMAGES_PATH + File.separator + fileName.trim();
        if (new File(IMAGES_PATH).isDirectory()) {
            FileUtil.writeFile(path, multipartFile);
        }
        accessToLocation.setAccessPhotoURL(fileName);
        accessToLocationGraphRepository.save(accessToLocation);
        return envConfig.getServerHost() + FORWARD_SLASH + envConfig.getImagesPath() + fileName;
    }

    public void removeAccessToLocationImage(long accessToLocationId) {
        AccessToLocation accessToLocation = accessToLocationGraphRepository.findOne(accessToLocationId);
        if (accessToLocation == null) {
            exceptionService.internalServerError(ERROR_ACCESSTOLOCATION_NULL);

        }
        accessToLocation.setAccessPhotoURL(null);
        accessToLocationGraphRepository.save(accessToLocation);
    }

    public HashMap<String, String> uploadImageOfNextToKin(MultipartFile multipartFile) {
        String fileName = writeFile(multipartFile);
        HashMap<String, String> imageurls = new HashMap<>();
        imageurls.put("profilePic", fileName);
        imageurls.put("profilePicUrl", envConfig.getServerHost() + FORWARD_SLASH + envConfig.getImagesPath() + fileName);
        return imageurls;
    }

    private String writeFile(MultipartFile multipartFile) {
        String fileName = DateUtils.getDate().getTime() + multipartFile.getOriginalFilename();
        createDirectory(IMAGES_PATH);
        final String path = IMAGES_PATH + File.separator + fileName.trim();
        if (new File(IMAGES_PATH).isDirectory()) {
            FileUtil.writeFile(path, multipartFile);
        }
        return fileName;
    }

    public HashMap<String, String> updateImageOfNextToKin(long unitId, long nextToKinId, MultipartFile multipartFile) {
        Client nextToKin = clientGraphRepository.findOne(nextToKinId, unitId);
        if (nextToKin == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_CLIENT_ID_NOTFOUND, nextToKinId);

        }
        String fileName = writeFile(multipartFile);
        nextToKin.setProfilePic(fileName);
        clientGraphRepository.save(nextToKin);
        HashMap<String, String> imageurls = new HashMap<>();
        imageurls.put("profilePic", fileName);
        imageurls.put("profilePicUrl", envConfig.getServerHost() + FORWARD_SLASH + envConfig.getImagesPath() + fileName);
        return imageurls;
    }


    public String uploadPortrait(Long clientId, MultipartFile multipartFile) {
        Client client = clientGraphRepository.findOne(clientId);
        if (client == null) {
            logger.debug("Client is null");
            return null;
        }

        String fileName = DateUtils.getDate().getTime() + multipartFile.getOriginalFilename();
        createDirectory(IMAGES_PATH);
        final String path = IMAGES_PATH + File.separator + fileName.trim();
        if (new File(IMAGES_PATH).isDirectory()) {
            FileUtil.writeFile(path, multipartFile);
        }
        client.setProfilePic(fileName);
        clientGraphRepository.save(client);
        return envConfig.getServerHost() + FORWARD_SLASH + envConfig.getImagesPath() + fileName;
    }


    private boolean createDirectory(String imagesPath) {
        File theDir = new File(imagesPath);
        if (!theDir.exists()) {
            logger.debug("creating directory:{} ", imagesPath);
            boolean result = false;
            try {
                theDir.mkdir();
                result = true;
            } catch (SecurityException ignored) {
            }
            if (result) {
                logger.debug("DIR created");
            }
        }
        return false;
    }


    public boolean deleteImage(Long clientId) {
        Client currentClient = clientGraphRepository.findOne(clientId);
        User user = clientGraphRepository.getUserByClientId(clientId);
        String defaultPic = (Gender.MALE.equals(user.getGender())) ? "default_male_icon.png" : "default_female_icon.png";
        currentClient.setProfilePic(defaultPic);
        clientGraphRepository.save(currentClient);
        return false;
    }

}
