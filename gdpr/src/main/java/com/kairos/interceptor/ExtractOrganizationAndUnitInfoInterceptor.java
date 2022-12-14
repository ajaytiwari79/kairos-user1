package com.kairos.interceptor;


import com.kairos.dto.user_context.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;

import static com.kairos.commons.utils.ObjectUtils.isNotNull;
import static com.kairos.commons.utils.ObjectUtils.isNull;

/**
 * Created by anil on 10/8/17.
 */

class ExtractOrganizationAndUnitInfoInterceptor extends HandlerInterceptorAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractOrganizationAndUnitInfoInterceptor.class);

    @SuppressWarnings("unchecked")
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        LOGGER.debug("request uri is "+request.getRequestURI());

        if(request.getRequestURI().contains("swagger-ui")) return true;
        else if(request.getRequestURI().contains("css")) return true;
        else if(request.getRequestURI().contains("js")) return true;
        else if(request.getRequestURI().contains("images")) return true;
//        else if(request.getRequestURI().contains("public/legal")) return true;
        final Map<String, String> pathVariables = (Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        updateUserInfo(request, pathVariables);
        ServletRequestAttributes servletRequest = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest httpServletRequest = servletRequest.getRequest();

        String tabId = httpServletRequest.getParameter("moduleId");
        if (Optional.ofNullable(tabId).isPresent()) {
            UserContext.setTabId(tabId);
        }
        return isNotNull(httpServletRequest);
    }

    private void updateUserInfo(HttpServletRequest request, Map<String, String> pathVariables) {
        String unitIdString=isNull(pathVariables) ? null : pathVariables.get("unitId");
        String orgIdString=isNull(pathVariables) ? null : pathVariables.get("organizationId");
        String countryIdString = isNull(pathVariables) ? null : pathVariables.get("countryId");
        LOGGER.debug("[preHandle][" + request + "]" + "[" + request.getMethod()
                + "]" + request.getRequestURI() + "[ organizationID ,Unit Id " + orgIdString + " ," + unitIdString + " ]");
        updateOrganizationId(orgIdString);
        updateCountryId(countryIdString);
        updateUnitId(unitIdString);
    }

    private void updateOrganizationId(String orgIdString) {
        if (orgIdString!=null && !"null".equalsIgnoreCase(orgIdString)) {
            final Long orgId = Long.valueOf(orgIdString);
            UserContext.setOrgId(orgId);
        }
    }

    private void updateCountryId(String countryIdString) {
        if (countryIdString != null) {
            final Long countryId = Long.valueOf(countryIdString);
            UserContext.setCountryId(countryId);

        }
    }

    private void updateUnitId(String unitIdString) {
        if (unitIdString != null) {
            final Long unitId = Long.valueOf(unitIdString);
            UserContext.setUnitId(unitId);
            if(isNotNull(UserContext.getUserDetails())){
                UserContext.getUserDetails().setLastSelectedOrganizationId(unitId);
            }
        }
    }


}
