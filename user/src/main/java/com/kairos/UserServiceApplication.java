package com.kairos;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.kairos.commons.config.mongo.EnableAuditLogging;
import com.kairos.custom_exception.UserExceptionHandler;
import com.kairos.dto.user_context.UserContextInterceptor;
import com.kairos.persistence.repository.custom_repository.Neo4jBaseRepositoryImpl;
import com.kairos.utils.user_context.SchedulerUserContextInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.kairos.commons.utils.ObjectMapperUtils.LOCALDATE_FORMATTER;
import static com.kairos.commons.utils.ObjectMapperUtils.LOCALTIME_FORMATTER;
import static java.time.format.DateTimeFormatter.ofPattern;

/**
 * Application Start Point
 */
@SpringBootApplication
@EnableEurekaClient
@EnableResourceServer
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableNeo4jRepositories(basePackages = {"com.kairos.persistence.repository"},repositoryBaseClass = Neo4jBaseRepositoryImpl.class)
@EnableTransactionManagement(proxyTargetClass=true)
@EnableCircuitBreaker
@EnableAsync
@EnableAuditLogging
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class UserServiceApplication implements WebMvcConfigurer {

	public static final DateTimeFormatter FORMATTER = ofPattern("yyyy-MM-dd");
	public static final Logger logger = LoggerFactory.getLogger(UserServiceApplication.class);


	static{
		java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
		System.setProperty("user.timezone", "UTC");
	}


	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new UserExceptionHandler());
		SpringApplication.run(UserServiceApplication.class, args);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}


	@Bean
	@Primary
	public ObjectMapper serializingObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(LOCALDATE_FORMATTER));
		javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(LOCALDATE_FORMATTER));
		javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(LOCALTIME_FORMATTER));
		javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(LOCALTIME_FORMATTER));
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.registerModule(javaTimeModule);
		return mapper;
	}
	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
		MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		mappingJackson2HttpMessageConverter.setObjectMapper(serializingObjectMapper());
		mappingJackson2HttpMessageConverter.setPrettyPrint(true);
		return mappingJackson2HttpMessageConverter;
	}
	@Bean
	public MappingJackson2XmlHttpMessageConverter mappingJackson2XmlHttpMessageConverter() {
		MappingJackson2XmlHttpMessageConverter mappingJackson2XmlHttpMessageConverter = new MappingJackson2XmlHttpMessageConverter();
		mappingJackson2XmlHttpMessageConverter.setPrettyPrint(true);
		return mappingJackson2XmlHttpMessageConverter;
	}
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
	//	super.configureMessageConverters(converters);
		converters.add(mappingJackson2HttpMessageConverter());
		converters.add(mappingJackson2XmlHttpMessageConverter());

	}
	@Profile({"development","qa","production"})
    @LoadBalanced
	@Primary
	@Bean
	public RestTemplate getCustomRestTemplate(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder
				.interceptors(new UserContextInterceptor())
				.messageConverters(mappingJackson2HttpMessageConverter())
				.build();
	}
	@Profile({"development","qa","production"})
	@LoadBalanced
	@Bean(name ="restTemplateWithoutAuth")
	public RestTemplate getCustomRestTemplateWithoutAuthorization(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder
				.messageConverters(mappingJackson2HttpMessageConverter())
				.build();
	}
    @Profile({"local", "test"})
    @Primary
    @Bean
    public RestTemplate getCustomRestTemplateLocal(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder
                .interceptors(new UserContextInterceptor())
                .messageConverters(mappingJackson2HttpMessageConverter())
                .build();
    }
    @Profile({"local", "test"})
    @Bean(name ="restTemplateWithoutAuth")
    public RestTemplate getCustomRestTemplateWithoutAuthorizationLocal(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder
                .messageConverters(mappingJackson2HttpMessageConverter())
                .build();
    }
	@Profile({"local", "test"})
	@Bean(name="schedulerServiceRestTemplate")
	public RestTemplate getRestTemplateWithoutUserContextLocal(RestTemplateBuilder restTemplateBuilder,  @Value("${scheduler.authorization}") String authorization) {

		return restTemplateBuilder
				.interceptors(new SchedulerUserContextInterceptor(authorization))
				.messageConverters(mappingJackson2HttpMessageConverter())
				.build();
	}

	@Profile({"development","qa","production"})
	@LoadBalanced
	@Bean(name="schedulerServiceRestTemplate")
	public RestTemplate getRestTemplateWithoutUserContext(RestTemplateBuilder restTemplateBuilder,  @Value("${scheduler.authorization}") String authorization) {

		return restTemplateBuilder
				.interceptors(new SchedulerUserContextInterceptor(authorization))
				.messageConverters(mappingJackson2HttpMessageConverter())
				.build();
	}

	@Bean(name ="restTemplateForThirdPartyAPI")
	public RestTemplate getRestTemplateForThirdPartyAPI(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder
				.messageConverters(mappingJackson2HttpMessageConverter())
				.build();
	}
}

