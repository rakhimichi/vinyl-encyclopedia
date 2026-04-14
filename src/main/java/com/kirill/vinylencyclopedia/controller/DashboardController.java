package com.kirill.vinylencyclopedia.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.kirill.vinylencyclopedia.dto.DashboardStatsDto;
import com.kirill.vinylencyclopedia.service.DashboardService;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        DashboardStatsDto stats = dashboardService.getStatsForUser(username);

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("username", username);
        model.addAttribute("stats", stats);
        model.addAttribute("isAdmin", isAdmin);

        return "dashboard";
    }
}
