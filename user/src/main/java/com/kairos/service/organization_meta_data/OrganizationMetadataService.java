package com.kairos.service.organization_meta_data;

import com.kairos.commons.custom_exception.DataNotFoundByIdException;
import com.kairos.config.env.EnvConfig;
import com.kairos.dto.user.organization.PaymentSettingsDTO;
import com.kairos.persistence.model.client.Client;
import com.kairos.persistence.model.client.query_results.ClientHomeAddressQueryResult;
import com.kairos.persistence.model.organization.PaymentSettings;
import com.kairos.persistence.model.organization.PaymentSettingsQueryResult;
import com.kairos.persistence.model.organization.Unit;
import com.kairos.persistence.model.user.region.LatLng;
import com.kairos.persistence.model.user.region.LocalAreaTag;
import com.kairos.persistence.repository.organization.OrganizationMetadataRepository;
import com.kairos.persistence.repository.organization.PaymentSettingRepository;
import com.kairos.persistence.repository.organization.UnitGraphRepository;
import com.kairos.persistence.repository.user.client.ClientGraphRepository;
import com.kairos.service.exception.ExceptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.constants.AppConstants.FORWARD_SLASH;
import static com.kairos.constants.UserMessagesConstants.MESSAGE_UNIT_PAYMENTSETTING_UPDATE_UNABLE;

/**
 * Created by neuron on 12/6/17.
 */
@Service
@Transactional
public class OrganizationMetadataService {


    @Inject
    OrganizationMetadataRepository organizationMetadataRepository;

    @Inject
    UnitGraphRepository unitGraphRepository;
    @Inject
    private EnvConfig envConfig;
    @Inject
    private ClientGraphRepository clientGraphRepository;
    @Inject
    private
    PaymentSettingRepository paymentSettingRepository;
    @Inject
    private ExceptionService exceptionService;
    private static final Logger logger = LoggerFactory.getLogger(OrganizationMetadataService.class);


    public Map<String, Object> findAllLocalAreaTags(long unitId) {
        Map<String, Object> localAreaTagData = new HashMap<String, Object>();
        List<Object> clientList = new ArrayList<>();
        List<Object> localAreaTagsList = new ArrayList<>();
        List<Map<String, Object>> mapList = unitGraphRepository.getClientsOfOrganization(unitId, envConfig.getServerHost() + FORWARD_SLASH);
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
        Unit unit = unitGraphRepository.findOne(unitId);


        if (unit != null) {

            List<LocalAreaTag> localAreaTagList = unit.getLocalAreaTags();
            LocalAreaTag areaTag = new LocalAreaTag();
            areaTag.setName(localAreaTag.getName());
            areaTag.setPaths(localAreaTag.getPaths());
            areaTag.setColor(localAreaTag.getColor());
            organizationMetadataRepository.save(areaTag);
            localAreaTagList.add(areaTag);
            unit.setLocalAreaTags(localAreaTagList);
            logger.debug("organization.getLocalAreaTags  " + unit.getLocalAreaTags());
            unitGraphRepository.save(unit);
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
        setCitizenInfo(existingLocalAreaTag, clientHomeAddressQueryResults, citizenMap, citizenList);
        clientGraphRepository.saveAll(citizenList);
        return organizationMetadataRepository.save(existingLocalAreaTag);
    }

    private void setCitizenInfo(LocalAreaTag existingLocalAreaTag, List<ClientHomeAddressQueryResult> clientHomeAddressQueryResults, Map<Long, Client> citizenMap, List<Client> citizenList) {
        for (ClientHomeAddressQueryResult clientHomeAddressQueryResult : clientHomeAddressQueryResults) {
            if (clientHomeAddressQueryResult != null) {
                boolean isVerified = isCoordinateInsidePolygon(existingLocalAreaTag.getPaths(), clientHomeAddressQueryResult.getHomeAddress().getLatitude(), clientHomeAddressQueryResult.getHomeAddress().getLongitude());
                Client citizen = citizenMap.get(clientHomeAddressQueryResult.getCitizen().getId());
                if (isVerified) {
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

    public PaymentSettingsQueryResult getPaymentSettings(Long unitId) {
        return paymentSettingRepository.getPaymentSettingByUnitId(unitId);
    }


    private Long savePaymentSettings(PaymentSettingsDTO paymentSettingsDTO, Unit unit) {
        PaymentSettings paymentSettings = updatePaymentSettingsWithDates(new PaymentSettings(), paymentSettingsDTO);
        unit.setPaymentSettings(paymentSettings);
        unitGraphRepository.save(unit);
        return paymentSettings.getId();

    }

    private PaymentSettings updatePaymentSettingsWithDates(PaymentSettings paymentSettings, PaymentSettingsDTO paymentSettingsDTO) {
        paymentSettings.setFornightlyPayDay(paymentSettingsDTO.getFornightlyPayDay());
        paymentSettings.setWeeklyPayDay(paymentSettingsDTO.getWeeklyPayDay());
        //TODO: calling date updation method
        return paymentSettings;
    }

    public PaymentSettingsDTO updatePaymentsSettings(PaymentSettingsDTO paymentSettingsDTO, Long unitId) {
        Optional<Unit> organization = unitGraphRepository.findById(unitId, 1);
        if (!organization.isPresent()) {
            logger.info("Unable to get unit while getting payments settings for unit ,{}", unitId);
            throw new DataNotFoundByIdException("Unable to get organization by id" + unitId);
        }
        PaymentSettings paymentSettings = paymentSettingRepository.getPaymentSettingByUnitId(unitId, paymentSettingsDTO.getId());
        if (!Optional.ofNullable(paymentSettings).isPresent()) {

            logger.info("Unable to payment while updating payments settings for unit ,{}", unitId);
            exceptionService.dataNotFoundByIdException(MESSAGE_UNIT_PAYMENTSETTING_UPDATE_UNABLE, unitId);

        }
        updatePaymentSettingsWithDates(paymentSettings, paymentSettingsDTO);
        paymentSettingRepository.save(paymentSettings);
        return paymentSettingsDTO;
    }
}
