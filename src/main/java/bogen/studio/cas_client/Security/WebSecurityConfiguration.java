package bogen.studio.cas_client.Security;//package com.example.cas_cli.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration {
    private PasswordEncoder passwordEncoder;
    @Bean
    public PasswordEncoder passwordEncoder() {

        if (passwordEncoder == null)
            passwordEncoder = new BCryptPasswordEncoder(12);

        return passwordEncoder;
    }

}