package com.gateway.controlplane.controller;

import com.gateway.controlplane.entity.AdminUser;
import com.gateway.controlplane.service.ReferenceDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final ReferenceDataService referenceDataService;

    public UserController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @GetMapping
    public List<AdminUser> list() {
        return referenceDataService.findUsers();
    }
}
