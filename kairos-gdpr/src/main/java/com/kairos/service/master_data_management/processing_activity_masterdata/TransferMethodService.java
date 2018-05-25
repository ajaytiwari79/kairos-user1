package com.kairos.service.master_data_management.processing_activity_masterdata;


import com.kairos.custome_exception.DataNotExists;
import com.kairos.custome_exception.DataNotFoundByIdException;
import com.kairos.custome_exception.InvalidRequestException;
import com.kairos.persistance.model.master_data_management.processing_activity_masterdata.TransferMethod;
import com.kairos.persistance.repository.master_data_management.processing_activity_masterdata.TransferMethodMongoRepository;
import com.kairos.service.MongoBaseService;
import com.kairos.utils.userContext.UserContext;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.*;

@Service
public class TransferMethodService extends MongoBaseService {


    @Inject
    private TransferMethodMongoRepository transferMethodDestinationRepository;


    public Map<String, List<TransferMethod>> createTransferMethod(Long countryId,List<TransferMethod> transferMethods) {
        Map<String, List<TransferMethod>> result = new HashMap<>();
        List<TransferMethod> existing = new ArrayList<>();
        List<TransferMethod> newTransferMethods = new ArrayList<>();
        if (transferMethods.size() != 0) {
            for (TransferMethod transferMethod : transferMethods) {
                if (!StringUtils.isBlank(transferMethod.getName())) {
                    TransferMethod exist = transferMethodDestinationRepository.findByName(countryId,transferMethod.getName());
                    if (Optional.ofNullable(exist).isPresent()) {
                        existing.add(exist);

                    } else {
                        TransferMethod newTransferMethod = new TransferMethod();
                        newTransferMethod.setName(transferMethod.getName());
                        newTransferMethod.setCountryId(countryId);
                        newTransferMethods.add(save(newTransferMethod));
                    }
                } else
                    throw new InvalidRequestException("name could not be empty or null");

            }

            result.put("existing", existing);
            result.put("new", newTransferMethods);
            return result;
        } else
            throw new InvalidRequestException("list cannot be empty");


    }

    public List<TransferMethod> getAllTransferMethod() {
       return transferMethodDestinationRepository.findAllTransferMethods(UserContext.getCountryId());
    }
    public TransferMethod getTransferMethod(Long countryId,BigInteger id) {

        TransferMethod exist = transferMethodDestinationRepository.findByIdAndNonDeleted(countryId,id);
        if (!Optional.ofNullable(exist).isPresent()) {
            throw new DataNotFoundByIdException("data not exist for id ");
        } else {
            return exist;

        }
    }


    public Boolean deleteTransferMethod(BigInteger id) {

        TransferMethod exist = transferMethodDestinationRepository.findByid(id);
        if (!Optional.ofNullable(exist).isPresent()) {
            throw new DataNotFoundByIdException("data not exist for id ");
        } else {
            exist.setDeleted(true);
            save(exist);
            return true;

        }
    }


    public TransferMethod updateTransferMethod(BigInteger id, TransferMethod transferMethod) {


        TransferMethod exist = transferMethodDestinationRepository.findByid(id);
        if (!Optional.ofNullable(exist).isPresent()) {
            throw new DataNotFoundByIdException("data not exist for id ");
        } else {
            exist.setName(transferMethod.getName());

            return save(exist);

        }
    }


    public TransferMethod getTransferMethodByName(Long countryId,String name) {


        if (!StringUtils.isBlank(name)) {
            TransferMethod exist = transferMethodDestinationRepository.findByName(countryId,name);
            if (!Optional.ofNullable(exist).isPresent()) {
                throw new DataNotExists("data not exist for name " + name);
            }
            return exist;
        } else
            throw new InvalidRequestException("request param cannot be empty  or null");

    }


}

    
    
    

