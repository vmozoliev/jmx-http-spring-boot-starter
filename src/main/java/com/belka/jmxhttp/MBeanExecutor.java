package com.belka.jmxhttp;

import com.belka.jmxhttp.exception.MBeanExecutionException;
import com.belka.jmxhttp.exception.MBeanNotFoundException;
import com.belka.jmxhttp.register.dto.MBeanArgumentDto;
import com.belka.jmxhttp.utils.ClassUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.management.MBeanOperationInfo;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class MBeanExecutor implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final MBeanOperationResolver mBeanOperationResolver;

    public MBeanExecutor(MBeanOperationResolver mBeanOperationResolver) {
        this.mBeanOperationResolver = mBeanOperationResolver;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Object execute(String mBean, String methodName, MBeanArgumentDto[] arguments) throws MBeanNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        checkMBean(mBean, methodName);
        return invokeMethod(mBean, methodName, arguments);
    }

    private void checkMBean(String mBean, String methodName) {
        MBeanOperationInfo operation = mBeanOperationResolver.getMBeanOperation(mBean, methodName);
        if (operation == null) {
            throw new MBeanNotFoundException("Cant find managed jmx bean with name: " + mBean);
        }
    }

    private Object invokeMethod(String mBean, String methodName, MBeanArgumentDto[] arguments) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Object bean = applicationContext.getBean(mBean);
        Class[] classes = Arrays.stream(arguments)
                .map(argument -> ClassUtils.parseType(argument.getType()))
                .toArray(Class[]::new);

        Object[] params = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            params[i] = ClassUtils.newInstance(classes[i], arguments[i].getValue());
        }

        return invokeMethod(bean, methodName, classes, params);
    }

    private Object invokeMethod(Object bean, String methodName, Class[] parameterTypes, Object[] params) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = bean.getClass().getDeclaredMethod(methodName, parameterTypes);
        try {
            return method.invoke(bean, params);
        } catch (Exception ex) {
            throw new MBeanExecutionException(ex);
        }
    }

}
