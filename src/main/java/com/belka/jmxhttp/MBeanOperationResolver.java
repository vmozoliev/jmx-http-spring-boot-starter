package com.belka.jmxhttp;

import com.belka.jmxhttp.register.MBeanInfoRegister;

import javax.management.MBeanOperationInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import java.util.Arrays;

public class MBeanOperationResolver {

    private final MBeanInfoRegister mBeanInfoRegister;

    public MBeanOperationResolver(MBeanInfoRegister mBeanInfoRegister) {
        this.mBeanInfoRegister = mBeanInfoRegister;
    }

    public MBeanOperationInfo getMBeanOperation(String mBeanName, String methodName) {
        ModelMBeanInfo beanInfo = mBeanInfoRegister.getModelMBeanInfo(mBeanName);
        if (beanInfo == null)
            return null;
        return Arrays.stream(beanInfo.getOperations())
                .filter(info -> info.getName().equals(methodName))
                .findAny()
                .orElse(null);
    }
}
