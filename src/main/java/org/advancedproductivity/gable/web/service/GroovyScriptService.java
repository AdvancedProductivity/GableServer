package org.advancedproductivity.gable.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.framework.config.GroovyScriptType;

/**
 * @author zzq
 */
public interface GroovyScriptService {

    /**
     * get the script code file content
     * @param namespace user's id
     * @return script content
     * */
    String getSampleScript(String namespace);

    /**
     * save sample code to file
     *
     * @param namespace     user's id
     * @param scriptContent code
     * @return is save success
     */
    boolean saveSampleScript(String namespace, String scriptContent);

    /**
     * get the script group menu by GroovyScriptType
     * @param type pre or post
     * @return an arrayNode
     * */
    JsonNode getScriptList(GroovyScriptType type);

    JsonNode addGroup(GroovyScriptType type, String groupName);

    boolean haveExist(GroovyScriptType type, String scriptName);

    JsonNode addItem(GroovyScriptType type, String groupUuid,  String scriptName, String code);

    String readCode(String uuid);

    void updateScript(String uuid, String code);

    JsonNode executePreScript(String uuid, ObjectNode in);
    JsonNode executePostScript(String uuid, ObjectNode in);

    String getUuidByName(String preScriptName, GroovyScriptType type);
}
