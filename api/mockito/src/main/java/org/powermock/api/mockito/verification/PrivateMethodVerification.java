package org.powermock.api.mockito.verification;

import org.mockito.Mockito;

public interface PrivateMethodVerification {

    /**
     * Expect calls to private methods without having to specify the method
     * name. The method will be looked up using the parameter types (if
     * possible).
     * 
     * @see {@link Mockito#invocation(Object)}
     * @throws Exception
     *             If something unexpected goes wrong.
     */
    public void invocation(Object... arguments) throws Exception;

    /**
     * Expect a private or inner class method call in cases where PowerMock
     * cannot automatically determine the type of the parameters, for example
     * when mixing primitive types and wrapper types in the same method. For
     * most situations use {@link #invocation(Object, Object...)} instead.
     * 
     * @see {@link Mockito#invocation(Object)}
     * @throws Exception
     *             If something unexpected goes wrong.
     */
    public void invocation(String methodToExecute, Class<?>[] argumentTypes, Object... arguments) throws Exception;

    /**
     * Expected a private or inner class method call in a subclass (defined by
     * <code>definedIn</code>) in cases where PowerMock cannot automatically
     * determine the type of the parameters, for example when mixing primitive
     * types and wrapper types in the same method. For most situations use
     * {@link #invokeinvocation(Object, Object...)} instead.
     * 
     * @see {@link Mockito#invocation(Object)}
     * @throws Exception
     *             If something unexpected goes wrong.
     */
    public void invocation(String methodToExecute, Class<?> definedIn, Class<?>[] argumentTypes, Object... arguments) throws Exception;

    /**
     * Expect a private or inner class method call that is located in a subclass
     * of the instance.
     * 
     * @see {@link Mockito#invocation(Object)}
     * @throws Exception
     *             If something unexpected goes wrong.
     */
    public void invocation(Class<?> declaringClass, String methodToExecute, Object... arguments) throws Exception;

    /**
     * Expect a private or inner class method call in that is located in a
     * subclass of the instance. This might be useful to test private methods.
     * <p>
     * Use this for overloaded methods.
     * 
     * @see {@link Mockito#invocation(Object)}
     * @throws Exception
     *             If something unexpected goes wrong.
     */
    public void invocation(Class<?> declaringClass, String methodToExecute, Class<?>[] parameterTypes, Object... arguments) throws Exception;

    /**
     * Expect a static private or inner class method call.
     * 
     * @see {@link Mockito#invocation(Object)}
     * @throws Exception
     *             If something unexpected goes wrong.
     */
    public void invocation(String methodToExecute, Object... arguments) throws Exception;
}
