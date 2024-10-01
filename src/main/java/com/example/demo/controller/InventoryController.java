package com.example.demo.controller;

import com.example.demo.model.Inventory;
import com.example.demo.services.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/inventories")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public String listInventories(Model model) {
        List<Inventory> inventories = inventoryService.findAll();
        model.addAttribute("inventories", inventories);
        return "inventory_list";
    }

    @GetMapping("/new")
    public String showNewInventoryForm(Model model) {
        model.addAttribute("inventory", new Inventory());
        return "inventory_form";
    }

    @PostMapping
    public String saveInventory(@ModelAttribute("inventory") Inventory inventory) {
        inventoryService.save(inventory);
        return "redirect:/inventories";
    }

    @GetMapping("/edit/{id}")
    public String showEditInventoryForm(@PathVariable("id") Long id, Model model) {
        Optional<Inventory> inventory = inventoryService.findById(id);
        inventory.ifPresent(value -> model.addAttribute("inventory", value));
        return "inventory_form";
    }

    @PostMapping("/update/{id}")
    public String updateInventory(@PathVariable("id") Long id, @ModelAttribute("inventory") Inventory inventory) {
        inventory.setId(id);
        inventoryService.save(inventory);
        return "redirect:/inventories";
    }

    @GetMapping("/delete/{id}")
    public String deleteInventory(@PathVariable("id") Long id) {
        inventoryService.deleteById(id);
        return "redirect:/inventories";
    }
    @GetMapping("/view/{id}")
    public String viewInventory(@PathVariable("id") Long id, Model model) {
        Optional<Inventory> inventory = inventoryService.findById(id);
        if (inventory.isPresent()) {
            model.addAttribute("inventory", inventory.get());
            model.addAttribute("items", inventory.get().getItems());
            return "inventory_view";
        } else {
            return "redirect:/inventories";
        }
    }

}
