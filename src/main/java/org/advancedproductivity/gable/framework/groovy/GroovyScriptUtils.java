package org.advancedproductivity.gable.framework.groovy;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zzq
 */
@Slf4j
public class GroovyScriptUtils {
    private static final ConcurrentHashMap<String, GroovyScriptEngine> engineMap = new ConcurrentHashMap<>();

    public static Object runSample(String namespace) throws IOException {
        File file = FileUtils.getFile(GableConfig.getGablePath(), namespace, UserDataType.GROOVY, GroovyType.SAMPLE_TYPE);
        if (!file.exists()) {
            return null;
        }
        GroovyScriptEngine engine = engineMap.get(namespace);
        if (engine == null) {
            engine = new GroovyScriptEngine(file.getAbsolutePath());
            engineMap.put(namespace, engine);
        }
        Binding binding = new Binding();
        try {
            return engine.run(GroovyType.SAMPLE_TYPE, binding);
        }catch (Throwable e) {
            System.out.println(e.getMessage());
            log.error("error happens while run sample groovy script", e);
            return e.getMessage();
        }
    }
}
