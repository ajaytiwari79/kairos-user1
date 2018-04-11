package com.kairos.activity.persistence.repository.task_type;
import com.kairos.activity.persistence.model.task_type.MapPointer;
import com.kairos.activity.persistence.repository.custom_repository.MongoBaseRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by oodles on 23/11/16.
 */
@Repository
public interface MapPointerMongoRepository extends MongoBaseRepository<MapPointer,BigInteger> {

    @Override
    List<MapPointer> findAll();
}
