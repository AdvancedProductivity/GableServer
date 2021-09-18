package org.advancedproductivity.gable.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.groovy.GroovyType;
import org.advancedproductivity.gable.web.service.GroovyScriptService;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author zzq
 */
@Service
@Slf4j
public class GroovyScriptServiceImpl implements GroovyScriptService {
    @Override
    public String getSampleScript(String namespace) {
        File file = FileUtils.getFile(GableConfig.getGablePath(), namespace, UserDataType.GROOVY, GroovyType.SAMPLE_TYPE);
        String content;
        if (!file.exists()) {
            content = "def a = 10\nassert a == 100";
            log.info("generate default groovy code for {}", namespace);
            try {
                FileUtils.write(file, content, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("save sample groovy code error", e);
            }
        }else {
            try {
                content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("read sample groovy code error", e);
                content = "read sample groovy code error" + e.getMessage();
            }
        }
        return content;
    }

    @Override
    public boolean saveSampleScript(String namespace, String scriptContent) {
        File file = FileUtils.getFile(GableConfig.getGablePath(), namespace, UserDataType.GROOVY, GroovyType.SAMPLE_TYPE);
        try {
            FileUtils.write(file, scriptContent, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            log.error("save sample script failed", e);
        }
        return false;
    }
}
