package com.kairos.planning.utils;

import com.kairos.planning.domain.*;
import com.kairos.planning.solution.TaskPlanningSolution;
import org.drools.core.base.DefaultKnowledgeHelper;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.kie.api.runtime.ObjectFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirector;
import org.optaplanner.core.impl.score.director.drools.LegacyDroolsScoreDirectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TaskPlanningUtility {
    public static long TASK_ID_SEQUENCE=2000000000l;
    private static Logger log= LoggerFactory.getLogger(TaskPlanningUtility.class);
    public static List<AvailabilityRequest> updatedList = new ArrayList<>();

    @Deprecated
    public static Task createBreak(Employee employee){
        int BREAK1_DURATION=30,BREAK1_SLA=10;

        Task currentTask= employee.getNextTask();
        Task lastTaskOfChain=null;
        while(currentTask!=null){
            if(currentTask.getNextTask()==null){
                lastTaskOfChain=currentTask;
            }
            currentTask=currentTask.getNextTask();
        }
        Task breakTask= new Task();
        breakTask.setInitialStartTime1(lastTaskOfChain.getPlannedEndTime());
        breakTask.setInitialEndTime1(lastTaskOfChain.getPlannedEndTime().plusMinutes(BREAK1_DURATION));
        breakTask.setDuration(BREAK1_DURATION);
        breakTask.setSlaDurationStart1(BREAK1_SLA);
        breakTask.setSlaDurationEnd1(BREAK1_SLA);
        //breakTask.setId(TASK_ID_SEQUENCE++);
        //breakTask.setPreviousTaskOrEmployee(lastTaskOfChain);
        breakTask.setLocation(lastTaskOfChain.getLocation());
       // breakTask.setEmployee(employee);
        breakTask.setTaskType(lastTaskOfChain.getTaskType());
        breakTask.setLocked(true);
        //lastTaskOfChain.setNextTask(breakTask);
        return breakTask;
    }
    /*public static void updateTaskVariables(Task task){
        scoreDirector.beforeVariableChanged(task,"plannedStartTime");
        task.setPlannedStartTime(plannedStartTime);
        scoreDirector.afterVariableChanged(task,"plannedStartTime");
    }*/
    @Deprecated
    public static void check(Object onj,InternalFactHandle factHandle){
        Task task= (Task)factHandle.getObject();
        //factHandle.getDataSource().
        Iterator iter = factHandle.getEntryPoint().getInternalWorkingMemory().getObjectStore().iterateObjects( new ObjectFilter() {
            public boolean accept(Object object) {
                    if ( object instanceof Task ) {
                        return ((Task) object).isLocked();
                    }
                    return false;
            }
        });
       iter.forEachRemaining(tsk->{
    	   tsk=tsk;
       });
       

    }
    @Deprecated
    public static void checkA(Object onj,InternalFactHandle factHandle){
        //factHandle.getDataSource().
        Iterator iter = factHandle.getEntryPoint().getInternalWorkingMemory().getObjectStore().iterateObjects( new ObjectFilter() {
            public boolean accept(Object object) {
                    if ( object instanceof AvailabilityRequest ) {
                        return ((AvailabilityRequest) object).isAutogenerated();
                    }
                    return false;
            }
        });
       iter.forEachRemaining(tsk->{
    	   tsk=tsk;
       });
    }
    public static void checker(Object... objs) throws Exception {
    	int i =0;
    	i++;
		/*
		Interval interval =(Interval) objs[2];
		Employee employee= (Employee)objs[1];*/
        DefaultKnowledgeHelper helper=(DefaultKnowledgeHelper ) objs[0];
		List<AvailabilityRequest> requests= new ArrayList<>();
		/*for (Object object : helper.getWorkingMemory().getObjects()) {
			if ( object instanceof AvailabilityRequest && ((AvailabilityRequest)object).getEmployee().getId().equals(employee.getId())
					&& ((AvailabilityRequest) object).containsInterval(interval)) {
				requests.add((AvailabilityRequest)object);
			}
		}*/
		if(requests.size()>0){
            log.info("size:{}",requests.size());
		    //throw  new Exception();
        }

    }
    public static boolean checkEmployeeCanWorkThisIntervalUsingDroolsMemory(Employee employee, Interval interval,ScoreDirector<TaskPlanningSolution> director){
		LegacyDroolsScoreDirectorFactory<TaskPlanningSolution> scoreDirectorFactory = (LegacyDroolsScoreDirectorFactory<TaskPlanningSolution>)((DroolsScoreDirector<TaskPlanningSolution>)director).getScoreDirectorFactory();
		KnowledgeBaseImpl kbase=(KnowledgeBaseImpl)scoreDirectorFactory.getKieBase();
		StatefulKnowledgeSessionImpl kieSession=null;//((org.drools.core.impl.StatefulKnowledgeSessionImpl)kbase.getWorkingMemories()[0]);
		for (Object object : kieSession.getObjects()) {
			if ( object instanceof AvailabilityRequest && ((AvailabilityRequest)object).getEmployee().getId().equals(employee.getId())
            		&& ((AvailabilityRequest) object).containsInterval(interval)) {
            	return true;
            }
		}
    	return false;
    }
    public static boolean checkEmployeeCanWorkThisInterval(Employee employee, Interval interval,ScoreDirector<TaskPlanningSolution> director){
    	boolean possibleToPlan=false;
    	for (AvailabilityRequest availabilityRequest : director.getWorkingSolution().getAvailabilityList()) {
			if(availabilityRequest.getEmployee().getId().equals(employee.getId()) && 
					availabilityRequest.containsInterval(interval)){
				possibleToPlan=true;
				break;
			}
		}
    	return possibleToPlan;
    }
    
    public static boolean checkEmployeeAttemptedToPlanThisInterval(Employee employee, Interval interval,ScoreDirector<TaskPlanningSolution> director){
		LegacyDroolsScoreDirectorFactory<TaskPlanningSolution> scoreDirectorFactory = (LegacyDroolsScoreDirectorFactory<TaskPlanningSolution>)((DroolsScoreDirector<TaskPlanningSolution>)director).getScoreDirectorFactory();
		KnowledgeBaseImpl kbase=(KnowledgeBaseImpl)scoreDirectorFactory.getKieBase();
		StatefulKnowledgeSessionImpl kieSession=null;//((org.drools.core.impl.StatefulKnowledgeSessionImpl)kbase.getWorkingMemories()[0]);
		for (Object object : kieSession.getObjects()) {
			if ( object instanceof AvailabilityRequest && ((AvailabilityRequest)object).getEmployee().getId().equals(employee.getId())
            		&& ((AvailabilityRequest) object).overlaps(interval)) {
            	return true;
            }
		}
    	return false;
    }
    public static boolean checkEmployeeAttemptedToPlanThisIntervals(Employee employee, List<Interval> intervals,ScoreDirector<TaskPlanningSolution> director){
    	@SuppressWarnings("all")
		boolean canBeAttempted=false;
		for(Interval taskTime:intervals){
			if(checkEmployeeAttemptedToPlanThisInterval(employee,taskTime,director)){
				canBeAttempted=true;
				break;
			}
		}

		return canBeAttempted;
    }
    public static AvailabilityRequest getEmployeeAvailabilityForDay(Employee employee,ScoreDirector<TaskPlanningSolution> director){
    	AvailabilityRequest req=null;
    	for (AvailabilityRequest availabilityRequest : director.getWorkingSolution().getAvailabilityList()) {
			if(availabilityRequest.getEmployee().getId().equals(employee.getId())){
				req=availabilityRequest;
				break;
			}
		}
    	return req;
    }
    public static void updateInsertedAvialabilities(ScoreDirector<TaskPlanningSolution> director){
    	LegacyDroolsScoreDirectorFactory<TaskPlanningSolution> scoreDirectorFactory = (LegacyDroolsScoreDirectorFactory<TaskPlanningSolution>)((DroolsScoreDirector<TaskPlanningSolution>)director).getScoreDirectorFactory();
		KnowledgeBaseImpl kbase=(KnowledgeBaseImpl)scoreDirectorFactory.getKieBase();
		StatefulKnowledgeSessionImpl kieSession=null;//((org.drools.core.impl.StatefulKnowledgeSessionImpl)kbase.getWorkingMemories()[0]);
		StatefulKnowledgeSessionImpl.ObjectStoreWrapper insertedFacts=(StatefulKnowledgeSessionImpl.ObjectStoreWrapper)kieSession.getObjects(new ObjectFilter() {
			@Override
			public boolean accept(Object object) {
				return object instanceof AvailabilityRequest && ((AvailabilityRequest)object).isAutogenerated();
			}
		});
		List<AvailabilityRequest> generatedAvailabilityRequests= new ArrayList<>();
		insertedFacts.forEach(obj->{
			generatedAvailabilityRequests.add((AvailabilityRequest)obj);
		});
		updatedList=generatedAvailabilityRequests;
		//log.info(Thread.currentThread().getName()+"added new availabilities size:"+insertedFacts.size());
    }
    public static DateTime getEarliestStartTimeForFirstTask( Task task,AvailabilityRequest shift) {
		List<DateTime> earlyStartTimes= new ArrayList<>();
        Integer drivingTime=task.getDrivingMinutesFromPreviousTaskOrEmployee();
		task.getTimeWindows().forEach(window->{
			Interval interval = window.getInterval();
			DateTime earliestStart=null;
			if(shift==null){
				earliestStart=interval.getStart();
				earlyStartTimes.add(earliestStart);
				return;
			}else if(shift.overlaps(interval)){
				DateTime overlapStart=shift.getInterval().overlap(interval).getStart();
				 earliestStart=(earliestStart=shift.getInterval().getStart().plusMinutes(drivingTime)).isAfter(overlapStart)?
						 earliestStart:overlapStart;
				 earlyStartTimes.add(earliestStart);
			}
			
		});
		Collections.sort(earlyStartTimes);
		DateTime plannedTime=earlyStartTimes.isEmpty()?task.getTimeWindows().get(0).getStart():earlyStartTimes.get(0);
		//log.info("First task {} planned on {}, planned fine:{}",task.getId(),plannedTime,task.isInPossibleInterval(plannedTime));
		return plannedTime;
	}
    public static DateTime getEarliestStartTimeForChain(Task task) {
		List<DateTime> earlyStartTimes= new ArrayList<>();
		Task prevTask= (Task)task.getPreviousTaskOrEmployee();
		Integer drivingTime=task.getDrivingMinutesFromPreviousTaskOrEmployee();
		DateTime earliestPossibleStart=prevTask.getPlannedEndTime().plusMinutes(drivingTime);
		task.getTimeWindows().forEach(window->{
			Interval interval = window.getInterval();
			DateTime earliestStart=earliestPossibleStart.isAfter(interval.getStart())?earliestPossibleStart:interval.getStart();	
			earlyStartTimes.add(earliestStart);
		});
		Collections.sort(earlyStartTimes);
		DateTime plannedTime=earlyStartTimes.isEmpty()?earliestPossibleStart:earlyStartTimes.get(0);
		//log.info("Chained task {} planned on {}, planned fine:{}",task.getId(),plannedTime,task.isInPossibleInterval(plannedTime));
		return plannedTime;
	}
    public static boolean contains(List<Interval> intervals,Interval interval ){
    	boolean[] contains=new boolean[]{false};
    	intervals.forEach(inter->{
    		if(inter.contains(interval)){
    			contains[0]=true;
    			return;
    		}
    	});
    	return contains[0];
    }
}