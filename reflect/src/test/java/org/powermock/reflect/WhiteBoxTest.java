/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.powermock.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import org.junit.Test;
import org.powermock.reflect.exceptions.MethodNotFoundException;
import org.powermock.reflect.exceptions.TooManyFieldsFoundException;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.powermock.reflect.proxyframework.RegisterProxyFramework;
import org.powermock.reflect.spi.ProxyFramework;
import org.powermock.reflect.testclasses.ClassWithChildThatHasInternalState;
import org.powermock.reflect.testclasses.ClassWithInternalState;
import org.powermock.reflect.testclasses.ClassWithPrivateMethods;
import org.powermock.reflect.testclasses.ClassWithSeveralMethodsWithSameName;
import org.powermock.reflect.testclasses.ClassWithSeveralMethodsWithSameNameOneWithoutParameters;
import org.powermock.reflect.testclasses.ClassWithVarArgsConstructor;

/**
 * Tests the WhiteBox's functionality.
 */
public class WhiteBoxTest {

	static {
		RegisterProxyFramework.registerProxyFramework(new ProxyFramework() {
			public Class<?> getUnproxiedType(Class<?> type) {
				return type;
			}

			public boolean isProxy(Class<?> type) {
				return false;
			}
		});
	}

	@Test
	public void testFindMethod_classContainingMethodWithNoParameters() throws Exception {
		Method expected = ClassWithSeveralMethodsWithSameNameOneWithoutParameters.class.getMethod("getDouble");
		Method actual = WhiteboxImpl.findMethodOrThrowException(ClassWithSeveralMethodsWithSameNameOneWithoutParameters.class, "getDouble");
		assertEquals(expected, actual);
	}

	@Test
	public void testFindMethod_classContainingOnlyMethodsWithParameters() throws Exception {
		try {
			WhiteboxImpl.findMethodOrThrowException(ClassWithSeveralMethodsWithSameName.class, "getDouble");
			fail("Should throw runtime exception!");
		} catch (RuntimeException e) {
			assertTrue("Error message did not match", e.getMessage().contains(
					"Several matching methods found, please specify the argument parameter types"));
		}
	}

	@Test
	public void testFindMethod_noMethodFound() throws Exception {
		try {
			WhiteboxImpl.findMethodOrThrowException(ClassWithSeveralMethodsWithSameName.class, "getDouble2");
			fail("Should throw runtime exception!");
		} catch (RuntimeException e) {
			assertEquals("Error message did not match", "No method found with name 'getDouble2' with argument types: [  ] in class "
					+ ClassWithSeveralMethodsWithSameName.class.getName(), e.getMessage());
		}
	}

	@Test
	public void testGetInternalState_object() throws Exception {
		ClassWithInternalState tested = new ClassWithInternalState();
		tested.increaseInteralState();
		Object internalState = Whitebox.getInternalState(tested, "internalState");
		assertTrue("InternalState should be instanceof Integer", internalState instanceof Integer);
		assertEquals(1, internalState);
	}

	@Test
	public void testGetInternalState_parmaterizedType() throws Exception {
		ClassWithInternalState tested = new ClassWithInternalState();
		tested.increaseInteralState();
		int internalState = Whitebox.getInternalState(tested, "internalState", tested.getClass(), int.class);
		assertEquals(1, internalState);
	}

	@Test
	public void testSetInternalState() throws Exception {
		ClassWithInternalState tested = new ClassWithInternalState();
		tested.increaseInteralState();
		Whitebox.setInternalState(tested, "anotherInternalState", 2);
		assertEquals(2, tested.getAnotherInternalState());
	}

	@Test
	public void testSetInternalState_superClass() throws Exception {
		ClassWithChildThatHasInternalState tested = new ClassWithChildThatHasInternalState();
		tested.increaseInteralState();
		Whitebox.setInternalState(tested, "anotherInternalState", 2, ClassWithInternalState.class);
		assertEquals(2, tested.getAnotherInternalState());
	}

	@Test
	public void testGetInternalState_superClass_object() throws Exception {
		ClassWithChildThatHasInternalState tested = new ClassWithChildThatHasInternalState();
		Object internalState = Whitebox.getInternalState(tested, "internalState", ClassWithInternalState.class);
		assertEquals(0, internalState);
	}

	@Test
	public void testGetInternalState_superClass_parameterized() throws Exception {
		ClassWithChildThatHasInternalState tested = new ClassWithChildThatHasInternalState();
		int internalState = Whitebox.getInternalState(tested, "internalState", ClassWithInternalState.class, int.class);
		assertEquals(0, internalState);
	}

	@Test
	public void testInvokePrivateMethod_primtiveType() throws Exception {
		assertTrue((Boolean) Whitebox.invokeMethod(new ClassWithPrivateMethods(), "primitiveMethod", 8.2));
	}

