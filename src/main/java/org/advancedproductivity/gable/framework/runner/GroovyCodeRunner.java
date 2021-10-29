package org.advancedproductivity.gable.framework.runner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.CodeField;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.core.TestType;
import org.advancedproductivity.gable.framework.groovy.GroovyScriptUtils;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zzq
 */
@Slf4j
public class GroovyCodeRunner implements TestAction {

    @Override
    public void execute(JsonNode in, JsonNode out, ObjectNode instance, ObjectNode global) {
        if (!in.isObject() || !out.isObject()) {
            return;
        }
        ObjectNode req = (ObjectNode) in;
        ObjectNode res = (ObjectNode) out;
        String userId = req.remove("userId").asText();
        if (StringUtils.isEmpty(userId)) {
            return;
        }
        String groovyTestUuid = req.remove(CodeField.GROOVY_TEST_UUID).asText();
        if (StringUtils.isEmpty(groovyTestUuid)) {
            return;
        }
        String groovyCode = req.remove(CodeField.GROOVY_TEST_CODE).asText();
        try {
            String testFile = groovyTestUuid + ".groovy";
            if (!StringUtils.isEmpty(groovyCode)) {
                boolean b = GableFileUtils.saveFile(groovyCode, GableConfig.getGablePath(), userId, UserDataType.GROOVY, testFile);
                if (!b) {
                    res.put("error", "save groovy code to file error");
                    return;
                }
            }
            GroovyScriptUtils.runTest(userId, req, res, testFile);
        } catch (Exception e) {
            res.put("error", e.getMessage());
            log.error("error happens while run groovy test", e);
        }
    }

    @Override
    public TestType getTestType() {
        return TestType.GROOVY_SCRIPT;
    }
}
