package com.kairos.service.period;


import com.kairos.activity.period.PeriodDTO;
import com.kairos.activity.period.FlippingDateDTO;
import com.kairos.activity.period.PeriodPhaseDTO;
import com.kairos.activity.period.PlanningPeriodDTO;
import com.kairos.activity.phase.PhaseDTO;
import com.kairos.constants.AppConstants;
import com.kairos.enums.DurationType;
import com.kairos.persistence.model.period.PeriodPhaseFlippingDate;
import com.kairos.persistence.model.period.PlanningPeriod;
import com.kairos.persistence.model.phase.Phase;
import com.kairos.persistence.repository.period.PlanningPeriodMongoRepository;
import com.kairos.persistence.repository.phase.PhaseMongoRepository;
import com.kairos.rest_client.OrganizationRestClient;
import com.kairos.service.MongoBaseService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.phase.PhaseService;
import com.kairos.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * Created by prerna on 6/4/18.
 */
@Service
@Transactional
public class PlanningPeriodService extends MongoBaseService {
    private static final Logger logger = LoggerFactory.getLogger(PlanningPeriodService.class);

    @Inject
    PhaseService phaseService;

    @Inject
    PlanningPeriodMongoRepository planningPeriodMongoRepository;

    @Inject
    PhaseMongoRepository phaseMongoRepository;

    @Inject
    OrganizationRestClient organizationRestClient;
    @Inject
    ExceptionService exceptionService;

    // To get list of phases with duration in days
    public List<PhaseDTO> getPhasesWithDurationInDays(Long unitId) {
        List<PhaseDTO> phases = phaseService.getApplicablePlanningPhasesByOrganizationId(unitId, Sort.Direction.DESC);
        phases.forEach(phase -> {
            if(DurationType.DAYS.equals(phase.getDurationType())){
                phase.setDurationInDays(phase.getDuration());
                phase.setDurationType(DurationType.DAYS);
            }else if(DurationType.WEEKS.equals(phase.getDurationType())){
                phase.setDurationInDays(phase.getDuration() * 7);
                phase.setDurationType(DurationType.DAYS);
            }else{
                phase.setDurationInDays(phase.getDuration());
                phase.setDurationType(DurationType.HOURS);
            }
        });
        return phases;
    }

    // Prepare map for phases with id as key and sequence as value
    public Map<BigInteger, Integer> getMapOfPhasesIdAndSequence(List<PhaseDTO> phases) {
        Map<BigInteger, Integer> phaseIdAndSequenceMap = new HashMap<>();
        for (PhaseDTO phase : phases) {
            phaseIdAndSequenceMap.put(phase.getId(), phase.getSequence());
        }
        return phaseIdAndSequenceMap;
    }

    // To fetch list of planning periods
    public List<PlanningPeriodDTO> getPlanningPeriods(Long unitId, LocalDate startDate, LocalDate endDate) {
        List<PhaseDTO> phases = phaseService.getPlanningPhasesByUnit(unitId);

        // Prepare map for phases with id as key and sequence as value
        Map<BigInteger, Integer> phaseIdAndSequenceMap = getMapOfPhasesIdAndSequence(phases);

        // Fetch planning periods
        List<PlanningPeriodDTO> planningPeriods = null;
        if (Optional.ofNullable(startDate).isPresent() && Optional.ofNullable(endDate).isPresent()) {
            planningPeriods = planningPeriodMongoRepository.findPeriodsOfUnitByStartAndEndDate(unitId, startDate, endDate);
        } else {
            planningPeriods = planningPeriodMongoRepository.findAllPeriodsOfUnit(unitId);
        }

        for (PlanningPeriodDTO planningPeriod : planningPeriods) {

            // Set duration of period
            planningPeriod.setPeriodDuration(DateUtils.getDurationOfTwoLocalDates(planningPeriod.getStartDate(), planningPeriod.getEndDate().plusDays(1)));

            // Set flipping dates
            FlippingDateDTO flippingDateDTO=null;
            for (PeriodPhaseDTO flippingDateTime : planningPeriod.getPhaseFlippingDate()) {
                int phaseSequence = phaseIdAndSequenceMap.get(flippingDateTime.getPhaseId());
                switch (phaseSequence) {
                    case 4: {
                        flippingDateDTO= setFlippingDateAndTime(flippingDateDTO,flippingDateTime);
                        planningPeriod.setConstructionToDraftDate(flippingDateDTO);
                        break;
                    }
                    case 3: {
                        flippingDateDTO= setFlippingDateAndTime(flippingDateDTO,flippingDateTime);
                        planningPeriod.setPuzzleToConstructionDate(flippingDateDTO);
                        break;
                    }
                    case 2: {
                        flippingDateDTO= setFlippingDateAndTime(flippingDateDTO,flippingDateTime);
                        planningPeriod.setRequestToPuzzleDate(flippingDateDTO);
                        break;
                    }
                }
            }
        }
        return planningPeriods;
    }

