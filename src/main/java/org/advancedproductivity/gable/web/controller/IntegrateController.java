package org.advancedproductivity.gable.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.web.entity.Result;
import org.advancedproductivity.gable.web.service.HistoryService;
import org.advancedproductivity.gable.web.service.IntegrateService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author zzq
 */
@RestController
@RequestMapping("/api/integrate")
public class IntegrateController {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private IntegrateService integrateService;

    @GetMapping
    public Result get() {
        JsonNode list = integrateService.list();
        return Result.success().setData(list);
    }

    @GetMapping("/detail")
    public Result getDetail(@RequestParam String uuid) {
        JsonNode list = integrateService.getOne(uuid);
        return Result.success().setData(list);
    }

    @PutMapping
    public Result addIntegrate(@RequestBody ArrayNode records, @RequestParam String name) {
        String uuid = integrateService.addIntegrate(records, name);
        return Result.success();
    }

    @DeleteMapping
    public Result deleteIntegrate(@RequestParam String uuid) {
        int c = integrateService.delete(uuid);
        return Result.success(String.valueOf(c));
    }

    @PostMapping
    public Result updateIntegrate(@RequestBody ArrayNode records,@RequestParam String uuid) {
        integrateService.updateIntegrate(records, uuid);
        return Result.success();
    }

    @Resource
    private HistoryService historyService;

    @PostMapping("/addHistory")
    public Result saveIntegrate(@RequestBody ArrayNode records, @RequestParam String uuid) {
        ObjectNode mapperObjectNode = objectMapper.createObjectNode();
        mapperObjectNode.set("detail", records);
        mapperObjectNode.put("createdAt", FORMAT.format(new Date()));
        int i = historyService.recordIntegrateTest(GableConfig.PUBLIC_PATH, uuid, mapperObjectNode.toPrettyString());
        return Result.success();
    }



    @GetMapping("/history")
    private Result getHistory(@RequestParam String uuid, @RequestParam Integer historyId,
                              @RequestParam(required = false) Boolean isPublic) {
        JsonNode node = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.INTEGRATE,
                uuid,
                UserDataType.HISTORY,
                historyId + ".json");
        return Result.success().setData(node);
    }

}
