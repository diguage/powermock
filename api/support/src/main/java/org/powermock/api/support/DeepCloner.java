/*
 * Copyright 2009 the original author or authors.
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
package org.powermock.api.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

import org.powermock.core.ListMap;
import org.powermock.reflect.Whitebox;

import sun.misc.Unsafe;

/**
 * The purpose of the deep cloner is to create a deep clone of an object. An
 * object can also be cloned to a different class-loader.
 * <p>
 */
public class DeepCloner {

    /**
     * Clones an object.
     * 
     * @return A deep clone of the object to clone.
     */
    public static <T> T clone(T objectToClone) {
        return clone(objectToClone, true);
    }

    /**
     * Clones an object.
     * 
     * @param includeStandardJavaType
     *            <code>true</code> also clones standard java types (using
     *            simple serialization), <code>false</code> simply reference to
     *            these objects (will be same instance).
     * @return A deep clone of the object to clone.
     */
    public static <T> T clone(T objectToClone, boolean includeStandardJavaType) {
        assertObjectNotNull(objectToClone);
        final Class<T> objectType = getType(objectToClone);
        return (T) performClone(objectType.getClassLoader(), objectType, objectToClone, new ListMap<Object, Object>(), includeStandardJavaType);
    }

    /**
     * Clone an object into an object loaded by the supplied classloader. Note
     * that standard java classes are not cloned but only referenced to.
     * 
     * @return A deep clone of the object to clone.
     */
    public static <T> T clone(ClassLoader classloader, T objectToClone) {
        return clone(classloader, objectToClone, false);
    }