	@Test
	public void testInvokePrivateMethod_primtiveType_Wrapped() throws Exception {
		assertTrue((Boolean) Whitebox.invokeMethod(new ClassWithPrivateMethods(), "primitiveMethod", new Double(8.2)));
	}

	@Test
	public void testInvokePrivateMethod_wrappedType() throws Exception {
		assertTrue((Boolean) Whitebox.invokeMethod(new ClassWithPrivateMethods(), "wrappedMethod", new Double(8.2)));
	}

	@Test
	public void testInvokePrivateMethod_wrappedType_primitive() throws Exception {
		assertTrue((Boolean) Whitebox.invokeMethod(new ClassWithPrivateMethods(), "wrappedMethod", 8.2));
	}

	@Test
	public void testMethodWithPrimitiveIntAndString_primitive() throws Exception {
		assertEquals("My int value is: " + 8, (String) Whitebox.invokeMethod(new ClassWithPrivateMethods(), "methodWithPrimitiveIntAndString", 8,
				"My int value is: "));
	}

	@Test
	public void testMethodWithPrimitiveIntAndString_Wrapped() throws Exception {
		assertEquals("My int value is: " + 8, (String) Whitebox.invokeMethod(new ClassWithPrivateMethods(), "methodWithPrimitiveIntAndString",
				Integer.valueOf(8), "My int value is: "));
	}

	@Test
	public void testMethodWithPrimitiveAndWrappedInt_primtive_wrapped() throws Exception {
		assertEquals(17, Whitebox.invokeMethod(new ClassWithPrivateMethods(), "methodWithPrimitiveAndWrappedInt", new Class[] { int.class,
				Integer.class }, 9, Integer.valueOf(8)));
	}

	@Test
	public void testStaticState() {
		int expected = 123;
		Whitebox.setInternalState(ClassWithInternalState.class, "staticState", expected);
		assertEquals(expected, ClassWithInternalState.getStaticState());
		assertEquals(expected, Whitebox.getInternalState(ClassWithInternalState.class, "staticState"));
	}

	@Test(expected = RuntimeException.class)
	public void testStaticFinalState() {
		Whitebox.setInternalState(ClassWithInternalState.class, "staticFinalState", 123);
		fail("Static final is not possible to change");
	}

