package org.advancedproductivity.gable.web.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.FileCenterField;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.web.service.FileCenterService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author zzq
 */
@Service
@Slf4j
public class FileCenterServiceImpl implements FileCenterService {
    private static final String FILE_CENTER_NAME = "file_center.json";

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void addFile(ObjectNode fileInfo) {
        ArrayNode list = list();
        if (list == null) {
            list = objectMapper.createArrayNode();
        }
        list.add(fileInfo);
        GableFileUtils.saveFile(list.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, FILE_CENTER_NAME);
    }

    @Override
    public ArrayNode list() {
        JsonNode jsonNode = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, FILE_CENTER_NAME);
        if (jsonNode == null) {
            return null;
        }
        return (ArrayNode) jsonNode;
    }

    @Override
    public int delete(String uuid) {
        ArrayNode list = list();
        int count = 0;
        if (list == null || list.isEmpty()) {
            return count;
        }
        ArrayNode newArray = objectMapper.createArrayNode();
        for (int i = 0; i < list.size(); i++) {
            JsonNode item = list.get(i);
            if (StringUtils.equals(item.path(FileCenterField.UUID).asText(), uuid)) {
                File waitForDelete = FileUtils.getFile(GableConfig.getGablePath(), UserDataType.FILE_CENTER, item.path(FileCenterField.PATH).asText());
                if (waitForDelete.exists()) {
                    FileUtils.deleteQuietly(waitForDelete);
                }
                count++;
                continue;
            }
            newArray.add(item);
        }
        GableFileUtils.saveFile(newArray.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, FILE_CENTER_NAME);
        return count;
    }
}
