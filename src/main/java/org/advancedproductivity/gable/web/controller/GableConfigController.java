/*
 *  Copyright (c) 2021 AdvancedProductivity
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

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
