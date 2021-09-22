package org.advancedproductivity.gable.web.controller.groovy;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.groovy.GroovyScriptUtils;
import org.advancedproductivity.gable.web.entity.Result;
import org.advancedproductivity.gable.web.service.GroovyScriptService;
import org.advancedproductivity.gable.web.service.UserService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author zzq
 */
@RestController
@RequestMapping("/api/SampleCode")
@Slf4j
public class SampleCodeRunController {

    @Resource
    private HttpServletRequest request;

    @Resource
    private UserService userService;

    @Resource
    private MessageSource messageSource;

    @Resource
    private GroovyScriptService groovyScriptService;

    @Resource
    private ObjectMapper objectMapper;

    @GetMapping
    private String getCode() {
        String userId = userService.getUserId(request);
        log.info("user id: {}", userId);
        return groovyScriptService.getSampleScript( userId);
    }

    @PostMapping
    private Result run(@RequestBody String codeContent) {
        String userId = userService.getUserId(request);
        boolean successSaveToFile = groovyScriptService.saveSampleScript(userId, codeContent);
        if (!successSaveToFile) {
            return Result.error();
        }
        try {
            Object o = GroovyScriptUtils.runSample(userId);
            if (o != null) {
                if (o instanceof String) {
                    return Result.success((String) o);
                }
                String s = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
                return Result.success(s);
            }else {
                return Result.success();
            }
        } catch (IOException e) {
            log.error("file error while run groovy sample code file", e);
            return Result.error(messageSource.getMessage("GroovySampleFileError", null, LocaleContextHolder.getLocale()));
        }

    }

}
