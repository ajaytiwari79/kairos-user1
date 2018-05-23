package com.kairos.service.master_data_management.asset_management;


import com.kairos.custome_exception.DataNotExists;
import com.kairos.custome_exception.DataNotFoundByIdException;
import com.kairos.custome_exception.InvalidRequestException;
import com.kairos.persistance.model.master_data_management.asset_management.HostingType;
import com.kairos.persistance.repository.master_data_management.asset_management.HostingTypeMongoRepository;
import com.kairos.service.MongoBaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.*;

@Service
public class HostingTypeService extends MongoBaseService {

    @Inject
    private HostingTypeMongoRepository hostingTypeMongoRepository;


    public Map<String, List<HostingType>> createHostingType(List<HostingType> hostingTypes) {
        Map<String, List<HostingType>> result = new HashMap<>();
        List<HostingType> existing = new ArrayList<>();
        List<HostingType> newHostingTypes = new ArrayList<>();
        if (hostingTypes.size() != 0) {
            for (HostingType hostingType : hostingTypes) {
                if (!StringUtils.isBlank(hostingType.getName())) {
                    HostingType exist = hostingTypeMongoRepository.findByName(hostingType.getName());
                    if (Optional.ofNullable(exist).isPresent()) {
                        existing.add(exist);

                    } else {
                        HostingType newHostingType = new HostingType();
                        newHostingType.setName(hostingType.getName());
                        newHostingTypes.add(save(newHostingType));
                    }
                } else
                    throw new InvalidRequestException("name could not be empty or null");
            }

            result.put("existing", existing);
            result.put("new", newHostingTypes);
            return result;
        } else
            throw new InvalidRequestException("list cannot be empty");


    }

    public List<HostingType> getAllHostingType() {
       return hostingTypeMongoRepository.findAllHostingTypes();
          }


    public HostingType getHostingType(BigInteger id) {

        HostingType exist = hostingTypeMongoRepository.findByIdAndNonDeleted(id);
        if (!Optional.ofNullable(exist).isPresent()) {
            throw new DataNotFoundByIdException("data not exist for id ");
        } else {
            return exist;

        }
    }


    public Boolean deleteHostingType(BigInteger id) {

        HostingType exist = hostingTypeMongoRepository.findByIdAndNonDeleted(id);
        if (!Optional.ofNullable(exist).isPresent()) {
            throw new DataNotFoundByIdException("data not exist for id ");
        } else {
            exist.setDeleted(true);
            save(exist);
            return true;

        }
    }


    public HostingType updateHostingType(BigInteger id, HostingType hostingType) {


        HostingType exist = hostingTypeMongoRepository.findByIdAndNonDeleted(id);
        if (!Optional.ofNullable(exist).isPresent()) {
            throw new DataNotFoundByIdException("data not exist for id ");
        } else {
            exist.setName(hostingType.getName());

            return save(exist);

        }
    }

    public HostingType getHostingTypeByName(String name) {


        if (!StringUtils.isBlank(name)) {
            HostingType exist = hostingTypeMongoRepository.findByName(name);
            if (!Optional.ofNullable(exist).isPresent()) {
                throw new DataNotExists("data not exist for name " + name);
            }
            return exist;
        } else
            throw new InvalidRequestException("request param cannot be empty  or null");

    }


}

    
    
    

