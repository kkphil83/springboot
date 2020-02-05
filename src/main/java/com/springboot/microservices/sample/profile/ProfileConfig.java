package com.springboot.microservices.sample.profile;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({ DevProfile.class, ProdProfile.class})
@Configuration
public class ProfileConfig {

}
