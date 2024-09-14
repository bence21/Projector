package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.UserDTO;
import com.bence.projector.common.dto.UserRegisterDTO;
import com.bence.projector.server.api.assembler.UserAssembler;
import com.bence.projector.server.api.assembler.UserRegisterAssembler;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.service.ServiceException;
import com.bence.projector.server.backend.service.StatisticsService;
import com.bence.projector.server.backend.service.UserService;
import com.bence.projector.server.mailsending.ConfigurationUtil;
import com.bence.projector.server.mailsending.FreemarkerConfiguration;
import com.bence.projector.server.mailsending.MailSenderService;
import com.bence.projector.server.utils.AppProperties;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringWriter;
import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.bence.projector.server.api.resources.StatisticsResource.saveStatistics;
import static com.bence.projector.server.api.resources.util.UserPrincipalUtil.getUserFromPrincipalAndUserService;

@RestController
public class UserResource {

    private static final Logger logger = LoggerFactory.getLogger(UserResource.class);
    private final UserService userService;
    private final UserRegisterAssembler userRegisterAssembler;
    private final UserAssembler userAssembler;
    private final JavaMailSender sender;
    private final StatisticsService statisticsService;
    private final MailSenderService mailSenderService;

    @Autowired
    public UserResource(UserService userService, UserRegisterAssembler userRegisterAssembler, UserAssembler userAssembler, JavaMailSender sender, StatisticsService statisticsService, MailSenderService mailSenderService) {
        this.userService = userService;
        this.userRegisterAssembler = userRegisterAssembler;
        this.userAssembler = userAssembler;
        this.sender = sender;
        this.statisticsService = statisticsService;
        this.mailSenderService = mailSenderService;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/api/users")
    public ResponseEntity<Object> createUser(@RequestBody final UserRegisterDTO userRegisterDTO,
                                             @RequestParam String language, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        User user = null;
        try {
            if (userService.findByEmail(userRegisterDTO.getEmail()) != null) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            userRegisterDTO.setPreferredLanguage(language);
            user = userRegisterAssembler.createModel(userRegisterDTO);
            user.setActivated(false);
            user.setActivationCode(UUID.randomUUID().toString());
            user.setCreatedDate(new Date());
            user.setModifiedDate(user.getCreatedDate());
            ResponseEntity<Object> responseEntity = new ResponseEntity<>(
                    userAssembler.createDto(userService.registerUser(user)), HttpStatus.ACCEPTED);
            User finalUser = user;
            Thread thread = new Thread(() -> sendActivationEmail(finalUser));
            thread.start();
            return responseEntity;
        } catch (ServiceException e) {
            if (user != null) {
                return new ResponseEntity<>(e.getResponseMessage(), HttpStatus.CONFLICT);
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "admin/api/users", method = RequestMethod.GET)
    public List<UserDTO> getUsers() {
        List<User> all = userService.findAll();
        return userAssembler.createDtoList(all);
    }

    @RequestMapping(value = "admin/api/user/{id}", method = RequestMethod.GET)
    public UserDTO getUser(@PathVariable final String id, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        User user = userService.findOneByUuid(id);
        return userAssembler.createDto(user);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/admin/api/user/{id}")
    public ResponseEntity<Object> updateUserAsAdmin(@PathVariable final String id, @RequestBody final UserDTO userDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        User user = userService.findOneByUuid(id);
        if (user != null) {
            Date modifiedDate = user.getModifiedDate();
            if (isModifiedDate(modifiedDate, userDTO.getModifiedDate())) {
                return new ResponseEntity<>("Already modified", HttpStatus.CONFLICT);
            }
            user.setModifiedDate(new Date());
            userAssembler.updateModel(user, userDTO);
            final User savedUser = userService.save(user);
            if (savedUser != null) {
                return new ResponseEntity<>(userAssembler.createDto(user), HttpStatus.ACCEPTED);
            }
        } else {
            return new ResponseEntity<>("No user with this id", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>("Could not update", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean isModifiedDate(Date modifiedDate, Date newModifiedDate) {
        if (modifiedDate == null) {
            return newModifiedDate != null;
        }
        if (newModifiedDate == null) {
            return true;
        }
        return modifiedDate.compareTo(newModifiedDate) != 0;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/user/api/user/{uuid}")
    public ResponseEntity<Object> updateUser(@PathVariable final String uuid, @RequestBody final UserDTO userDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        User user = userService.findOneByUuid(uuid);
        if (user != null) {
            Date modifiedDate = user.getModifiedDate();
            if (isModifiedDate(modifiedDate, userDTO.getModifiedDate())) {
                return new ResponseEntity<>("Already modified", HttpStatus.CONFLICT);
            }
            user.setModifiedDate(new Date());
            user.setSurname(userDTO.getSurname());
            user.setFirstName(userDTO.getFirstName());
            final User savedUser = userService.save(user);
            if (savedUser != null) {
                return new ResponseEntity<>(userAssembler.createDto(user), HttpStatus.ACCEPTED);
            }
        } else {
            return new ResponseEntity<>("No user with this id", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>("Could not update", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void sendActivationEmail(User user) {
        try {
            logger.info("Got email: " + user.getEmail());
            sendJustActivationEmail(user);
            logger.info("Mail sent!");
            sendNewUserEmail(user);
        } catch (MessagingException | TemplateException | IOException | MailSendException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendJustActivationEmail(User user)
            throws MessagingException, IOException, TemplateException, MailSendException {
        String language = user.getPreferredLanguage();
        if (language == null || (!language.equals("hu") && !language.equals("ro") && !language.equals("en"))) {
            language = "en";
        }
        final String freemarkerName = FreemarkerConfiguration.FREEMARKER_NAME_REGISTRATION + language + ".html";
        Configuration config = ConfigurationUtil.getConfiguration();
        config.setDefaultEncoding("UTF-8");
        Template template = config.getTemplate(freemarkerName);

        StringWriter writer = new StringWriter();
        template.process(createPattern(user), writer);

        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(new InternetAddress(user.getEmail()));
        helper.setFrom(mailSenderService.getNoReplyInternetAddress());
        if (language.equals("hu")) {
            helper.setSubject("Aktiválás");
        } else if (language.equals("ro")) {
            helper.setSubject("Activare");
        } else {
            helper.setSubject("Activation");
        }
        helper.getMimeMessage().setContent(writer.toString(), "text/html;charset=utf-8");
        sender.send(message);
    }

    private void sendNewUserEmail(User user)
            throws MessagingException, IOException, TemplateException, MailSendException {
        final String freemarkerName = "newUser.html";
        Configuration config = ConfigurationUtil.getConfiguration();
        config.setDefaultEncoding("UTF-8");
        Template template = config.getTemplate(freemarkerName);

        StringWriter writer = new StringWriter();

        Map<String, Object> data = new HashMap<>();
        data.put("email", user.getEmail());
        data.put("surname", user.getSurname());
        data.put("firstName", user.getFirstName());
        template.process(data, writer);

        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        mailSenderService.setToAdmin(helper);
        helper.setFrom(mailSenderService.getNoReplyInternetAddress());
        helper.setSubject("New user");
        helper.getMimeMessage().setContent(writer.toString(), "text/html;charset=utf-8");
        sender.send(message);
    }

    private Map<String, Object> createPattern(User user) {
        Map<String, Object> data = new HashMap<>();
        AppProperties properties = AppProperties.getInstance();
        data.put("baseUrl", properties.baseUrl());
        data.put("shortBaseUrl", properties.shortBaseUrl());
        data.put("activationCode", user.getActivationCode());
        return data;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/api/user/activate")
    public ResponseEntity<Object> activate(Principal principal, @RequestParam String activationCode, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        if (principal != null) {
            String email = principal.getName();
            User user = userService.findByEmail(email);
            if (user != null) {
                if (user.isActivated()) {
                    return new ResponseEntity<>(HttpStatus.ACCEPTED);
                }
                if (user.getActivationCode().equals(activationCode)) {
                    user.setActivated(true);
                    user.setActivationCode(null);
                    userService.save(user);
                    return new ResponseEntity<>(HttpStatus.ACCEPTED);
                } else {
                    return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/api/sendActivation")
    public ResponseEntity<Object> sendActivation(Principal principal) {
        User user = getUserFromPrincipalAndUserService(principal, userService);
        if (user != null) {
            if (!user.isActivated()) {
                if (user.getActivationCode() == null) {
                    user.setActivationCode(UUID.randomUUID().toString());
                    userService.save(user);
                }
                Thread thread = new Thread(() -> {
                    try {
                        sendJustActivationEmail(user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
