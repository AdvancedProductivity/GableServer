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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.web.entity.Result;
import org.advancedproductivity.gable.web.service.EnvService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author zzq
 */
@RestController
@RequestMapping("/api/env")
public class EnvController {

    @Resource
    private EnvService envService;

    @GetMapping
    public JsonNode get() {
        return envService.getEnvConfigMenu();
    }

    @GetMapping("/detail")
    public JsonNode detail(@RequestParam String uuid) {
        return envService.getEnv(uuid);
    }

    @PostMapping()
    public Result add(@RequestParam String type, @RequestParam String name, @RequestBody ObjectNode config) {
        name = StringUtils.trim(name);
        JsonNode jsonNode = this.get();
        boolean have = false;
        for (int i = 0; i < jsonNode.size(); i++) {
            if (StringUtils.equals(jsonNode.path(i).path("name").asText(), name)) {
                have = true;
            }
        }
        if (have) {
            return Result.error("Env Name: " + name + " Have Exist");
        }
        boolean b = envService.addEnv(name, config);
        return Result.success(envService.getEnvConfigMenu());
    }

    @PutMapping()
    public Result update(@RequestParam String uuid, @RequestParam String name, @RequestBody ObjectNode config) {
        boolean b = envService.updateEnv(uuid, name, config);
        return Result.success(envService.getEnvConfigMenu());
    }
}
