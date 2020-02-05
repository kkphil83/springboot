package com.springboot.microservices.sample.profile;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Profile(value="dev")
@PropertySource({"classpath:profiles/dev/application.yml"})
public class DevProfile {

}
