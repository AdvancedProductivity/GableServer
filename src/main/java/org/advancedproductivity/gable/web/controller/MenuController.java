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
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.web.entity.Result;
import org.advancedproductivity.gable.web.service.UserService;
import org.advancedproductivity.gable.web.service.impl.MenuServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author zzq
 */
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @Resource
    private HttpServletRequest request;

    @Resource
    private UserService userService;

    @Resource
    private MessageSource messageSource;

    @Resource
    private MenuServiceImpl menuService;

    @GetMapping
    public Result getMenu(){
        ArrayNode publicUnitMenus = menuService.getPublicUnitMenus();
        ArrayNode userUnitMenus = menuService.getUserUnitMenus(userService.getUserId(request));
        Result success = Result.success();
        ObjectNode jsonNodes = success.objectNode();
        jsonNodes.set("public", publicUnitMenus);
        jsonNodes.set("user", userUnitMenus);
        return success.setData(jsonNodes);
    }

    @DeleteMapping
    public Result deleteUnit(@RequestParam String uuid){
        String userId = userService.getUserId(request);
        ArrayNode userUnitMenus = menuService.getUserUnitMenus(userId);
        menuService.deleteUnitTest(userUnitMenus, uuid, userId);
        Result success = Result.success();
        ObjectNode jsonNodes = success.objectNode();
        jsonNodes.set("user", userUnitMenus);
        return success.setData(jsonNodes);
    }

    @PostMapping("/group")
    public Result addGroup(@RequestBody String groupName, @RequestParam String type) {
        Result success = Result.success();
        ObjectNode jsonNodes = success.objectNode();
        if (StringUtils.equals(type, "public")) {
            ArrayNode userUnitMenus = menuService.getPublicUnitMenus();
            ObjectNode newGroup = menuService.addGroup(groupName);
            userUnitMenus.add(newGroup);
            menuService.updateUserMenu(userUnitMenus, GableConfig.PUBLIC_PATH);
            jsonNodes.set("public", userUnitMenus);
        }else {
            String userId = userService.getUserId(request);
            ArrayNode userUnitMenus = menuService.getUserUnitMenus(userId);
            ObjectNode newGroup = menuService.addGroup(groupName);
            userUnitMenus.add(newGroup);
            menuService.updateUserMenu(userUnitMenus, userId);
            jsonNodes.set("user", userUnitMenus);
        }
        return success.setData(jsonNodes);
    }

    @PostMapping("/unit")
    public Result addUnit(@RequestBody String unitName,
                          @RequestParam String groupUuid,
                          @RequestParam String type) {
        String userId = userService.getUserId(request);
        ArrayNode userUnitMenus = menuService.getUserUnitMenus(userId);
        String newTestUuid = menuService.addUnit(userUnitMenus, unitName, groupUuid, type, userId);
        menuService.updateUserMenu(userUnitMenus, userId);
        Result success = Result.success();
        ObjectNode jsonNodes = success.objectNode();
        jsonNodes.set("user", userUnitMenus);
        return success.setData(jsonNodes);
    }
}
