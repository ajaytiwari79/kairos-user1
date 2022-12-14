package com.kairos.configuration;

import com.kairos.annotations.*;
import com.kairos.dto.kpermissions.ActionDTO;
import com.kairos.enums.kpermissions.PermissionAction;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static com.kairos.commons.utils.ObjectUtils.isCollectionNotEmpty;
import static com.kairos.constants.ApplicationConstants.*;


public class PermissionSchemaScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionSchemaScanner.class);

    public List<Map<String, Object>> createPermissionSchema(String domainPackagePath) {
        List<Map<String, Object>> modelData = new ArrayList<>();
        try {
            Reflections reflections = new Reflections(ClasspathHelper.forPackage(domainPackagePath));
            reflections.getTypesAnnotatedWith(KPermissionModel.class)
                    .forEach(permissionClass -> {
                        Map<String, Object> modelMetaData = new HashMap<>();
                        Set<Map<String, String>> fields = new HashSet<>();
                        Arrays.stream(permissionClass.getDeclaredFields())
                                .filter(entityField -> entityField.isAnnotationPresent(KPermissionField.class))
                                .forEach(permissionEntityField -> {
                                    Map<String, String> fieldsData = new HashMap<>();
                                    fieldsData.put(FIELD_NAME, permissionEntityField.getName());
                                    fields.add(fieldsData);
                                });
                        List<Map<String, Object>> subModelData = findSubModelData(permissionClass);
                        getRelationShipTypeModelData(permissionClass, fields, reflections);
                        modelMetaData.put(MODEL_NAME, permissionClass.getSimpleName());
                        modelMetaData.put(MODEL_CLASS, permissionClass.toString());
                        modelMetaData.put(FIELDS, fields);
                        modelMetaData.put(SUB_MODEL, subModelData);
                        modelMetaData.put(ACTION_PERMISSIONS,getActionPermissionSchema());
                        modelData.add(modelMetaData);
                    });
            LOGGER.info("model== {}",modelData);

        } catch (Exception ex) {
            LOGGER.error("ERROR in identifying permission models====== {}",ex.getMessage());
        }
        return modelData;
    }

    private Set<Map<String,String>> getActionPermissionSchema(){
        Set<Map<String,String>> actionPermissionsMap = new HashSet<>();
        actionPermissionsMap.add(new HashMap<String, String>(){{
            put(ACTION_NAME,"Add");
        }});
        actionPermissionsMap.add(new HashMap<String, String>(){{
            put(ACTION_NAME,"Update");
        }});
        actionPermissionsMap.add(new HashMap<String, String>(){{
            put(ACTION_NAME,"Delete");
        }});
        return actionPermissionsMap;
    }

    private void getRelationShipTypeModelData(Class permissionClass, Set<Map<String, String>> fields, Reflections reflections) {
        reflections.getTypesAnnotatedWith(KPermissionRelatedModel.class).forEach(kpermissionRelationModel ->
            Arrays.stream(kpermissionRelationModel.getDeclaredFields())
                    .filter(entityField -> entityField.isAnnotationPresent(KPermissionRelationshipFrom.class))
                    .findAny().ifPresent(field -> {
                if (field.getGenericType().equals(permissionClass)) {
                    Arrays.stream(kpermissionRelationModel.getDeclaredFields())
                            .filter(entityField -> entityField.isAnnotationPresent(KPermissionRelationshipTo.class))
                            .findAny().ifPresent(childField -> {
                        Map<String, String> fieldsData = new HashMap<>();
                        fieldsData.put(FIELD_NAME, childField.getName());
                        fields.add(fieldsData);
                    });
                }
            })
        );
    }

    private List<Map<String, Object>> findSubModelData(Class permissionClass) {
        List<Map<String, Object>> subModelData = new ArrayList<>();
        Arrays.stream(permissionClass.getDeclaredFields())
                .filter(entityField -> entityField.isAnnotationPresent(KPermissionSubModel.class))
                .forEach(permissionField -> {
                    Map<String, Object> subModelMetaData = new HashMap<>();
                    Set<Map<String, String>> subModelFields = new HashSet<>();
                    if (Collection.class.isAssignableFrom(permissionField.getType())) {
                        Type genericFieldType = permissionField.getGenericType();
                        ParameterizedType aType = (ParameterizedType) genericFieldType;
                        Type[] fieldArgTypes = aType.getActualTypeArguments();
                        for (Type fieldArgType : fieldArgTypes) {
                            Class fieldArgClass = (Class) fieldArgType;
                            subModelMetaData.put(MODEL_CLASS, fieldArgClass.toString());
                            subModelMetaData.put(MODEL_NAME, fieldArgClass.getSimpleName());
                            getFieldsOFModelAndSubModel(fieldArgClass.getDeclaredFields(), subModelFields);
                        }
                    } else {
                        subModelMetaData.put(MODEL_CLASS, permissionField.getType().toString());
                        subModelMetaData.put(MODEL_NAME, permissionField.getType().getSimpleName());
                        getFieldsOFModelAndSubModel(permissionField.getType().getDeclaredFields(), subModelFields);
                    }
                    if (isCollectionNotEmpty(subModelFields)) {
                        subModelMetaData.put(MODEL_NAME, permissionField.getName());
                        subModelMetaData.put(FIELDS, subModelFields);
                        subModelMetaData.put(IS_PERMISSION_SUB_MODEL, true);
                    }
                    subModelData.add(subModelMetaData);
                });
        return subModelData;
    }

    private void getFieldsOFModelAndSubModel(Field[] fields, Set<Map<String, String>> subModelFields) {
        for (Field field : fields) {
            if (field.isAnnotationPresent(KPermissionField.class)) {
                Map<String, String> fieldsData = new HashMap<>();
                fieldsData.put(FIELD_NAME, field.getName());
                subModelFields.add(fieldsData);
            }
        }
    }

    public List<ActionDTO> createActionPermissions(String packagePath) {
        Map<String,List<PermissionAction>> map = new HashMap<>();
        Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(packagePath)).setScanners(new MethodAnnotationsScanner()));
        Set<Method> controllers =reflections.getMethodsAnnotatedWith(KPermissionActions.class);
        for(Method method:controllers){
            KPermissionActions annotation=method.getAnnotation(KPermissionActions.class);
            if(!map.containsKey(annotation.modelName())){
                List<PermissionAction> permissionActions=new ArrayList<>();
                permissionActions.add(annotation.action());
                map.put(annotation.modelName(),permissionActions);
            }else {
                map.get(annotation.modelName()).add(annotation.action());
            }
        }
        List<ActionDTO> permissionActions = new ArrayList<>();
        map.forEach((modelName,actions)-> actions.forEach(action-> permissionActions.add(new ActionDTO(modelName,action))));
        return permissionActions;
    }

}
