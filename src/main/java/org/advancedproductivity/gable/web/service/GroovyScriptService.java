package org.advancedproductivity.gable.web.service;

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
}
