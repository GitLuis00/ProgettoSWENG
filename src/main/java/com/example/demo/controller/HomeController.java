package com.example.demo.controller;

        import com.example.demo.CustomUserDetails;
        import com.example.demo.model.User;
        import com.example.demo.model.PremiumUser;
        import com.example.demo.services.CustomUserDetailsService;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.stereotype.Controller;
        import org.springframework.ui.Model;
        import org.springframework.web.bind.annotation.GetMapping;

        import javax.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @GetMapping("/home")
    public String viewHomePage(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username != null) {
            CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(username);
            User user = userDetails.getUser(); // Assicurati che questo metodo esista in CustomUserDetails
            boolean isPremium = user instanceof PremiumUser;
            model.addAttribute("isPremium", isPremium);
        }
        return "home";
    }
}
