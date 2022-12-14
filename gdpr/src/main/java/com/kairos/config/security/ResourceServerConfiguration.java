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

/**
 * created by @bobby sharma
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    Logger log = LoggerFactory.getLogger(ResourceServerConfiguration.class);
    @Inject
    private RedisService redisService;
    @Inject
    private ExceptionService exceptionService;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests()
                .antMatchers("/**").authenticated()
                .and().addFilterBefore(getAuthenticationFilter(), AbstractPreAuthenticatedProcessingFilter.class);
    }


    public OAuth2AuthenticationProcessingFilter getAuthenticationFilter() {
        return new CustomBasicAuthenticationFilter(tokenStore(), redisService, exceptionService);
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
