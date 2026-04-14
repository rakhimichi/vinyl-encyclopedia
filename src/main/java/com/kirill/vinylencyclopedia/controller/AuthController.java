package com.kirill.vinylencyclopedia.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.kirill.vinylencyclopedia.dto.RegistrationFormDto;
import com.kirill.vinylencyclopedia.service.RegistrationService;

import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final RegistrationService registrationService;

    public AuthController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/login")
    public String showLoginPage(Authentication authentication) {
        // If the user is already logged in, there is no reason to keep him on the login page.
        if (isAuthenticated(authentication)) {
            return "redirect:/dashboard";
        }

        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationPage(Authentication authentication, Model model) {
        if (isAuthenticated(authentication)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("registrationForm", new RegistrationFormDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registrationForm") RegistrationFormDto registrationForm,
            BindingResult bindingResult,
            Model model
    ) {
        // Bean validation handles field-level checks such as empty values and email format.
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            registrationService.registerNewUser(registrationForm);
        } catch (IllegalArgumentException exception) {
            // Business validation errors, for example duplicate username, are shown back on the same page.
            model.addAttribute("registrationError", exception.getMessage());
            return "register";
        }

        return "redirect:/login?registered";
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
