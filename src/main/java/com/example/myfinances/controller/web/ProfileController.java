package com.example.myfinances.controller.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @GetMapping
    public String profile(Model model, Authentication authentication) {
        model.addAttribute("title", "Perfil");
        model.addAttribute("username", authentication.getName());
        return "pages/profile/profile";
    }
}