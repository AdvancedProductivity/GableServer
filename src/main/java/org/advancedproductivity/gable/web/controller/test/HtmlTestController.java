package org.advancedproductivity.gable.web.controller.test;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


/**
 * provided for test different http page. Will not actually be used.
 *
 * @author zzq
 */
@Controller
@RequestMapping("/page/test")
public class HtmlTestController {
    @GetMapping("/add")
    private String add(Model view) {
        view.addAttribute("type", "add");
        return "Calculate";
    }

    @GetMapping("/subtract")
    private String subtract (Model view) {
        view.addAttribute("type", "subtract");
        return "Calculate";
    }

    @GetMapping("/multiply")
    private String multiply (Model view) {
        view.addAttribute("type", "multiply");
        return "Calculate";
    }

    @GetMapping("/divide")
    private String divide(Model view) {
        view.addAttribute("type", "divide");
        return "Calculate";
    }

}
