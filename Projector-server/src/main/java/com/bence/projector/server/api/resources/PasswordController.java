package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.UserRegisterDTO;
import com.bence.projector.server.api.ResponseMessage;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.service.UserService;
import com.bence.projector.server.mailsending.MailSenderService;
import com.bence.projector.server.utils.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.UUID;

@Controller
public class PasswordController {
    public static final long TOKEN_DURATION = 1000 * 60 * 30;
    private static final Logger logger = LoggerFactory.getLogger(PasswordController.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private UserService userService;
    @Autowired
    private MailSenderService mailSenderService;

    @RequestMapping(method = RequestMethod.POST, value = "/forgotten_password")
    public ResponseEntity<Object> tokenRequesting(@RequestParam String email) {

        if (email != null) {
            User user = userService.findByEmail(email);
            if (user != null) {

                user.setToken(UUID.randomUUID().toString());
                final Date expiryDate = new Date();
                expiryDate.setTime(expiryDate.getTime() + TOKEN_DURATION);
                user.setExpiryDate(expiryDate);
                userService.save(user);
                final String link = AppProperties.getInstance().baseUrl() + "/#/changePasswordByToken?email=" + user.getEmail() + "&token="
                        + user.getToken() + "&expiryDate=" + user.getExpiryDate().getTime();
                try {
                    mailSenderService.sendForgottenEmail(user.getEmail(), link);
                    return new ResponseEntity<>(new ResponseMessage("EMAIL_SUCCESSFULLY_SENT"), HttpStatus.ACCEPTED);
                } catch (MailSendException e) {
                    logger.error(e.getMessage());
                    return new ResponseEntity<>(new ResponseMessage("COULD_NOT_SEND_EMAIL"),
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }
        return new ResponseEntity<>(new ResponseMessage("INVALID_EMAIL_OR_USER"), HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/reset_password")
    public ResponseEntity<Object> resetPassword(@RequestBody UserRegisterDTO userRegisterDTO,
                                                @RequestParam String token) {
        if (userRegisterDTO != null && userRegisterDTO.getEmail() != null) {
            User user = userService.findByEmail(userRegisterDTO.getEmail());
            if (user != null) {
                if (user.getExpiryDate() != null) {
                    Date currentDate = new Date();
                    if (user.getExpiryDate().after(currentDate) && token.equals(user.getToken())) {
                        user.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));
                        user.setToken(null);
                        user.setExpiryDate(null);
                        userService.save(user);
                        return new ResponseEntity<>(new ResponseMessage("PASSWORD_CHANGED"), HttpStatus.ACCEPTED);
                    }
                    user.setToken(null);
                    user.setExpiryDate(null);
                    userService.save(user);
                }
                return new ResponseEntity<>(new ResponseMessage("TOKEN_EXPIRED_REQUIRE"), HttpStatus.NOT_ACCEPTABLE);
            }
        }
        return new ResponseEntity<>(new ResponseMessage("INVALID_EMAIL_OR_USER"), HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getTime")
    public ResponseEntity<String> getTime() {
        return new ResponseEntity<>(new Date().getTime() + "", HttpStatus.ACCEPTED);
    }
}