    /**
     * Clone an object into an object loaded by the supplied classloader.
     * 
     * @param includeStandardJavaType
     *            <code>true</code> also clones standard java types (using
     *            simple serialization), <code>false</code> simply reference to
     *            these objects (will be same instance).
     * @return A deep clone of the object to clone.
     */
    public static <T> T clone(ClassLoader classloader, T objectToClone, boolean includeStandardJavaType) {
        assertObjectNotNull(objectToClone);
        return performClone(classloader, ClassLoaderUtil.loadClassWithClassloader(classloader, getType(objectToClone)), objectToClone,
                new ListMap<Object, Object>(), includeStandardJavaType);
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> getType(T object) {
        if (object == null) {
            return null;
        }
        return (Class<T>) (object instanceof Class ? object : object.getClass());
    }

    private static boolean isClass(Object object) {
        if (object == null) {
            return false;
        }
        return object instanceof Class<?>;
    }

    private static void assertObjectNotNull(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Object to clone cannot be null");
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T performClone(ClassLoader targetCL, Class<T> targetClass, Object source, ListMap<Object, Object> referenceMap,
            boolean shouldCloneStandardJavaTypes) {
        Object target = null;
        if (targetClass.isArray()) {
            target = instantiateArray(targetCL, targetClass, source, referenceMap, shouldCloneStandardJavaTypes);
        } else if (isCollection(targetClass)) {
            target = cloneCollection(targetCL, source, referenceMap, shouldCloneStandardJavaTypes);
        } else if (targetClass.isPrimitive() && !shouldCloneStandardJavaTypes) {
            target = source;
        } else if (isStandardJavaType(targetClass) && isSerializable(targetClass)) {
            target = serializationClone(source);
        } else if (targetClass.isEnum()) {
            target = cloneEnum(targetCL, source);
        } else if (isClass(source)) {
            target = ClassLoaderUtil.loadClassWithClassloader(targetCL, getType(source));
        } else {
            target = isClass(source) ? source : Whitebox.newInstance(targetClass);
        }

        if (!targetClass.isEnum() && !isStandardJavaType(targetClass) && !targetClass.isPrimitive() && !isClass(source)) {
            cloneFields(targetCL, targetClass, source, target, referenceMap, shouldCloneStandardJavaTypes);
        }
        return (T) target;
    }

    private static boolean isSerializable(Class<?> cls) {
        return Serializable.class.isAssignableFrom(cls);
    }

    /*
     * Perform simple serialization
     */
    private static Object serializationClone(Object source) {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(source);
            oos.flush();
            ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
            ois = new ObjectInputStream(bin);
            return ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                oos.close();
                ois.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Object cloneEnum(ClassLoader targetCL, Object source) {
        Object target;
        final Class enumClassLoadedByTargetCL = ClassLoaderUtil.loadClassWithClassloader(targetCL, getType(source));
        target = getEnumValue(source, enumClassLoadedByTargetCL);
        return target;
    }

    private static <T> void cloneFields(ClassLoader targetCL, Class<T> targetClass, Object source, Object target,
            ListMap<Object, Object> referenceMap, boolean cloneStandardJavaTypes) {
        Class<?> currentTargetClass = targetClass;
        while (currentTargetClass != null) {
            for (Field field : currentTargetClass.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    final Field declaredField = Whitebox.getField(getType(source), field.getName());
                    declaredField.setAccessible(true);
                    final Object object = declaredField.get(source);
                    final Object instantiatedValue;
                    if (referenceMap.containsKey(object)) {
                        instantiatedValue = referenceMap.get(object);
                    } else {
                        final Class<Object> type = getType(object);
                        if (object == null && !isCollection(object)) {
                            instantiatedValue = object;
                        } else {
                            final Class<Object> typeLoadedByCL = ClassLoaderUtil.loadClassWithClassloader(targetCL, type);
                            if (type.isEnum()) {
                                instantiatedValue = getEnumValue(object, typeLoadedByCL);
                            } else {
                                instantiatedValue = performClone(targetCL, typeLoadedByCL, object, referenceMap, cloneStandardJavaTypes);
                            }
                        }
                        referenceMap.put(object, instantiatedValue);
                    }

                    final boolean needsUnsafeWrite = field.isEnumConstant() || isStaticFinalModifier(field);
                    if (needsUnsafeWrite) {
                        UnsafeFieldWriter.write(field, target, instantiatedValue);
                    } else {
                        field.set(target, instantiatedValue);
                    }
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            currentTargetClass = currentTargetClass.getSuperclass();
        }
    }

    private static <T> boolean isStandardJavaType(Class<T> targetClass) {
        return targetClass.getName().startsWith("java.");
    }

    private static boolean isStaticFinalModifier(final Field field) {
        final int modifiers = field.getModifiers();
        return Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers) || field.getDeclaringClass().equals(Character.class)
                && field.getName().equals("MIN_RADIX");
    }

    @SuppressWarnings("unchecked")
    private static Object cloneCollection(ClassLoader targetCL, Object source, ListMap<Object, Object> referenceMap, boolean cloneStandardJavaTypes) {
        Object target;
        Collection sourceCollection = (Collection) source;
        final Class<Object> collectionClass = ClassLoaderUtil.loadClassWithClassloader(targetCL, getType(source));
        Collection newInstance = null;
        try {
            newInstance = (Collection) collectionClass.newInstance();
        } catch (Exception e) {
            // Should never happen for collections
            throw new RuntimeException(e);
        }
        for (Object collectionValue : sourceCollection) {
            final Class<? extends Object> typeLoadedByTargetCL = ClassLoaderUtil.loadClassWithClassloader(targetCL, collectionValue.getClass());
            newInstance.add(performClone(targetCL, typeLoadedByTargetCL, collectionValue, referenceMap, cloneStandardJavaTypes));
        }
        target = newInstance;
        return target;
    }

    private static boolean isCollection(final Object object) {
        return object == null ? false : isCollection(object.getClass());
    }

    private static boolean isCollection(final Class<?> cls) {
        return Collection.class.isAssignableFrom(cls);
    }

    @SuppressWarnings("unchecked")
    private static Enum getEnumValue(final Object enumValueOfSourceClassloader, final Class<Object> enumTypeLoadedByTargetCL) {
        return Enum.valueOf((Class) enumTypeLoadedByTargetCL, ((Enum) enumValueOfSourceClassloader).toString());
    }

    private static Object instantiateArray(ClassLoader targetCL, Class<?> arrayClass, Object objectToClone, ListMap<Object, Object> referenceMap,
            boolean cloneStandardJavaTypes) {
        final int arrayLength = Array.getLength(objectToClone);
        final Object array = Array.newInstance(arrayClass.getComponentType(), arrayLength);
        for (int i = 0; i < arrayLength; i++) {
            final Object object = Array.get(objectToClone, i);
            final Object performClone = performClone(targetCL, ClassLoaderUtil.loadClassWithClassloader(targetCL, getType(object)), object,
                    referenceMap, cloneStandardJavaTypes);
            Array.set(array, i, performClone);
        }
        return array;
    }

    /**
     * Most of this code has been copied from the Sun14ReflectionProvider in the
     * XStream project. Some changes has been made, namely if the field is
     * static final then the {@link Unsafe#staticFieldOffset(Field)} method is
     * used instead of {@link Unsafe#objectFieldOffset(Field)}.
     * 
     * @author Joe Walnes
     * @author Brian Slesinsky
     * @author Johan Haleby
     */
    private static class UnsafeFieldWriter {
        private final static Unsafe unsafe;
        private final static Exception exception;
        static {
            Unsafe u = null;
            Exception ex = null;
            try {
                Class<?> objectStreamClass = Class.forName("sun.misc.Unsafe");
                Field unsafeField = objectStreamClass.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                u = (Unsafe) unsafeField.get(null);
            } catch (Exception e) {
                ex = e;
            }
            exception = ex;
            unsafe = u;
        }

        public static void write(Field field, Object object, Object value) {
            if (exception != null) {
                throw new RuntimeException("Could not set field " + object.getClass() + "." + field.getName(), exception);
            }
            try {
                final long offset;
                if (DeepCloner.isStaticFinalModifier(field)) {
                    offset = unsafe.staticFieldOffset(field);
                } else {
                    offset = unsafe.objectFieldOffset(field);
                }
                Class<?> type = field.getType();
                if (type.isPrimitive()) {
                    if (type.equals(Integer.TYPE)) {
                        unsafe.putInt(object, offset, ((Integer) value).intValue());
                    } else if (type.equals(Long.TYPE)) {
                        unsafe.putLong(object, offset, ((Long) value).longValue());
                    } else if (type.equals(Short.TYPE)) {
                        unsafe.putShort(object, offset, ((Short) value).shortValue());
                    } else if (type.equals(Character.TYPE)) {
                        unsafe.putChar(object, offset, ((Character) value).charValue());
                    } else if (type.equals(Byte.TYPE)) {
                        unsafe.putByte(object, offset, ((Byte) value).byteValue());
                    } else if (type.equals(Float.TYPE)) {
                        unsafe.putFloat(object, offset, ((Float) value).floatValue());
                    } else if (type.equals(Double.TYPE)) {
                        unsafe.putDouble(object, offset, ((Double) value).doubleValue());
                    } else if (type.equals(Boolean.TYPE)) {
                        unsafe.putBoolean(object, offset, ((Boolean) value).booleanValue());
                    } else {
                        throw new RuntimeException("Could not set field " + object.getClass() + "." + field.getName() + ": Unknown type " + type);
                    }
                } else {
                    unsafe.putObject(object, offset, value);
                }
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Could not set field " + object.getClass() + "." + field.getName(), e);
            }
        }
    }
}
