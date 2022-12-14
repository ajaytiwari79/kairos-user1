package com.kairos.scheduler.service;

import com.kairos.commons.utils.DateUtils;
import com.kairos.scheduler.persistence.model.common.MongoBaseEntity;
import com.kairos.scheduler.persistence.repository.custom_repository.MongoSequenceRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Pankaj on 12/4/17.
 */
@Service
public class MongoBaseService {

    @Inject
    private MongoSequenceRepository mongoSequenceRepository;

    @Inject
    private MongoTemplate mongoTemplate;
    @Inject
    private DB database;


    private static final Logger logger = LoggerFactory.getLogger(MongoBaseService.class);


    public <T extends MongoBaseEntity> T save(T entity){

        Assert.notNull(entity, "Entity must not be null!");
        /**
        *  Get class name for sequence class
        * */
        String className = entity.getClass().getSimpleName();

        /**
         *  Set Id if entity don't have Id
         * */
        if(entity.getId() == null){

            entity.setId(mongoSequenceRepository.nextSequence(className));
        }
        /**
         *  Set createdAt if entity don't have createdAt
         * */
        if(entity.getCreatedAt() == null){
            entity.setCreatedAt(DateUtils.getDate());
        }
        /**
         *  Set updatedAt time as current time
         * */
        entity.setUpdatedAt(DateUtils.getDate());
        mongoTemplate.save(entity);
        return entity;
    }

    public <T extends MongoBaseEntity> List<T> save(List<T> entities){
        Assert.notNull(entities, "Entity must not be null!");
        Assert.notEmpty(entities, "Entity must not be Empty!");

        String collectionName = mongoTemplate.getCollectionName(entities.get(0).getClass());

        /**
         *  Creating BulkWriteOperation object
         * */

        BulkWriteOperation bulkWriteOperation= database.getCollection(collectionName).initializeUnorderedBulkOperation();

        /**
         *  Creating MongoConverter object (We need converter to convert Entity Pojo to BasicDbObject)
         * */
        MongoConverter converter = mongoTemplate.getConverter();

        BasicDBObject dbObject;

        /**
         *  Handling bulk write exceptions
         * */
        try{

            for (T entity: entities) {
                /**
                 *  Get class name for sequence class
                 * */
                String className = entity.getClass().getSimpleName();

                /**
                 *  Set createdAt if entity don't have createdAt
                 * */
                if(entity.getCreatedAt() == null){
                    entity.setCreatedAt(DateUtils.getDate());
                }
                /**
                 *  Set updatedAt time as current time
                 * */
                entity.setUpdatedAt(DateUtils.getDate());


                if(entity.getId() == null){
                    /**
                     *  Set Id if entity don't have Id
                     * */

                    entity.setId(mongoSequenceRepository.nextSequence(className));

                    dbObject = new BasicDBObject();

                    /*
                    *  Converting entity object to BasicDBObject
                    * */
                    converter.write(entity, dbObject);

                    /*
                    *  Adding entity (BasicDBObject)
                    * */
                    bulkWriteOperation.insert(dbObject);
                }else {

                    dbObject = new BasicDBObject();

                    /*
                    *  Converting entity object to BasicDBObject
                    * */
                    converter.write(entity, dbObject);

                    /**
                     *  Creating BasicDbObject for find query
                     * */
                    BasicDBObject query = new BasicDBObject();

                    /**
                     *  Adding query (find by ID)
                     * */
                    query.put("_id", dbObject.get("_id"));

                    /**
                     *  Replacing whole Object
                     * */
                    bulkWriteOperation.find(query).replaceOne(dbObject);
                }
            }

            /**
             * Executing the Operation
             * */
            bulkWriteOperation.execute();
            return entities;

        } catch(Exception ex){
            logger.error("BulkWriteOperation Exception ::  ", ex);
            return null;
        }
    }



}
