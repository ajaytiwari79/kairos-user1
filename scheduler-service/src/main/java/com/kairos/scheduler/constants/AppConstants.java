package com.kairos.scheduler.constants;

import com.kairos.enums.scheduler.JobSubType;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kairos.enums.scheduler.JobSubType.*;

public class AppConstants {


    public final static String SCHEDULER_PANEL_INTERVAL_STRING = ". Every {0} minutes during selected hours.";
    public final static String SCHEDULER_PANEL_RUN_ONCE_STRING = ". At {0}.";
    public final static String USER_TO_SCHEDULER_JOB_QUEUE_TOPIC = "UserToSchedulerJobQueue";
    public final static String USER_TO_SCHEDULER_LOGS_QUEUE_TOPIC = "UserToSchedulerLogsQueue";
    public final static String SCHEDULER_TO_USER_QUEUE_TOPIC = "SchedulerToUserQueue";
    public final static String SCHEDULER_TO_ACTIVITY_QUEUE_TOPIC = "SchedulerToActivityQueue";


    public final static Set<JobSubType> userSubTypes = Stream.of(INTEGRATION,EMPLOYMENT_END,QUESTIONAIRE_NIGHTWORKER,SENIORITY_LEVEL).collect(Collectors.toSet());
    public final static Set<JobSubType> activitySubTypes = Stream.of(PRIORITYGROUP_FILTER).collect(Collectors.toSet());


}
