package org.advancedproductivity.gable.web.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.ConfigField;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.web.entity.Result;
import org.advancedproductivity.gable.web.service.CaseService;
import org.advancedproductivity.gable.web.service.EnvService;
import org.advancedproductivity.gable.web.service.UnitConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zzq
 */
@Service
@Slf4j
public class UnitConfigServiceImpl implements UnitConfigService {
    @Resource
    private EnvService envService;
    @Resource
    private CaseService caseService;

    @Override
    public JsonNode getConfig(String nameSpace, String uuid, String env, String caseId, Integer caseVersion) {
        JsonNode in = GableFileUtils.readFileAsJson(GableConfig.getGablePath(),
                nameSpace,
                UserDataType.UNIT,
                uuid,
                ConfigField.CONFIG_DEFINE_FILE_NAME);
        if (in == null) {
            return null;
        }
        in = in.deepCopy();
        if (!StringUtils.isEmpty(env)) {
            JsonNode envConfig = envService.getEnv(env);
            if (envConfig != null && !envConfig.isMissingNode()) {
                envService.handleConfig(in, envConfig);
            }
        }
        if (!StringUtils.isEmpty(caseId)) {
            ObjectNode caseDetail = caseService.getCase(nameSpace, uuid, caseVersion, caseId);
            if (caseDetail != null) {
                caseService.handleCase(in.path("config"), caseDetail);
            }
        }
        return in;
    }
}
