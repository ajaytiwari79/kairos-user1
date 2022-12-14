package com.kairos.service.redis;

import com.kairos.commons.config.EnvConfigCommon;
import com.kairos.commons.utils.CommonsExceptionUtil;
import com.kairos.persistence.model.ExceptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.ObjectUtils.isCollectionNotEmpty;
import static com.kairos.constants.CommonConstants.LOCAL_PROFILE;

/**
 * created by @bobby sharma
 */
@Service
public class RedisService extends CommonsExceptionUtil {


    private static Logger LOGGER = LoggerFactory.getLogger(RedisService.class);

    @Inject
    private RedisTemplate<String, Map<String, String>> valueOperations;
    @Inject
    private EnvConfigCommon envConfigCommon;
    @Inject
    private ExceptionService exceptionService;

    public boolean verifyTokenInRedisServer(String userName, String accessToken) {
        if(!LOCAL_PROFILE.equals(envConfigCommon.getCurrentProfile())) {
            Map<String, String> userTokensFromDifferentMachine = valueOperations.opsForValue().get(userName);
            boolean validToken = false;
            if(userTokensFromDifferentMachine != null) {
                String userAccessToken = userTokensFromDifferentMachine.get(getTokenKey(accessToken));
                if(accessToken.equalsIgnoreCase(userAccessToken)) {
                    validToken = true;
                }
            }
            return validToken;
        }
        return true;
    }

    public boolean removeUserTokenFromRedisByUserNameAndToken(String userName,  String accessToken) {
        if(!LOCAL_PROFILE.equals(envConfigCommon.getCurrentProfile())) {
            boolean tokenRemoved = false;
            Map<String, String> userTokensFromDifferentMachine = valueOperations.opsForValue().get(userName);
            if(Optional.ofNullable(userTokensFromDifferentMachine).isPresent()) {
                String tokenKey = getTokenKey(accessToken);
                if(userTokensFromDifferentMachine.size() == 1) valueOperations.delete(userName);
                else {
                    if(!userTokensFromDifferentMachine.get(tokenKey).equalsIgnoreCase(accessToken)) {
                        throw new InvalidTokenException(exceptionService.convertMessage("message.redis.perssistedtoken.notEqualToRequestedToken"));
                    }
                    userTokensFromDifferentMachine.remove(tokenKey);
                    valueOperations.opsForValue().set(userName, userTokensFromDifferentMachine);
                }
                tokenRemoved = true;
            } else {
                throw new InvalidTokenException(exceptionService.convertMessage("message.user.notFoundInRedis"));
            }
            return tokenRemoved;
        }
        return true;
    }

    private String getTokenKey(String accessToken) {
        String[] tokenSplitString = accessToken.split("\\.");
        return tokenSplitString[tokenSplitString.length - 1].toLowerCase();
    }

    @Async
    public void removeKeyFromCacheAsyscronously(Set<String> removeKeys){
        removeEntryFromCache(removeKeys);
    }
    private void removeEntryFromCache(Set<String> removeKeys) {
        Set<String> patternKeys = removeKeys.stream().filter(key -> key.contains("*")).collect(Collectors.toSet());
        if (isCollectionNotEmpty(patternKeys)) {
            patternKeys.forEach(key -> removeKeys.addAll(valueOperations.keys(key)));
        }
        valueOperations.delete(removeKeys);
    }

    public void removeKeyFromCache(Set<String> removeKeys){
        removeEntryFromCache(removeKeys);
    }

}
