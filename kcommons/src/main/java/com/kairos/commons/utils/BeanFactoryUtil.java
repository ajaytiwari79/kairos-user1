package com.kairos.commons.utils;

import com.kairos.commons.config.ApplicationContextProviderNonManageBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;
import java.util.Properties;

/**
 * Created by oodles on 29/1/17.
 */
public class BeanFactoryUtil {
    private static final Logger logger = LoggerFactory.getLogger(BeanFactoryUtil.class);

    /**
     * Helper method to register a bean in spring bean factory
     *
     * @param beanName
     *            bean name
     * @param beanDefinition
     *            bean definition
     */
    public synchronized static void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        getDefaultListableBeanFactory().registerBeanDefinition(beanName, beanDefinition);
        logger.info(String.format("Bean registered in spring bean factory with name %s", beanName));
    }

    /**
     * Helper method to register a bean in spring bean factory
     *
     * @param beanName
     *            bean name
     * @param beanDefinition
     *            bean definition
     */
    public synchronized static void registerAndInializeBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        registerBeanDefinition(beanName, beanDefinition);
        getDefaultListableBeanFactory().getBean(beanName);
    }

    /**
     * Registers alias for the given name
     *
     * @param beanName
     *            bean name
     * @param alias
     *            alias to be registered
     */
    public synchronized static void registerAlias(String beanName, String alias) {
        getDefaultListableBeanFactory().registerAlias(beanName, alias);
    }

    /**
     * Helper method to remove bean definition
     *
     * @param beanName
     *            bean name
     * @return true if bean removed, false otherwise
     */
    public synchronized static boolean removeBeanDefinition(String beanName) {
        getDefaultListableBeanFactory().removeBeanDefinition(beanName);
        return true;
    }

    /**
     * Checks bean exists in bean factory
     *
     * @param beanName
     *            bean name
     * @return boolean true if exists, false otherwise
     */
    public synchronized static boolean containsBeanDefinition(String beanName) {
        return getDefaultListableBeanFactory().containsBeanDefinition(beanName);
    }

    /**
     * Checks bean exists in bean factory
     *
     * @param beanName
     *            bean name
     * @return boolean true if exists, false otherwise
     */
    public synchronized static boolean containsBean(String beanName) {
        return getDefaultListableBeanFactory().containsBean(beanName);
    }

    /**
     * Registers the singleton bean with name beanName
     *
     * @param beanName
     *            bean name
     * @param instance
     *            Singleton
     */
    public synchronized static void registerSingleton(String beanName, Object instance) {
        getDefaultListableBeanFactory().registerSingleton(beanName, instance);
    }

    /**
     * Helper method to get DefaultListableBeanFactory
     *
     * @return DefaultListableBeanFactory
     */
    public static DefaultListableBeanFactory getDefaultListableBeanFactory() {
        return (DefaultListableBeanFactory) ((ConfigurableApplicationContext)
                ApplicationContextProviderNonManageBean.getApplicationContext()
        ).getBeanFactory();
    }

    public static AbstractBeanDefinition getBeanDefinition(final Class<?> beanClass) {
        return BeanDefinitionBuilder.rootBeanDefinition(beanClass).setLazyInit(Boolean.FALSE)
                .setAutowireMode(GenericBeanDefinition.AUTOWIRE_NO).getBeanDefinition();
    }

    public static AbstractBeanDefinition getBeanDefinition(final Class<?> beanClass, final Properties properties) {
        final AbstractBeanDefinition beanDefinition = getBeanDefinition(beanClass);
        beanDefinition.setPropertyValues(preparePropertyValues(properties));
        return beanDefinition;
    }

    private static MutablePropertyValues preparePropertyValues(final Properties properties) {
        final MutablePropertyValues values = new MutablePropertyValues();
        if (properties != null && !properties.isEmpty()) {
            for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
                values.add(entry.getKey().toString(), entry.getValue());
            }
        }
        return values;
    }
}
