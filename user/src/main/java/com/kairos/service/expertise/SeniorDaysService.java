package com.kairos.service.expertise;

import com.kairos.commons.custom_exception.DataNotFoundByIdException;
import com.kairos.commons.utils.CommonsExceptionUtil;
import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.dto.user.country.experties.AgeRangeDTO;
import com.kairos.dto.user.country.experties.CareDaysDetails;
import com.kairos.persistence.model.user.expertise.CareDays;
import com.kairos.persistence.model.user.expertise.Expertise;
import com.kairos.persistence.model.user.expertise.SeniorDays;
import com.kairos.persistence.repository.user.expertise.SeniorDaysGraphRepository;
import com.kairos.service.exception.ExceptionService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.ObjectUtils.isCollectionEmpty;
import static com.kairos.commons.utils.ObjectUtils.isNull;
import static com.kairos.constants.UserMessagesConstants.*;

@Service
public class SeniorDaysService {

    @Inject
    private ExpertiseService expertiseService;
    @Inject
    private ExceptionService exceptionService;
    @Inject
    private SeniorDaysGraphRepository seniorDaysGraphRepository;

    public CareDaysDetails saveSeniorDays(Long expertiseId, CareDaysDetails careDaysDetails) {
        Expertise expertise = expertiseService.findById(expertiseId, 1);
        SeniorDays seniorDays = ObjectMapperUtils.copyPropertiesByMapper(careDaysDetails, SeniorDays.class);
        seniorDays.setExpertise(expertise);
        seniorDaysGraphRepository.save(seniorDays);
        careDaysDetails.setId(seniorDays.getId());
        return careDaysDetails;
    }

    public List<CareDaysDetails> getSeniorDays(Long expertiseId) {
        List<SeniorDays> seniorDays = seniorDaysGraphRepository.getSeniorDaysOfExpertise(expertiseId);
        return ObjectMapperUtils.copyCollectionPropertiesByMapper(seniorDays, CareDaysDetails.class);
    }

    public CareDaysDetails updateSeniorDays(CareDaysDetails careDaysDetails) {
        SeniorDays seniorDays = seniorDaysGraphRepository.findById(careDaysDetails.getId()).orElseThrow(() -> new DataNotFoundByIdException(CommonsExceptionUtil.convertMessage(MESSAGE_DATANOTFOUND, "Senior Days", careDaysDetails.getId())));
        if (!seniorDays.getStartDate().equals(careDaysDetails.getStartDate()) || seniorDays.isPublished()) {
            exceptionService.actionNotPermittedException(MESSAGE_FUNCTIONALPAYMENT_UNEDITABLE, "startdate");
        }
        seniorDays.setStartDate(careDaysDetails.getStartDate());
        seniorDays.setEndDate(careDaysDetails.getEndDate());
        seniorDaysGraphRepository.save(seniorDays);
        return careDaysDetails;
    }

