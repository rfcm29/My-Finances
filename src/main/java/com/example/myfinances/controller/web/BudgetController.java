package com.example.myfinances.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/budgets")
public class BudgetController {

    @GetMapping
    public String budgets(Model model) {
        model.addAttribute("title", "Orçamentos");
        return "pages/budgets/list";
    }

    @GetMapping("/add")
    public String addBudget(Model model) {
        model.addAttribute("title", "Adicionar Orçamento");
        return "pages/budgets/add";
    }
}