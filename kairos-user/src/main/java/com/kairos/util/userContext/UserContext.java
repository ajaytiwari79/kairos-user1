package com.kairos.util.userContext;

import org.springframework.stereotype.Component;

@Component
public class UserContext {
    public static final String CORRELATION_ID = "correlation-id";
    public static final String AUTH_TOKEN     = "Authorization";
    public static final String USER_ID        = "user-id";
    public static final String ORG_ID         = "org-id";
    public static final String UNIT_ID         = "unit-id";

    private static final ThreadLocal<String> correlationId= new ThreadLocal<String>();
    private static final ThreadLocal<String> authToken= new ThreadLocal<String>();
    private static final ThreadLocal<String> userId = new ThreadLocal<String>();
    private static final ThreadLocal<Long> orgId = new ThreadLocal<Long>();
    private static final ThreadLocal<Long> unitId = new ThreadLocal<Long>();

    public static String getCorrelationId() { return correlationId.get(); }
    public static void setCorrelationId(String cid) {correlationId.set(cid);}

    public static String getAuthToken() { return authToken.get(); }
    public static void setAuthToken(String aToken) {authToken.set(aToken);}

    public static String getUserId() { return userId.get(); }
    public static void setUserId(String aUser) {userId.set(aUser);}

    public static Long getOrgId() { return orgId.get(); }
    public static void setOrgId(Long aOrg) {orgId.set(aOrg);}

    public static void setUnitId(Long unitid) {unitId.set(unitid);}
    public static Long getUnitId() {return unitId.get();}


}