package com.turkraft.springfilter.helper;

import java.lang.reflect.Field;

public interface FieldTypeResolver {

    FieldTypeResolverImpl.ClassInfo resolve(Class<?> root, String path);

    Field getField(Class<?> klass, String fieldName);

}
