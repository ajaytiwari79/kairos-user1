package com.kairos.persistence.repository.open_shift;

import com.kairos.persistence.model.open_shift.OpenShiftNotification;
import com.kairos.persistence.repository.custom_repository.MongoBaseRepository;
import org.springframework.data.mongodb.repository.Query;

import java.math.BigInteger;
import java.util.List;

public interface OpenShiftNotificationMongoRepository extends MongoBaseRepository<OpenShiftNotification, BigInteger>,CustomOpenShiftNotificationMongoRepository {

    @Query("{'deleted':false, 'staffId':{'$in':?0},'response':{$ne: true}}")
    List<OpenShiftNotification> findByOpenShiftIds(List<Long>staffIds);
}
