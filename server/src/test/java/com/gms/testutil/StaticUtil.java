package com.gms.testutil;

import org.junit.Assert;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Asiel Leal Celdeiro | lealceldeiro@gmail.com
 * @version 0.1
 */
public class StaticUtil {

    public static void testFieldsAreNorRepeated(Class<?> targetClass) {
        List<String> values = new LinkedList<>();
        final Field[] fields = targetClass.getFields();
        String value;
        for(Field f : fields) {
            value = ReflectionTestUtils.getField(targetClass, f.getName()).toString();
            if (!values.contains(value)) {
                values.add(value);
            }
        }
        Assert.assertEquals(fields.length, values.size());
    }

}
