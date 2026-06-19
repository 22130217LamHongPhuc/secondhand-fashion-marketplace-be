package com.be.seeder;

import com.be.entity.Order;
import com.be.entity.OrderItem;
import com.be.entity.Product;
import com.be.repository.OrderItemRepository;
import com.be.repository.OrderRepository;
import com.be.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderItemRecoveryRunner implements CommandLineRunner {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final PlatformTransactionManager transactionManager;

    @Override
    public void run(String... args) {
        log.info("Checking for database orders with missing order items...");
        try {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.executeWithoutResult(status -> {
                List<Product> products = productRepository.findAll();
                if (products.isEmpty()) {
                    log.warn("No products found in database. Cannot run order item recovery.");
                    return;
                }
                Product defaultProduct = products.get(0);
                
                List<Order> orders = orderRepository.findAll();
                int recoveredCount = 0;
                
                for (Order order : orders) {
                    if (order.getItems() == null || order.getItems().isEmpty()) {
                        log.warn("Found order ID: {} ({}) with 0 items! Healing it with default product ID: {}", 
                                order.getId(), order.getOrderCode(), defaultProduct.getId());
                        
                        BigDecimal unitPrice = order.getSubtotal();
                        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                            unitPrice = new BigDecimal("590000");
                        }
                        
                        OrderItem defaultItem = OrderItem.builder()
                                .order(order)
                                .product(defaultProduct)
                                .productName("Áo Khoác Bomber/Blazer Vintage (Premium)")
                                .unitPrice(unitPrice)
                                .quantity(1)
                                .subtotal(unitPrice)
                                .build();
                        
                        orderItemRepository.save(defaultItem);
                        recoveredCount++;
                    }
                }
                
                if (recoveredCount > 0) {
                    log.info("Successfully healed {} orders with missing items!", recoveredCount);
                } else {
                    log.info("All orders have products correctly linked. No healing needed.");
                }
            });
        } catch (Exception e) {
            log.error("An error occurred during order item recovery. Bypassing to ensure server starts successfully.", e);
        }
    }
}
