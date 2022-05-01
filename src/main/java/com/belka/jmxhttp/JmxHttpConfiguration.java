package com.belka.jmxhttp;

import com.belka.jmxhttp.register.MBeanInfoRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import java.util.Collections;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Configuration
@ConditionalOnProperty(prefix = "spring.jmxHttp", name = "enabled", havingValue = "true")
public class JmxHttpConfiguration {

    private static final String ENDPOINT_NAME_PROPERTY = "spring.jmxHttp.endpointName";
    private static final String DEFAULT_ENDPOINT_NAME = "jmx";

    @Autowired
    private Environment env;

    @Bean
    public SimpleUrlHandlerMapping jmxHttpUrlHandlerMapping(JmxHttpRequestHandler jmxHttpRequestHandler) {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(HIGHEST_PRECEDENCE);
        mapping.setUrlMap(Collections.singletonMap("/" + resolveEndpointName() + "/**", jmxHttpRequestHandler));
        return mapping;
    }

    @Bean
    public MBeanInfoRegister mBeanInfoRegister() {
        return new MBeanInfoRegister();
    }

    @Bean
    public JmxHttpRequestHandler jmxRequestHandler(MBeanMethodInvoker mBeanMethodInvoker, MBeanInfoRegister mBeanInfoRegister) {
        return new JmxHttpRequestHandler(mBeanMethodInvoker, mBeanInfoRegister);
    }

    @Bean
    public MBeanMethodInvoker mBeanExecutor(MBeanOperationResolver mBeanOperationResolver) {
        return new MBeanMethodInvoker(mBeanOperationResolver);
    }

    @Bean
    public MBeanOperationResolver mBeanOperationResolver(MBeanInfoRegister mBeanInfoRegister) {
        return new MBeanOperationResolver(mBeanInfoRegister);
    }

    private String resolveEndpointName() {
        return env.containsProperty(ENDPOINT_NAME_PROPERTY) ?
                env.getProperty(ENDPOINT_NAME_PROPERTY) : DEFAULT_ENDPOINT_NAME;
    }

}


