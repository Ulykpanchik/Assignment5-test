package org.example.ecomap.controller;

import org.example.ecomap.model.Item;
import org.example.ecomap.model.Partner;
import org.example.ecomap.model.User;
import org.example.ecomap.service.ItemService;
import org.example.ecomap.service.PartnerService;
import org.example.ecomap.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/ecomap")
public class EcoMapController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private UserService userService;

    private int currentUserId = 0;

    public int getCurrentUserId() {
        return currentUserId;
    }

    @GetMapping
    public String viewHomePage(Model model) {
        model.addAttribute("isLoggedIn", currentUserId != 0);
        return "index";
    }

    // Регистрация
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("isLoggedIn", currentUserId != 0);
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        if (userService.findByUsername(user.getUsername()) != null) {
            model.addAttribute("error", "Username already exists");
            model.addAttribute("isLoggedIn", currentUserId != 0);
            return "register";
        }
        if (userService.findByEmail(user.getEmail()) != null) {
            model.addAttribute("error", "Email already exists");
            model.addAttribute("isLoggedIn", currentUserId != 0);
            return "register";
        }
        userService.saveUser(user);
        return "redirect:/ecomap/login";
    }

    // Логин
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        if (currentUserId != 0) {
            return "redirect:/ecomap/profile";
        }
        model.addAttribute("isLoggedIn", false);
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username, @RequestParam String password, Model model) {
        User user = userService.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            model.addAttribute("error", "Invalid username or password");
            model.addAttribute("isLoggedIn", false);
            return "login";
        }
        currentUserId = user.getId();
        return "redirect:/ecomap/profile";
    }

    @GetMapping("/logout")
    public String logout() {
        currentUserId = 0;
        return "redirect:/ecomap/login";
    }

    @GetMapping("/profile")
    public String viewProfile(Model model) {
        if (currentUserId == 0) {
            model.addAttribute("error", "Please login to view your profile");
            model.addAttribute("isLoggedIn", false);
            return "login";
        }
        User user = userService.findById(currentUserId);
        if (user == null) {
            model.addAttribute("error", "User not found");
            model.addAttribute("isLoggedIn", false);
            return "login";
        }
        List<Item> userItems = itemService.getItemsByUserEmail(user.getEmail()); // Обновленный метод
        model.addAttribute("user", user);
        model.addAttribute("items", userItems);
        model.addAttribute("isLoggedIn", true);
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String editProfileForm(Model model) {
        if (currentUserId == 0) {
            model.addAttribute("error", "Please login to edit your profile");
            model.addAttribute("isLoggedIn", false);
            return "login";
        }
        User user = userService.findById(currentUserId);
        if (user == null) {
            model.addAttribute("error", "User not found");
            model.addAttribute("isLoggedIn", false);
            return "login";
        }
        model.addAttribute("user", user);
        model.addAttribute("isLoggedIn", true);
        return "profile-edit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@ModelAttribute User updatedUser, Model model) {
        if (currentUserId == 0) {
            model.addAttribute("error", "Please login to edit your profile");
            model.addAttribute("isLoggedIn", false);
            return "login";
        }
        User user = userService.findById(currentUserId);
        if (user == null) {
            model.addAttribute("error", "User not found");
            model.addAttribute("isLoggedIn", false);
            return "login";
        }
        user.setEmail(updatedUser.getEmail());
        userService.updateUser(user);
        return "redirect:/ecomap/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String newPassword, Model model) {
        if (currentUserId == 0) {
            model.addAttribute("error", "Please login to change your password");
            model.addAttribute("isLoggedIn", false);
            return "login";
        }
        User user = userService.findById(currentUserId);
        if (user == null) {
            model.addAttribute("error", "User not found");
            model.addAttribute("isLoggedIn", false);
            return "login";
        }
        user.setPassword(newPassword);
        userService.updateUser(user);
        return "redirect:/ecomap/profile";
    }

    @GetMapping("/items")
    public String viewItems(@RequestParam(required = false) String category, Model model) {
        List<Item> items = category != null ? itemService.getItemsByCategory(category) : itemService.getItems();
        model.addAttribute("items", items);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("isLoggedIn", currentUserId != 0);
        return "item-exchange";
    }

    @GetMapping("/items/{id}")
    public String viewItemDetails(@PathVariable int id, Model model) {
        Item item = itemService.getItemById(id);
        if (item == null) {
            return "redirect:/ecomap/items";
        }

        model.addAttribute("item", item);
        model.addAttribute("isLoggedIn", currentUserId != 0);

        // Проверяем, может ли текущий пользователь редактировать этот предмет
        boolean canEdit = false;
        if (currentUserId != 0) {
            User currentUser = userService.findById(currentUserId);
            if (currentUser != null && currentUser.getEmail().equals(item.getUserEmail())) {
                canEdit = true;
            }
        }
        model.addAttribute("canEdit", canEdit);

        return "item-details";
    }
    // Форма создания объявления
    @GetMapping("/items/new")
    public String showItemForm(Model model) {
        if (currentUserId == 0) {
            model.addAttribute("error", "Please login to create an item");
            model.addAttribute("isLoggedIn", false);
            return "login";
        }
        User user = userService.findById(currentUserId);
        if (user == null) {
            model.addAttribute("error", "User not found");
            model.addAttribute("isLoggedIn", false);
            return "login";
        }
        Item item = new Item();
        item.setUserEmail(user.getEmail()); // Устанавливаем email текущего пользователя
        model.addAttribute("item", item);
        model.addAttribute("isLoggedIn", true);
        return "item-form";
    }

    // Создание объявления
    @PostMapping("/items")
    public String createItem(@ModelAttribute Item item, Model model) {
        if (currentUserId == 0) {
            model.addAttribute("error", "Please login to create an item");
            model.addAttribute("isLoggedIn", false);
            return "login";
        }
        User user = userService.findById(currentUserId);
        if (user == null) {
            model.addAttribute("error", "User not found");
            model.addAttribute("isLoggedIn", false);
            return "login";
        }
        item.setUserEmail(user.getEmail()); // Устанавливаем email текущего пользователя
        itemService.saveItem(item);
        return "redirect:/ecomap/items";
    }

    // Форма редактирования объявления
    @GetMapping("/items/edit/{id}")
    public String editItemForm(@PathVariable int id, Model model) {
        if (currentUserId == 0) {
            model.addAttribute("error", "Please login to edit an item");
            model.addAttribute("isLoggedIn", false);
            return "login";
        }
        
        User user = userService.findById(currentUserId);
        if (user == null) {
            model.addAttribute("error", "User not found");
            model.addAttribute("isLoggedIn", false);
            return "login";
        }
        
        Item item = itemService.getItemById(id);
        if (item == null || !item.getUserEmail().equals(user.getEmail())) {
            return "redirect:/ecomap/items";
        }
        
        model.addAttribute("item", item);
        model.addAttribute("isLoggedIn", true);
        return "item-form";
    }

    @PostMapping("/items/edit/{id}")
    public String updateItem(@PathVariable int id, @ModelAttribute Item item, Model model) {
        if (currentUserId == 0) {
            model.addAttribute("error", "Please login to edit an item");
            model.addAttribute("isLoggedIn", false);
            return "login";
        }
        User user = userService.findById(currentUserId);
        if (user == null) {
            model.addAttribute("error", "User not found");
            model.addAttribute("isLoggedIn", false);
            return "login";
        }
        Item existingItem = itemService.getItemById(id);
        if (existingItem == null || !Objects.equals(existingItem.getUserEmail(), user.getEmail())) {
            return "redirect:/ecomap/items";
        }
        item.setId(id);
        item.setUserEmail(user.getEmail()); // Устанавливаем email текущего пользователя
        itemService.saveItem(item);
        return "redirect:/ecomap/items";
    }

    // API для объявлений
    @GetMapping("/api/items")
    @ResponseBody
    public List<Item> getItemsApi(@RequestParam(required = false) String category) {
        return category != null ? itemService.getItemsByCategory(category) : itemService.getItems();
    }

    @PostMapping("/api/items")
    @ResponseBody
    public Item createItemApi(@RequestBody Item item) {
        return itemService.saveItem(item);
    }

    // Партнеры
    @GetMapping("/partners")
    public String viewPartners(Model model) {
        List<Partner> partners = partnerService.getPartners();
        model.addAttribute("partners", partners);
        model.addAttribute("isLoggedIn", currentUserId != 0);
        if (currentUserId != 0) {
            User currentUser = userService.findById(currentUserId);
            model.addAttribute("user", currentUser);
        }
        return "partner-list";
    }

    @GetMapping("/partners/new")
    public String showPartnerForm(Model model) {
        User currentUser = userService.findById(currentUserId);
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/ecomap/partners";
        }
        model.addAttribute("partner", new Partner());
        model.addAttribute("isLoggedIn", currentUserId != 0);
        return "partner-form";
    }

    @PostMapping("/partners")
    public String createPartner(@ModelAttribute Partner partner) {
        User currentUser = userService.findById(currentUserId);
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/ecomap/partners";
        }
        partnerService.savePartner(partner);
        return "redirect:/ecomap/partners";
    }

    @GetMapping("/partners/edit/{id}")
    public String editPartnerForm(@PathVariable int id, Model model) {
        User currentUser = userService.findById(currentUserId);
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/ecomap/partners";
        }
        Optional<Partner> partnerOptional = partnerService.getPartnerById(id);
        if (partnerOptional.isPresent()) {
            model.addAttribute("partner", partnerOptional.get());
            model.addAttribute("isLoggedIn", currentUserId != 0);
            return "partner-form";
        }
        return "redirect:/ecomap/partners";
    }

    @PostMapping("/partners/edit/{id}")
    public String updatePartner(@PathVariable int id, @ModelAttribute Partner partner) {
        User currentUser = userService.findById(currentUserId);
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/ecomap/partners";
        }
        partner.setId(id);
        partnerService.savePartner(partner);
        return "redirect:/ecomap/partners";
    }

    @GetMapping("/api/partners")
    @ResponseBody
    public List<Partner> getPartnersApi() {
        return partnerService.getPartners();
    }

    @PostMapping("/api/partners")
    @ResponseBody
    public Partner createPartnerApi(@RequestBody Partner partner) {
        return partnerService.savePartner(partner);
    }

    @GetMapping("/items/delete/{id}")
    public String deleteItem(@PathVariable int id, Model model) {
        if (currentUserId == 0) {
            model.addAttribute("error", "Please login to delete an item");
            model.addAttribute("isLoggedIn", false);
            return "login";
        }
        
        User user = userService.findById(currentUserId);
        if (user == null) {
            model.addAttribute("error", "User not found");
            model.addAttribute("isLoggedIn", false);
            return "login";
        }
        
        Item item = itemService.getItemById(id);
        if (item != null && item.getUserEmail().equals(user.getEmail())) {
            itemService.deleteItem(id);
        }
        
        return "redirect:/ecomap/profile";
    }
}