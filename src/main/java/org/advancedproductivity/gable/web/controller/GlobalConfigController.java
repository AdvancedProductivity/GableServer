package org.advancedproductivity.gable.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.core.GlobalVar;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.web.entity.Result;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;

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
