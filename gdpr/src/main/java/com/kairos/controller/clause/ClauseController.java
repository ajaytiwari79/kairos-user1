package com.kairos.controller.clause;

import static com.kairos.constants.ApiConstant.API_CLAUSES_URL;


import com.kairos.custom_exception.DataNotExists;
import com.kairos.dto.master_data.ClauseDTO;
import com.kairos.persistance.model.clause.Clause;
import com.kairos.service.clause.ClauseService;
import com.kairos.service.clause.paginated_result_service.PaginatedResultsRetrievedEvent;
import com.kairos.utils.ResponseHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
import java.util.*;
/*
 *
 *  created by bobby 04/5/2018
 * */


@RestController
@RequestMapping(API_CLAUSES_URL)
@Api(API_CLAUSES_URL)
public class ClauseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClauseController.class);

    @Inject
    private ClauseService clauseService;

    @Inject
    ApplicationEventPublisher eventPublisher;


    @ApiOperation("add new clause")
    @PostMapping("/add")
    public ResponseEntity<Object> createClause(@PathVariable Long countryId, @PathVariable Long organizationId, @Validated @RequestBody ClauseDTO clauseDto) throws RepositoryException {
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can't be null");
        } else if (organizationId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");

        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, clauseService.createClause(countryId, organizationId, clauseDto));
    }

    @ApiOperation("get clause by id")
    @GetMapping("/{id}")
    public ResponseEntity<Object> getClause(@PathVariable Long countryId, @PathVariable Long organizationId, @PathVariable ObjectId id) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id cannot be null");
        }
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can't be null");
        }
        if (organizationId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, clauseService.getClause(countryId, organizationId, id));


    }


    @ApiOperation("delete clause by id")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteClause(@PathVariable Long countryId, @PathVariable Long organizationId, @PathVariable ObjectId id) {

        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id cannot be null");
        }
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can't be null");
        }
        if (organizationId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, clauseService.deleteClause(countryId,organizationId,id));

    }

    @ApiOperation("update clause description")
    @PutMapping("/update/{clauseId}")
    public ResponseEntity<Object> updateClause(@PathVariable Long countryId, @PathVariable Long organizationId, @PathVariable ObjectId clauseId, @Validated @RequestBody ClauseDTO clauseDto) throws RepositoryException {

        if (clauseId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "clauseId cannot be null or empty");
        }
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can't be null");
        }
        if (organizationId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, clauseService.updateClause(countryId, organizationId, clauseId, clauseDto));

    }


    @ApiOperation("get clause by list")
    @PostMapping("/clauses")
    public ResponseEntity<Object> getClauseList(@PathVariable Long countryId, @PathVariable Long organizationId, @RequestBody Set<ObjectId> clausesids) {
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can't be null");
        } else if (organizationId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");

        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, clauseService.getClauseList(countryId, organizationId, clausesids));
    }

    @ApiOperation("get All clauses")
    @GetMapping("/all")
    public ResponseEntity<Object> getAllClauses(@PathVariable Long countryId, @PathVariable Long organizationId) {
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can't be null");
        } else if (organizationId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, clauseService.getAllClauses(countryId, organizationId));
    }


 /*   @ApiOperation("get specific version of clause")
    @GetMapping("/{id}/version")
    public ResponseEntity<Object> getClauseVersion(@PathVariable BigInteger id, @RequestParam String version) throws RepositoryException {
        if (id != null) {
            return ResponseHandler.generateResponse(HttpStatus.OK, true, clauseService.getClauseVersion(id, version));
        }
        return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "clause Id cannot be null or empty");
    }
*/
    @ApiOperation("all version")
    @GetMapping("/{id}/versions")
    public ResponseEntity<Object> getAllClauseVersion(@PathVariable BigInteger id) throws RepositoryException {
        if (id != null) {
            return ResponseHandler.generateResponse(HttpStatus.OK, true, clauseService.getAllClauseVersion(id));
        }
        return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "clause Id cannot be null or empty");
    }
    @ApiOperation("get specific version of clause")
    @GetMapping("/{id}/version")
    public ResponseEntity<Object> getClauseVersion(@PathVariable String id) throws RepositoryException {
        if (id != null) {
            return ResponseHandler.generateResponse(HttpStatus.OK, true, clauseService.getClauseVersions(id));
        }
        return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "clause Id cannot be null or empty");
    }


    @ApiOperation("default clauses page with default size 10 ")
    @GetMapping("/page/clause")
    public Page<Clause> getClausePagination(@RequestParam int page, @RequestParam(defaultValue = "10") int size, UriComponentsBuilder uriComponentsBuilder
            , HttpServletResponse httpServletResponse) {
        Page<Clause> resultPage = clauseService.getClausePagination(page, size);
        if (page > resultPage.getTotalPages()) {
            throw new DataNotExists("Clauses Not for Page" + page);
        }
        eventPublisher.publishEvent(new PaginatedResultsRetrievedEvent(uriComponentsBuilder, httpServletResponse, Clause.class, page, resultPage.getTotalPages(), size));
        return resultPage;
    }


}