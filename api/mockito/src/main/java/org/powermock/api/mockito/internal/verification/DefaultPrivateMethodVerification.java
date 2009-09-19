package org.powermock.api.mockito.internal.verification;

import org.powermock.api.mockito.verification.PrivateMethodVerification;
import org.powermock.reflect.Whitebox;

public class DefaultPrivateMethodVerification implements PrivateMethodVerification {

    private final Object objectToVerify;

    public DefaultPrivateMethodVerification(Object objectToVerify) {
        this.objectToVerify = objectToVerify;
    }

    public void invocation(Object... arguments) throws Exception {
        Whitebox.invokeMethod(objectToVerify, arguments);

    }

    public void invocation(String methodToExecute, Class<?>[] argumentTypes, Object... arguments) throws Exception {
        Whitebox.invokeMethod(objectToVerify, methodToExecute, argumentTypes, arguments);

    }

    public void invocation(String methodToExecute, Class<?> definedIn, Class<?>[] argumentTypes, Object... arguments) throws Exception {
        Whitebox.invokeMethod(objectToVerify, methodToExecute, definedIn, argumentTypes, (Object[]) arguments);
    }

    public void invocation(Class<?> declaringClass, String methodToExecute, Object... arguments) throws Exception {
        Whitebox.invokeMethod(objectToVerify, declaringClass, methodToExecute, (Object[]) arguments);
    }

    public void invocation(Class<?> declaringClass, String methodToExecute, Class<?>[] parameterTypes, Object... arguments) throws Exception {
        Whitebox.invokeMethod(objectToVerify, declaringClass, methodToExecute, parameterTypes, (Object[]) arguments);
    }

    public void invocation(String methodToExecute, Object... arguments) throws Exception {
        Whitebox.invokeMethod(objectToVerify, methodToExecute, (Object[]) arguments);
    }
}