        public  FlippingDateDTO setFlippingDateAndTime(FlippingDateDTO flippingDateDTO, PeriodPhaseDTO flippingDateTime){
                return flippingDateDTO = (Optional.ofNullable(flippingDateTime.getFlippingDate()).isPresent())?new FlippingDateDTO(flippingDateTime.getFlippingDate(), flippingDateTime.getFlippingTime().getHour(), flippingDateTime.getFlippingTime().getMinute()):null;
        }


    /// API END Point
    public List<PlanningPeriodDTO> migratePlanningPeriods(Long unitId, PlanningPeriodDTO planningPeriodDTO) {
        List<PlanningPeriod> requestPlanningPeriods = planningPeriodMongoRepository.findAllPeriodsOfUnitByRequestPhaseId(unitId, AppConstants.REQUEST_PHASE_NAME);
        if (requestPlanningPeriods.isEmpty()) {
            exceptionService.actionNotPermittedException("message.period.request.phase.notfound");
        }
        List<PhaseDTO> phases = getPhasesWithDurationInDays(unitId);
        if (!Optional.ofNullable(phases).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.organization.phases", unitId);
        }
        LocalDate startDate = requestPlanningPeriods.get(0).getStartDate();
        LocalDate endDate = requestPlanningPeriods.get(requestPlanningPeriods.size() - 1).getEndDate();
        if (requestPlanningPeriods.size() > 0) {
            createMigratedPlanningPeriodForTimeDuration(startDate, endDate, unitId, planningPeriodDTO, phases);
            for (PlanningPeriod planningPeriod : requestPlanningPeriods) {
                planningPeriod.setActive(false);
            }
            save(requestPlanningPeriods);
        }
        return getPlanningPeriods(unitId, null, null);
    }

    public void createMigratedPlanningPeriodForTimeDuration(LocalDate oldStartDate, LocalDate oldEndDate, Long unitId, PlanningPeriodDTO planningPeriodDTO, List<PhaseDTO> phases) {
        List<PlanningPeriod> planningPeriods = new ArrayList<>();
        List<LocalDate> startDateList = getListOfStartDateInWeekOrMonths(oldStartDate, oldEndDate, planningPeriodDTO);
        Set<LocalDate> existingPlanningPeriods = new HashSet<>();
        for (LocalDate startDate : startDateList) {
            boolean alreadyExist = false;
            LocalDate endDate = getNextValidDateForPlanningPeriod(startDate, planningPeriodDTO);
            alreadyExist = existingPlanningPeriods.stream().filter(endDateValue -> startDate.isBefore(endDateValue)).findAny().isPresent();
            if (!alreadyExist) {
                if (endDate.isAfter(oldEndDate) || endDate.isEqual(oldEndDate)) {
                    endDate = oldEndDate;
                }
                existingPlanningPeriods.add(endDate);
                planningPeriods.add(createPlanningPeriodOnMigration(startDate, endDate, unitId, planningPeriodDTO, phases));
            }
        }
        if (planningPeriods.isEmpty()) {
            exceptionService.actionNotPermittedException("message.period.request.phase.notfound");
        }
        save(planningPeriods);
    }

    public PlanningPeriod createPlanningPeriodOnMigration(LocalDate startDate, LocalDate endDate, Long unitId, PlanningPeriodDTO planningPeriodDTO, List<PhaseDTO> applicablePhases) {
        String name = DateUtils.formatLocalDate(startDate, AppConstants.DATE_FORMET_STRING) + "  " + DateUtils.formatLocalDate(endDate, AppConstants.DATE_FORMET_STRING);
        PlanningPeriod planningPeriod = new PlanningPeriod(name, startDate, endDate, unitId);
        planningPeriod = setPhaseFlippingDatesForPlanningPeriod(startDate, applicablePhases, planningPeriod);
        return planningPeriod;
    }

