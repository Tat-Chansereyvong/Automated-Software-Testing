package com.cloudstore.controller;

import com.cloudstore.model.FileEntity;
import com.cloudstore.model.Folder;
import com.cloudstore.model.User;
import com.cloudstore.service.StorageService;
import com.cloudstore.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

/**
 * Serves the Thymeleaf web UI pages (login, register, dashboard).
 * The dashboard provides data-testid attributes for Playwright browser tests.
 */
@Controller
public class WebController {

    private final UserService userService;
    private final StorageService storageService;

    public WebController(UserService userService, StorageService storageService) {
        this.userService = userService;
        this.storageService = storageService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /** Handle registration form submission. */
    @PostMapping("/register")
    public String register(@RequestParam String email,
                           @RequestParam String password,
                           @RequestParam(required = false) String displayName) {
        userService.register(email, password, displayName);
        return "redirect:/login?registered";
    }

    /** Dashboard showing quota, files, and folders for the authenticated user. */
    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        User user = userService.findByEmail(principal.getName());
        List<Folder> folders = storageService.listFolders(user, null);
        List<FileEntity> files = storageService.listFiles(user, null);

        long usedMB = user.getUsedBytes() / (1024 * 1024);
        long totalMB = user.getQuotaBytes() / (1024 * 1024);
        long freeMB = totalMB - usedMB;
        int usagePercent = (int) ((user.getUsedBytes() * 100) / user.getQuotaBytes());

        model.addAttribute("user", user);
        model.addAttribute("folders", folders);
        model.addAttribute("files", files);
        model.addAttribute("usedMB", usedMB);
        model.addAttribute("totalMB", totalMB);
        model.addAttribute("freeMB", freeMB);
        model.addAttribute("usagePercent", usagePercent);

        return "dashboard";
    }
}
