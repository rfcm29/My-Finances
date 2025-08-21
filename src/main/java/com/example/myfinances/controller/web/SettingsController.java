package com.example.myfinances.controller.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    @GetMapping
    public String settings(Model model, Authentication authentication) {
        model.addAttribute("title", "Definições");
        model.addAttribute("username", authentication.getName());
        return "pages/settings/settings";
    }
}