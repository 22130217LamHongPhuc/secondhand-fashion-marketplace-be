package com.be.controller.seller;

import com.be.common.enums.OrderStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller/orders")
public class SellerOrderController {
    @GetMapping
    public void getListByPage(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
    }

    @GetMapping("/{id}")
    public void getDetails(@PathVariable Long id) {
    }

    @GetMapping("/status")
    public void getListByStatus(
            @RequestParam OrderStatus status,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
    }

    @PostMapping
    public void createOrder() {
    }

    @PutMapping("/{id}")
    public void updateOrder(@PathVariable Long id) {
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Long id) {
    }
}
