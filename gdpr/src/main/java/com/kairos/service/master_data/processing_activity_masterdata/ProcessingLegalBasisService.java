package com.kairos.service.master_data.processing_activity_masterdata;


import com.kairos.custom_exception.DataNotExists;
import com.kairos.custom_exception.DataNotFoundByIdException;
import com.kairos.custom_exception.DuplicateDataException;
import com.kairos.custom_exception.InvalidRequestException;
import com.kairos.persistance.model.master_data.default_proc_activity_setting.ProcessingLegalBasis;
import com.kairos.persistance.repository.master_data.processing_activity_masterdata.ProcessingLegalBasisMongoRepository;
import com.kairos.service.common.MongoBaseService;
import com.kairos.utils.ComparisonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.inject.Inject;
import java.math.BigInteger;
import java.util.*;
import static com.kairos.constants.AppConstant.EXISTING_DATA_LIST;
import static com.kairos.constants.AppConstant.NEW_DATA_LIST;


@Service
public class ProcessingLegalBasisService extends MongoBaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingLegalBasisService.class);

    @Inject
    private ProcessingLegalBasisMongoRepository legalBasisMongoRepository;

    @Inject
    private ComparisonUtils comparisonUtils;


    /**
     * @description this method create new ProcessingLegalBasis if ProcessingLegalBasis not exist with same name ,
     * and if exist then simply add  ProcessingLegalBasis to existing list and return list ;
     * findByNamesList()  return list of existing ProcessingLegalBasis using collation ,used for case insensitive result
     * @param countryId
     * @param organizationId
     * @param legalBasisList
     * @return return map which contain list of new ProcessingLegalBasis and list of existing ProcessingLegalBasis if ProcessingLegalBasis already exist
     *
     */
    public Map<String, List<ProcessingLegalBasis>> createProcessingLegalBasis(Long countryId,Long organizationId,List<ProcessingLegalBasis> legalBasisList) {

        Map<String, List<ProcessingLegalBasis>> result = new HashMap<>();
        Set<String> legalBasisNames = new HashSet<>();
        if (legalBasisList.size() != 0) {
            for (ProcessingLegalBasis legalBasis : legalBasisList) {
                if (!StringUtils.isBlank(legalBasis.getName())) {
                    legalBasisNames.add(legalBasis.getName());
                } else
                    throw new InvalidRequestException("name could not be empty or null");

            }
            List<ProcessingLegalBasis> existing =  findByNamesList(countryId,organizationId,legalBasisNames,ProcessingLegalBasis.class);
            legalBasisNames = comparisonUtils.getNameListForMetadata(existing, legalBasisNames);

            List<ProcessingLegalBasis> newProcessingLegalBasisList = new ArrayList<>();
            if (legalBasisNames.size() != 0) {
                for (String name : legalBasisNames) {

                    ProcessingLegalBasis newProcessingLegalBasis = new ProcessingLegalBasis();
                    newProcessingLegalBasis.setName(name);
                    newProcessingLegalBasis.setCountryId(countryId);
                    newProcessingLegalBasis.setOrganizationId(organizationId);
                    newProcessingLegalBasisList.add(newProcessingLegalBasis);

                }

                newProcessingLegalBasisList = legalBasisMongoRepository.saveAll(sequenceGenerator(newProcessingLegalBasisList));
            }
            result.put(EXISTING_DATA_LIST, existing);
            result.put(NEW_DATA_LIST, newProcessingLegalBasisList);
            return result;
        } else
            throw new InvalidRequestException("list cannot be empty");


    }


    /**
     *
     * @param countryId
     * @param organizationId
     * @return list of ProcessingLegalBasis
     */
    public List<ProcessingLegalBasis> getAllProcessingLegalBasis(Long countryId,Long organizationId) {
        return legalBasisMongoRepository.findAllProcessingLegalBases(countryId,organizationId);
    }

    /**
     * @throws DataNotFoundByIdException throw exception if ProcessingLegalBasis not found for given id
     * @param countryId
     * @param organizationId
     * @param id id of ProcessingLegalBasis
     * @return ProcessingLegalBasis object fetch by given id
     */
    public ProcessingLegalBasis getProcessingLegalBasis(Long countryId,Long organizationId,BigInteger id) {

        ProcessingLegalBasis exist = legalBasisMongoRepository.findByIdAndNonDeleted(countryId,organizationId,id);
        if (!Optional.ofNullable(exist).isPresent()) {
            throw new DataNotFoundByIdException("data not exist for id ");
        } else {
            return exist;

        }
    }


    public Boolean deleteProcessingLegalBasis(Long countryId,Long organizationId,BigInteger id) {

        ProcessingLegalBasis exist = legalBasisMongoRepository.findByIdAndNonDeleted(countryId,organizationId,id);
        if (!Optional.ofNullable(exist).isPresent()) {
            throw new DataNotFoundByIdException("data not exist for id ");
        } else {
            delete(exist);
            return true;

        }
    }

    /***
     * @throws  DuplicateDataException throw exception if ProcessingLegalBasis data not exist for given id
     * @param countryId
     * @param organizationId
     * @param id id of ProcessingLegalBasis
     * @param legalBasis
     * @return ProcessingLegalBasis updated object
     */
    public ProcessingLegalBasis updateProcessingLegalBasis(Long countryId,Long organizationId,BigInteger id, ProcessingLegalBasis legalBasis) {


        ProcessingLegalBasis exist = legalBasisMongoRepository.findByName(countryId,organizationId,legalBasis.getName());
        if (Optional.ofNullable(exist).isPresent() ) {
            if (id.equals(exist.getId())) {
                return exist;
            }
            throw new DuplicateDataException("data  exist for  "+legalBasis.getName());
        } else {
            exist=legalBasisMongoRepository.findByid(id);
            exist.setName(legalBasis.getName());
            return legalBasisMongoRepository.save(sequenceGenerator(exist));

        }
    }

    /**
     * @throws DataNotExists throw exception if ProcessingLegalBasis not exist for given name
     * @param countryId
     * @param organizationId
     * @param name name of ProcessingLegalBasis
     * @return ProcessingLegalBasis object fetch on basis of  name
     */
    public ProcessingLegalBasis getProcessingLegalBasisByName(Long countryId,Long organizationId, String name) {


        if (!StringUtils.isBlank(name)) {
            ProcessingLegalBasis exist = legalBasisMongoRepository.findByName(countryId,organizationId, name);
            if (!Optional.ofNullable(exist).isPresent()) {
                throw new DataNotExists("data not exist for name " + name);
            }
            return exist;
        } else
            throw new InvalidRequestException("request param cannot be empty  or null");

    }


}

    
    
    

