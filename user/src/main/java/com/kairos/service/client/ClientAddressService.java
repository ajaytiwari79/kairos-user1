package com.kairos.service.client;

import com.kairos.config.env.EnvConfig;
import com.kairos.dto.user.organization.AddressDTO;
import com.kairos.persistence.model.client.AccessToLocation;
import com.kairos.persistence.model.client.Client;
import com.kairos.persistence.model.client.ClientTemporaryAddress;
import com.kairos.persistence.model.client.ContactAddress;
import com.kairos.persistence.model.client.query_results.ClientAddressQueryResult;
import com.kairos.persistence.model.client.query_results.ClientTempAddressQueryResult;
import com.kairos.persistence.model.country.default_data.HousingType;
import com.kairos.persistence.model.country.default_data.HousingTypeDTO;
import com.kairos.persistence.model.user.region.Municipality;
import com.kairos.persistence.model.user.region.ZipCode;
import com.kairos.persistence.repository.user.client.AccessToLocationGraphRepository;
import com.kairos.persistence.repository.user.client.ClientGraphRepository;
import com.kairos.persistence.repository.user.client.ContactAddressGraphRepository;
import com.kairos.persistence.repository.user.client.TemporaryAddressGraphRepository;
import com.kairos.persistence.repository.user.country.CountryGraphRepository;
import com.kairos.persistence.repository.user.country.HousingTypeGraphRepository;
import com.kairos.persistence.repository.user.region.MunicipalityGraphRepository;
import com.kairos.persistence.repository.user.region.RegionGraphRepository;
import com.kairos.persistence.repository.user.region.ZipCodeGraphRepository;
import com.kairos.service.country.CountryService;
import com.kairos.service.country.HousingTypeService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.utils.FormatUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;

import static com.kairos.commons.utils.DateUtils.getDate;
import static com.kairos.constants.AppConstants.FORWARD_SLASH;
import static com.kairos.constants.UserMessagesConstants.*;
import static com.kairos.persistence.model.constants.RelationshipConstants.HAS_HOME_ADDRESS;
import static com.kairos.persistence.model.constants.RelationshipConstants.HAS_TEMPORARY_ADDRESS;

/**
 * Created by oodles on 22/5/17.
 */
@Service
@Transactional
public class ClientAddressService{
    public static final String MUNICIPALITIES = "municipalities";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Inject
    private ClientGraphRepository clientGraphRepository;
    @Inject
    private RegionGraphRepository regionGraphRepository;
    @Inject
    private CountryGraphRepository countryGraphRepository;
    @Inject
    private ZipCodeGraphRepository zipCodeGraphRepository;
    @Inject
    private HousingTypeService housingTypeService;
    @Inject
    private HousingTypeGraphRepository housingTypeGraphRepository;
    @Inject
    private AddressVerificationService addressVerificationService;
    @Inject
    private TemporaryAddressGraphRepository temporaryAddressGraphRepository;
    @Inject
    private ContactAddressGraphRepository contactAddressGraphRepository;
    @Inject
    private MunicipalityGraphRepository municipalityGraphRepository;
    @Inject
    private AccessToLocationGraphRepository accessToLocationGraphRepository;
    @Inject
    private EnvConfig envConfig;
    @Inject
    private ExceptionService exceptionService;
    @Inject
    private CountryService countryService;


