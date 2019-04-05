package com.kairos.service.unit_position;

import com.kairos.commons.utils.DateUtils;
import com.kairos.dto.scheduler.queue.KairosSchedulerLogsDTO;
import com.kairos.dto.user.employment.PositionDTO;
import com.kairos.dto.user.employment.UnitPositionIdDTO;
import com.kairos.enums.scheduler.JobSubType;
import com.kairos.enums.scheduler.Result;
import com.kairos.persistence.model.auth.User;
import com.kairos.persistence.model.country.reason_code.ReasonCode;
import com.kairos.persistence.model.organization.Organization;
import com.kairos.persistence.model.staff.position.Position;
import com.kairos.persistence.model.staff.position.PositionQueryResult;
import com.kairos.persistence.model.staff.position.EmploymentUnitPositionDTO;
import com.kairos.persistence.model.user.unit_position.EmploymentLine;
import com.kairos.persistence.model.user.unit_position.UnitPosition;
import com.kairos.persistence.model.user.unit_position.EmploymentLineEmploymentTypeRelationShip;
import com.kairos.persistence.model.user.unit_position.query_result.UnitPositionSeniorityLevelQueryResult;
import com.kairos.persistence.repository.organization.OrganizationGraphRepository;
import com.kairos.persistence.repository.user.auth.UserGraphRepository;
import com.kairos.persistence.repository.user.country.ReasonCodeGraphRepository;
import com.kairos.persistence.repository.user.staff.PositionGraphRepository;
import com.kairos.persistence.repository.user.unit_position.UnitPositionEmploymentTypeRelationShipGraphRepository;
import com.kairos.persistence.repository.user.unit_position.UnitPositionGraphRepository;
import com.kairos.scheduler.queue.producer.KafkaProducer;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.scheduler.UserToSchedulerQueueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CreatedBy vipulpandey on 27/10/18
 **/
@Service
@Transactional
public class UnitPositionJobService {
    @Inject private UnitPositionGraphRepository unitPositionGraphRepository;
    @Inject private KafkaProducer kafkaProducer;
    @Inject private UnitPositionEmploymentTypeRelationShipGraphRepository unitPositionEmploymentTypeRelationShipGraphRepository;
    @Inject private OrganizationGraphRepository organizationGraphRepository;
    @Inject private PositionGraphRepository positionGraphRepository;
    @Inject private ExceptionService exceptionService;
    @Inject private ReasonCodeGraphRepository reasonCodeGraphRepository;
    @Inject private UserToSchedulerQueueService userToSchedulerQueueService;
    @Inject private UserGraphRepository userGraphRepository;

