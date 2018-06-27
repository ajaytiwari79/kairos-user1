package com.planner.service.vrpService;

import com.kairos.activity.util.DateUtils;
import com.kairos.activity.util.ObjectMapperUtils;
import com.kairos.planner.vrp.taskplanning.model.*;
import com.kairos.planner.vrp.taskplanning.solution.VrpTaskPlanningSolution;

import com.kairos.response.dto.web.planning.vrpPlanning.EmployeeDTO;
import com.kairos.response.dto.web.planning.vrpPlanning.ShiftDTO;
import com.kairos.response.dto.web.planning.vrpPlanning.VrpTaskPlanningDTO;
import com.planner.domain.tomtomResponse.Matrix;
import com.planner.service.staffService.EmployeeService;
import com.planner.service.taskService.TaskService;
import com.planner.service.tomtomService.TomTomService;
import com.planner.util.wta.FileIOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author pradeep
 * @date - 7/6/18
 */
@Service
public class VRPGeneratorService {

    @Autowired private TaskService taskService;
    @Autowired private EmployeeService employeeService;
    @Autowired private TomTomService tomTomService;

    public VrpTaskPlanningSolution writeToJson(){
        VrpTaskPlanningSolution solution = new VrpTaskPlanningSolution();
        List<Employee> employees = ObjectMapperUtils.copyPropertiesOfListByMapper(employeeService.getEmployees(),Employee.class);

        List<Matrix> matrix=tomTomService.getMatrix();
        LocationsDistanceMatrix locationsDistanceMatrix= new LocationsDistanceMatrix();
        matrix.forEach(m->{
            locationsDistanceMatrix.addLocationDistance(new LocationPair(m.getFirstLatitude(),m.getFirstLongitude(),m.getSecondLattitude(),m.getSecondLongitude()),
                    new LocationPairDifference(m.getResponse().getRouteSummary().getLengthInMeters(),m.getResponse().getRouteSummary().getTravelTimeInSeconds(),m.getResponse().getRouteSummary().getTrafficDelayInSeconds()));
        });
        List<com.kairos.planner.vrp.taskplanning.model.Task> tasks = taskService.getUniqueTask();
        List<Shift> shifts = getShifts(employees);
        solution.setTasks(tasks);
        solution.setShifts(shifts);
        solution.setEmployees(employees);
        solution.setLocationsDistanceMatrix(locationsDistanceMatrix);
        FileIOUtil.writeVrpPlanningXMLToFile(solution,System.getProperty("user.dir")+"/optaplanner-vrp-taskplanning/src/main/resources/problem");
        /*try {
        //String json = ObjectMapperUtils.objectToJsonString(solution);
            PrintWriter out = new PrintWriter(new File(System.getProperty("user.dir")+"/optaplanner-vrp-taskplanning/src/main/resources/problem.json").getAbsolutePath());
            out.write(json);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
        return solution;
    }





    private List<Shift> getShifts(List<Employee> employeeList){
        List<Shift> shifts = new ArrayList<>();
        employeeList.forEach(e->{
            for (int i=4;i<=8;i++) {
                shifts.add(new Shift(e.getId()+i, e, LocalDate.of(2018, 6, i), null, null));
            }
        });
        return shifts;
    }

    public VrpTaskPlanningSolution getVRPProblemSolution(VrpTaskPlanningDTO vrpTaskPlanningDTO){
        VrpTaskPlanningSolution solution = new VrpTaskPlanningSolution();
        List<Matrix> matrix=tomTomService.getMatrix();
        LocationsDistanceMatrix locationsDistanceMatrix= new LocationsDistanceMatrix();
        matrix.forEach(m->{
            locationsDistanceMatrix.addLocationDistance(new LocationPair(m.getFirstLatitude(),m.getFirstLongitude(),m.getSecondLattitude(),m.getSecondLongitude()),
                    new LocationPairDifference(m.getResponse().getRouteSummary().getLengthInMeters(),m.getResponse().getRouteSummary().getTravelTimeInSeconds(),m.getResponse().getRouteSummary().getTrafficDelayInSeconds()));
        });
        List<Task> tasks = new ArrayList<>(vrpTaskPlanningDTO.getTasks().size());
        vrpTaskPlanningDTO.getTasks().forEach(t->{
            tasks.add(new Task(t.getId(),t.getInstallationNumber(),t.getLatitude(),t.getLongitude(),t.getSkills(),t.getDuration(),t.getStreetName(),t.getHouseNo(),t.getBlock(),t.getFloorNo(),t.getPost(),t.getCity(),false));
        });
        Object[] objects= getEmployeesAndShifts(vrpTaskPlanningDTO.getShifts());
        List<Shift> shifts = (List<Shift>)objects[0];
        List<Employee> employees = (List<Employee>)objects[1];
        solution.setSolverConfigId(vrpTaskPlanningDTO.getSolverConfig().getId());
        solution.setTasks(tasks);
        solution.setShifts(shifts);
        solution.setEmployees(employees);
        solution.setLocationsDistanceMatrix(locationsDistanceMatrix);
        return solution;
    }

    private Object[] getEmployeesAndShifts(List<ShiftDTO> shiftDTOS){
        List<Shift> shifts = new ArrayList<>();
        List<EmployeeDTO> employeeDTOSet = new ArrayList<>(shiftDTOS.stream().map(shiftDTO -> shiftDTO.getEmployee()).collect(Collectors.toSet()));
        List<Employee> employees = ObjectMapperUtils.copyPropertiesOfListByMapper(employeeDTOSet,Employee.class);
        Map<String,Employee> employeeMap = employees.stream().collect(Collectors.toMap(k->k.getId(), v->v));
        shiftDTOS.forEach(s->{
            for (int i=4;i<=8;i++) {
                shifts.add(new Shift(s.getId(), employeeMap.get(s.getEmployee().getId()),s.getLocalDate(), DateUtils.dateToLocalDateTime(s.getStartDate()), DateUtils.dateToLocalDateTime(s.getEndDate())));
            }
        });
        return new Object[]{shifts,employeeDTOSet};
    }



}
