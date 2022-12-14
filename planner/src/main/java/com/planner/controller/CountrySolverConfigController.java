package com.planner.controller;

import com.kairos.dto.planner.solverconfig.SolverConfigDTO;
import com.planner.commonUtil.ResponseHandler;
import com.planner.service.solverconfiguration.CountrySolverConfigService;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static com.planner.constants.ApiConstants.API_PARENT_ORGANIZATION_COUNTRY_SOLVER_CONFIG_URL;

@RestController
@RequestMapping(value = API_PARENT_ORGANIZATION_COUNTRY_SOLVER_CONFIG_URL)
public class CountrySolverConfigController {

    public static final String SUCCESS = "Success";
    @Inject
    private CountrySolverConfigService countrySolverConfigService;

    @PostMapping
    @ApiOperation("Create Country Solver Config")
    public ResponseEntity<Map<String, Object>> createCountrySolverConfig(@PathVariable Long countryId, @RequestBody @Valid SolverConfigDTO countrySolverConfigDTO) {
        return ResponseHandler.generateResponseWithData(SUCCESS, HttpStatus.OK, countrySolverConfigService.createCountrySolverConfig(countryId, countrySolverConfigDTO));
    }

    @PostMapping(value = "/copy")
    @ApiOperation("Copy Country Solver Config")
    public ResponseEntity<Map<String, Object>> copyCountrySolverConfig(@PathVariable Long countryId, @RequestBody @Valid SolverConfigDTO countrySolverConfigDTO) {
        return ResponseHandler.generateResponseWithData(SUCCESS, HttpStatus.OK, countrySolverConfigService.copyCountrySolverConfig(countryId, countrySolverConfigDTO));
    }

    @GetMapping
    @ApiOperation("GetAll Country Solver Configration By Country Id")
    public ResponseEntity<Map<String, Object>> getAllCountrySolverConfigByCountryId(@PathVariable Long countryId) {
        return ResponseHandler.generateResponseWithData(SUCCESS, HttpStatus.OK, countrySolverConfigService.getAllCountrySolverConfigByCountryId(countryId));
    }

    @PutMapping(value = "/{solverConfigId}")
    @ApiOperation("Update Country Solver Configration")
    public ResponseEntity<Map<String, Object>> updateCountrySolverConfig(@PathVariable Long countryId, @RequestBody @Valid SolverConfigDTO countrySolverConfigDTO) {
        return ResponseHandler.generateResponseWithData(SUCCESS, HttpStatus.OK, countrySolverConfigService.updateCountrySolverConfig(countryId, countrySolverConfigDTO));
    }

    @DeleteMapping(value = "/{solverConfigId}")
    @ApiOperation("Delete Country Solver Configration")
    public ResponseEntity<Map<String, Object>> deleteCountrySolverConfig(@PathVariable Long countryId, @PathVariable BigInteger solverConfigId) {
        return ResponseHandler.generateResponseWithData(SUCCESS, HttpStatus.OK, countrySolverConfigService.deleteCountrySolverConfig(solverConfigId));
    }

    /**
     * Requires this data so that can send id of Sub OrganizationServicesId
     *
     * @param countryId
     * @return
     */
    @GetMapping("/default_data")
    @ApiOperation("Get Default Data")
    public ResponseEntity<Map<String, Object>> getDefaultData(@PathVariable Long countryId) {
        return ResponseHandler.generateResponseWithData(SUCCESS, HttpStatus.OK, countrySolverConfigService.getDefaultData(countryId));
    }

    @GetMapping("/map_organization_sub_service_to_solver_configuration")
    @ApiOperation("Get organization sub service")
    public ResponseEntity<Map<String, Object>> getOrganizationSubServiceId(@RequestBody List<Long> organizationSubServiceIds, @RequestParam BigInteger solverConfigId) {
        countrySolverConfigService.mapSolverConfigToOrganization(solverConfigId, organizationSubServiceIds);
        return ResponseHandler.generateResponseWithData(SUCCESS, HttpStatus.OK, null);
    }
}
