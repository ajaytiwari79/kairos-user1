package com.kairos.service.organizationMetadata;

import com.kairos.config.env.EnvConfig;
import com.kairos.custom_exception.DataNotFoundByIdException;
import com.kairos.persistence.model.organization.Organization;
import com.kairos.persistence.model.organization.PaymentSettings;
import com.kairos.persistence.model.organization.PaymentSettingsDTO;
import com.kairos.persistence.model.user.client.Client;
import com.kairos.persistence.model.user.client.ClientHomeAddressQueryResult;
import com.kairos.persistence.model.user.region.LatLng;
import com.kairos.persistence.model.user.region.LocalAreaTag;
import com.kairos.persistence.repository.organization.OrganizationGraphRepository;
import com.kairos.persistence.repository.organization.OrganizationMetadataRepository;
import com.kairos.persistence.repository.organization.PaymentSettingRepository;
import com.kairos.persistence.repository.user.client.ClientGraphRepository;
import com.kairos.response.dto.web.experties.PaidOutFrequencyEnum;
import com.kairos.service.UserBaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.constants.AppConstants.FORWARD_SLASH;

/**
 * Created by neuron on 12/6/17.
 */
@Service
@Transactional
public class OrganizationMetadataService extends UserBaseService {


    @Inject
    OrganizationMetadataRepository organizationMetadataRepository;

    @Inject
    OrganizationGraphRepository organizationGraphRepository;
    @Inject
    private EnvConfig envConfig;
    @Inject
    private ClientGraphRepository clientGraphRepository;
    @Inject
    private
    PaymentSettingRepository paymentSettingRepository;

    private static final Logger logger = LoggerFactory.getLogger(OrganizationMetadataService.class);


    public Map<String, Object> findAllLocalAreaTags(long unitId) {
        Map<String, Object> localAreaTagData = new HashMap<String, Object>();
        List<Object> clientList = new ArrayList<>();
        List<Object> localAreaTagsList = new ArrayList<>();
        List<Map<String, Object>> mapList = organizationGraphRepository.getClientsOfOrganization(unitId, envConfig.getServerHost() + FORWARD_SLASH);
        for (Map<String, Object> map : mapList) {
            clientList.add(map.get("Client"));
        }
        localAreaTagData.put("citizenList", clientList);
        List<Map<String, Object>> tagList = organizationMetadataRepository.findAllByIsDeletedAndUnitId(unitId);
        for (Map<String, Object> map : tagList) {
            localAreaTagsList.add(map.get("tags"));
        }
        localAreaTagData.put("localAreaTags", localAreaTagsList);
        return localAreaTagData;
    }

    public LocalAreaTag createNew(LocalAreaTag localAreaTag, long unitId) {
        logger.info("local area tag is" + localAreaTag.toString());
        Organization organization = organizationGraphRepository.findOne(unitId);


        if (organization != null) {

            List<LocalAreaTag> localAreaTagList = organization.getLocalAreaTags();
            LocalAreaTag areaTag = new LocalAreaTag();
            areaTag.setName(localAreaTag.getName());
            areaTag.setPaths(localAreaTag.getPaths());
            areaTag.setColor(localAreaTag.getColor());
            organizationMetadataRepository.save(areaTag);
            localAreaTagList.add(areaTag);
            organization.setLocalAreaTags(localAreaTagList);
            logger.debug("organization.getLocalAreaTags  " + organization.getLocalAreaTags());
            organizationGraphRepository.save(organization);
            return areaTag;
        } else {
            return null;
        }
    }


    public LocalAreaTag updateTagData(LocalAreaTag localAreaTag, long unitId) {
        LocalAreaTag existingLocalAreaTag = organizationMetadataRepository.findOne(localAreaTag.getId());
        existingLocalAreaTag.setPaths(localAreaTag.getPaths());
        existingLocalAreaTag.setName(localAreaTag.getName());
        existingLocalAreaTag.setColor(localAreaTag.getColor());

        List<ClientHomeAddressQueryResult> clientHomeAddressQueryResults = clientGraphRepository.getClientsAndHomeAddressByUnitId(unitId);
        Set<Long> clientIds = clientHomeAddressQueryResults.stream().map(clientHomeAddressQueryResult -> clientHomeAddressQueryResult.getCitizen().getId()).collect(Collectors.toSet());

        Iterable<Client> clientList = clientGraphRepository.findAllById(clientIds, 1);
        Map<Long, Client> citizenMap = new HashMap<>();
        for (Client citizen : clientList) {
            citizenMap.put(citizen.getId(), citizen);
        }
        List<Client> citizenList = new ArrayList<>(clientHomeAddressQueryResults.size());
        for (ClientHomeAddressQueryResult clientHomeAddressQueryResult : clientHomeAddressQueryResults) {
            if (clientHomeAddressQueryResult != null) {
                boolean isVerified = isCoordinateInsidePolygon(existingLocalAreaTag.getPaths(), clientHomeAddressQueryResult.getHomeAddress().getLatitude(),
                        clientHomeAddressQueryResult.getHomeAddress().getLongitude());
                //Client citizen = clientGraphRepository.findOne(clientHomeAddressQueryResult.getCitizen().getId());
                Client citizen = citizenMap.get(clientHomeAddressQueryResult.getCitizen().getId());
                if (isVerified) {
                    //Client citizen = clientHomeAddressQueryResult.getCitizen();
                    citizen.setLocalAreaTag(existingLocalAreaTag);
                    citizenList.add(citizen);
                } else if (Optional.ofNullable(clientHomeAddressQueryResult.getLocalAreaTagId()).isPresent()) {
                    if (existingLocalAreaTag.getId().longValue() == clientHomeAddressQueryResult.getLocalAreaTagId().longValue()) {
                        citizen.setLocalAreaTag(null);
                        citizenList.add(citizen);
                    }
                }
            }
        }
        clientGraphRepository.saveAll(citizenList);

        return save(existingLocalAreaTag);
    }

