package com.kirill.vinylencyclopedia.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.kirill.vinylencyclopedia.service.AdminService;

@Controller
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/admin")
    public String showAdminPage(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("totalUsers", adminService.getTotalUsers());
        model.addAttribute("totalRecords", adminService.getTotalRecords());
        model.addAttribute("users", adminService.getAllUserSummaries());

        return "admin";
    }
}
