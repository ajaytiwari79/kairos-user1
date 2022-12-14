package com.kairos.config.security;

import com.kairos.service.auth.UserOauth2Service;
import com.kairos.service.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import javax.inject.Inject;

import static com.kairos.constants.AppConstants.*;


@Configuration
@EnableWebSecurity
@Order(SecurityProperties.BASIC_AUTH_ORDER)
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserOauth2Service userDetailsService;
    @Inject
    private RedisService redisService;
    @Inject
    private JwtAccessTokenConverter jwtAccessTokenConverter;


    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        auth.authenticationProvider(authenticationProvider());
    }


    @Bean
    public CustomAuthenticationProvider authenticationProvider() {
        return new CustomAuthenticationProvider(userDetailsService, passwordEncoder());

    }

    /*
     * @see org.springframework.security.config.annotation.web.configuration.
     * WebSecurityConfigurerAdapter#configure(org.springframework.security.
     * config.annotation.web.builders.HttpSecurity) This method is where the
     * actual URL-based security is set up.
     */

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
        web.ignoring().antMatchers("/webjars/**");
        web.ignoring().antMatchers("/swagger-resources/**/**");
        web.ignoring().antMatchers("/swagger-ui.html");
        web.ignoring().antMatchers("/v2/api-docs");
        web.ignoring().antMatchers("/api/v1/ids");
        web.ignoring().antMatchers("/api/v1/unit/{unitId}/WithoutAuth");
        web.ignoring().antMatchers("/api/v1/time_care/**");
        web.ignoring().antMatchers(API_KMD_CARE_CITIZEN_GRANTS);
        web.ignoring().antMatchers(API_KMD_CARE_CITIZEN);
        web.ignoring().antMatchers(API_KMD_CARE_CITIZEN_RELATIVE_DATA);
        web.ignoring().antMatchers(API_KMD_CARE_STAFF_SHIFTS);
        web.ignoring().antMatchers(API_TIME_CARE_SHIFTS);
        web.ignoring().antMatchers(API_TIME_SLOTS_NAME);
        //web.ignoring().antMatchers(API_V1+SCHEDULER_EXECUTE_JOB);
        web.ignoring().antMatchers(API_KMD_CARE_TIME_SLOTS);
        web.ignoring().antMatchers("/api/v1/unit/{unitId}/client/client_ids_by_unitIds");
        web.ignoring().antMatchers("/api/v1/unit/{unitId}/staff/chat_server/register");
        web.ignoring().antMatchers("/api/v1/login");
        web.ignoring().antMatchers("/api/v1/create_permission_schema");
        web.ignoring().antMatchers("/api/v1/create_action_permission");
        web.ignoring().antMatchers("/api/v1/forgot");
       // web.ignoring().antMatchers("/oauth/token");
        web.ignoring().antMatchers("/api/v1/reset","/api/v1/logout");

    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .anonymous().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/oauth/*").permitAll()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin();
    }


   /* @Bean
    public FilterRegistrationBean registration() {
        FilterRegistrationBean registration = new FilterRegistrationBean(getAuthenticationFilter());
        registration.setEnabled(true);
        return registration;
    }
*/


}

