package com.kairos.config.listener;
import com.kairos.persistence.model.common.SpringApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Created by oodles on 29/1/17.
 */
@Component
public class SpringApplicationContextListener implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringApplicationContextListener.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent arg0) {
        LOGGER.info("Spring ApplicationContext is initialized. Lets hold the reference of ApplicationContext");

        SpringApplicationContext.set(applicationContext);
    }
}
