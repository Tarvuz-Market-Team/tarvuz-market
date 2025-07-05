package uz.pdp;

import com.sun.org.apache.xpath.internal.operations.Or;
import uz.pdp.dto.UserDto;
import uz.pdp.factory.OrderBuilder;
import uz.pdp.model.*;
import uz.pdp.renderer.CategoryRenderer;
import uz.pdp.service.*;

import java.io.IOException;
import java.util.Optional;

public class Testing {
    static CartService cartService;
    static CategoryService categoryService;
    static OrderService orderService;
    static ProductService productService;
    static UserService userService;

    static {
        try {
            cartService = new CartService();
            categoryService = new CategoryService();
            orderService = new OrderService();
            productService = new ProductService();
            userService = new UserService();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Optional<User> adminO = userService.findByUsername("admin");
        Optional<User> customerO = userService.findByUsername("customer");
        Optional<User> sellerO = userService.findByUsername("seller");

        Optional<Category> carsO = categoryService.findByName("Cars");
        Optional<Category> motorcyclesO = categoryService.findByName("Motorcycles");
        Optional<Category> trucksO = categoryService.findByName("Trucks");

        Optional<Category> suvsO = categoryService.findByName("SUVs");

        Optional<Product> bmwX5 = productService.findByName("BMW X5");
        Optional<Product> bmwX6 = productService.findByName("BMW X6");

        Optional<Cart> cart = cartService.findByCustomerId(customerO.get().getId());

        OrderBuilder orderBuilder = new OrderBuilder(cart.get());
        Order order = orderBuilder.buildNewOrder(
                new UserDto(customerO.get()),
                id -> userService.findIgnoreActiveSeller(id).get(),
                id -> productService.findById(id).get(),
                id -> productService.findById(id).get().getPrice()
        );

        orderService.add(order);
    }
}