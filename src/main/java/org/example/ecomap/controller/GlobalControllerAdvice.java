package org.example.ecomap.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private EcoMapController ecoMapController;

    @ModelAttribute("isLoggedIn")
    public boolean isLoggedIn() {
        return ecoMapController.getCurrentUserId() != 0;
    }
} 