package com.kairos.persistance.repository.custom_repository;

import com.kairos.custom_exception.DataNotFoundByIdException;
import com.kairos.persistance.model.common.MongoBaseEntity;
import com.kairos.persistance.repository.common.MongoSequenceRepository;
import com.kairos.commons.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Optional;


public class MongoBaseRepositoryImpl<T extends MongoBaseEntity, ID extends Serializable>
        extends SimpleMongoRepository<T, ID> implements MongoBaseRepository<T, ID> {


    private final Logger LOGGER = LoggerFactory.getLogger(MongoBaseRepositoryImpl.class);
    private final MongoSequenceRepository mongoSequenceRepository;
    private final MongoEntityInformation<T, ID> entityInformation;
    private final MongoOperations mongoOperations;

    public MongoBaseRepositoryImpl(MongoEntityInformation<T, ID> entityInformation,
                                   MongoOperations mongoOperations) {
        super(entityInformation, mongoOperations);
        // Keep the EntityManager around to used from the newly introduced methods.
        this.mongoOperations = mongoOperations;
        this.entityInformation = entityInformation;
        this.mongoSequenceRepository = new MongoSequenceRepository(mongoOperations);
    }


    @Override
    public <S extends T> S save(S entity) {

        Assert.notNull(entity, "Entity must not be null!");

        // Get class name for sequence class
        String className = entity.getClass().getSimpleName();

        // Set Id if entity don't have Id
        if (entity.getId() == null) {
            entity.setId(mongoSequenceRepository.nextSequence(className));
        }

        // Set createdAt if entity don't have createdAt
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(DateUtils.getDate());
        }
        // Set updatedAt time as current time
        entity.setUpdatedAt(DateUtils.getDate());
        mongoOperations.save(entity);
        return entity;
    }


    @Override
    public boolean findByIdAndSaveDelete(ID id) {
        Assert.notNull(id, "The given id must not be null!");
        Query query = new Query(Criteria.where("_id").is(id).and("deleted").is(false));
        Update update = new Update().set("deleted", true);
        update.set("updatedAt", DateUtils.getDate());
        T entity = mongoOperations.findAndModify(query, update, entityInformation.getJavaType());
        if (!Optional.ofNullable(entity).isPresent()) {
            throw new DataNotFoundByIdException("invalid request " + entityInformation.getJavaType().getSimpleName() + " id " + id);
        }
        return true;
    }
}