    public List<AgeRangeDTO> addMatrixInSeniorDays(Long seniorDayId, List<AgeRangeDTO> ageRangeDTO) {
        SeniorDays seniorDays = seniorDaysGraphRepository.findOne(seniorDayId);
        if (isNull(seniorDays) || seniorDays.isDeleted()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_EXPERTISE_ID_NOTFOUND, seniorDayId);
        }
        validateAgeRange(ageRangeDTO);
        List<CareDays> careDays = ObjectMapperUtils.copyCollectionPropertiesByMapper(ageRangeDTO, CareDays.class);
        seniorDays.setCareDays(careDays);
        seniorDaysGraphRepository.save(seniorDays);
        return ageRangeDTO;
    }

    //Validating age range
    private void validateAgeRange(List<AgeRangeDTO> ageRangeDTO) {
        if(ageRangeDTO.stream().filter(ageRange -> isNull(ageRange.getTo())).collect(Collectors.toList()).size() > 1){
            exceptionService.actionNotPermittedException(MESSAGE_EXPERTISE_AGE_OVERLAP);
        }
        Collections.sort(ageRangeDTO);
        for (int i = 0; i < ageRangeDTO.size(); i++) {
            if (ageRangeDTO.get(i).getTo() != null && (ageRangeDTO.get(i).getFrom() > ageRangeDTO.get(i).getTo()))
                exceptionService.actionNotPermittedException(MESSAGE_EXPERTISE_AGE_RANGEINVALID, ageRangeDTO.get(i).getFrom(), ageRangeDTO.get(i).getTo());
            if (ageRangeDTO.size() > 1 && i < ageRangeDTO.size() - 1 && ageRangeDTO.get(i).getTo() > ageRangeDTO.get(i + 1).getFrom())
                exceptionService.actionNotPermittedException(MESSAGE_EXPERTISE_AGE_OVERLAP);

        }

    }

    public SeniorDays getMatrixOfSeniorDays(Long seniorDayId) {
        return seniorDaysGraphRepository.findOne(seniorDayId);
    }

    public CareDaysDetails updateMatrixInSeniorDays(Long seniorDayId,List<AgeRangeDTO> ageRangeDTOS) {
        SeniorDays seniorDays = seniorDaysGraphRepository.findById(seniorDayId).orElseThrow(()->new DataNotFoundByIdException(CommonsExceptionUtil.convertMessage(MESSAGE_DATANOTFOUND, FUNCTIONALPAYMENT, seniorDayId)));

        if (seniorDays.isOneTimeUpdatedAfterPublish()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_DRAFT_COPY_CREATED);
        }
        validateAgeRange(ageRangeDTOS);
        if (seniorDays.isPublished()) {
            // functional payment is published so we need to create a  new copy and update in same
            SeniorDays seniorDayCopy = ObjectMapperUtils.copyPropertiesByMapper(seniorDays,SeniorDays.class);
            seniorDayCopy.setPublished(false);
            seniorDayCopy.setOneTimeUpdatedAfterPublish(false);
            seniorDays.setOneTimeUpdatedAfterPublish(true);
            seniorDayCopy.setParentSeniorDays(seniorDays);
            seniorDayCopy.setId(null);
            List<CareDays> careDays=ObjectMapperUtils.copyCollectionPropertiesByMapper(ageRangeDTOS,CareDays.class);
            careDays.forEach(careDays1 -> careDays1.setId(null));
            seniorDayCopy.setCareDays(careDays);
            seniorDaysGraphRepository.save(seniorDayCopy);

        } else {
            // update in current copy

            List<CareDays> careDays = ObjectMapperUtils.copyCollectionPropertiesByMapper(ageRangeDTOS, CareDays.class);
            seniorDays.setCareDays(careDays);
            seniorDaysGraphRepository.save(seniorDays);
        }
        return ObjectMapperUtils.copyPropertiesByMapper(seniorDays,CareDaysDetails.class);
    }

    public CareDaysDetails publishSeniorDays(Long seniorDaysId, LocalDate publishedDate) {
        SeniorDays seniorDays = seniorDaysGraphRepository.findById(seniorDaysId).orElseThrow(()->new DataNotFoundByIdException(CommonsExceptionUtil.convertMessage(MESSAGE_DATANOTFOUND, FUNCTIONALPAYMENT, seniorDaysId)));
        if (isCollectionEmpty(seniorDays.getCareDays())) {
            exceptionService.actionNotPermittedException(MESSAGE_SENIOR_DAY_EMPTY);
        }
        if (seniorDays.isPublished()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_FUNCTIONALPAYMENT_ALREADYPUBLISHED);
        }
        if (seniorDays.getStartDate().isAfter(publishedDate) ||
                (seniorDays.getEndDate()!=null && seniorDays.getEndDate().isBefore(publishedDate))) {
            exceptionService.dataNotFoundByIdException(MESSAGE_PUBLISHDATE_NOTLESSTHAN_STARTDATE);
        }
        seniorDays.setPublished(true);
        seniorDays.setStartDate(publishedDate); // changing
        SeniorDays parentSeniorDays = seniorDays.getParentSeniorDays();
        SeniorDays lastSeniorDays = seniorDaysGraphRepository.findLastByExpertiseId(seniorDays.getExpertise().getId());
        boolean onGoingUpdated = false;
        if (lastSeniorDays != null && publishedDate.isAfter(lastSeniorDays.getStartDate()) && lastSeniorDays.getEndDate() == null) {
            lastSeniorDays.setEndDate(publishedDate.minusDays(1));
            seniorDaysGraphRepository.save(lastSeniorDays);
            seniorDaysGraphRepository.detachSeniorDays(seniorDaysId, parentSeniorDays.getId());
            seniorDays.setEndDate(null);
            onGoingUpdated = true;
        }
        if (!onGoingUpdated && Optional.ofNullable(parentSeniorDays).isPresent()) {
            if (parentSeniorDays.getStartDate().isEqual(publishedDate) || parentSeniorDays.getStartDate().isAfter(publishedDate)) {
                exceptionService.dataNotFoundByIdException(MESSAGE_PUBLISHDATE_NOTLESSTHAN_OR_EQUALS_PARENT_STARTDATE);
            }
            seniorDaysGraphRepository.setEndDateToSeniorDays(seniorDaysId, parentSeniorDays.getId(), publishedDate.minusDays(1L).toString());
            parentSeniorDays.setEndDate(publishedDate.minusDays(1L));
            if (lastSeniorDays == null && seniorDays.getEndDate() != null && seniorDays.getEndDate().isBefore(publishedDate)) {
                seniorDays.setEndDate(null);
            }
        }
        seniorDays.setParentSeniorDays(null);
        seniorDaysGraphRepository.save(seniorDays);
        return ObjectMapperUtils.copyPropertiesByMapper(parentSeniorDays,CareDaysDetails.class);

    }

    public boolean deleteSeniorDays(Long seniorDaysId) {
        return seniorDaysGraphRepository.deleteSeniorDays(seniorDaysId);
    }


}
