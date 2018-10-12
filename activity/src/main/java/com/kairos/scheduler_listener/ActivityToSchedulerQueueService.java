package com.kairos.scheduler_listener;

import com.kairos.commons.utils.DateUtils;
import com.kairos.dto.activity.activity.activity_tabs.communication_tab.ActivityReminderSettings;
import com.kairos.dto.scheduler.queue.KairosScheduleJobDTO;
import com.kairos.enums.IntegrationOperation;
import com.kairos.enums.scheduler.JobSubType;
import com.kairos.enums.scheduler.JobType;
import com.kairos.scheduler_listener.producer.KafkaProducer;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityToSchedulerQueueService {

    @Inject
    private KafkaProducer kafkaProducer;

    public void pushToJobQueueForShiftReminder(Long unitId, BigInteger shiftId, LocalDateTime triggerDateTime) {
        KairosScheduleJobDTO
        scheduledJob = new KairosScheduleJobDTO(unitId, JobType.FUNCTIONAL, JobSubType.SHIFT_REMINDER, shiftId, IntegrationOperation.CREATE, DateUtils.getMillisFromLocalDateTime(triggerDateTime), true);
        kafkaProducer.pushToJobQueue(scheduledJob);
    }
}