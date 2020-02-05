package com.springboot.microservices.sample.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class EnvPropCheck {
	
//    @Value("${spring.profile.value}")
//    private String profile;
	
//    @GetMapping("/ping")
//    public ResponseEntity<String> ping() {
//        return new ResponseEntity<>(profile, HttpStatus.OK);
//    }	

}