    public PlanningPeriod setPhaseFlippingDatesForPlanningPeriod(LocalDate startDate, List<PhaseDTO> applicablePhases, PlanningPeriod planningPeriod) {

        BigInteger currentPhaseId = null;
        BigInteger nextPhaseId = null;
        List<PeriodPhaseFlippingDate> tempPhaseFlippingDate = new ArrayList<>();
        if (Optional.ofNullable(applicablePhases).isPresent()) {

            LocalDateTime tempFlippingDate = startDate.atStartOfDay();
            boolean scopeToFlipNextPhase = true;
            BigInteger previousPhaseId = null;
            int index = 0;


            for (PhaseDTO phase : applicablePhases) {
                // Check if duration of period is enough to assign next flipping
                if(DurationType.DAYS.equals(phase.getDurationType())) {
                    tempFlippingDate = DateUtils.addDurationInLocalDateTime(LocalDateTime.of(tempFlippingDate.toLocalDate(),phase.getFlippingDefaultTime()), -phase.getDurationInDays(), DurationType.DAYS, 1);
                }else{
                    tempFlippingDate = DateUtils.addDurationInLocalDateTime(tempFlippingDate, -phase.getDurationInDays(), DurationType.HOURS, 1);
                }
                // DateUtils.getDate().compareTo(tempFlippingDate) >= 0
                if (applicablePhases.size() == index + 1 || (scopeToFlipNextPhase && DateUtils.getLocalDateFromDate(DateUtils.getDate()).isAfter(tempFlippingDate.toLocalDate()))) {
                    if (scopeToFlipNextPhase) {
                        currentPhaseId = phase.getId();
                        nextPhaseId = previousPhaseId;
                    }
                    scopeToFlipNextPhase = false;
                }
                previousPhaseId = phase.getId();
                // Calculate flipping date by duration
                PeriodPhaseFlippingDate periodPhaseFlippingDate = new PeriodPhaseFlippingDate(phase.getId(), scopeToFlipNextPhase ? tempFlippingDate.toLocalDate() : null,scopeToFlipNextPhase?tempFlippingDate.toLocalTime():null);
                tempPhaseFlippingDate.add(periodPhaseFlippingDate);
                index += 1;
            }
        }
        planningPeriod.setCurrentPhaseId(currentPhaseId);
        planningPeriod.setNextPhaseId(nextPhaseId);
        planningPeriod.setPhaseFlippingDate(tempPhaseFlippingDate);
        return planningPeriod;
    }

    // To create Planning Period object and to save the list
    public void createPlanningPeriod(Long unitId, LocalDate startDate, List<PlanningPeriod> planningPeriods, List<PhaseDTO> applicablePhases, PlanningPeriodDTO planningPeriodDTO, int recurringNumber) {
        LocalDate endDate = getNextValidDateForPlanningPeriod(startDate, planningPeriodDTO);
        // Set name of period dynamically
        String name = DateUtils.formatLocalDate(startDate, AppConstants.DATE_FORMET_STRING) + "  " + DateUtils.formatLocalDate(endDate, AppConstants.DATE_FORMET_STRING);
        PlanningPeriod planningPeriod = new PlanningPeriod(name, startDate,endDate, unitId);
        planningPeriod = setPhaseFlippingDatesForPlanningPeriod(startDate, applicablePhases, planningPeriod);
        // Add planning period object in list
        planningPeriods.add(planningPeriod);
        if (recurringNumber > 1) {
            if (planningPeriodDTO.getDurationType().equals(DurationType.MONTHS)) {
                startDate = startDate.withDayOfMonth(1);
            }else{
                startDate = startDate.minusDays(startDate.getDayOfWeek().getValue() - 1);
            }
            createPlanningPeriod(unitId,
                    DateUtils.addDurationInLocalDate(startDate, planningPeriodDTO.getDuration(), planningPeriodDTO.getDurationType(), 1),
                    planningPeriods, applicablePhases, planningPeriodDTO, --recurringNumber);
        }
    }

