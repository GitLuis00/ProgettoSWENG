package com.example.demo.controller;

import com.example.demo.model.PaymentRequest;
import com.example.demo.model.PaymentResponse;
import com.example.demo.model.PremiumUser;
import com.example.demo.model.User;
import com.example.demo.repository.PremiumUserRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.regex.Pattern;

@Controller
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PremiumUserRepository premiumUserRepository;

    @Autowired
    private PaymentService paymentService;

    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("\\d{16}");
    private static final Pattern CVV_PATTERN = Pattern.compile("\\d{3}");

    @GetMapping("/")
    public String viewHomePage() {
        return "index";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "signup_form";
    }

    @PostMapping("/registration")
    public String processRegistration(
            @RequestParam("userType") String userType,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("firstName") String firstName,
            @RequestParam(value = "cardHolder", required = false) String cardHolder,
            @RequestParam(value = "cardNumber", required = false) String cardNumber,
            @RequestParam(value = "expireDate", required = false) String expireDate,
            @RequestParam(value = "cvv", required = false) Integer cvv,
            Model model) {

        if (userRepository.findByUsername(username) != null) {
            model.addAttribute("error", "Nome utente già in uso.");
            return "signup_form";
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(password);

        if ("premium".equals(userType)) {
            if (!CARD_NUMBER_PATTERN.matcher(cardNumber).matches()) {
                model.addAttribute("error", "Il numero della carta deve essere di 16 cifre.");
                return "signup_form";
            }
            if (cvv == null || !CVV_PATTERN.matcher(cvv.toString()).matches()) {
                model.addAttribute("error", "Il CVV deve essere di 3 cifre.");
                return "signup_form";
            }

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setAmount(10.0);
            paymentRequest.setCardHolder(cardHolder);
            paymentRequest.setCardNumber(cardNumber);
            paymentRequest.setExpireDate(expireDate);
            paymentRequest.setCvv(cvv);

            PaymentResponse paymentResponse = paymentService.processPayment(paymentRequest);

            if (!"Autorizzato".equals(paymentResponse.getStatus())) {
                model.addAttribute("error", "Pagamento fallito: " + paymentResponse.getMessage());
                return "signup_form";
            }

            PremiumUser premiumUser = new PremiumUser();
            premiumUser.setUsername(username);
            premiumUser.setPassword(encodedPassword);
            premiumUser.setEmail(email);
            premiumUser.setFirstName(firstName);
            premiumUser.setCardHolder(cardHolder);
            premiumUser.setCardNumber(cardNumber);
            premiumUser.setExpireDate(expireDate);
            premiumUser.setCvv(cvv);
            premiumUser.setPremium(true);
            premiumUserRepository.save(premiumUser);
        } else {
            User user = new User();
            user.setUsername(username);
            user.setPassword(encodedPassword);
            user.setEmail(email);
            user.setFirstName(firstName);
            userRepository.save(user);
        }

        return "register_success";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> listAllUsers = userRepository.findAll();
        model.addAttribute("list", listAllUsers);
        return "users_list";
    }

    @GetMapping("/login")
    public String userLogin() {
        return "login";
    }
    @GetMapping("/premium")
    public String showPremiumForm(Model model) {
        return "premium_form";
    }

    @PostMapping("/premium")
    public String upgradeToPremium(
            @RequestParam("cardHolder") String cardHolder,
            @RequestParam("cardNumber") String cardNumber,
            @RequestParam("expireDate") String expireDate,
            @RequestParam("cvv") int cvv,
            Model model) {

        // Ottieni l'utente corrente
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username);

        if (user == null) {
            model.addAttribute("error", "User not found");
            return "premium_form";
        }

        // Esegui il pagamento
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(10.0); // Importo fisso per l'upgrade premium
        paymentRequest.setCardHolder(cardHolder);
        paymentRequest.setCardNumber(cardNumber);
        paymentRequest.setExpireDate(expireDate);
        paymentRequest.setCvv(cvv);

        PaymentResponse paymentResponse = paymentService.processPayment(paymentRequest);

        if (!"Autorizzato".equals(paymentResponse.getStatus())) {
            model.addAttribute("error", "Payment failed: " + paymentResponse.getMessage());
            return "premium_form";
        }

        // Converti l'utente standard in un utente premium
        PremiumUser premiumUser = new PremiumUser();
        premiumUser.setUsername(user.getUsername());
        premiumUser.setPassword(user.getPassword());
        premiumUser.setEmail(user.getEmail());
        premiumUser.setFirstName(user.getFirstName());
        premiumUser.setPremium(true);
        premiumUser.setCardHolder(cardHolder);
        premiumUser.setCardNumber(cardNumber);
        premiumUser.setExpireDate(expireDate);
        premiumUser.setCvv(cvv);

        // Elimina l'utente esistente prima di salvare il nuovo PremiumUser
        userRepository.delete(user);
        userRepository.save(premiumUser);

        return "redirect:/home";
    }
}