	/**
	 * Verifies that the http://code.google.com/p/powermock/issues/detail?id=6
	 * is fixed.
	 * 
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testInvokeMethodWithNullParameter() throws Exception {
		Whitebox.invokeMethod(null, "method");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvokeConstructorWithNullParameter() throws Exception {
		Whitebox.invokeConstructor(null, "constructor");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetInternalWithNullParameter() throws Exception {
		Whitebox.getInternalState(null, "state");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetInternalWithNullParameter() throws Exception {
		Whitebox.setInternalState(null, "state", new Object());
	}

	@Test
	public void testInstantiateVarArgsConstructor() throws Exception {
		final String argument1 = "argument1";
		final String argument2 = "argument2";
		ClassWithVarArgsConstructor instance = Whitebox.invokeConstructor(ClassWithVarArgsConstructor.class, argument1, argument2);
		String[] strings = instance.getStrings();
		assertEquals(2, strings.length);
		assertEquals(argument1, strings[0]);
		assertEquals(argument2, strings[1]);
	}

	@Test
	public void testInstantiateVarArgsConstructor_noArguments() throws Exception {
		ClassWithVarArgsConstructor instance = Whitebox.invokeConstructor(ClassWithVarArgsConstructor.class);
		String[] strings = instance.getStrings();
		assertEquals(0, strings.length);
	}

	@Test
	public void testInvokeVarArgsMethod_multipleValues() throws Exception {
		ClassWithPrivateMethods tested = new ClassWithPrivateMethods();
		assertEquals(6, Whitebox.invokeMethod(tested, "varArgsMethod", 1, 2, 3));
	}

	@Test
	public void testInvokeVarArgsMethod_noArguments() throws Exception {
		ClassWithPrivateMethods tested = new ClassWithPrivateMethods();
		assertEquals(0, Whitebox.invokeMethod(tested, "varArgsMethod"));
	}

	@Test
	public void testInvokeVarArgsMethod_oneArgument() throws Exception {
		ClassWithPrivateMethods tested = new ClassWithPrivateMethods();
		assertEquals(4, Whitebox.invokeMethod(tested, "varArgsMethod", 2));
	}

	@Test
	public void testInvokeVarArgsMethod_invokeVarArgsWithOneArgument() throws Exception {
		ClassWithPrivateMethods tested = new ClassWithPrivateMethods();
		assertEquals(1, Whitebox.invokeMethod(tested, "varArgsMethod", new Class<?>[] { int[].class }, 1));
	}

	@Test
	public void testInvokePrivateMethodWithASubTypeOfTheArgumentType() throws Exception {
		ClassWithPrivateMethods tested = new ClassWithPrivateMethods();
		ClassWithChildThatHasInternalState argument = new ClassWithChildThatHasInternalState();
		assertSame(argument, Whitebox.invokeMethod(tested, "methodWithObjectArgument", argument));
	}

	@Test
	public void testInvokePrivateMethodWithAClassArgument() throws Exception {
		ClassWithPrivateMethods tested = new ClassWithPrivateMethods();
		assertEquals(ClassWithChildThatHasInternalState.class, Whitebox.invokeMethod(tested, "methodWithClassArgument",
				ClassWithChildThatHasInternalState.class));
	}

	@Test
	public void testSetInternalStateInChildClassWithoutSpecifyingTheChildClass() throws Exception {
		final int value = 22;
		final String fieldName = "internalState";
		ClassWithChildThatHasInternalState tested = new ClassWithChildThatHasInternalState() {
		};
		Whitebox.setInternalState(tested, fieldName, value);
		assertEquals(value, Whitebox.getInternalState(tested, fieldName));
	}

	@Test
	public void testSetInternalStateInClassAndMakeSureThatTheChildClassIsNotAffectedEvenThoughItHasAFieldWithTheSameName() throws Exception {
		final int value = 22;
		final String fieldName = "anotherInternalState";
		ClassWithChildThatHasInternalState tested = new ClassWithChildThatHasInternalState() {
		};
		Whitebox.setInternalState(tested, fieldName, value);
		assertEquals(value, Whitebox.getInternalState(tested, fieldName));
		assertEquals(-1, Whitebox.getInternalState(tested, fieldName, ClassWithInternalState.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetInternalStateWithInvalidArgumentType() throws Exception {
		final int value = 22;
		final String fieldName = "internalState";
		ClassWithChildThatHasInternalState tested = new ClassWithChildThatHasInternalState() {
		};
		Whitebox.setInternalState(tested, fieldName, new Object());
		assertEquals(value, Whitebox.getInternalState(tested, fieldName));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetInternalStateWithNull() throws Exception {
		final int value = 22;
		final String fieldName = "internalState";
		ClassWithChildThatHasInternalState tested = new ClassWithChildThatHasInternalState() {
		};
		Whitebox.setInternalState(tested, fieldName, (Object) null);
		assertEquals(value, Whitebox.getInternalState(tested, fieldName));
	}

	@Test
	public void testSetAndGetInternalStateBasedOnFieldType() throws Exception {
		final int value = 22;
		ClassWithChildThatHasInternalState tested = new ClassWithChildThatHasInternalState();
		Whitebox.setInternalState(tested, int.class, value);
		assertEquals(value, (int) Whitebox.getInternalState(tested, int.class));
		assertEquals(value, Whitebox.getInternalState(tested, "anotherInternalState"));
		assertEquals(value, Whitebox.getInternalState(tested, "anotherInternalState", ClassWithChildThatHasInternalState.class));
	}

	@Test
	public void testSetAndGetInternalStateAtASpecificPlaceInTheHierarchyBasedOnFieldType() throws Exception {
		final int value = 22;
		ClassWithChildThatHasInternalState tested = new ClassWithChildThatHasInternalState();
		Whitebox.setInternalState(tested, int.class, value, ClassWithInternalState.class);
		assertEquals(42, (int) Whitebox.getInternalState(tested, int.class));
		assertEquals(value, (int) Whitebox.getInternalState(tested, int.class, ClassWithInternalState.class));
		assertEquals(value, Whitebox.getInternalState(tested, "staticState", ClassWithInternalState.class));
	}

	@Test
	public void testSetInternalStateBasedOnObjectType() throws Exception {
		final String value = "a string";
		ClassWithChildThatHasInternalState tested = new ClassWithChildThatHasInternalState();
		Whitebox.setInternalState(tested, value);
		assertEquals(value, Whitebox.getInternalState(tested, String.class));
	}

	@Test
	public void testSetInternalStateBasedOnObjectTypeWhenArgumentIsAPrimitiveType() throws Exception {
		final int value = 21;
		ClassWithChildThatHasInternalState tested = new ClassWithChildThatHasInternalState();
		Whitebox.setInternalState(tested, value);
		assertEquals((Integer) value, Whitebox.getInternalState(tested, "anotherInternalState", ClassWithChildThatHasInternalState.class,
				Integer.class));
	}

	@Test
	public void testSetInternalStateBasedOnObjectTypeAtASpecificPlaceInTheClassHierarchy() throws Exception {
		final String value = "a string";
		ClassWithChildThatHasInternalState tested = new ClassWithChildThatHasInternalState();
		Whitebox.setInternalState(tested, (Object) value, ClassWithInternalState.class);
		assertEquals(value, Whitebox.getInternalState(tested, "finalString"));
	}

	@Test
	public void testSetInternalStateBasedOnObjectTypeAtASpecificPlaceInTheClassHierarchyForPrimitiveType() throws Exception {
		final long value = 31;
		ClassWithChildThatHasInternalState tested = new ClassWithChildThatHasInternalState();
		Whitebox.setInternalState(tested, value, ClassWithInternalState.class);
		assertEquals(value, tested.getInternalLongState());
	}

	@Test
	public void testSetInternalStateBasedOnObjectTypeAtANonSpecificPlaceInTheClassHierarchyForPrimitiveType() throws Exception {
		final long value = 31;
		ClassWithChildThatHasInternalState tested = new ClassWithChildThatHasInternalState();
		Whitebox.setInternalState(tested, value);
		assertEquals(value, tested.getInternalLongState());
	}

	@Test
	public void testSetInternalMultipleOfSameTypeOnSpecificPlaceInHierarchy() throws Exception {
		final int value = 31;
		ClassWithChildThatHasInternalState tested = new ClassWithChildThatHasInternalState();
		try {
			Whitebox.setInternalState(tested, value, ClassWithInternalState.class);
			fail("should throw TooManyFieldsFoundException!");
		} catch (TooManyFieldsFoundException e) {
			assertEquals("Two or more fields matching type int.", e.getMessage());
		}
	}

	@Test
	public void testSetInternalMultipleOfSameType() throws Exception {
		final int value = 31;
		ClassWithInternalState tested = new ClassWithInternalState();
		try {
			Whitebox.setInternalState(tested, value);
			fail("should throw TooManyFieldsFoundException!");
		} catch (TooManyFieldsFoundException e) {
			assertEquals("Two or more fields matching type int.", e.getMessage());
		}
	}

	@Test
	public void testSetInternalStateBasedOnObjectSubClassTypeAtASpecificPlaceInTheClassHierarchy() throws Exception {
		final ClassWithPrivateMethods value = new ClassWithPrivateMethods() {
		};
		ClassWithChildThatHasInternalState tested = new ClassWithChildThatHasInternalState();
		Whitebox.setInternalState(tested, value, ClassWithInternalState.class);
		assertSame(value, tested.getClassWithPrivateMethods());
	}

	@Test
	public void testSetInternalStateBasedOnObjectSubClassType() throws Exception {
		final ClassWithPrivateMethods value = new ClassWithPrivateMethods() {
		};
		ClassWithChildThatHasInternalState tested = new ClassWithChildThatHasInternalState() {
		};
		Whitebox.setInternalState(tested, value);
		assertSame(value, tested.getClassWithPrivateMethods());
	}

	@Test
	public void testGetAllInstanceFields() throws Exception {
		Set<Field> allFields = Whitebox.getAllInstanceFields(new ClassWithChildThatHasInternalState());
		assertEquals(6, allFields.size());
	}

	@Test
	public void testGetAllStaticFields_assertNoFieldsFromParent() throws Exception {
		Set<Field> allFields = Whitebox.getAllStaticFields(ClassWithChildThatHasInternalState.class);
		assertEquals(0, allFields.size());
	}

	@Test
	public void testGetAllStaticFields() throws Exception {
		Set<Field> allFields = Whitebox.getAllStaticFields(ClassWithInternalState.class);
		assertEquals(2, allFields.size());
	}

	@Test
	public void testMethodWithNoMethodName_noMethodFound() throws Exception {
		try {
			Whitebox.getMethod(ClassWithInternalState.class, String.class);
			fail("Should throw MethodNotFoundException");
		} catch (MethodNotFoundException e) {
			assertEquals(
					"No method was found with argument types: [ java.lang.String ] in class org.powermock.reflect.testclasses.ClassWithInternalState",
					e.getMessage());
		}
	}

	@Test
	public void testMethodWithNoMethodName_tooManyMethodsFound() throws Exception {
		try {
			Whitebox.getMethod(ClassWithSeveralMethodsWithSameName.class);
			fail("Should throw RuntimeException");
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().contains(
					"Several matching methods found, please specify the method name so that PowerMock can determine which method you're refering to"));
		}
	}

	@Test
	public void testMethodWithNoMethodName_ok() throws Exception {
		final Method method = Whitebox.getMethod(ClassWithSeveralMethodsWithSameName.class, double.class);
		assertEquals(method, ClassWithSeveralMethodsWithSameName.class.getDeclaredMethod("getDouble", double.class));
	}

	public void testFinalState() {
		ClassWithInternalState state = new ClassWithInternalState();
		String expected = "changed";
		Whitebox.setInternalState(state, "finalString", expected);
		assertEquals(expected, state.getFinalString());
		assertEquals(expected, Whitebox.getInternalState(state, "finalString"));
	}
}
