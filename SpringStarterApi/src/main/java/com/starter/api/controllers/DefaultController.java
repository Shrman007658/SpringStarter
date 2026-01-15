package com.starter.api.controllers;

import com.starter.api.common.dto.DefaultRequest;
import com.starter.api.common.dto.DefaultResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
@RestController
@RequestMapping("/v1/api")
public class DefaultController {

    @PostMapping("/example")
    public DefaultResponse postDefaultResponse( DefaultRequest request){
        DefaultResponse defaultResponse =
         DefaultResponse.builder()
                .uuid(UUID.randomUUID())
                .status("SUCCESS")
                .build();
        return defaultResponse;
    }
}