    public boolean deleteTagData(Long localAreaTagId) {
        LocalAreaTag localAreaTag = organizationMetadataRepository.findOne(localAreaTagId);
        localAreaTag.setDeleted(true);

        List<Client> citizenList = clientGraphRepository.getClientsByLocalAreaTagId(localAreaTagId);
        for (Client citizen : citizenList) {
            citizen.setLocalAreaTag(null);
        }
        clientGraphRepository.saveAll(citizenList);

        organizationMetadataRepository.save(localAreaTag);
        if (localAreaTag.isDeleted()) {
            return true;
        } else {
            return false;
        }
    }

    /*
This method accepts Latitude and Longitude of Citizen Home address.
It searches whether citizen's address lies within LocalAreaTag coordinates list or not
 */
    boolean isCoordinateInsidePolygon(List<LatLng> coordinatesList, float latitude, float longitude) {
        float x = latitude;
        float y = longitude;
        boolean coordinateInPolygon = false;

        for (int i = 0, j = coordinatesList.size() - 1; i < coordinatesList.size(); j = i++) {

            float xi = coordinatesList.get(i).getLat();
            float yi = coordinatesList.get(i).getLng();

            float xj = coordinatesList.get(j).getLat();
            float yj = coordinatesList.get(j).getLng();

            boolean intersect = ((yi > y) != (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
            if (intersect) {
                coordinateInPolygon = !coordinateInPolygon;
            }
        }
        return coordinateInPolygon;
    }

    public PaymentSettingsDTO getPaymentSettings(Long unitId) {
        PaymentSettings paymentSettings = paymentSettingRepository.getPaymentSettingByUnitId(unitId);
        if (!Optional.ofNullable(paymentSettings).isPresent()) {
            logger.info("Unable to payments settings for unit ,{}", unitId);
            throw new DataNotFoundByIdException("Unable to get  payments settings for unit " + unitId);
        }
        PaymentSettingsDTO paymentSettingsDTO = new PaymentSettingsDTO(paymentSettings.getId(), paymentSettings.getType(), paymentSettings.getDateOfPayment(), paymentSettings.getMonthOfPayment());

        return paymentSettingsDTO;
    }

    public PaymentSettingsDTO createPaymentsSettings(PaymentSettingsDTO paymentSettingsDTO, Long unitId) {
        Optional<Organization> organization = organizationGraphRepository.findById(unitId, 1);
        if (!organization.isPresent()) {
            logger.info("Unable to get unit while getting payments settings for unit ,{}", unitId);
            throw new DataNotFoundByIdException("Unable to get organization by id" + unitId);
        }
        if (Optional.ofNullable(organization.get().getPaymentSettings()).isPresent()) {
            logger.info("payment settings already found, for unit{}", unitId);
            throw new DataNotFoundByIdException("payment settings already found, for unit" + unitId);
        }
        PaymentSettings paymentSettings;
        paymentSettings = paymentSettingsDTO.getType().equals(PaidOutFrequencyEnum.MONTHLY)
                ? new PaymentSettings(PaidOutFrequencyEnum.MONTHLY, paymentSettingsDTO.getDateOfPayment())
                : new PaymentSettings(PaidOutFrequencyEnum.YEARLY, paymentSettingsDTO.getDateOfPayment(), paymentSettingsDTO.getMonthOfPayment());
        organization.get().setPaymentSettings(paymentSettings);
        save(organization.get());
        paymentSettingsDTO.setId(paymentSettings.getId());
        return paymentSettingsDTO;
    }

    public PaymentSettingsDTO updatePaymentsSettings(PaymentSettingsDTO paymentSettingsDTO, Long unitId) {
        PaymentSettings paymentSettings = paymentSettingRepository.getPaymentSettingByUnitId(unitId);
        if (!Optional.ofNullable(paymentSettings).isPresent()) {
            logger.info("Unable to payment while updating payments settings for unit ,{}", unitId);
            throw new DataNotFoundByIdException("Unable to get payment updating payments settings for unit ,{}" + unitId);
        }
        if(paymentSettingsDTO.getType().equals(PaidOutFrequencyEnum.MONTHLY))
        {
            paymentSettings.setDateOfPayment(paymentSettingsDTO.getDateOfPayment());
        }else{
            paymentSettings.setDateOfPayment(paymentSettingsDTO.getDateOfPayment());
            paymentSettings.setMonthOfPayment(paymentSettingsDTO.getMonthOfPayment());
        }
        save(paymentSettings);
        return paymentSettingsDTO;
    }
}
