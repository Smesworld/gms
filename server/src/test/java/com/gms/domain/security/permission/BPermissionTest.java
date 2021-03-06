package com.gms.domain.security.permission;

import com.gms.Application;
import com.gms.domain.security.role.BRole;
import com.gms.testutil.EntityUtil;
import com.gms.testutil.StringUtil;
import com.gms.testutil.validation.PersistenceValidation;
import com.gms.util.i18n.CodeI18N;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.validation.ConstraintViolation;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Asiel Leal Celdeiro | lealceldeiro@gmail.com
 * @version 0.1
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class BPermissionTest {

    private final String name = "sampleN";
    private final String label = "sampleL";
    private final Set<BRole> roles = new HashSet<>();
    private final int MAX_RANGE_255 = 255;
    private BPermission entity0;
    private BPermission entity1;

    @Before
    public void setUp() {
        roles.add(EntityUtil.getSampleRole());
    }

    //region persistence constraints validations
    @Test
    public void checkValidEntity() {
        cleanEntity0();
        entity0 = EntityUtil.getSamplePermission();
        final Set<ConstraintViolation<Object>> cv = PersistenceValidation.validate(entity0);
        assertTrue(cv.isEmpty());
    }

    @Test
    public void nameIsNotBlank() {
        propertyIsNot("", label, CodeI18N.FIELD_NOT_BLANK, "name property must not be blank");
    }

    @Test
    public void nameIsNotNull() {
        propertyIsNot(null, label, CodeI18N.FIELD_NOT_NULL, "name property must not be null");
    }

    @Test
    public void nameIsNotWithInvalidPattern() {
        String[] invalidNames = StringUtil.INVALID_USERNAME;
        for (String name: invalidNames) {
            propertyIsNot(name, label, CodeI18N.FIELD_PATTERN_INCORRECT_USERNAME,
                    "name property does not fulfill the username pattern restrictions");
        }
    }

    @Test
    public void nameIsValidWithValidPattern() {
        BPermission p;
        String[] validNames = StringUtil.VALID_USERNAME;
        for (String name: validNames) {
            p = new BPermission(name, label);
            assertTrue("Permission is not valid with a valid name: " + name, PersistenceValidation.validate(p).isEmpty());
        }
    }

    @Test
    public void labelIsNotBlank() {
        propertyIsNot(name, "", CodeI18N.FIELD_NOT_BLANK, "label property must not be blank");
    }

    @Test
    public void labelIsNotNull() {
        propertyIsNot(name, null, CodeI18N.FIELD_NOT_NULL, "label property must not be null");
    }

    @Test
    public void nameIsNotOutOfRange() {
        propertyIsNot(StringUtil.createJString(MAX_RANGE_255 + 1), label, CodeI18N.FIELD_SIZE, "name property must not be of size lesser than 0 and larger than " + MAX_RANGE_255 + " characters");
    }

    @Test
    public void labelIsNotOutOfRange() {
        propertyIsNot(name, StringUtil.createJString(MAX_RANGE_255 + 1), CodeI18N.FIELD_SIZE, "label property must not be of size lesser than 0 and larger than " + MAX_RANGE_255 + " characters");
    }

    public void propertyIsNot(String name, String label, String messageTest, String assertMessage) {
        BPermission e = new BPermission(name, label);
        assertTrue(assertMessage, PersistenceValidation.objectIsInvalidWithErrorMessage(e, messageTest));
    }
    //endregion

    @Test
    public void getName() {
        cleanEntity0();

        ReflectionTestUtils.setField(entity0, "name", name);
        assertEquals(entity0.getName(), name);
    }

    @Test
    public void getLabel() {
        cleanEntity0();

        ReflectionTestUtils.setField(entity0, "label", label);
        assertEquals(entity0.getLabel(), label);
    }

    @Test
    public void getRoles() {
        cleanEntity0();

        ReflectionTestUtils.setField(entity0, "roles", roles);
        assertEquals(entity0.getRoles(), roles);
    }

    @Test
    public void setRoles() {
        cleanEntity0();

        entity0.setRoles(roles);
        assertEquals(roles, ReflectionTestUtils.getField(entity0, "roles"));
    }

    @Test
    public void toStringTest() {
        prepareEntitiesForEqualityTest();

        assertEquals(entity0.toString(), entity1.toString());
    }

    @Test
    public void equalsTest() {
        prepareEntitiesForEqualityTest();

        assertEquals(entity0, entity1);
    }

    @Test
    public void hashCodeTest() {
        prepareEntitiesForEqualityTest();

        assertEquals(entity0.hashCode(), entity1.hashCode());
    }


    private void cleanEntity1() {
        entity1 = new BPermission();
        assertEntityValidity(entity1);
    }

    private void cleanEntity0() {
        entity0 = new BPermission();
        assertEntityValidity(entity0);
    }

    private void assertEntityValidity(BPermission entity) {
        assertNull(ReflectionTestUtils.getField(entity, "name"));
        assertNull(ReflectionTestUtils.getField(entity, "label"));
        assertNull(ReflectionTestUtils.getField(entity, "roles"));
    }

    private void prepareEntitiesForEqualityTest() {
        cleanEntity0();
        cleanEntity1();
        prepareEntityForEqualityTest(entity0);
        prepareEntityForEqualityTest(entity1);
    }
    private void prepareEntityForEqualityTest(BPermission e) {
        ReflectionTestUtils.setField(e, "name", name);
        ReflectionTestUtils.setField(e, "label", label);
        ReflectionTestUtils.setField(e, "roles", roles);
    }
}