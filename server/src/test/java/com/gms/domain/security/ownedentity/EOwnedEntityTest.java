package com.gms.domain.security.ownedentity;

import com.gms.Application;
import com.gms.testutil.EntityUtil;
import com.gms.testutil.StringUtil;
import com.gms.testutil.validation.PersistenceValidation;
import com.gms.util.i18n.CodeI18N;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.validation.ConstraintViolation;
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
public class EOwnedEntityTest {

    private final String name = "sampleN";
    private final String username = "sampleU";
    private final String description = "sampleD";
    private final int MAX_RANGE_255 = 255;
    private final int MAX_RANGE_10485760 = 10485760;
    private EOwnedEntity entity;
    private EOwnedEntity entity3;

    //region persistence constraints validations

    @Test
    public void checkValidEntity() {
        cleanEntity();
        entity = EntityUtil.getSampleEntity();
        final Set<ConstraintViolation<Object>> cv = PersistenceValidation.validate(entity);
        assertTrue(cv.isEmpty());
    }

    @Test
    public void nameIsNotBlank() {
        propertyIsNot("", username, description, CodeI18N.FIELD_NOT_BLANK, "name property must not be blank");
    }

    @Test
    public void nameIsNotNull() {
        propertyIsNot(null, username, description, CodeI18N.FIELD_NOT_NULL, "name property must not be null");
    }

    @Test
    public void nameIsNotOutOfRange() {
        propertyIsNot(StringUtil.createJString(MAX_RANGE_255 + 1), username, description, CodeI18N.FIELD_SIZE, "name property must not be of size lesser than 0 and larger than " + MAX_RANGE_255 + " characters");
    }

    @Test
    public void usernameIsNotBlank() {
        propertyIsNot(name, "", description, CodeI18N.FIELD_NOT_BLANK, "username property must not be blank");
    }

    @Test
    public void usernameIsNotNull() {
        propertyIsNot(name, null, description, CodeI18N.FIELD_NOT_NULL, "username property must not be null");
    }

    @Test
    public void usernameIsNotOutOfRange() {
        propertyIsNot(name, StringUtil.createJString(MAX_RANGE_255 + 1), description, CodeI18N.FIELD_SIZE, "username property must not be of size lesser than 0 and larger than " + MAX_RANGE_255 + " characters");
    }

    @Test
    public void usernameIsNotWithInvalidPattern() {
        String[] invalidUsername = StringUtil.INVALID_USERNAME;
        for (String username: invalidUsername) {
            propertyIsNot(name, username, description, CodeI18N.FIELD_PATTERN_INCORRECT_USERNAME,
                    "username property does not fulfill the username pattern restrictions");
        }
    }

    @Test
    public void usernameIsValidWithValidPattern() {
        EOwnedEntity e;
        String[] validUsername = StringUtil.VALID_USERNAME;
        for (String username: validUsername) {
            e = new EOwnedEntity(name, username, description);
            assertTrue("Owned entity is not valid with a valid username: " + username, PersistenceValidation.validate(e).isEmpty());
        }
    }

    @Test
    public void descriptionIsNotBlank() {
        propertyIsNot(name, username, "", CodeI18N.FIELD_NOT_BLANK, "description property must not be blank");
    }

    @Test
    public void descriptionIsNotNull() {
        propertyIsNot(name, username, null, CodeI18N.FIELD_NOT_NULL, "description property must not be null");
    }

    @Test
    public void descriptionIsNotOutOfRange() {
        propertyIsNot(name, username, StringUtil.createJString(MAX_RANGE_10485760 + 1), CodeI18N.FIELD_SIZE, "description property must not be of size lesser than 0 and larger than " + MAX_RANGE_10485760 + " characters");
    }

    private void propertyIsNot(String name, String username, String description, String messageTest, String assertMessage) {
        EOwnedEntity e = new EOwnedEntity(name, username, description);
        assertTrue(assertMessage, PersistenceValidation.objectIsInvalidWithErrorMessage(e, messageTest));
    }
    //endregion

    @Test
    public void getName() {
        cleanEntity();

        ReflectionTestUtils.setField(entity, "name", name);
        assertEquals(entity.getName(), name);
    }

    @Test
    public void getUsername() {
        cleanEntity();

        ReflectionTestUtils.setField(entity, "username", username);
        assertEquals(entity.getUsername(), username);
    }

    @Test
    public void getDescription() {
        cleanEntity();

        ReflectionTestUtils.setField(entity, "description", description);
        assertEquals(entity.getDescription(), description);
    }

    @Test
    public void hashCodeTest() {
        prepareEntitiesForEqualityTest();

        assertEquals(entity.hashCode(),entity3.hashCode());
    }

    @Test
    public void equalsTest() {
        prepareEntitiesForEqualityTest();

        assertEquals(entity, entity3);
    }

    @Test
    public void toStringTest() {
        prepareEntitiesForEqualityTest();

        assertEquals(entity.toString(), entity3.toString());
    }

    private void cleanEntity() {
        entity = new EOwnedEntity();
        assertEntityValidity(entity);
    }

    private void cleanEntity3() {
        entity3 = new EOwnedEntity();
        assertEntityValidity(entity3);
    }

    private void assertEntityValidity(EOwnedEntity entity) {
        assertNull(ReflectionTestUtils.getField(entity, "name"));
        assertNull(ReflectionTestUtils.getField(entity, "username"));
        assertNull(ReflectionTestUtils.getField(entity, "description"));
    }


    private void prepareEntitiesForEqualityTest() {
        cleanEntity();
        cleanEntity3();
        prepareEntityForEqualityTest(entity);
        prepareEntityForEqualityTest(entity3);
    }
    private void prepareEntityForEqualityTest(EOwnedEntity e) {
        ReflectionTestUtils.setField(e, "name", name);
        ReflectionTestUtils.setField(e, "username", username);
        ReflectionTestUtils.setField(e, "description", description);
    }
}