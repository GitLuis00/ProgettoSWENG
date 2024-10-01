package com.example.demo.controller;

import com.example.demo.model.Item;
import com.example.demo.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public String listItems(Model model) {
        List<Item> items = itemService.findAll();
        model.addAttribute("items", items);
        return "item_list";
    }

    @GetMapping("/new")
    public String showNewItemForm(Model model) {
        Item item = new Item();
        model.addAttribute("item", item);
        return "item_form";
    }

    @PostMapping("/save")
    public String saveItem(@ModelAttribute("item") Item item, Model model) {
        itemService.save(item);
        model.addAttribute("item", new Item());
        model.addAttribute("successMessage", "Item saved successfully!");
        return "item_form";
    }
}