    public Map<String, Object> getAddressDetails(Long clientId, Long unitId) {
        Client citizen = clientGraphRepository.getClientByClientIdAndUnitId(clientId,unitId);
        if (citizen == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_CLIENT_CITIZEN_NOTFOUND,clientId,unitId);

        }
        ClientAddressQueryResult clientAddressQueryResult = clientGraphRepository.getAllAddress(clientId);
        List<ClientTempAddressQueryResult> clientTempAddressQueryResult = clientGraphRepository.getTemporaryAddress(clientId);
        Map<String, Object> homeAddressInfo = getHomeAddress(clientAddressQueryResult.getHomeAddress(), clientAddressQueryResult.getHomeZipCode(), clientAddressQueryResult.getHomeAddressMunicipality(), clientAddressQueryResult.getHomeAddressHousingType());
        Map<String, Object> secondaryAddressInfo = getSecondaryAddressInfo(clientAddressQueryResult);
        Map<String, Object> partnerAddressInfo = getHomeAddress(clientAddressQueryResult.getPartnerAddress(), clientAddressQueryResult.getPartnerZipCode(), clientAddressQueryResult.getPartnerAddressMunicipality(), clientAddressQueryResult.getPartnerAddressHousingType());

        List<Map<String, Object>> temporaryAddressList = getTemporaryAddressList(clientTempAddressQueryResult);
        Map<String, Object> response = new HashMap<>();
        response.put("homeAddress", homeAddressInfo);
        response.put("secondaryAddress", secondaryAddressInfo);
        response.put("partnerAddress", partnerAddressInfo);
        response.put("temporaryAddress", temporaryAddressList);
        Long countryId = countryService.getCountryIdByUnitId(unitId);
        if (countryId != null) {
            response.put("zipCodeData", FormatUtil.formatNeoResponse(zipCodeGraphRepository.getAllZipCodeByCountryId(countryId)));
        } else {
            logger.debug("Country null");
            response.put("zipCodeData", Collections.emptyList());
        }
        List<HousingTypeDTO> housingTypeList = housingTypeService.getHousingTypeByCountryId(countryId);
        response.put("typeOfHousingData", housingTypeList != null ? housingTypeList : Collections.EMPTY_LIST);
        return response;
    }

    private List<Map<String, Object>> getTemporaryAddressList(List<ClientTempAddressQueryResult> clientTempAddressQueryResult) {
        List<Map<String, Object>> temporaryAddressList = new ArrayList<>();
        for (ClientTempAddressQueryResult tempAddressQueryResult : clientTempAddressQueryResult) {
            if (tempAddressQueryResult.getTemporaryAddress().isEnabled()) {
                Map<String, Object> temporaryAddressInfo = filterAddressResponse(tempAddressQueryResult.getTemporaryAddress(),
                        tempAddressQueryResult.getTemporaryZipCode(), tempAddressQueryResult.getTemporaryAddressMunicipality(), tempAddressQueryResult.getTemporaryAddressHousingType());
                temporaryAddressInfo.put(MUNICIPALITIES, (tempAddressQueryResult.getTemporaryZipCode() != null) ?
                        FormatUtil.formatNeoResponse(regionGraphRepository.getGeographicTreeData(tempAddressQueryResult.getTemporaryZipCode().getId())) : Collections.emptyList());
                temporaryAddressList.add(temporaryAddressInfo);
            }
        }
        return temporaryAddressList;
    }

    private Map<String, Object> getSecondaryAddressInfo(ClientAddressQueryResult clientAddressQueryResult) {
        Map<String, Object> secondaryAddressInfo = null;
        if (clientAddressQueryResult.getSecondaryAddress() != null) {
            secondaryAddressInfo = filterAddressResponse(clientAddressQueryResult.getSecondaryAddress(),
                    clientAddressQueryResult.getSecondaryZipCode(), clientAddressQueryResult.getSecondaryAddressMunicipality(), clientAddressQueryResult.getSecondaryAddressHousingType());
            secondaryAddressInfo.put(MUNICIPALITIES, (clientAddressQueryResult.getSecondaryAddress() != null && clientAddressQueryResult.getSecondaryZipCode() != null) ?
                    FormatUtil.formatNeoResponse(regionGraphRepository.getGeographicTreeData(clientAddressQueryResult.getSecondaryZipCode().getId())) : Collections.emptyList());
        }
        return secondaryAddressInfo;
    }

    private Map<String, Object> getHomeAddress(ContactAddress homeAddress, ZipCode homeZipCode, Municipality homeAddressMunicipality, HousingType homeAddressHousingType) {
        Map<String, Object> homeAddressInfo = null;
        if (homeAddress != null) {
            homeAddressInfo = filterAddressResponse(homeAddress,
                    homeZipCode, homeAddressMunicipality, homeAddressHousingType);
            homeAddressInfo.put(MUNICIPALITIES, (homeZipCode != null) ?
                    FormatUtil.formatNeoResponse(regionGraphRepository.getGeographicTreeData(homeZipCode.getId())) : Collections.emptyList());
        }
        return homeAddressInfo;
    }

    private Map<String, Object> filterAddressResponse(ContactAddress contactAddress, ZipCode zipCode, Municipality municipality, HousingType housingType) {
        if (contactAddress == null) {
            return null;
        }
        HashMap<String, Object> address = new HashMap<>();
        address.put("id", contactAddress.getId());
        address.put("floorNumber", contactAddress.getFloorNumber());
        address.put("houseNumber", contactAddress.getHouseNumber());
        address.put("city", contactAddress.getCity());
        address.put("regionName", contactAddress.getRegionName());
        address.put("province", contactAddress.getProvince());
        address.put("longitude", contactAddress.getLongitude());
        address.put("latitude", contactAddress.getLatitude());
        address.put("privateAddress", contactAddress.isPrivateAddress());
        address.put("zipCode", zipCode);
        address.put("typeOfHousing", housingType);
        address.put("municipalityId", (municipality == null) ? null : municipality.getId());
        address.put("verifiedByVisitour", contactAddress.isVerifiedByVisitour());
        address.put("addressProtected", contactAddress.isAddressProtected());
        address.put("description", contactAddress.getDescription());
        address.put("startDate", getDate(contactAddress.getStartDate()));
        address.put("endDate", getDate(contactAddress.getEndDate()));
        address.put("street1", contactAddress.getStreet());
        address.put("locationName",contactAddress.getLocationName());
        return address;
    }

    /**
     * @param unitId
     * @param clientId
     * @param addressDTO contains address details to update
     * @param type       {values={home address,temporary address,secondary address,partner address}}
     * @return if success will return updated address otherwise throw Internal error
     * @auhor prabjot
     * to save address of client based upon parameter {type}
     */
    public ContactAddress saveAddress(AddressDTO addressDTO, long clientId, String type, long unitId) {

        Client client = clientGraphRepository.findOne(clientId);
        if (client == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_CLIENT_CITIZEN_NOTFOUND,clientId,unitId);

        }
        ContactAddress contactAddress;
        if (type.equals(HAS_TEMPORARY_ADDRESS)) {
            contactAddress = ClientTemporaryAddress.getInstance();
        } else {
            contactAddress = ContactAddress.getInstance();
        }
        contactAddress = persistAddress(addressDTO, client, contactAddress, unitId);
        if (contactAddress == null) {
            return null;
        }
        return addressVerificationService.saveAndUpdateClientAddress(client, contactAddress, type);
    }

    // Add new home address of client after detaching all household members
    public ContactAddress addNewHomeAddress(long oldContactAddressId, AddressDTO addressDTO, Client client, long unitId, String type ){

        ContactAddress contactAddress = ContactAddress.getInstance();
        contactAddress = persistAddress(addressDTO, client, contactAddress, unitId);
        if (contactAddress == null) {
            return null;
        }
        // Detach relationship with old address and hosehold members
//        detachHomeAddressFromClient(client.getId(), oldContactAddressId);
//        detachHouseHoldMembersFromClient(client.getId());

        return addressVerificationService.saveAndUpdateClientAddress(client, contactAddress, type);
    }

    /**
     * @param clientId
     * @param contactAddressId
     * @return if success will return boolean
     * @auhor prerna
     * Detach relationship with old home address
     */
    /*public boolean detachHomeAddressFromClient(long clientId, long contactAddressId){
        return clientGraphRepository.detachHomeAddressRelationOfClient(clientId, contactAddressId);
    }*/

    /**
     * @return if success will return boolean
     * @auhor prerna
     * Detach relationship with House hold members
     */
    /*public boolean detachHouseHoldMembersFromClient(long clientId){
        return clientGraphRepository.detachHouseholdRelationOfClient(clientId);
    }*/


    public Boolean updateAddressOfAllHouseHoldMembers(long contactAddressId, long addressIdOfHouseHold){
        List<Long> listOfIdsOfHouseholdMembers = getListOfAllHouseHoldMemberssByAddressId(addressIdOfHouseHold);
        if(listOfIdsOfHouseholdMembers.size() > 0){
            detachAddressOfHouseholdMembersWithDifferentAddress(contactAddressId, listOfIdsOfHouseholdMembers);
            return clientGraphRepository.updateAddressOfAllHouseHoldMembers(contactAddressId, listOfIdsOfHouseholdMembers);
        }
        return true;
    }

    public Boolean detachAddressOfHouseholdMembersWithDifferentAddress(long contactAddressId,List<Long> listOfIdsOfHouseholdMembers){
        return clientGraphRepository.detachAddressOfHouseholdMembersWithDifferentAddress(contactAddressId, listOfIdsOfHouseholdMembers);
    }

    public List<Long> getListOfAllHouseHoldMemberssByAddressId(long contactAddressId){
        return clientGraphRepository.getIdsOfAllHouseHoldMembers(contactAddressId);
    }

    /**
     * @param unitId
     * @param clientId
     * @param addressId
     * @param addressDTO contains address details to update
     * @param type       {values={home address,temporary address,secondary address,partner address}}
     * @return if success will return updated address otherwise throw Internal error
     * @auhor prabjot
     * to update address of client based upon parameter {type}
     */
    public ContactAddress updateAddress(long unitId, long clientId, long addressId, AddressDTO addressDTO, String type) {

        Client client = clientGraphRepository.findOne(clientId);
        if (client == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_CLIENT_CITIZEN_NOTFOUND,clientId,unitId);
        }
        ContactAddress contactAddress;
        if (HAS_TEMPORARY_ADDRESS.equals(type)) {
            contactAddress = temporaryAddressGraphRepository.findOne(addressId);
        } else {
            contactAddress = contactAddressGraphRepository.findOne(addressId);
        }
        if (contactAddress == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_CLIENT_CONTACTAADDRESS_NOTFOUND);
        }
        if( addressDTO.isUpdateHouseholdAddress() == false && HAS_HOME_ADDRESS.equals(type)){
            return addNewHomeAddress(addressId, addressDTO, client, unitId, type);
        } else {
            contactAddress = persistAddress(addressDTO, client, contactAddress, unitId);
        }
        if (contactAddress == null) {
            return null;
        }
        contactAddressGraphRepository.save(contactAddress);
        return contactAddress;
    }


    private ContactAddress persistAddress(AddressDTO addressDTO, Client client, ContactAddress contactAddress, long unitId) {
        ZipCode zipCode;
        if (addressDTO.isVerifiedByGoogleMap()) {
            zipCode = validateZipDetails(addressDTO, contactAddress);
            if (zipCode == null) return null;
        } else {
            zipCode = verifyAddressAndSetDetails(addressDTO, contactAddress, unitId);
            if (zipCode == null) return null;
        }
        Municipality municipality = municipalityGraphRepository.findOne(addressDTO.getMunicipality().getId());
        if (municipality == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_MUNICIPALITY_NOTFOUND);
        }
        Map<String, Object> geographyData = regionGraphRepository.getGeographicData(addressDTO.getMunicipality().getId());
        if (geographyData == null) {
            logger.info("Geography  not found with zipcodeId: {}" , zipCode.getId());
            exceptionService.dataNotFoundByIdException(MESSAGE_GEOGRAPHYDATA_NOTFOUND,addressDTO.getMunicipality().getId());
        }
        HousingType housingType = (addressDTO.getTypeOfHouseId() == null) ? null : housingTypeGraphRepository.findOne(addressDTO.getTypeOfHouseId());
        // Geography Data
        setContactAddressDetails(addressDTO, contactAddress, zipCode, municipality, geographyData, housingType);
        return contactAddress;
    }

    private ZipCode verifyAddressAndSetDetails(AddressDTO addressDTO, ContactAddress contactAddress, long unitId) {
        ZipCode zipCode;
        Map<String, Object> tomtomResponse = addressVerificationService.verifyAddress(addressDTO, unitId);
        if (tomtomResponse == null) {
            return null;
        }
        contactAddress.setCountry("Denmark");
        contactAddress.setVerifiedByVisitour(true);
        contactAddress.setLongitude(Float.valueOf(String.valueOf(tomtomResponse.get("yCoordinates"))));
        contactAddress.setLatitude(Float.valueOf(String.valueOf(tomtomResponse.get("xCoordinates"))));
        zipCode = zipCodeGraphRepository.findOne(addressDTO.getZipCode().getId());
        if (zipCode == null) {
            logger.debug("ZipCode Not Found returning null");
            return null;
        }
        return zipCode;
    }

    private ZipCode validateZipDetails(AddressDTO addressDTO, ContactAddress contactAddress) {
        ZipCode zipCode;
        if (addressDTO.getZipCode().getZipCode() == 0) {
            logger.debug("No ZipCode value received");
            return null;
        }
        zipCode = zipCodeGraphRepository.findByZipCode(addressDTO.getZipCode().getZipCode());
        if (zipCode == null) {
            logger.debug("ZipCode Not Found returning null");
            return null;
        }

        contactAddress.setLongitude(addressDTO.getLongitude());
        contactAddress.setLatitude(addressDTO.getLatitude());
        return zipCode;
    }

    private void setContactAddressDetails(AddressDTO addressDTO, ContactAddress contactAddress, ZipCode zipCode, Municipality municipality, Map<String, Object> geographyData, HousingType housingType) {
        contactAddress.setMunicipality(municipality);
        contactAddress.setProvince(String.valueOf(geographyData.get("provinceName")));
        contactAddress.setCountry(String.valueOf(geographyData.get("countryName")));
        contactAddress.setRegionName(String.valueOf(geographyData.get("regionName")));
        contactAddress.setCountry(String.valueOf(geographyData.get("countryName")));
        contactAddress.setTypeOfHousing(housingType);
        contactAddress.setStreet(addressDTO.getStreet());
        contactAddress.setHouseNumber(addressDTO.getHouseNumber());
        contactAddress.setFloorNumber(addressDTO.getFloorNumber());
        contactAddress.setZipCode(zipCode);
        contactAddress.setCity(zipCode.getName());
        contactAddress.setDescription(addressDTO.getDescription());
        contactAddress.setLocationName(addressDTO.getLocationName());
    }

    public AccessToLocation addAccessLocationDetails(AccessToLocation contactAddress, long contactAddressId) {
        ContactAddress address = contactAddressGraphRepository.findOne(contactAddressId);
        if (address != null) {
            logger.debug("Creating Access to Location");
            AccessToLocation accessToLocation = new AccessToLocation();
            // Access to Location
            if (contactAddress.getAlarmCode() != null) {
                accessToLocation.setHaveAlarmCode(true);
            }
            accessToLocation.setAlarmCode(contactAddress.getAlarmCode());
            accessToLocation.setAlarmCodeDescription(contactAddress.getAlarmCodeDescription());
            accessToLocation.setHowToAccessAddress(contactAddress.getHowToAccessAddress());
            accessToLocation.setKeySystem(contactAddress.getKeySystem());
            accessToLocation.setEmergencyCallDeviceType(contactAddress.getEmergencyCallDeviceType());
            accessToLocation.setEmergencyCallNumber(contactAddress.getEmergencyCallNumber());
            accessToLocation.setSerialNumber(contactAddress.getSerialNumber());
            accessToLocation.setReasonForDailyPhoneCall(contactAddress.getReasonForDailyPhoneCall());
            accessToLocation.setPortPhoneNumber(contactAddress.getPortPhoneNumber());
            accessToLocation.setDailyPhoneCallIsAgreed(contactAddress.isDailyPhoneCallIsAgreed());
            accessToLocation.setReasonForEmergencyCall(contactAddress.getReasonForEmergencyCall());
            accessToLocation.setRemarks(contactAddress.getRemarks());
            // Saving data
            AccessToLocation savedAccessToLocation = accessToLocationGraphRepository.save(accessToLocation);
            address.setAccessToLocation(savedAccessToLocation);
            contactAddressGraphRepository.save(address);
        } else {
            logger.debug("AddressDTO not found to set Access to location");
        }
        return null;
    }

    public boolean removeClientAddress(long addressId) {
        ClientTemporaryAddress contactAddress = temporaryAddressGraphRepository.findOne(addressId);
        if (contactAddress != null) {
            logger.debug("Disabling Contact AddressDTO");
            contactAddress.setEnabled(false);
            temporaryAddressGraphRepository.save(contactAddress);
            return true;
        }
        return false;
    }

    public AccessToLocation setAccessLocationDetails(AccessToLocation contactAddress) {
        // Contact AddressDTO
        logger.debug("Updating Access to Location");
        AccessToLocation accessToLocation = accessToLocationGraphRepository.findOne(contactAddress.getId());
        if (accessToLocation != null ) {
            logger.debug("Access to location found {}" ,contactAddress.getAlarmCode());
            accessToLocation.setHaveAlarmCode(!StringUtils.isEmpty(contactAddress.getAlarmCode()));
            accessToLocation.setAlarmCode(contactAddress.getAlarmCode());
            accessToLocation.setAlarmCodeDescription(contactAddress.getAlarmCodeDescription());
            accessToLocation.setHowToAccessAddress(contactAddress.getHowToAccessAddress());
            accessToLocation.setKeySystem(contactAddress.getKeySystem());
            accessToLocation.setEmergencyCallDeviceType(contactAddress.getEmergencyCallDeviceType());
            accessToLocation.setEmergencyCallNumber(contactAddress.getEmergencyCallNumber());
            accessToLocation.setSerialNumber(contactAddress.getSerialNumber());
            accessToLocation.setReasonForDailyPhoneCall(contactAddress.getReasonForDailyPhoneCall());
            accessToLocation.setPortPhoneNumber(contactAddress.getPortPhoneNumber());
            accessToLocation.setDailyPhoneCallIsAgreed(contactAddress.isDailyPhoneCallIsAgreed());
            accessToLocation.setReasonForEmergencyCall(contactAddress.getReasonForEmergencyCall());
            accessToLocation.setRemarks(contactAddress.getRemarks());
            // Saving data
            return accessToLocationGraphRepository.save(accessToLocation);
        } else {
            logger.debug("Access to location is null");
            return null;
        }
    }

    public List<Object> getAccessLocationDetails(Long clientId) {
        return (List) accessToLocationGraphRepository.findHomeAccessToLocation(clientId,envConfig.getServerHost() + FORWARD_SLASH).get("accessDetails");

    }

    // Update Contact adddress Longitude and Latitude
    public Object updateAddressCoordinates(AddressDTO address) {
        ContactAddress contactAddress = contactAddressGraphRepository.findOne(address.getId());
        if (contactAddress == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_CLIENT_CONTACTAADDRESS_NOTFOUND);

        }
        contactAddress.setLongitude(address.getLongitude());
        contactAddress.setLatitude(address.getLatitude());
        contactAddressGraphRepository.save(contactAddress);
        return contactAddress;
    }
}
