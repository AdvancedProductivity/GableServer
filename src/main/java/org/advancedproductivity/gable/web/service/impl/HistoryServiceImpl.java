package org.advancedproductivity.gable.web.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.web.entity.Result;
import org.advancedproductivity.gable.web.service.HistoryService;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zzq
 */
@Service
public class HistoryServiceImpl implements HistoryService {
    private static final ConcurrentHashMap<String, AtomicInteger> RECORDER = new ConcurrentHashMap<>();

    @Override
    public int recordUnitTest(String nameSpace, String uuid, String content) {
        AtomicInteger atomicInteger = RECORDER.get(uuid);
        int id = 0;
        if (atomicInteger == null) {
            File hisPath = FileUtils.getFile(GableConfig.getGablePath(), nameSpace, UserDataType.UNIT, uuid, UserDataType.HISTORY);
            if (hisPath.exists()) {
                String[] list = hisPath.list();
                if (list != null) {
                    id = list.length;
                }
            }
            atomicInteger = new AtomicInteger(id);
            RECORDER.put(uuid, atomicInteger);
        } else {
            id = atomicInteger.incrementAndGet();
        }
        GableFileUtils.saveFile(content, GableConfig.getGablePath(), nameSpace, UserDataType.UNIT, uuid, UserDataType.HISTORY, id + ".json");
        return id;
    }

    @Override
    public int recordGroovy(String nameSpace, String uuid, String content) {
        AtomicInteger atomicInteger = RECORDER.get(uuid);
        int id = 0;
        if (atomicInteger == null) {
            File hisPath = FileUtils.getFile(GableConfig.getGablePath(), nameSpace, UserDataType.GROOVY_HIS, uuid, UserDataType.HISTORY);
            if (hisPath.exists()) {
                String[] list = hisPath.list();
                if (list != null) {
                    id = list.length;
                }
            }
            atomicInteger = new AtomicInteger(id);
            RECORDER.put(uuid, atomicInteger);
        } else {
            id = atomicInteger.incrementAndGet();
        }
        GableFileUtils.saveFile(content, GableConfig.getGablePath(), nameSpace, UserDataType.GROOVY_HIS, uuid, UserDataType.HISTORY, id + ".json");
        return id;
    }

    @Override
    public int recordJsonSchemaStep(String nameSpace, String uuid, String content) {
        AtomicInteger atomicInteger = RECORDER.get(uuid);
        int id = 0;
        if (atomicInteger == null) {
            File hisPath = FileUtils.getFile(GableConfig.getGablePath(), nameSpace, UserDataType.JSON_SCHEMA_HIS,
                    uuid, UserDataType.HISTORY);
            if (hisPath.exists()) {
                String[] list = hisPath.list();
                if (list != null) {
                    id = list.length;
                }
            }
            atomicInteger = new AtomicInteger(id);
            RECORDER.put(uuid, atomicInteger);
        } else {
            id = atomicInteger.incrementAndGet();
        }
        GableFileUtils.saveFile(content, GableConfig.getGablePath(), nameSpace, UserDataType.JSON_SCHEMA_HIS,
                uuid, UserDataType.HISTORY, id + ".json");
        return id;
    }

    @Override
    public int recordIntegrateTest(String nameSpace, String uuid, String content) {
        AtomicInteger atomicInteger = RECORDER.get(uuid);
        int id = 0;
        if (atomicInteger == null) {
            File hisPath = FileUtils.getFile(GableConfig.getGablePath(), nameSpace, UserDataType.INTEGRATE, uuid, UserDataType.HISTORY);
            if (hisPath.exists()) {
                String[] list = hisPath.list();
                if (list != null) {
                    id = list.length;
                }
            }
            atomicInteger = new AtomicInteger(id);
            RECORDER.put(uuid, atomicInteger);
        } else {
            id = atomicInteger.incrementAndGet();
        }
        GableFileUtils.saveFile(content, GableConfig.getGablePath(), nameSpace, UserDataType.INTEGRATE, uuid, UserDataType.HISTORY, id + ".json");
        return id;
    }
}
