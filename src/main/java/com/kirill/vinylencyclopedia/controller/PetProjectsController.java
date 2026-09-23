package com.kirill.vinylencyclopedia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PetProjectsController {

    @GetMapping("/pet-projects")
    public String showPetProjects() {
        return "pet-projects";
    }

    @GetMapping("/pet-projects/naruto")
    public String showNarutoTracker() {
        return "naruto-tracker";
    }
}
