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

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.advancedproductivity.gable.web.entity.Result;
import org.advancedproductivity.gable.web.service.IntegrateService;
import org.advancedproductivity.gable.web.service.TagService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author zzq
 */
@RequestMapping("/api/tag")
@RestController
public class TagController {

    @Resource
    private TagService tagService;
    @Resource
    private IntegrateService integrateService;

    @PostMapping
    public Result addTag(@RequestParam String uuid, @RequestParam String tagName) {
        if (tagName.isEmpty()) {
            return Result.error("Tag Name can not empty");
        }
        boolean addSuccess = integrateService.addTag(tagName, uuid);
        if (addSuccess) {
            tagService.addTagForIntegrateTest(uuid, tagName);
            return Result.success();
        }
        return Result.error("add Tag error");
    }


    @GetMapping("/test")
    public Result addTag( @RequestParam String tagName) {
        if (tagName.isEmpty()) {
            return Result.error("Tag Name can not empty");
        }
        ArrayNode arrayNode = tagService.getTestByTag(tagName);
        return Result.success(arrayNode);
    }
}
