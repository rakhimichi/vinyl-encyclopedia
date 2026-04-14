package com.kirill.vinylencyclopedia.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kirill.vinylencyclopedia.domain.AppUser;
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

    @GetMapping("/admin/users/{userId}/records")
    public String showUserRecordsAsAdmin(@PathVariable Long userId, Authentication authentication, Model model) {
        AppUser targetUser = adminService.getUserById(userId);

        // Admin gets a dedicated view of another user's collection.
        // This keeps the normal user flow untouched and makes the role separation clearer.
        model.addAttribute("username", authentication.getName());
        model.addAttribute("targetUser", targetUser);
        model.addAttribute("myCollectionRecords", adminService.getMyCollectionRecordsForUser(userId));
        model.addAttribute("wishlistRecords", adminService.getWishlistRecordsForUser(userId));

        return "admin-user-records";
    }

    @PostMapping("/admin/records/{recordId}/delete")
    public String deleteRecordAsAdmin(
            @PathVariable Long recordId,
            @RequestParam("userId") Long userId
    ) {
        adminService.deleteRecordAsAdmin(recordId);
        return "redirect:/admin/users/" + userId + "/records";
    }
}
