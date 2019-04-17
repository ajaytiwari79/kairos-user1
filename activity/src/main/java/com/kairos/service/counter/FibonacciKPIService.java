package com.kairos.service.counter;

import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.dto.activity.counter.enums.ConfLevel;
import com.kairos.dto.activity.counter.fibonacci_kpi.FibonacciKPIDTO;
import com.kairos.persistence.model.counter.FibonacciKPI;
import com.kairos.persistence.repository.counter.FibonacciKPIRepository;
import com.kairos.rest_client.UserIntegrationService;
import com.kairos.service.exception.ExceptionService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.List;

import static com.kairos.commons.utils.ObjectUtils.isNull;

@Service
public class FibonacciKPIService {

    @Inject private FibonacciKPIRepository fibonacciKPIRepository;
    @Inject private UserIntegrationService userIntegrationService;
    @Inject private ExceptionService exceptionService;

    public FibonacciKPIDTO createFibonacciKPI(Long referenceId, FibonacciKPIDTO fibonacciKPIDTO, ConfLevel confLevel) {
        if(confLevel.equals(ConfLevel.COUNTRY) && !userIntegrationService.isCountryExists(referenceId)) {
            exceptionService.dataNotFoundByIdException("message.country.id");
        }
        if(confLevel.equals(ConfLevel.UNIT) && !userIntegrationService.isExistOrganization(referenceId)){
            exceptionService.dataNotFoundByIdException("message.organization.id");
        }
        fibonacciKPIDTO.setReferenceId(referenceId);
        fibonacciKPIDTO.setConfLevel(confLevel);
        FibonacciKPI fibonacciKPI = ObjectMapperUtils.copyPropertiesByMapper(fibonacciKPIDTO, FibonacciKPI.class);
        fibonacciKPIRepository.save(fibonacciKPI);
        fibonacciKPIDTO.setId(fibonacciKPI.getId());
        return fibonacciKPIDTO;
    }

    public FibonacciKPIDTO updateFibonacciKPI(Long referenceId,FibonacciKPIDTO fibonacciKPIDTO,ConfLevel confLevel){
        if(confLevel.equals(ConfLevel.COUNTRY) && !userIntegrationService.isCountryExists(referenceId)) {
            exceptionService.dataNotFoundByIdException("message.country.id");
        }
        if(confLevel.equals(ConfLevel.UNIT) && !userIntegrationService.isExistOrganization(referenceId)){
            exceptionService.dataNotFoundByIdException("message.organization.id");
        }
        fibonacciKPIDTO.setReferenceId(referenceId);
        fibonacciKPIDTO.setConfLevel(confLevel);
        FibonacciKPI fibonacciKPI = fibonacciKPIRepository.findFibonacciKPIById(fibonacciKPIDTO.getId());
        if(isNull(fibonacciKPI)){
            exceptionService.dataNotFoundByIdException("message.dataNotFound","FibonacciKPI",fibonacciKPIDTO.getId());
        }
        fibonacciKPI = ObjectMapperUtils.copyPropertiesByMapper(fibonacciKPIDTO,FibonacciKPI.class);
        fibonacciKPIRepository.save(fibonacciKPI);
        fibonacciKPIDTO.setId(fibonacciKPI.getId());
        return fibonacciKPIDTO;
    }

    public List<FibonacciKPIDTO> getAllFibonacciKPI(Long referenceId,ConfLevel confLevel){
        List<FibonacciKPIDTO> fibonacciKPIDTOS = fibonacciKPIRepository.findAllFibonacciKPIByCountryId(referenceId,confLevel);
        for (FibonacciKPIDTO fibonacciKPIDTO : fibonacciKPIDTOS) {
            fibonacciKPIDTO.setFibonacciKPI(true);
        }
        return fibonacciKPIDTOS;
    }

    public FibonacciKPIDTO getOneFibonacciKPI(BigInteger fibonacciKPIId){
        FibonacciKPI fibonacciKPI = fibonacciKPIRepository.findFibonacciKPIById(fibonacciKPIId);
        FibonacciKPIDTO fibonacciKPIDTO = ObjectMapperUtils.copyPropertiesByMapper(fibonacciKPI,FibonacciKPIDTO.class);
        fibonacciKPIDTO.setFibonacciKPI(true);
        return fibonacciKPIDTO;
    }

    public boolean deleteFibonacciKPI(BigInteger fibonacciKPIId){
        FibonacciKPI fibonacciKPI = fibonacciKPIRepository.findFibonacciKPIById(fibonacciKPIId);
        if(isNull(fibonacciKPI)){
            exceptionService.dataNotFoundByIdException("message.dataNotFound","FibonacciKPI",fibonacciKPIId);
            return false;
        }
        fibonacciKPI.setDeleted(true);
        fibonacciKPIRepository.save(fibonacciKPI);
        return true;
    }

}