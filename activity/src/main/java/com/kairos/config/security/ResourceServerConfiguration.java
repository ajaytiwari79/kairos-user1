package com.kairos.config.security;


import com.kairos.service.exception.ExceptionService;
import com.kairos.service.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import javax.inject.Inject;

import static com.kairos.constants.ApiConstants.API_UNIT_URL;
import static com.kairos.constants.AppConstants.API_CREATE_KMD_TASK_DEMAND;

/**
 *  created by @bobby sharma
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    Logger log = LoggerFactory.getLogger(ResourceServerConfiguration.class);

    @Inject
    private ExceptionService exceptionService;
    @Inject
    private RedisService redisService;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/webjars/**", "/static/**","/api/v1/ws/**", "/swagger-resources/**/**", "/swagger-ui.html", "/v2/api-docs").permitAll()
                .antMatchers(API_CREATE_KMD_TASK_DEMAND).permitAll()
                .antMatchers(API_UNIT_URL + "/api/v1/ws/**").permitAll()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/**").authenticated()
                .and().addFilterBefore(getAuthenticationFilter(), AbstractPreAuthenticatedProcessingFilter.class);
    }

    public OAuth2AuthenticationProcessingFilter getAuthenticationFilter() {
        return new CustomBasicAuthenticationProcessingFilter(tokenStore(), redisService, exceptionService);
    }


    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new CustomJwtAccessTokenConverter();
        converter.setSigningKey("123456");
        return converter;
    }

}
