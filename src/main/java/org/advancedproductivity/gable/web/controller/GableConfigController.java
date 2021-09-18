package org.advancedproductivity.gable.web.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.web.entity.Result;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author zzq
 */
@RestController
@RequestMapping("/api/GableConfig")
public class GableConfigController {

    @GetMapping
    public ObjectNode get(){
        return GableConfig.getConfig();
    }

    @Resource
    private MessageSource messageSource;

    @PostMapping
    public Result post(@RequestBody ObjectNode config) {
        String needField = GableConfig.checkRequired(config);
        if (null != needField) {
            return Result.error(messageSource.getMessage("MissingField", new Object[]{needField}, LocaleContextHolder.getLocale()));
        }
        GableConfig.updateConfig(config);
        return Result.success();
    }
}
