package com.gms.service.configuration;

import com.gms.domain.configuration.BConfiguration;
import com.gms.domain.security.BAuthorization;
import com.gms.domain.security.ownedentity.EOwnedEntity;
import com.gms.domain.security.role.BRole;
import com.gms.domain.security.user.EUser;
import com.gms.repository.configuration.BConfigurationRepository;
import com.gms.repository.security.authorization.BAuthorizationRepository;
import com.gms.repository.security.ownedentity.EOwnedEntityRepository;
import com.gms.repository.security.role.BRoleRepository;
import com.gms.repository.security.user.EUserRepository;
import com.gms.service.db.QueryService;
import com.gms.util.configuration.ConfigKey;
import com.gms.util.constant.DefaultConst;
import com.gms.util.exception.GmsGeneralException;
import com.gms.util.exception.domain.NotFoundEntityException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Asiel Leal Celdeiro | lealceldeiro@gmail.com
 * @version 0.1
 */
@Service
@Transactional
public class ConfigurationService {

    private final DefaultConst dc;

    private final BConfigurationRepository configurationRepository;
    private final EUserRepository userRepository;
    private final EOwnedEntityRepository entityRepository;
    private final BRoleRepository roleRepository;
    private final BAuthorizationRepository authRepository;

    @Getter private boolean multiEntity;
    @Getter private boolean userRegistrationAllowed;

    private static final List<ConfigKey> KEYS = Arrays.asList(ConfigKey.values());
    public static final String IN_SERVER = "_IN_SERVER";
    public static final String CONFIG_NOT_FOUND = "config.not.found";
    private static final String CONFIG_USER_PARAM_NUMBER = "config.param.user.not.valid";


    @Autowired
    public ConfigurationService(BConfigurationRepository configurationRepository, EUserRepository userRepository,
                                EOwnedEntityRepository entityRepository, BRoleRepository roleRepository,
                                BAuthorizationRepository authRepository, DefaultConst defaultConst, QueryService queryService) {
        this.configurationRepository = configurationRepository;
        this.userRepository = userRepository;
        this.entityRepository = entityRepository;
        this.roleRepository = roleRepository;
        this.authRepository = authRepository;
        this.dc = defaultConst;

        loadDBConfig();
    }

    //region default config

    /**
     * Returns whether the default configuration was set in place or not, buy counting the entries in the configuration
     * table.
     * @return <code>true</code> if there is any entry in the configuration table, <code>false</code> otherwise.
     */
    public boolean configurationExist() {
        return configurationRepository.count() > 0;
    }

    /**
     * Creates the default system configuration. All not user-specific configurations are set in place after this method
     * is called.
     * @return <code>true</code> if the configurations are created properly, <code>false</code> otherwise.
     */
    public boolean createDefaultConfig() {
        BConfiguration isMultiEntity = new BConfiguration(ConfigKey.IS_MULTI_ENTITY_APP_IN_SERVER.toString(),
                dc.getIsMultiEntity().toString());
        BConfiguration isUserRegistrationAllowed = new BConfiguration(ConfigKey.IS_USER_REGISTRATION_ALLOWED_IN_SERVER.toString(),
                dc.getIsUserRegistrationAllowed().toString());

        configurationRepository.save(isMultiEntity);
        configurationRepository.save(isUserRegistrationAllowed);
        multiEntity = Boolean.parseBoolean(isMultiEntity.getValue());
        userRegistrationAllowed = Boolean.parseBoolean(isUserRegistrationAllowed.getValue());
        return true;
    }

    /**
     * Creates the default configuration related to the default user, owned entity and roles assigned to him/her over that
     * owned entity. The required authorization of the default user is created after this method is called.
     * @return <code>true</code> if the authorization is created properly, <code>false</code> otherwise.
     */
    public boolean assignDefaultUserToEntityWithRole() {
        EUser u = userRepository.findFirstByUsernameOrEmail(dc.getUserAdminDefaultName(), dc.getUserAdminDefaultEmail());
        if (u != null) { //got default user
            EOwnedEntity e = entityRepository.findFirstByUsername(dc.getEntityDefaultUsername());
            if (e != null) { //got entity
                BRole role = roleRepository.findFirstByLabel(dc.getRoleAdminDefaultLabel());
                if (role != null) {
                    com.gms.domain.security.BAuthorization.BAuthorizationPk pk =
                            new BAuthorization.BAuthorizationPk(u.getId(), e.getId(), role.getId());
                    authRepository.save(new BAuthorization(pk, u, e, role));
                    return true;
                }
            }
        }
        return false;
    }

    //endregion

    /**
     * Gets all configuration that are not "user-specific"
     * @return A {@link Map} with all the configuration where every key is the configuration key and every value is the
     * configuration value.
     */
    public Map<String, String> getConfig() {
        List<BConfiguration> configs = configurationRepository.findAllByKeyEndingWith(IN_SERVER);
        Map<String, String> map = new HashMap<>();
        for (BConfiguration c: configs) {
            map.put(c.getKey(), c.getValue());
        }
        return map;
    }

