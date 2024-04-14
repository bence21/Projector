package com.bence.projector.server;

import com.bence.projector.server.backend.service.UserService;
import com.bence.projector.server.mailsending.MailSenderService;
import com.bence.projector.server.utils.CreateAdmin;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner demo(UserService userService, MailSenderService mailSenderService) {
        try {
            mailSenderService.tryToSendAllPrevious();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (String... args) -> CreateAdmin.registerAdmin(userService);
    }
}
