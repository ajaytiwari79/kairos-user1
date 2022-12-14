package com.kairos.commons.audit_logging;

import com.kairos.dto.user_context.UserContext;
import com.kairos.enums.audit_logging.LoggingType;
import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.Visit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.kairos.commons.utils.ObjectUtils.*;
import static com.kairos.constants.CommonConstants.PACKAGE_NAME;
import static de.danielbechler.diff.node.DiffNode.State.ADDED;
import static de.danielbechler.diff.node.DiffNode.State.CHANGED;

//import org.neo4j.ogm.annotation.NodeEntity;

/**
 * pradeep
 * 8/5/19
 */

//@Component
public class AuditLogging {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogging.class);
    private static Set<String> primitives = newHashSet("int", "long", "boolean", "short", "byte", "float", "double");

    private static MongoTemplate mongoTemplate;

    @Autowired
    public void setMongoTemplate(@Qualifier("AuditLoggingMongoTemplate") MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Async
    public static <S> void doAudit(S oldEntity, S newEntity){
        checkDifferences(oldEntity,newEntity);
    }

    public static <S> Map<String, Object> checkDifferences(S oldEntity, S newEntity) {
        Map<String, Object> result = null;
        try {
            ObjectDifferBuilder builder = ObjectDifferBuilder.startBuilding();
            Class parentNodeClass = oldEntity.getClass();
            DiffNode diff = builder.build().compare(newEntity, oldEntity);
            final Map<String, Object> diffResult = new HashMap<>();
            diff.visit(new DiffNode.Visitor() {
                @Override
                public void node(DiffNode arg0, Visit arg1) {
                    final Object oldValue = arg0.canonicalGet(oldEntity);
                    final Object newValue = arg0.canonicalGet(newEntity);
                    if(!isIgnoreLogging(arg0)) {
                        updateMap(arg0, oldValue, newValue, arg0.getPropertyName(), diffResult, parentNodeClass);
                    }
                }
            });
            diffResult.put("loggingType", getLoggingType(oldEntity, newEntity));
            if(newEntity.getClass().getSimpleName().equals("Shift")){
                diffResult.put("staffId", newEntity.getClass().getMethod("getStaffId").invoke(newEntity));
                diffResult.put("management", UserContext.getUserDetails().isManagement());
                if((Boolean) newEntity.getClass().getMethod("isDraft").invoke(newEntity)){
                    return null;
                }
                if((Boolean) oldEntity.getClass().getMethod("isDraft").invoke(oldEntity)){
                    diffResult.put("loggingType", LoggingType.CREATED);
                }
            }
            result = diffResult;
            mongoTemplate.save(result, newEntity.getClass().getSimpleName());
            LOGGER.info("test {}", oldEntity);
        }catch (Exception e){
            e.getStackTrace();
        }
        return result;
    }

    private static boolean isIgnoreLogging(DiffNode arg0) {
        boolean isIgnoreLogging = false;
        if(isIgnoredField(arg0) || isIgnoredMethod(arg0) || (isNotNull(arg0.getParentNode()) && isIgnoredClass(arg0.getParentNode().getValueType()))) {
            isIgnoreLogging = true;
        }
        return isIgnoreLogging;
    }

    private static boolean isIgnoredMethod(DiffNode arg0) {
        return arg0.getPropertyAnnotation(IgnoreLogging.class) != null;
    }

    private static boolean isIgnoredClass(Class className) {
        return className.getAnnotation(IgnoreLogging.class) != null;
    }

    private static boolean isIgnoredField(DiffNode arg0) {
        return arg0.getFieldAnnotation(IgnoreLogging.class) != null;
    }

    private static void updateMap(DiffNode arg0, Object oldValue, Object newValue, String properteyName, Map<String, Object> result, Class parentNodeClass) {
        if(isArgumentValid(arg0, properteyName) && isPropertyValid(arg0, properteyName) && parentNodeClass.equals(arg0.getParentNode().getValueType())) {
            if(!primitives.contains(arg0.getValueType().getSimpleName())){// && arg0.getValueType().isAnnotationPresent(NodeEntity.class)) {
                if(isNull(oldValue)){
                    try {
                        oldValue = newValue.getClass().newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
                result.put(properteyName, checkDifferences(oldValue, newValue));
            } else {
                result.put(properteyName, newValue);
                result.put("old_" + properteyName, oldValue);
            }
        }
    }

    private static boolean isArgumentValid(DiffNode arg0, String properteyName) {
        return (isNotNull(properteyName) && isValid(arg0)) || isParentValid(arg0);
    }

    private static boolean isPropertyValid(DiffNode arg0, String properteyName) {
        return newHashSet(ADDED, CHANGED).contains(arg0.getState()) && !properteyName.toUpperCase().contains("UPDATEDATE") && !properteyName.equals("/");
    }

    static boolean isValid(DiffNode arg0) {
        return primitives.contains(arg0.getValueType().getName()) && isNotNull(arg0.getParentNode()) && arg0.getParentNode().getValueType().getPackage().getName().contains(PACKAGE_NAME);
    }

    static boolean isParentValid(DiffNode arg0) {
        return isNotNull(arg0.getParentNode()) && arg0.getParentNode().getValueType().getPackage().getName().contains(PACKAGE_NAME);
    }

    private static <S,T> LoggingType getLoggingType(S oldEntity, S newEntity) {
        T id = null;
        boolean deleted = false;
        try {
            id = (T)oldEntity.getClass().getMethod("getId").invoke(oldEntity);
            deleted = (boolean)newEntity.getClass().getMethod("isDeleted").invoke(newEntity);

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOGGER.error(e.getMessage());
        }
        if(isNull(id)) {
            return LoggingType.CREATED;
        } else if(deleted) {
            return LoggingType.DELETED;
        }
        return LoggingType.UPDATED;
    }
}
