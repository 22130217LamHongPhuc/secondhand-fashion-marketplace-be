package com.be.controller.seller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller/products")
public class SellerProductController {
    @GetMapping
    public void getListByPage(
            @RequestParam(required = false, defaultValue = "0") long lastId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
    }

    @GetMapping("/{id}")
    public void getDetails(@PathVariable long id) {
    }

    @GetMapping("/status")
    public void getListByStatus(
            @RequestParam Boolean isActive,
            @RequestParam(required = false, defaultValue = "0") long lastId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
    }

    @PostMapping
    public void createProduct() {
    }

    @PutMapping("/{id}")
    public void updateProduct(@PathVariable long id) {
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable long id) {
    }
}
