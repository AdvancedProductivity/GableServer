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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.FileCenterField;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.web.entity.Result;
import org.advancedproductivity.gable.web.service.FileCenterService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * @author zzq
 */
@RestController
@RequestMapping("/api/fileCenter")
@Slf4j
public class FileCenterController {
    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private FileCenterService fileCenterService;

    @GetMapping
    public Result get(){
        return Result.success(fileCenterService.list());
    }

    @DeleteMapping
    public Result delete(@RequestParam String uuid){
        return Result.success(String.valueOf(fileCenterService.delete(uuid)));
    }

    @GetMapping("/file")
    public void download(@RequestParam String uuid, HttpServletResponse response) {
        JsonNode list = fileCenterService.list();
        if (list == null) {
            writeError(response);
            return;
        }
        try {
            File file = null;
            String fileName = "";
            for (JsonNode jsonNode : list) {
                if (StringUtils.equals(jsonNode.path(FileCenterField.UUID).asText(), uuid)) {
                    String path = jsonNode.path(FileCenterField.PATH).asText();
                    file = FileUtils.getFile(GableConfig.getGablePath(), UserDataType.FILE_CENTER, path);
                    fileName = jsonNode.path(FileCenterField.NAME).asText();
                    break;
                }
            }
            if (file == null || !file.exists()) {
                writeError(response);
                return;
            }
            fileName += "_" + file.getName();
            response.setHeader("Content-type", "application/file");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            response.getOutputStream().write(FileUtils.readFileToByteArray(file));
        } catch (Exception e) {
            log.error("error happens while write file", e);
            writeError(response);
        }
    }

    private void writeError(HttpServletResponse response) {
        try {
            response.setHeader("Content-type", "text/html");
            response.getOutputStream().write("<h1>file not found :)</h1>".getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("error while write file", e);
        }
    }

    @PostMapping
    public Result post(
            @RequestParam(value = "file") MultipartFile file,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String tag) {
        if (StringUtils.isEmpty(name)) {
            name = file.getOriginalFilename();
        }
        String saveFileName = null;
        String fileSuffix = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
        if (!StringUtils.isEmpty(tag)) {
            saveFileName = tag + "/" + System.currentTimeMillis() + "." + fileSuffix;
        }else {
            saveFileName =  System.currentTimeMillis() + "." + fileSuffix;
        }
        try {
            File dest = FileUtils.getFile(GableConfig.getGablePath(), UserDataType.FILE_CENTER, saveFileName);
            FileUtils.copyInputStreamToFile(file.getInputStream(), dest);
            ObjectNode fileInfo = objectMapper.createObjectNode();
            String uuid = UUID.randomUUID().toString();
            fileInfo.put(FileCenterField.UUID, uuid);
            fileInfo.put(FileCenterField.NAME, name);
            fileInfo.put(FileCenterField.TAG, tag);
            fileInfo.put(FileCenterField.PATH, saveFileName);
            fileCenterService.addFile(fileInfo);
            return Result.success(fileInfo);
        } catch (Exception e) {
            log.error("error happens while save file", e);
            return Result.error(e.getMessage());
        }
    }
}
