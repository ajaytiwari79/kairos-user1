package com.kairos.aspects;

import com.kairos.enums.kpermissions.FieldLevelPermission;
import com.kairos.service.kpermissions.ActivityPermissionService;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.kairos.commons.utils.ObjectUtils.*;
import static com.kairos.utils.PermissionMapperUtils.checkAndReturnValidModel;


@Aspect
@Component
public class ReadPermissionAspect {


    private static ActivityPermissionService activityPermissionService;
    @Inject
    public void setPermissionService(ActivityPermissionService activityPermissionService) {
        ReadPermissionAspect.activityPermissionService = activityPermissionService;
    }


   // @Before("execution(* com.kairos.utils.response.ResponseHandler.generateResponse(..))")
    public static <T> void validateResponseAsPerPermission(Object object) {
        Collection<Object> objectCollection=object instanceof Collection ?(Collection) object: Arrays.asList(object);
        Object[] objectArray = removeNull(objectCollection);
        List<T> objects = checkAndReturnValidModel(objectArray);
        if(isCollectionNotEmpty(objects)) {
            activityPermissionService.updateModelBasisOfPermission(objects, newHashSet(FieldLevelPermission.READ));
        }
    }
}
