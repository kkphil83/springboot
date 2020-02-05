package com.springboot.microservices.sample.profile;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Profile(value="prod")
@PropertySource({"classpath:profiles/prod/application.yml"})
public class ProdProfile {

}
