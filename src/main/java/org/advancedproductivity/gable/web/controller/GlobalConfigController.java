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
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.core.GlobalVar;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.web.entity.Result;
import org.springframework.web.bind.annotation.*;

/**
 * @author zzq
 */
@RestController
@RequestMapping("/api/global_config")
@Slf4j
public class GlobalConfigController {

    @GetMapping
    public ObjectNode get(){
        return GlobalVar.globalVar.deepCopy();
    }

    @PostMapping
    public Result post(@RequestBody ObjectNode newGlobalConfig) {
        try {
            GableFileUtils.saveFile(newGlobalConfig.toPrettyString(), GableConfig.getGablePath(),
                    GableConfig.PUBLIC_PATH, "global.json");
            GlobalVar.globalVar = newGlobalConfig;
        } catch (Exception e) {
            log.error("save config error: " + e.getMessage(), e);
            return Result.error(e.getMessage());
        }
        return Result.success();
    }
}