    public void updateSeniorityLevelOnJobTrigger(BigInteger schedulerPanelId, Long unitId) {

        LocalDateTime started = LocalDateTime.now();
        LocalDate todaysDate = DateUtils.getCurrentLocalDate();
        KairosSchedulerLogsDTO schedulerLogsDTO;
        LocalDateTime stopped;
        String log = null;
        Result result = Result.SUCCESS;
        try {
            List<UnitPositionSeniorityLevelQueryResult> unitPositionSeniorityLevelQueryResults = unitPositionGraphRepository.findUnitPositionSeniorityLeveltoUpdate();
            if (!unitPositionSeniorityLevelQueryResults.isEmpty()) {

                Map<Long, UnitPositionSeniorityLevelQueryResult> unitPositionSeniorityLevelQueryResultMap
                        = unitPositionSeniorityLevelQueryResults.stream().collect(Collectors.toMap(UnitPositionSeniorityLevelQueryResult::getUnitPositionId, java.util.function.Function.identity()));

                Set<Long> unitPositionIds = unitPositionSeniorityLevelQueryResultMap.keySet();
                Iterable<UnitPosition> unitPositions = unitPositionGraphRepository.findAllById(unitPositionIds, 2);

                Map<UnitPositionIdDTO, EmploymentLine> newEmploymentLineWithParentId = new HashMap<>();

                for (UnitPosition currentUnitPosition : unitPositions) {
                    Optional<EmploymentLine> employmentLine = currentUnitPosition.getEmploymentLines().stream()
                            .filter(pl -> (todaysDate.isAfter(pl.getStartDate()) || todaysDate.isEqual(pl.getStartDate()) && (pl.getEndDate() == null || pl.getEndDate().isBefore(todaysDate) || pl.getEndDate().isEqual(todaysDate))))
                            .findAny();
                    if (employmentLine.isPresent()) {
                        EmploymentLine newEmploymentLine = new EmploymentLine.EmploymentLineBuilder()
                                .setAvgDailyWorkingHours(employmentLine.get().getAvgDailyWorkingHours())
                                .setTotalWeeklyMinutes(employmentLine.get().getTotalWeeklyMinutes())
                                .setHourlyCost(employmentLine.get().getHourlyCost())
                                .setStartDate(todaysDate.plusDays(1))
                                .setFunctions(employmentLine.get().getFunctions())
                                .setFullTimeWeeklyMinutes(employmentLine.get().getFullTimeWeeklyMinutes())
                                .setWorkingDaysInWeek(employmentLine.get().getWorkingDaysInWeek())
                                .setEndDate(employmentLine.get().getEndDate())
                                .setSeniorityLevel(unitPositionSeniorityLevelQueryResultMap.get(currentUnitPosition.getId()).getSeniorityLevel())
                                .build();
                        employmentLine.get().setEndDate(todaysDate);
                        currentUnitPosition.getEmploymentLines().add(newEmploymentLine);
                        newEmploymentLineWithParentId.put(new UnitPositionIdDTO(currentUnitPosition.getId(), null, employmentLine.get().getId()), newEmploymentLine);
                    }

                }
                List<EmploymentLineEmploymentTypeRelationShip> employmentLineEmploymentTypeRelationShips = new ArrayList<>();

                for (Map.Entry<UnitPositionIdDTO, EmploymentLine> currentMap : newEmploymentLineWithParentId.entrySet()) {
                    UnitPositionSeniorityLevelQueryResult currentObject = unitPositionSeniorityLevelQueryResultMap.get(currentMap.getKey().getOldUnitPositionID());
                    if (currentObject != null) {
                        EmploymentLineEmploymentTypeRelationShip employmentLineEmploymentTypeRelationShip =
                                new EmploymentLineEmploymentTypeRelationShip(currentMap.getValue(), currentObject.getEmploymentType(),
                                        currentObject.getEmploymentLineEmploymentTypeRelationShip().getEmploymentTypeCategory());
                        employmentLineEmploymentTypeRelationShips.add(employmentLineEmploymentTypeRelationShip);
                    }
                }

                unitPositionGraphRepository.saveAll(unitPositions);
                unitPositionEmploymentTypeRelationShipGraphRepository.saveAll(employmentLineEmploymentTypeRelationShips);

            }

        } catch (Exception ex) {

            log = ex.getMessage();
            result = Result.ERROR;
        }

        stopped = LocalDateTime.now();

        schedulerLogsDTO = new KairosSchedulerLogsDTO(result, log, schedulerPanelId, unitId, DateUtils.getMillisFromLocalDateTime(started), DateUtils.getMillisFromLocalDateTime(stopped), JobSubType.SENIORITY_LEVEL);

        kafkaProducer.pushToSchedulerLogsQueue(schedulerLogsDTO);

        // List<CTAWTAResponseDTO> ctaWTAs =  activityIntegrationService.copyWTACTA(unitPositionNewOldIds);

    }
    public EmploymentUnitPositionDTO updateUnitPositionEndDateFromEmployment(Long staffId, Long unitId,PositionDTO positionDTO) {
        Organization unit=organizationGraphRepository.findOne(unitId);
        Long endDateMillis = DateUtils.getIsoDateInLong(positionDTO.getEndDate());
        String unitPositionStartDateMax=unitPositionGraphRepository.getMaxUnitPositionStartDate(staffId);
        if (Optional.ofNullable(unitPositionStartDateMax).isPresent() && DateUtils.getDateFromEpoch(endDateMillis).isBefore(LocalDate.parse(unitPositionStartDateMax))) {
            exceptionService.actionNotPermittedException("message.position_end_date.greater_than.employment_start_date", unitPositionStartDateMax);

        }
        List<UnitPosition> unitPositions = unitPositionGraphRepository.getUnitPositionsFromEmploymentEndDate(staffId, DateUtils.getDateFromEpoch(endDateMillis).toString());
        Optional<ReasonCode> reasonCode = reasonCodeGraphRepository.findById(positionDTO.getReasonCodeId(), 0);
        if (!reasonCode.isPresent()) {
            exceptionService.dataNotFoundByIdException("message.reasonCode.id.notFound", positionDTO.getReasonCodeId());
        }

        for (UnitPosition unitPosition : unitPositions) {
            unitPosition.setEndDate(DateUtils.getLocalDate(endDateMillis));
            if (!Optional.ofNullable(unitPosition.getReasonCode()).isPresent()) {
                unitPosition.setReasonCode(reasonCode.get());
            }
        }

        Position position = positionGraphRepository.findByStaffId(staffId);
//        userToSchedulerQueueService.pushToJobQueueOnEmploymentEnd(endDateMillis, position.getEndDateMillis(), unit.getId(), position.getId(),
//                unit.getTimeZone());

        position.setEndDateMillis(endDateMillis);
        positionGraphRepository.deletePositionReasonCodeRelation(staffId);

        position.setReasonCode(reasonCode.get());
        position.setAccessGroupIdOnPositionEnd(positionDTO.getAccessGroupIdOnPositionEnd());
        unitPositionGraphRepository.saveAll(unitPositions);
        positionGraphRepository.save(position);
        User user = userGraphRepository.getUserByStaffId(staffId);
        PositionQueryResult positionQueryResult = new PositionQueryResult(position.getId(), position.getStartDateMillis(), position.getEndDateMillis(), position.getReasonCode().getId(), position.getAccessGroupIdOnPositionEnd());
        return new EmploymentUnitPositionDTO(positionQueryResult, unitPositionGraphRepository.getAllUnitPositionsByUser(user.getId()));

    }

}