    public List<LocalDate> getListOfStartDateInWeekOrMonths(LocalDate startDate, LocalDate endDate, PlanningPeriodDTO planningPeriodDTO) {
        List<LocalDate> startDateList = new ArrayList<>();
        if (planningPeriodDTO.getDurationType().equals(DurationType.WEEKS)) {
            if (!startDate.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
                startDateList.add(startDate);
            }
            while (startDate.isBefore(endDate) || startDate.isEqual(endDate)) {
                LocalDate startDateOfMonday = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
                startDateList.add((startDateOfMonday.isBefore(endDate) ? startDateOfMonday : startDate));
                startDate = startDate.plusWeeks(planningPeriodDTO.getDuration());
            }
        } else {
            while (startDate.isBefore(endDate) || startDate.isEqual(endDate)) {
                LocalDate startDateOfMonth = ((startDate.getDayOfMonth() != 1) ? startDate : startDate.withDayOfMonth(1));
                startDateList.add(startDateOfMonth);
                startDate = startDate.withDayOfMonth(1).plusMonths(1);
            }
        }
        return startDateList;
    }

    public LocalDate getNextValidDateForPlanningPeriod(LocalDate startDate, PlanningPeriodDTO planningPeriodDTO) {
        LocalDate endDate;
        if (validateStartDateForPeriodCreation(startDate, planningPeriodDTO.getDurationType())) {
            endDate = DateUtils.addDurationInLocalDateExcludingLastDate(startDate, planningPeriodDTO.getDuration(),
                    planningPeriodDTO.getDurationType(), 1);
        } else {
            if (planningPeriodDTO.getDurationType().equals(DurationType.MONTHS)) {
                endDate = startDate.withDayOfMonth(1).plusMonths(1).minusDays(1);
            } else{
                endDate = startDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).minusDays(1);
            }
        }
        return endDate;
    }

    public boolean validateStartDateForPeriodCreation(LocalDate startDate, DurationType durationType) {
        if (durationType.equals(DurationType.WEEKS)) {
            return startDate.getDayOfWeek().equals(DayOfWeek.MONDAY);
        }else {
            return startDate.equals(startDate.withDayOfMonth(1));
        }
    }


    public List<PlanningPeriodDTO> addPlanningPeriods(Long unitId, PlanningPeriodDTO planningPeriodDTO) {
        List<PhaseDTO> phases = getPhasesWithDurationInDays(unitId);
        ;
        if (!Optional.ofNullable(phases).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.organization.phases", unitId);
        }

        // period can't be created in past
        if (DateUtils.getLocalDateFromDate(DateUtils.getDate()).isAfter(planningPeriodDTO.getStartDate())) {
            exceptionService.actionNotPermittedException("error.period.past.date.creation");
        }


        List<PlanningPeriod> planningPeriods = new ArrayList<PlanningPeriod>(planningPeriodDTO.getRecurringNumber());
        PlanningPeriod lastEndDate = planningPeriodMongoRepository.findLastPlaningPeriodEndDate(unitId);
        if (Optional.ofNullable(lastEndDate).isPresent()) {
            planningPeriodDTO.setStartDate(lastEndDate.getEndDate().plusDays(1));
        }else{
            if (!validateStartDateForPeriodCreation(planningPeriodDTO.getStartDate(),planningPeriodDTO.getDurationType())) {
                exceptionService.actionNotPermittedException("error.period.start.date.invalid");
            }
        }
        createPlanningPeriod(unitId, planningPeriodDTO.getStartDate(), planningPeriods, phases, planningPeriodDTO, planningPeriodDTO.getRecurringNumber());
        save(planningPeriods);
        return getPlanningPeriods(unitId,planningPeriodDTO.getStartDate(),(planningPeriodDTO.getEndDate()!=null)?planningPeriodDTO.getEndDate():null);
    }

    public boolean updateFlippingDate(BigInteger periodId, Long unitId, LocalDate date) {
        PlanningPeriod planningPeriod = planningPeriodMongoRepository.findByIdAndUnitId(periodId, unitId);
        boolean updateCurrentAndNextPhases = false;
        BigInteger nextPhaseId = null;
        for (PeriodPhaseFlippingDate phaseFlippingDate : planningPeriod.getPhaseFlippingDate()) {
            if (phaseFlippingDate.getFlippingDate().isEqual(date)) {
                planningPeriod.setCurrentPhaseId(phaseFlippingDate.getPhaseId());
                updateCurrentAndNextPhases = true;
                break;
            }
            nextPhaseId = phaseFlippingDate.getPhaseId();
        }
        if (updateCurrentAndNextPhases) {
            planningPeriod.setNextPhaseId(nextPhaseId);
            save(planningPeriod);
        }
        return true;
    }


    public PlanningPeriod updatePhaseFlippingDateOfPeriod(PlanningPeriod planningPeriod, PlanningPeriodDTO planningPeriodDTO, Long unitId) {
        List<PeriodPhaseFlippingDate> phaseFlippingDateList = planningPeriod.getPhaseFlippingDate();
        List<PhaseDTO> phases = phaseService.getPlanningPhasesByUnit(unitId);
        Map<BigInteger, Integer> phasesMap = getMapOfPhasesIdAndSequence(phases);

        for (PeriodPhaseFlippingDate phaseFlippingDate : phaseFlippingDateList) {
            switch (phasesMap.get(phaseFlippingDate.getPhaseId())) {
                case 4: {
                    phaseFlippingDate.setFlippingDate(planningPeriodDTO.getConstructionToDraftDate().getDate());
                    phaseFlippingDate.setFlippingTime(LocalTime.of(planningPeriodDTO.getConstructionToDraftDate().getHours(),planningPeriodDTO.getConstructionToDraftDate().getMinutes()));
                    break;
                }
                case 3: {
                    phaseFlippingDate.setFlippingDate(planningPeriodDTO.getPuzzleToConstructionDate().getDate());
                    phaseFlippingDate.setFlippingTime(LocalTime.of(planningPeriodDTO.getPuzzleToConstructionDate().getHours(),planningPeriodDTO.getPuzzleToConstructionDate().getMinutes()));
                    break;
                }
                case 2: {
                    phaseFlippingDate.setFlippingDate(planningPeriodDTO.getRequestToPuzzleDate().getDate());
                    phaseFlippingDate.setFlippingTime(LocalTime.of(planningPeriodDTO.getRequestToPuzzleDate().getHours(),planningPeriodDTO.getRequestToPuzzleDate().getMinutes()));
                    break;
                }
            }
        }
        return planningPeriod;
    }


    public List<PlanningPeriodDTO> updatePlanningPeriod(Long unitId, BigInteger periodId, PlanningPeriodDTO planningPeriodDTO) {
        PlanningPeriod planningPeriod = planningPeriodMongoRepository.findOne(periodId);

        if (!Optional.ofNullable(planningPeriod).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.period.organization.notfound", periodId);
        }
        // Check if period is in request phase (Changes for start date and end date can be done in Request Phase
        // We are checking request phase by its name, can be done by sequence, need to ask
        // We know here that sequence of request phase is 0
        if (!phaseMongoRepository.checkPhaseBySequence(planningPeriod.getCurrentPhaseId(), AppConstants.REQUEST_PHASE_SEQUENCE)) {
            exceptionService.actionNotPermittedException("message.period.phase.request.name", planningPeriod.getName());
        }
        //Check if startDate and endDate is different from the original one
        if (!planningPeriodDTO.getStartDate().isEqual(planningPeriod.getStartDate()) &&
                !planningPeriodDTO.getEndDate().isEqual(planningPeriod.getStartDate())) {
            exceptionService.actionNotPermittedException("message.period.startdate.enddate.notupdate");
        }
        //If No change in date

        LocalDateTime puzzleFlippingDateTime=(Optional.ofNullable(planningPeriodDTO.getRequestToPuzzleDate()).isPresent())?DateUtils.getLocalDateTime(planningPeriodDTO.getRequestToPuzzleDate().getDate(),
                planningPeriodDTO.getRequestToPuzzleDate().getHours(),planningPeriodDTO.getRequestToPuzzleDate().getMinutes()):null;
        LocalDateTime constructionFlippingDate=(Optional.ofNullable(planningPeriodDTO.getPuzzleToConstructionDate()).isPresent())?DateUtils.getLocalDateTime(planningPeriodDTO.getPuzzleToConstructionDate().getDate(),
                planningPeriodDTO.getPuzzleToConstructionDate().getHours(),planningPeriodDTO.getPuzzleToConstructionDate().getMinutes()):null;
        LocalDateTime draftFlippingDate=(Optional.ofNullable(planningPeriodDTO.getConstructionToDraftDate()).isPresent())?DateUtils.getLocalDateTime(planningPeriodDTO.getConstructionToDraftDate().getDate(),planningPeriodDTO.getConstructionToDraftDate().getHours(),
                planningPeriodDTO.getConstructionToDraftDate().getMinutes()):null;
        if(Optional.ofNullable(draftFlippingDate).isPresent() && Optional.ofNullable(constructionFlippingDate).isPresent() && draftFlippingDate.isBefore(constructionFlippingDate)
                || Optional.ofNullable(constructionFlippingDate).isPresent() && Optional.ofNullable(puzzleFlippingDateTime).isPresent()&&constructionFlippingDate.isBefore(puzzleFlippingDateTime)){
                exceptionService.actionNotPermittedException("message.period.invalid.flippingdate");
        }
        planningPeriod = updatePhaseFlippingDateOfPeriod(planningPeriod, planningPeriodDTO, unitId);
        planningPeriod.setName(planningPeriodDTO.getName());
        save(planningPeriod);
        return getPlanningPeriods(unitId, planningPeriod.getStartDate(), planningPeriod.getEndDate());
    }

    // To delete planning period

    public boolean deletePlanningPeriod(Long unitId, BigInteger periodId) {

        PlanningPeriod planningPeriod = planningPeriodMongoRepository.findByIdAndUnitId(periodId, unitId);

        if (!Optional.ofNullable(planningPeriod).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.period.unit.id", periodId);
        }

        // Check if it is last period
        PlanningPeriod lastPlanningPeriod = planningPeriodMongoRepository.getLastPlanningPeriod(unitId);

        if (!lastPlanningPeriod.getId().equals(planningPeriod.getId())) {
            exceptionService.actionNotPermittedException("message.period.delete.last");
        }

        // Check if period is in request phase
        // We are checking request phase by its name, can be done by sequence, need to ask
        // TO DO check phase by sequence
        if (!phaseMongoRepository.checkPhaseByName(planningPeriod.getCurrentPhaseId(), "REQUEST")) {
            exceptionService.actionNotPermittedException("message.period.phase.request.name", planningPeriod.getName());
        }

        planningPeriod.setDeleted(true);
        save(planningPeriod);
        return true;
    }

    public PlanningPeriodDTO setPlanningPeriodPhaseToNext(Long unitId, BigInteger periodId) {

        PlanningPeriod planningPeriod = planningPeriodMongoRepository.findByIdAndUnitId(periodId, unitId);

        if (!Optional.ofNullable(planningPeriod).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.period.unit.id", periodId);
        }
        if (!Optional.ofNullable(planningPeriod.getNextPhaseId()).isPresent()) {
            exceptionService.actionNotPermittedException("message.period.phase.last");
        }
        Phase initialNextPhase = phaseMongoRepository.findOne(planningPeriod.getNextPhaseId());
        List<PhaseDTO> toBeNextPhase = phaseMongoRepository.getNextApplicablePhasesOfUnitBySequence(unitId, initialNextPhase.getSequence());
        planningPeriod.setCurrentPhaseId(initialNextPhase.getId());
        planningPeriod.setNextPhaseId(Optional.ofNullable(toBeNextPhase).isPresent() && toBeNextPhase.size() > 0 ? toBeNextPhase.get(0).getId() : null);
        save(planningPeriod);

        return getPlanningPeriods(unitId, planningPeriod.getStartDate(), planningPeriod.getEndDate()).get(0);
    }

    public List<PeriodDTO> getPeriodOfInterval(Long unitId, LocalDate startDate, LocalDate endDate){
        return planningPeriodMongoRepository.findAllPeriodsByStartDateAndLastDate(unitId,startDate,endDate);
    }


/*  not delete this code harish
* public boolean updateFlippingDate(BigInteger periodId, Long unitId, LocalDate date){
        PlanningPeriod planningPeriod = planningPeriodMongoRepository.findByIdAndUnitId(periodId, unitId);
        boolean updateCurrentAndNextPhases = false;
        BigInteger nextPhaseId = null;
        for(PeriodPhaseFlippingDate phaseFlippingDate : planningPeriod.getPhaseFlippingDate()){

            if(planningPeriod.getNextPhaseId().equals(phaseFlippingDate.getPhaseId()) ){
                if(phaseFlippingDate.getFlippingDate().compareTo(date) <= 0){
                    updateCurrentAndNextPhases = true;
                }
                break;
            }
            nextPhaseId = phaseFlippingDate.getPhaseId();
        }
        if(updateCurrentAndNextPhases){
            planningPeriod.setCurrentPhaseId(planningPeriod.getNextPhaseId());
            planningPeriod.setNextPhaseId(nextPhaseId);
            save(planningPeriod);
        }
        return true;
    }
* */



}
