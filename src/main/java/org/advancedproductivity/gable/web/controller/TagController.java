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
