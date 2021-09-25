package org.advancedproductivity.gable.framework.urils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
public class GableFileUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static boolean saveFile(final String content, final String... names) {
        try {
            File file = FileUtils.getFile(names);
            FileUtils.write(file, content, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            log.error("error happens while write str to file: {}", Arrays.toString(names), e);
        }
        return false;
    }

    public static String readFileAsString(final String... names) {
        File file = FileUtils.getFile(names);
        if (!file.exists()) {
            return null;
        }
        try {
            return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("read file as string errors: {}", Arrays.toString(names), e);
            return null;
        }
    }

    public static JsonNode readFileAsJson(final String... names) {
        File file = FileUtils.getFile(names);
        if (!file.exists()) {
            return null;
        }
        try {
            return mapper.readTree(file);
        } catch (Exception e) {
            log.error("read file as json errors: {}", Arrays.toString(names), e);
            return null;
        }
    }
}
