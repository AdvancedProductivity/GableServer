/*
 *  Copyright (c) 2021 AdvancedProductivity
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.advancedproductivity.gable.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.ConfigField;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zzq
 */
@Controller
@RequestMapping("/api/report")
@Slf4j
public class ReportController {
    private static final ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    private static final SpringTemplateEngine templateEngine = new SpringTemplateEngine();

    static {
        resolver.setPrefix("templates/");
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setSuffix(".html");
        templateEngine.setTemplateResolver(resolver);
    }

    @GetMapping
    public void get(@RequestParam String uuid,
                    @RequestParam Integer hisId,
                    @RequestParam String server,
                    HttpServletResponse response) {
        try {
            Context context = new Context();
            Map<String, Object> variables = new HashMap<>();
            variables.put("server", server);
            variables.put("canDownload", false);
            variables.put(ConfigField.HISTORY_ID, hisId);
            variables.put(ConfigField.UUID, uuid);
            context.setVariables(variables);
            response.setStatus(200);
            response.setHeader("Content-type", "application/file");
            response.setHeader("Content-Disposition", "attachment; filename=Report.html");
            OutputStream outputStream = response.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            templateEngine.process("DefaultTemplate", context, writer);
            writer.close();
        } catch (Exception e) {
            response.setStatus(500);
            response.setHeader("Content-type", "text/plain");
            try {
                response.getOutputStream().write(e.getMessage().getBytes(StandardCharsets.UTF_8));
            } catch (Exception exception) {
                log.error("error while write error msg", e);
            }
            log.error("error while export template", e);
        }
    }

    @GetMapping("preview")
    public String preview(@RequestParam String uuid,
                          @RequestParam Integer hisId,
                          @RequestParam String server,
                          Model view) {
        view.addAttribute("server", server);
        view.addAttribute("canDownload", true);
        view.addAttribute(ConfigField.HISTORY_ID, hisId);
        view.addAttribute(ConfigField.UUID, uuid);
        return "DefaultTemplate";
    }
}
