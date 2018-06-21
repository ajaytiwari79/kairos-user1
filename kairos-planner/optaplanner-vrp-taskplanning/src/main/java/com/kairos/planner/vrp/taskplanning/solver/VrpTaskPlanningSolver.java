package com.kairos.planner.vrp.taskplanning.solver;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.kairos.planner.vrp.taskplanning.model.*;
import com.kairos.planner.vrp.taskplanning.solution.VrpTaskPlanningSolution;
import com.kairos.planner.vrp.taskplanning.util.VrpPlanningUtil;
import com.thoughtworks.xstream.XStream;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.persistence.xstream.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScoreXStreamConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class VrpTaskPlanningSolver {
    public static String config = "src/main/resources/config/Kamstrup_Vrp_taskPlanning.solver.xml";
    private static Logger log= LoggerFactory.getLogger(VrpTaskPlanningSolver.class);
    Solver<VrpTaskPlanningSolution> solver;
    SolverFactory<VrpTaskPlanningSolution> solverFactory;


    public VrpTaskPlanningSolver(){
        solverFactory = SolverFactory.createFromXmlFile(new File(config));
        solver = solverFactory.buildSolver();
    }

    public void solve(String problemXML) throws IOException {
        XStream xstream = getxStream();
        VrpTaskPlanningSolution problem=(VrpTaskPlanningSolution) xstream.fromXML(new File(problemXML));
        solve(problem);
    }

    private XStream getxStream() {
        XStream xstream= new XStream();
        xstream.setMode(XStream.ID_REFERENCES);
        xstream.processAnnotations(LocationPair.class);
        xstream.processAnnotations(LocationPairDifference.class);
        xstream.registerConverter(new HardMediumSoftLongScoreXStreamConverter());
        return xstream;
    }

    public void solve(VrpTaskPlanningSolution problem) throws IOException {
        AtomicInteger at=new AtomicInteger(0);
        problem.getTasks().forEach(t->{
            at.addAndGet(t.getDuration());
            t.setLocationsDistanceMatrix(problem.getLocationsDistanceMatrix());
        });
        //TODO ease efficiency for debugging
        //problem.getEmployees().forEach(e->e.setEfficiency(100));
        log.info("Number of tasks:"+problem.getTasks().size());
        VrpTaskPlanningSolution solution=null;
        try {
            solution = solver.solve(problem);

        }catch (Exception e){
            //e.printStackTrace();
            throw  e;
        }
        getxStream().toXML(solution,new FileWriter("src/main/resources/solution.xml"));
        int totalDrivingTime=0;
        StringBuilder sbs= new StringBuilder("Locs data:\n");
        StringBuilder shiftChainInfo= new StringBuilder("Shift chain data:\n");
        for(Shift shift: solution.getShifts()){
            StringBuffer sb= new StringBuffer(shift+":::"+shift.getNumberOfTasks()+">>>"+shift.getTaskChainString()+" ,lat long chain:"+shift.getLocationsString());
            log.info(sb.toString());
            sbs.append(shift.getId()+":"+getLocationList(shift).toString()+"\n");
            shiftChainInfo.append(shift.getId()+":"+getShiftChainInfo(shift)+"\n");
            totalDrivingTime+=shift.getChainDrivingTime();
        }
        log.info(sbs.toString());
        log.info(shiftChainInfo.toString());
        log.info("total driving time:"+totalDrivingTime);

    }

    private String getShiftChainInfo(Shift shift) {
        StringBuilder sb = new StringBuilder();
        int tasks=shift.getNumberOfTasks();
        int uniqueTasks=shift.getTaskList().stream().map(t->t.getLattitude()+"_"+t.getLongitude()).collect(Collectors.toSet()).size();
        if(tasks==uniqueTasks) return " all fine ";
        Map<String,List<Task>> groupedTasks= shift.getTaskList().stream().collect(Collectors.groupingBy(t->t.getLattitude()+"_"+t.getLongitude()));
        groupedTasks.entrySet().forEach(e->{
            if(e.getValue().size()<2){
                return;
            }
            for (int i = 0; i < e.getValue().size(); i++) {
                for (int j = 1; j < e.getValue().size(); j++) {
                    Task t1=e.getValue().get(i);
                    Task t2=e.getValue().get(j);
                    if(!VrpPlanningUtil.isConsecutive(t1,t2)){
                        sb.append("Not consecutive{"+t1+t2+"},");
                    }
                }
            }
        });

        return sb.toString();
    }


    public List<Location> getLocationList(Shift shift){
        List<Location> list= new ArrayList<>();
        Task temp=shift.getNextTask();
        int i=0;
        while(temp!=null){
            Location location = new Location(temp.getLattitude(), temp.getLongitude(), ++i, temp.getIntallationNo());
            if(!list.contains(location)){
                list.add(location);
            }
            temp=temp.getNextTask();
        }
        return list;
    }
}
