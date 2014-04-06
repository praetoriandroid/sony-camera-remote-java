package com.praetoriandroid.cameraremote.rpc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnusedDeclaration")
public class MethodTypesEntity {

    private String name;

    private List<AbstractClass> parameterTypes;

    private List<AbstractClass> responseTypes;

    private String version;

    public MethodTypesEntity(String name, List<AbstractClass> parameterTypes, List<AbstractClass> responseTypes, String version) {
        this.name = name;
        this.parameterTypes = parameterTypes;
        this.responseTypes = responseTypes;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public List<AbstractClass> getParameterTypes() {
        return parameterTypes;
    }

    public List<AbstractClass> getResponseTypes() {
        return responseTypes;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return '{' + name + '(' + version + "): " + parameterTypes + ", " + responseTypes + '}';
    }

    public interface AbstractClass {
        /**
         * @return <code>true</code> if instance represents one of <code>boolean</code>, <code>int</code>,
         * <code>double</code>, {@link String} or array of them,
         * that could be obtained with {@link #getSimpleClass()};
         * <br/>
         * or <code>false</code> if instance represents a compound class description,
         * that could be obtained with {@link #getClassDescription()}.
         */
        public boolean isSimpleClass();

        /**
         * @return <code>true</code> if instance represents an array of compound objects.
         * In this case {@link #isSimpleClass()} returns <code>false</code>.
         */
        public boolean isClassDescriptionArray();

        /**
         * @return Class that represents one of simple classes (see {@link #isSimpleClass()}).
         * @throws IllegalStateException if {@link #isSimpleClass()} returns <code>false</code>.
         */
        public Class<?> getSimpleClass() throws IllegalStateException;

        /**
         * @return Class
         * @throws IllegalStateException if {@link #isSimpleClass()} returns <code>true</code>.
         */
        public Map<String, Class<?>> getClassDescription() throws IllegalStateException;
    }

    public static class SimpleClass implements AbstractClass {

        private Class<?> clazz;

        public SimpleClass(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public boolean isSimpleClass() {
            return true;
        }

        @Override
        public boolean isClassDescriptionArray() {
            return false;
        }

        @Override
        public Class<?> getSimpleClass() throws IllegalStateException {
            return clazz;
        }

        @Override
        public Map<String, Class<?>> getClassDescription() throws IllegalStateException {
            throw new IllegalStateException();
        }

        @Override
        public String toString() {
            return clazz.getSimpleName();
        }
    }

    public static class ClassDescription extends HashMap<String, Class<?>> implements AbstractClass {
        @Override
        public boolean isSimpleClass() {
            return false;
        }

        @Override
        public boolean isClassDescriptionArray() {
            return false;
        }

        @Override
        public Class<?> getSimpleClass() throws IllegalStateException {
            throw new IllegalStateException();
        }

        @Override
        public Map<String, Class<?>> getClassDescription() throws IllegalStateException {
            return this;
        }
    }

    public static class ClassDescriptionArray extends ClassDescription {

        @Override
        public boolean isClassDescriptionArray() {
            return true;
        }

        @Override
        public String toString() {
             return '[' + super.toString() + ']';
        }
    }
}
