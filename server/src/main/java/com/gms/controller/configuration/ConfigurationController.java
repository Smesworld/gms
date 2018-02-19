package com.gms.controller.configuration;

import com.gms.controller.BaseController;
import com.gms.service.configuration.ConfigurationService;
import com.gms.util.constant.DefaultConst;
import com.gms.util.constant.ResourcePath;
import com.gms.util.exception.domain.NotFoundEntityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ConfigurationController
 *
 * @author Asiel Leal Celdeiro | lealceldeiro@gmail.com
 *
 * @version 0.1
 * Feb 18, 2017
 */
@RestController
@RequestMapping(ResourcePath.CONFIGURATION)
public class ConfigurationController extends BaseController{

    private final ConfigurationService configService;

    @Autowired
    public ConfigurationController(ConfigurationService userService, DefaultConst defaultConst) {
        super(defaultConst);
        this.configService = userService;
    }

    @GetMapping
    @ResponseBody
    public Object getConfig(@RequestParam(value = "key", required = false) String key,
                            @RequestParam(value = "id", required = false) Long id) throws NotFoundEntityException {
        return key != null ? getConfigByKey(key, id) : configService.getConfig();
    }

    @GetMapping("{id}")
    @ResponseBody
    public Map getConfigByUser(@PathVariable(value = "id") Long id) {
        return configService.getConfigByUser(id);
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveConfig(@RequestBody Map<String, Object> configs) throws NotFoundEntityException {
        configService.saveConfig(configs);
    }

    private Object getConfigByKey(String key, Long userId) throws NotFoundEntityException {
        return userId != null ?
                configService.getConfig(key, userId) :
                configService.getConfig(key);
    }
}