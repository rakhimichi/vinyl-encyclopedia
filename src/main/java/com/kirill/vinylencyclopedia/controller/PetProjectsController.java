package com.kirill.vinylencyclopedia.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PetProjectsController {
    private static final String ACCESS = "narutoAccessGranted";
    private static final byte[] PASSWORD_HASH = HexFormat.of().parseHex(
            "84b10a5c6d113a40fad77d9d8d91b2517099c6a0246a8e0edf87e87a4b533f74");

    @GetMapping("/pet-projects")
    public String showPetProjects() {
        return "pet-projects";
    }

    @GetMapping("/pet-projects/naruto")
    public String showNarutoTracker(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute(ACCESS))
                ? "naruto-tracker" : "naruto-login";
    }

    @PostMapping("/pet-projects/naruto/unlock")
    public String unlockNaruto(@RequestParam String password, HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {
        if (matchesPassword(password)) {
            request.changeSessionId();
            request.getSession().setAttribute(ACCESS, true);
        } else {
            request.getSession().removeAttribute(ACCESS);
            redirectAttributes.addFlashAttribute("passwordError", "Incorrect password. Try again.");
        }
        return "redirect:/pet-projects/naruto";
    }

    private boolean matchesPassword(String password) {
        try {
            byte[] candidate = MessageDigest.getInstance("SHA-256")
                    .digest(password.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(PASSWORD_HASH, candidate);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
