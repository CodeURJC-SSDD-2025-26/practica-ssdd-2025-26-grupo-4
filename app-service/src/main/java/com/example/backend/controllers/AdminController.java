package com.example.backend.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.services.AdminService;

@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/admin/admin-dashboard")
    public String dashboard(Model model) {
        model.addAllAttributes(adminService.getDashboardData());
        return "pages/admin/admin-dashboard";
    }

    @GetMapping("/admin/item-list")
    public String itemList(Model model) {
        model.addAttribute("productos", adminService.getActiveProducts());
        return "pages/admin/item-list";
    }

    @GetMapping("/admin/item-edit")
    public String itemEdit(@RequestParam(required = false) Long id, Model model) {
        if (id != null) {
            adminService.getProduct(id).ifPresent(p -> model.addAttribute("producto", p));
        }
        return "pages/admin/item-edit";
    }

    @PostMapping("/admin/item-create")
    public String createProduct(@RequestParam String nombre,
                                @RequestParam String descripcion,
                                @RequestParam String categoria,
                                @RequestParam double precio,
                                @RequestParam int stock,
                                @RequestParam(required = false) MultipartFile imageFile) throws IOException {

        adminService.createProduct(nombre, descripcion, categoria, precio, stock, imageFile);
        return "redirect:/admin/item-list";
    }

    @PostMapping("/admin/item-edit")
    public String editProduct(@RequestParam Long id,
                              @RequestParam String nombre,
                              @RequestParam String descripcion,
                              @RequestParam String categoria,
                              @RequestParam double precio,
                              @RequestParam int stock,
                              @RequestParam(required = false) MultipartFile imageFile) throws IOException {

        adminService.updateProduct(id, nombre, descripcion, categoria, precio, stock, imageFile);
        return "redirect:/admin/item-list";
    }

    @PostMapping("/admin/item-delete")
    public String deleteProduct(@RequestParam Long id) {
        adminService.softDeleteProduct(id);
        return "redirect:/admin/item-list";
    }
}