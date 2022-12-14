package com.kairos.persistence.model.user.resources;

import com.kairos.commons.utils.DateUtils;
import com.kairos.persistence.model.common.UserBaseEntity;
import com.kairos.service.exception.ExceptionService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.annotation.NodeEntity;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.kairos.commons.utils.DateUtils.MONGODB_QUERY_DATE_FORMAT;
import static com.kairos.constants.UserMessagesConstants.MESSAGE_DATE_SOMETHINGWRONG;

/**
 * Created by arvind on 6/10/16.
 */

@NodeEntity
@Getter
@Setter
@NoArgsConstructor
public class ResourceUnAvailability extends UserBaseEntity {

    private static final long serialVersionUID = 1073192210088064671L;
    @Inject
    private ExceptionService exceptionService;
    private Long date;
    private Long startTime;
    private Long endTime;
    private boolean fullDay;


    public ResourceUnAvailability(boolean fullDay) {
        this.fullDay = fullDay;
    }

    public ResourceUnAvailability setUnavailability(ResourceUnavailabilityDTO unavailabilityDTO, String unavailabilityDate)  {
        try{
            LocalDateTime startDateIncludeTime = LocalDateTime.ofInstant(DateUtils.convertToOnlyDate(unavailabilityDate,
                    MONGODB_QUERY_DATE_FORMAT).toInstant(), ZoneId.systemDefault());
            this.date = startDateIncludeTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            if(!unavailabilityDTO.isFullDay() && !StringUtils.isBlank(unavailabilityDTO.getStartTime())){
                LocalDateTime timeFrom = LocalDateTime.ofInstant(DateUtils.convertToOnlyDate(unavailabilityDTO.getStartTime(),
                        MONGODB_QUERY_DATE_FORMAT).toInstant(), ZoneId.systemDefault());
                this.startTime = timeFrom.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            }

            if(!unavailabilityDTO.isFullDay() && !StringUtils.isBlank(unavailabilityDTO.getEndTime())){
                LocalDateTime timeTo = LocalDateTime.ofInstant(DateUtils.convertToOnlyDate(unavailabilityDTO.getEndTime(),
                        MONGODB_QUERY_DATE_FORMAT).toInstant(), ZoneId.systemDefault());
                this.endTime = timeTo.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            }
            return this;
        } catch (Exception e){
            exceptionService.dataNotFoundByIdException(MESSAGE_DATE_SOMETHINGWRONG);

        }
        return null;
    }
}
