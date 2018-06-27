package com.kairos.activity.persistence.repository.activity_stream;

import com.kairos.activity.persistence.model.activity_stream.Notification;
import com.kairos.activity.persistence.repository.custom_repository.MongoBaseRepository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.math.BigInteger;
import java.util.List;


public interface NotificationMongoRepository extends MongoBaseRepository<Notification,BigInteger> {
    

    List<Notification> findNotificationByOrganizationIdAndUserIdAndSource(Long unitId, Long userId, String source);

}
