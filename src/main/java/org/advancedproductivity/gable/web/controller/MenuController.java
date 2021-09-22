package org.advancedproductivity.gable.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.framework.core.HttpMethodType;
import org.advancedproductivity.gable.framework.urils.TestConfigGenerate;
import org.advancedproductivity.gable.web.entity.Result;
import org.advancedproductivity.gable.web.service.UserService;
import org.advancedproductivity.gable.web.service.impl.MenuServiceImpl;
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

    @PostMapping("/group")
    public Result addGroup(@RequestBody String groupName) {
        String userId = userService.getUserId(request);
        ArrayNode userUnitMenus = menuService.getUserUnitMenus(userId);
        ObjectNode newGroup = menuService.addGroup(groupName);
        userUnitMenus.add(newGroup);
        menuService.updateUserMenu(userUnitMenus, userId);
        Result success = Result.success();
        ObjectNode jsonNodes = success.objectNode();
        jsonNodes.set("user", userUnitMenus);
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
