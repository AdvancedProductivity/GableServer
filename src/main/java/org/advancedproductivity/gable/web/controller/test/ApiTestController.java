package org.advancedproductivity.gable.web.controller.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.framework.utils.DateFormatHolder;
import org.advancedproductivity.gable.web.entity.Result;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * provided for test different http api. Will not actually be used.
 * @author zzq
 */
@RestController
@RequestMapping("/api/test")
public class ApiTestController {

    @Resource
    private MessageSource messageSource;

    @Resource
    private ObjectMapper objectMapper;

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

    @GetMapping("/language")
    public String language() {
        return messageSource.getMessage("language", null, LocaleContextHolder.getLocale());
    }

    @GetMapping("/testDateQuery")
    public Result language(@RequestParam("date") String dateStr) {
        SimpleDateFormat format = DateFormatHolder.getInstance("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(dateStr);
        } catch (Exception e) {
            return Result.error("query param format error must be yyyy-MM-dd HH:mm:ss, but get " + dateStr);
        }
        long time = date.getTime();
        int count = RandomUtils.nextInt(10, 20);
        ArrayNode arrayNode = objectMapper.createArrayNode();
        long oneDay = 60L * 60 * 24 * 1000;
        for (int i = 0; i < count; i++) {
            int dayOffset = RandomUtils.nextInt(0, 10);
            int mills = RandomUtils.nextInt(256, 4096);
            arrayNode.add(objectMapper.createObjectNode()
                    .put("uuid", UUID.randomUUID().toString())
                    .put("dateCreated", format.format(new Date(time + oneDay * dayOffset + mills * 1000)))
                    .put("name", "MockData_" + i)
            );
        }
        Result success = Result.success(arrayNode);
        success.put("count", arrayNode.size());
        return success;
    }

}
