package org.advancedproductivity.gable.web.controller.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * @author zzq
 */
@RestController
@RequestMapping("/api/test")
public class ApiTestController {

    @PostMapping("/add")
    private Double add(@RequestParam Double a, @RequestParam Double b) {
        return a + b;
    }

    @PostMapping("/subtract")
    private Double subtract (@RequestParam Double a, @RequestParam Double b) {
        return a - b;
    }

    @PostMapping("/multiply")
    private Double multiply (@RequestParam Double a, @RequestParam Double b) {
        return a * b;
    }

    @PostMapping("/divide")
    private BigDecimal divide (@RequestParam Double a, @RequestParam Integer b) {
        return NumberUtils.toScaledBigDecimal(a / (double) b, 4, RoundingMode.HALF_DOWN);
    }

    @GetMapping("/testGetString")
    public String getString(@RequestParam(required = false) String content) {
        return "Hello " + (StringUtils.isEmpty(content) ? " world" : content);
    }

    @PostMapping("/testPostString")
    public String postString(@RequestBody String content) {
        return "Hello " + (StringUtils.isEmpty(content) ? " world" : content);
    }

    @PostMapping("/PostJson")
    public ObjectNode postJson(@RequestBody ObjectNode json) {
        ObjectNode result = json.objectNode();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        result.put("nowIs", dateFormat.format(new Date()));
        result.set("receive", json);
        return result;
    }
}
