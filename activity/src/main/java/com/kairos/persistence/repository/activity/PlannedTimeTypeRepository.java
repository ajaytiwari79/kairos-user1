package com.kairos.persistence.repository.activity;

import com.kairos.dto.activity.presence_type.PresenceTypeDTO;
import com.kairos.persistence.model.activity.PlannedTimeType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

/*
 * @author: Mohit Shakya
 * Usage: Repository class for PlannedTimeTypes
 */
public interface PlannedTimeTypeRepository extends MongoRepository<PlannedTimeType, BigInteger> {
    @Query("{'name':{$regex:?0,$options:'i'}, 'deleted':?1, 'countryId':?2}")
    PlannedTimeType findByNameAndDeletedAndCountryId(String name, boolean deleted, Long countryId);

    @Query("{'countryId':?0, 'deleted':?1}")
    List<PresenceTypeDTO> getAllPresenceTypeByCountryId(Long countryId, boolean deleted);

    @Query("{'countryId':?0, 'name':{$regex:?1,$options:'i'}, 'deleted':?2}")
    List<PlannedTimeType> findByNameAndDeletedAndCountryIdExcludingCurrent(Long countryId, String plannedTimeTypeName, boolean deleted);

    @Query("{'_id':{$in:?0},'deleted':false}")
    List<PlannedTimeType> findAllById(Collection<BigInteger> ids);

}
