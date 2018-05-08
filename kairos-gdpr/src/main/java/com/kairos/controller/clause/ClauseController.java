package com.kairos.controller.clause;

import static com.kairos.constant.ApiConstant.API_ASSET_URL;
import static com.kairos.constant.ApiConstant.API_CLAUSES_URL;


import com.kairos.ExceptionHandler.NotExists;
import com.kairos.persistance.model.clause.Clause;
import com.kairos.persistance.model.clause.dto.ClauseDto;
import com.kairos.persistance.model.clause.dto.ClauseGetQueryDto;
import com.kairos.service.clause.ClauseService;
import com.kairos.service.clause.paginated_result_service.PaginatedResultsRetrievedEvent;
import com.kairos.utils.ResponseHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
import java.util.*;

@RestController
@RequestMapping(API_CLAUSES_URL)
@Api(API_CLAUSES_URL)
@CrossOrigin
public class ClauseController {


    @Inject
    private ClauseService clauseService;

    @Inject
    ApplicationEventPublisher eventPublisher;


    @ApiOperation("add new clause")
    @PostMapping("/add_clause")
    public ResponseEntity<Object> createClause(@Validated @RequestBody ClauseDto clauseDto) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, clauseService.createClause(clauseDto));
    }

    @ApiOperation("get clause by organization type")
    @GetMapping("/byorganizationType")
    public ResponseEntity<Object> getClauseByOrganizationType(@RequestParam String organizationType) {
        Map<String, Object> result = new HashMap<>();
        if (!Optional.ofNullable(organizationType).isPresent()) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organizationtype cannot be null or empty");
        } else {
            result = clauseService.getClauseByOrganizationType(organizationType);
            return ResponseHandler.generateResponse(HttpStatus.OK, true, result.get("data"));
        }
    }

    @ApiOperation("get clause by id")
    @GetMapping("/clause/id/{id}")
    public ResponseEntity<Object> getClauseById(@PathVariable Long id) {
        if (id == null && id.toString().equals("")) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id cannot be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, clauseService.getClauseById(id));

    }

    @ApiOperation("get clause by account type")
    @GetMapping("/byAccount")
    public ResponseEntity<Object> getClauseByAccountType(@RequestParam String accountType) {
        if (accountType == null || accountType.equals("")) {
            throw new NullPointerException("AccountType Cannot be Null or Empty");
        } else
            return ResponseHandler.generateResponse(HttpStatus.OK, true, clauseService.getClauseByAccountType(accountType));

    }


    @ApiOperation("get clause by multi select")
    @PostMapping("/clause")
    public ResponseEntity<Object> getClause(@RequestBody ClauseGetQueryDto clauseQueryDto) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, clauseService.getClause(clauseQueryDto));
    }

    @ApiOperation("delete clause by id")
    @DeleteMapping("/delete/id/{id}")
    public ResponseEntity<Object> deleteClause(@PathVariable Long id) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, clauseService.deleteClause(id));
    }

    @ApiOperation("update clause description")
    @PutMapping("/update/clause/id/{clauseId}")
    public ResponseEntity<Object> updateClause(@PathVariable Long clauseId, @RequestParam String description) {
        if (description == null || description.equals("")) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "description cannot be null");
        } else
            return ResponseHandler.generateResponse(HttpStatus.OK, true, clauseService.updateClause(clauseId, description));

    }


    @ApiOperation("get clause by list")
    @PostMapping("/clause/list")
    public ResponseEntity<Object> getClausesByIds(@RequestBody List<BigInteger> clausesids) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, clauseService.getClausesByIds(clausesids));
    }

    @ApiOperation("get All clauses")
    @GetMapping("/getAll")
    public ResponseEntity<Object> getAllClauses() {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, clauseService.getAllClauses());
    }


    @ApiOperation("default clauses page with default size 10 ")
    @GetMapping("/page/clause")
    public Page<Clause> getClausePagination(@RequestParam int page, @RequestParam(defaultValue = "10") int size, UriComponentsBuilder uriComponentsBuilder
            , HttpServletResponse httpServletResponse) {
        Page<Clause> resultPage = clauseService.getClausePagination(page, size);
        if (page > resultPage.getTotalPages()) {
            throw new NotExists("Clauses Not for Page" + page);
        }
        eventPublisher.publishEvent(new PaginatedResultsRetrievedEvent(uriComponentsBuilder, httpServletResponse, Clause.class, page, resultPage.getTotalPages(), size));
        return resultPage;
    }


}