    /**
     * Gets a configuration which is not "user-specific"
     * @param key String representation under the configuration is saved.
     * @return A {@link Map} with all the configuration where every key is the configuration key and every value is the
     * configuration value.
     * @throws NotFoundEntityException if the configuration key is not valid.
     */
    public Object getConfig(String key) throws NotFoundEntityException {
        key = key.toUpperCase();
        checkKey(key);

        if (key.endsWith(IN_SERVER)) {
            switch (ConfigKey.valueOf(key)) {
                case IS_MULTI_ENTITY_APP_IN_SERVER:
                    return isMultiEntity();
                case IS_USER_REGISTRATION_ALLOWED_IN_SERVER:
                    return isUserRegistrationAllowed();
                default: throw new NotFoundEntityException(CONFIG_NOT_FOUND);
            }
        }
        BConfiguration c = getValue(key);
        if (c != null) return c;
        throw new NotFoundEntityException(CONFIG_NOT_FOUND);
    }

    /**
     * Gets a configuration which is "user-specific"
     * @param key String representation under the configuration is saved.
     * @param userId User identifier for who the configuration was saved.
     * @return A {@link String} with the configuration value saved under a specific key and for a specific user with
     * <code>id</code> as the provided under the <code>userId</code> param. If the key is a valid one but there is
     * not such configuration saved for the user with id as the one provide in <code>userId</code> param, <code>null</code>
     * is returned.
     * @throws NotFoundEntityException if the configuration key is not valid.
     */
    public String getConfig(String key, long userId) throws NotFoundEntityException {
        key = key.toUpperCase();
        checkKey(key);
        return getValueByUser(key, userId);
    }

    /**
     * Returns all configurations for a specific user.
     * @param userId Identifier of the user whose configuration are going to be retrieved.
     * @return A {@link Map} containing all configurations for a user where every key is the configuration key and the
     * values is the configuration key associated to that value.
     * @see EUser#id
     * @see BConfiguration#key
     * @see BConfiguration#userId
     * @see BConfiguration#value
     */
    public Map<String, Object> getConfigByUser(long userId) {
        final List<BConfiguration> configs = configurationRepository.findAllByUserId(userId);
        Map<String, Object> map = new HashMap<>();
        for (BConfiguration c: configs) {
            map.put(c.getKey(), c.getValue());
        }
        return map;
    }

    /**
     * Save various configurations at once.
     * @param configs A {@link Map} of configurations. Every key in the Map is the configuration key, and the corresponding
     *                value is the configuration value.
     @throws NotFoundEntityException if any of the provided keys is not a valid key. Keys must correspond to any of the
      * values in {@link ConfigKey} as String.
      * @see ConfigKey
     */
    public void saveConfig(Map<String, Object> configs) throws NotFoundEntityException, GmsGeneralException {
        if (configs.get("user") != null) {
            try {
                long id = Long.parseLong(configs.remove("user").toString());
                saveConfig(configs, id);
            } catch (NumberFormatException e) {
                throw new GmsGeneralException(CONFIG_USER_PARAM_NUMBER, e, false, HttpStatus.UNPROCESSABLE_ENTITY);
            }
        }
        String kU;
        for (Map.Entry<String, Object> entry : configs.entrySet()) {
            kU = entry.getKey().toUpperCase();
            if (isValidKey(kU) && kU.endsWith(IN_SERVER)) {
                switch (ConfigKey.valueOf(kU)) {
                    case IS_MULTI_ENTITY_APP_IN_SERVER:
                        setIsMultiEntity(Boolean.parseBoolean(entry.getValue().toString()));
                        break;
                    case IS_USER_REGISTRATION_ALLOWED_IN_SERVER:
                        setUserRegistrationAllowed(Boolean.parseBoolean(entry.getValue().toString()));
                        break;
                    default: throw new NotFoundEntityException(CONFIG_NOT_FOUND);
                }
            }
        }
    }

    /**
     * Save various configurations at once for a specific user.
     * @param configs A {@link Map} of configurations. Every key in the Map is the configuration key, and the corresponding
     *                value is the configuration value.
     * @throws NotFoundEntityException if any of the provided keys is not a valid key. Keys must correspond to any of the
     * values in {@link ConfigKey} as String.
     * @see ConfigKey
     */
    public void saveConfig(Map<String, Object> configs, long userId) throws NotFoundEntityException {
        for (Map.Entry<String, Object> entry : configs.entrySet()) {
            checkKey(entry.getKey().toUpperCase());
            insertOrUpdateValue(entry.getKey(), entry.getValue().toString(), userId);
        }
    }

