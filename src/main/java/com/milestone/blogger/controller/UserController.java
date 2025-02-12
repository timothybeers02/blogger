package com.milestone.blogger.controller;

import com.milestone.blogger.model.User;
import com.milestone.blogger.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpSession;
import java.util.Date;
import java.util.Optional;

/**
 * Controller to handle user-related operations such as registration, login, and
 * logout.
 */
@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * Adds a "loggedIn" attribute to the model for templates to check the login
     * status of the user.
     *
     * @param session the current {@link HttpSession}.
     * @return true if the user is logged in, false otherwise.
     */
    @ModelAttribute("loggedIn")
    public Boolean addLoggedInAttribute(HttpSession session) {
        logger.info("Entering addLoggedInAttribute()");
        logger.info("Exiting addLoggedInAttribute()");
        return session.getAttribute("loggedIn") != null && (Boolean) session.getAttribute("loggedIn");
    }

    /**
     * Displays the registration page.
     *
     * @return the name of the Thymeleaf template for the registration page.
     */
    @GetMapping("/register")
    public String showRegistrationPage() {
        logger.info("showRegistrationPage(): Accessing Registration Page");
        return "register";
    }

    /**
     * Handles user registration by validating input and saving the user if valid.
     *
     * @param user  the {@link User} object populated from the registration form.
     * @param model the {@link Model} to pass attributes to the view.
     * @return the name of the Thymeleaf template to display, either the
     *         registration page on failure or the login page on success.
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        logger.info("Entering registerUser()");
        
        String username = user.getUsername();
        String email = user.getEmail();

        logger.info("Attempting to register user: {}", username);

        boolean usernameExists = userRepository.findByUsername(username).isPresent();
        boolean emailExists = userRepository.findByEmail(email).isPresent();

        if (usernameExists) {
            logger.warn("Registration failed: Username '{}' already exists.", username);
            model.addAttribute("usernameError", "Username '" + username + "' is already taken.");
        }
        if (emailExists) {
            logger.warn("Registration failed: Email '{}' already exists.", email);
            model.addAttribute("emailError", "Email '" + email + "' is already registered.");
        }
        if (usernameExists || emailExists) {
            logger.info("Exiting registerUser()");
            return "register";
        }

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setDatejoined(new Date());
        userRepository.save(user);
        logger.info("User registered successfully: {}", username);
        model.addAttribute("success", "User registered successfully. Please log in.");
        logger.info("Exiting registerUser()");
        return "login";
    }

    /**
     * Displays the login page.
     *
     * @return the name of the Thymeleaf template for the login page.
     */
    @GetMapping("/login")
    public String showLoginPage() {
        logger.info("showLoginPage(): Accessed login page.");
        return "login";
    }

    /**
     * Handles user login by validating credentials and setting the session
     * attributes.
     *
     * @param username the username entered by the user.
     * @param password the password entered by the user.
     * @param session  the current {@link HttpSession}.
     * @param model    the {@link Model} to pass attributes to the view.
     * @return a redirect to the homepage on success, or the login page on failure.
     */
    @PostMapping("/login")
    public String loginUser(@RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        logger.info("Entering loginUser()");
        logger.info("User login attempt: {}", username);
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty() || !passwordEncoder.matches(password, user.get().getPasswordHash())) {
            logger.warn("Login failed for username: {}", username);
            model.addAttribute("error", "Invalid username or password.");
            logger.info("Exiting loginUser()");
            return "login";
        }

        session.setAttribute("loggedIn", true);
        session.setAttribute("userId", user.get().getId());
        logger.info("User logged in successfully: {}", username);
        logger.info("Exiting loginUser()");
        return "redirect:/";
    }

    /**
     * Logs out the user by invalidating the session and redirecting to the
     * homepage.
     *
     * @param session the current {@link HttpSession}.
     * @return a redirect to the homepage.
     */
    @GetMapping("/logout")
    public String logoutUser(HttpSession session) {
        logger.info("Entering logoutUser()");
        session.invalidate();
        logger.info("User logged out.");
        logger.info("Exiting logoutUser()");
        return "redirect:/";
    }
}
