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

package org.advancedproductivity.gable.web.controller.test;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


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
