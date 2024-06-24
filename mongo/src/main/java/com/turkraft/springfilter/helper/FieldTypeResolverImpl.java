package com.turkraft.springfilter.helper;

import com.turkraft.springfilter.converter.DateConverter.CustomDate;
import com.turkraft.springfilter.converter.StringCustomObjectIdConverter.CustomObjectId;
import com.turkraft.springfilter.converter.StringCustomUUIDConverter.CustomUUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

// source: https://github.com/RutledgePaulV/rest-query-engine

// this class is only used by the BsonGenerator, and adds the lang3 dependency unfortunately
// it should be possible to get rid of it and use reflection/LambdaMetafactory

@Service
class FieldTypeResolverImpl implements FieldTypeResolver {

    @Override
    public ClassInfo resolve(Class<?> root, String path) {
        if (root.isAssignableFrom(Map.class)) {
            if (path.endsWith(".id")) {
                return new ClassInfo(false, CustomUUID.class);
            }
            if (path.endsWith(".dynamicFieldSettings.value")) {
                return new ClassInfo(true, String.class);
            }
            return new ClassInfo(false, String.class);
        }

        String[] splitField = path.split("\\.", 2);
        if (splitField.length == 1) {
            return normalize(getField(root, splitField[0]));
        } else {
            return resolve(normalize(getField(root, splitField[0])).getClazz(), splitField[1]);
        }
    }

    @Override
    public Field getField(final Class<?> klass, final String fieldName) {
        Field field = org.apache.commons.lang3.reflect.FieldUtils.getField(klass, fieldName, true);
        if (field == null) {
            throw new IllegalArgumentException("Could not find field '" + fieldName + "' in " + klass);
        }
        return field;
    }

    private ClassInfo normalize(Field field) {

        if (field.isAnnotationPresent(Id.class) && field.getType().equals(String.class)) {
            return new ClassInfo(false, CustomObjectId.class);
        }

        if (field.getType().equals(UUID.class)) {
            return new ClassInfo(false, CustomUUID.class);
        }

        if (field.getType().equals(LocalDateTime.class) || field.getType().equals(ZonedDateTime.class) || field.getType().equals(LocalDate.class)) {
            return new ClassInfo(false, CustomDate.class);
        }

        if (Collection.class.isAssignableFrom(field.getType())) {
            return new ClassInfo(true, getFirstTypeParameterOf(field));
        } else if (field.getType().isArray()) {
            return new ClassInfo(true, field.getType().getComponentType());
        } else {
            return new ClassInfo(false, field.getType());
        }

    }

    private Class<?> getFirstTypeParameterOf(Field field) {
        return (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
    }

    @Data
    @AllArgsConstructor
    public static class ClassInfo {
        private Boolean isArray;
        private Class<?> clazz;
    }
}
