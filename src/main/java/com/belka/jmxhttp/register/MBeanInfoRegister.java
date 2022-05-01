package com.belka.jmxhttp.register;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler;

import javax.management.modelmbean.ModelMBeanInfo;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MBeanInfoRegister implements BeanFactoryAware, ApplicationContextAware {

    private Logger log = LoggerFactory.getLogger(MBeanInfoRegister.class);

    private final AnnotationJmxAttributeSource annotationSource =
            new AnnotationJmxAttributeSource();

    private final MetadataMBeanInfoAssembler metadataAssembler =
            new MetadataMBeanInfoAssembler(this.annotationSource);

    private Map<String, ModelMBeanInfo> mbeansMap;

    public ModelMBeanInfo getModelMBeanInfo(String beanName) {
        return mbeansMap.get(beanName);
    }

    public Map<String, ModelMBeanInfo> getMBeans() {
        return mbeansMap;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        annotationSource.setBeanFactory(beanFactory);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        String[] beans = BeanFactoryUtils.beanNamesForAnnotationIncludingAncestors(applicationContext,
                ManagedResource.class);

        Map<String, ModelMBeanInfo> beansMap = new HashMap<>();

        for (String beanName : beans) {
            log.debug("Processing jmx bean: {}", beanName);

            Object bean = applicationContext.getBean(beanName);
            ModelMBeanInfo info = null;
            String className = bean.getClass().getName();
            try {
                info = metadataAssembler.getMBeanInfo(bean, className);
                log.debug("Found mBean info: {}", info);
            } catch (Exception ex) {
                log.debug("Error jmx bean processing: {}, {}", beanName, ex.getMessage());
            }
            if (info != null) {
                beansMap.put(beanName, info);
                log.debug("jmx bean has been registered: {}", beanName);
            }
        }

        this.mbeansMap = Collections.unmodifiableMap(beansMap);
    }
}
