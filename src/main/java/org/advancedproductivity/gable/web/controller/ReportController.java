package org.advancedproductivity.gable.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.servlet.ServletOutputStream;
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
            variables.put("historyId", hisId);
            variables.put("uuid", uuid);
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
        view.addAttribute("historyId", hisId);
        view.addAttribute("uuid", uuid);
        return "DefaultTemplate";
    }
}