    /**
     * Sets whether the user registration via sign-up is allowed or not.
     * @param userRegistrationAllowed Indicates whether the user registration via sign-up is allowed or not.
     */
    public void setUserRegistrationAllowed(boolean userRegistrationAllowed) {
        insertOrUpdateValue(ConfigKey.IS_USER_REGISTRATION_ALLOWED_IN_SERVER, userRegistrationAllowed);
        this.userRegistrationAllowed = userRegistrationAllowed;
    }

    /**
     * Sets whether the application will handle multiple entities or not.
     * @param isMultiEntity Indicates whether the application will handle multiple entities or not.
     */
    public void setIsMultiEntity(boolean isMultiEntity) {
        insertOrUpdateValue(ConfigKey.IS_MULTI_ENTITY_APP_IN_SERVER, isMultiEntity);
        this.multiEntity = isMultiEntity;
    }

    /**
     * Returns the identifier of the las accessed entity by a given user.
     * @param userId Identifier of the user who the last accessed entity is being looked for.
     * @return The identifier of the last accessed entity or <code>null</code> if not found any.
     */
    public Long getLastAccessedEntityIdByUser(long userId) {
        String v = getValueByUser(ConfigKey.LAST_ACCESSED_ENTITY.toString(), userId);
        return v != null ? Long.parseLong(v) : null;
    }

    /**
     * Sets the last accessed entity by a user.
     * @param userId {@link EUser#id}
     * @param entityId {@link EOwnedEntity#id}
     */
    public void setLastAccessedEntityIdByUser(long userId, long entityId) {
        insertOrUpdateValue(ConfigKey.LAST_ACCESSED_ENTITY, entityId, userId);
    }

    //region private stuff
    private boolean isValidKey(ConfigKey key) {
        return (KEYS.contains(key));
    }

    private boolean isValidKey(String key) {
        return isValidKey(ConfigKey.valueOf(key));
    }

    private void checkKey(ConfigKey key) throws NotFoundEntityException {
        if (!isValidKey(key)) throw new NotFoundEntityException(CONFIG_NOT_FOUND);
    }

    private void checkKey(String key) throws NotFoundEntityException {
        try {
            checkKey(ConfigKey.valueOf(key));
        }
        catch (IllegalArgumentException e) {
            throw new NotFoundEntityException(CONFIG_NOT_FOUND);
        }
    }

    /**
     * Loads the most frequently queried configuration from database to memory
     */
    private void loadDBConfig() {
        if (configurationExist()) {
            final BConfiguration multi = configurationRepository.findFirstByKey(ConfigKey.IS_MULTI_ENTITY_APP_IN_SERVER.toString());
            if (multi != null) {
                multiEntity = Boolean.parseBoolean(multi.getValue());
            }
            else {
                setIsMultiEntity(dc.getIsMultiEntity());
            }
            final BConfiguration registration = configurationRepository.findFirstByKey(ConfigKey.IS_USER_REGISTRATION_ALLOWED_IN_SERVER.toString());
            if (registration != null) {
                userRegistrationAllowed = Boolean.parseBoolean(registration.getValue());
            }
            else {
                setUserRegistrationAllowed(dc.getIsUserRegistrationAllowed());
            }
        }
        else {
            multiEntity = dc.getIsMultiEntity();
            userRegistrationAllowed = dc.getIsUserRegistrationAllowed();
        }
    }

    private void insertOrUpdateValue(String key, String value) {
        key = key.toUpperCase();
        BConfiguration c = configurationRepository.findFirstByKey(key);
        if (c == null) {
            c = new BConfiguration(key, value);
        }
        else {
            c.setValue(value);
        }
        configurationRepository.save(c);
    }

    private void insertOrUpdateValue(String key, String value, long userId) {
        key = key.toUpperCase();
        BConfiguration c = configurationRepository.findFirstByKeyAndUserId(key, userId);
        if (c == null) {
            c = new BConfiguration(key, value, userId);
        }
        else {
            c.setValue(value);
        }
        configurationRepository.save(c);
    }

    private void insertOrUpdateValue(ConfigKey key, String value) {
        insertOrUpdateValue(key.toString(), value);
    }

    private void insertOrUpdateValue(ConfigKey key, String value, long userId) {
        insertOrUpdateValue(key.toString(), value, userId);
    }

    private void insertOrUpdateValue(ConfigKey key, Object value) {
        insertOrUpdateValue(key, String.valueOf(value));
    }

    private void insertOrUpdateValue(ConfigKey key, Object value, long userId) {
        insertOrUpdateValue(key, String.valueOf(value), userId);
    }

    private String getValueByUser(String key, long userId) {
        final BConfiguration c = configurationRepository.findFirstByKeyAndUserId(key.toUpperCase(), userId);
        if (c != null) {
            return c.getValue();
        }
        return null;
    }

    private BConfiguration getValue(String key) {
        return configurationRepository.findFirstByKey(key.toUpperCase());
    }
    //endregion
}
