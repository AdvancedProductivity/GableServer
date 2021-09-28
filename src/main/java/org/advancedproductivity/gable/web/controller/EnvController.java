package org.advancedproductivity.gable.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.web.entity.Result;
import org.advancedproductivity.gable.web.service.EnvService;
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
        boolean b = envService.addEnv(type, name, config);
        return Result.success(envService.getEnvConfigMenu());
    }

    @PutMapping()
    public Result update(@RequestParam String uuid, @RequestParam String name, @RequestBody ObjectNode config) {
        boolean b = envService.updateEnv(uuid, name, config);
        return Result.success(envService.getEnvConfigMenu());
    }
}
