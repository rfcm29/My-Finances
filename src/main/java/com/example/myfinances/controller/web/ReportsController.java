package com.example.myfinances.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reports")
public class ReportsController {

    @GetMapping
    public String reports(Model model) {
        model.addAttribute("title", "Relatórios");
        return "pages/reports/overview";
    }

    @GetMapping("/monthly")
    public String monthlyReports(Model model) {
        model.addAttribute("title", "Relatórios Mensais");
        return "pages/reports/monthly";
    }

    @GetMapping("/yearly")
    public String yearlyReports(Model model) {
        model.addAttribute("title", "Relatórios Anuais");
        return "pages/reports/yearly";
    }
}