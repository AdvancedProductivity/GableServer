package org.advancedproductivity.gable.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.groovy.GroovyType;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.web.service.GroovyScriptService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author zzq
 */
@Service
@Slf4j
public class GroovyScriptServiceImpl implements GroovyScriptService {
    @Override
    public String getSampleScript(String namespace) {
        String scriptContent = GableFileUtils.readFileAsString(GableConfig.getGablePath(), namespace, UserDataType.GROOVY, GroovyType.SAMPLE_TYPE);
        if (StringUtils.isEmpty(scriptContent)) {
            scriptContent = "def a = 10\nassert a == 100";
            GableFileUtils.saveFile(scriptContent, GableConfig.getGablePath(), namespace, UserDataType.GROOVY, GroovyType.SAMPLE_TYPE);
        }
        return scriptContent;
    }

    @Override
    public boolean saveSampleScript(String namespace, String scriptContent) {
        return GableFileUtils.saveFile(scriptContent, GableConfig.getGablePath(), namespace, UserDataType.GROOVY, GroovyType.SAMPLE_TYPE);
    }
}
