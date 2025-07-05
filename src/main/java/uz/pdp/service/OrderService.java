package uz.pdp.service;

import uz.pdp.base.BaseService;
import uz.pdp.factory.OrderBuilder;
import uz.pdp.exception.InvalidOrderException;
import uz.pdp.model.Cart;
import uz.pdp.model.Order;
import uz.pdp.model.Product;
import uz.pdp.model.User;
import uz.pdp.dto.UserDto;
import uz.pdp.util.FileUtils;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OrderService implements BaseService<Order> {
    private static final String FILE = "orders.json";
    private final List<Order> orders;

    public OrderService() throws IOException {
        orders = loadOrdersFromFile();
    }

    @Override
    public void add(Order order) throws IOException, InvalidOrderException {
        throwIfInvalid(order);

        orders.add(order);
        order.touch();

        saveOrdersToFile();
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return orders.stream()
                .filter(Order::isActive)
                .filter(order -> order.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Order> getAll() {
        return orders.stream()
                .filter(Order::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public boolean update(UUID id, Order order) throws IOException {
        saveOrdersToFile();
        return false;
    }

    @Override
    public void deactivate(UUID id) throws IOException, IllegalArgumentException {
        Order order = findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order with this ID does not exist."));

        if (!order.isActive()) {
            throw new IllegalArgumentException("Order is already inactive: " + id);
        }

        order.setActive(false);
        order.touch();

        saveOrdersToFile();
    }

    @Override
    public void clearAndSave() throws IOException {
        orders.clear();
        saveOrdersToFile();
    }

    private void throwIfInvalid(Order order) throws InvalidOrderException {
        if (findById(order.getId()).isPresent()) {
            throw new InvalidOrderException("Order ID must be unique: " + order.getId());
        }
    }

    public Order buildOrder(
            Cart cart,
            User user,
            Function<UUID, User> ignoreActiveSellerFinder,
            Function<UUID, Product> productFinder,
            Function<UUID, Double> productPriceGetter
    ) throws IOException, InvalidOrderException {

        OrderBuilder orderBuilder = new OrderBuilder(cart);

        return orderBuilder.buildNewOrder(
                new UserDto(user),
                ignoreActiveSellerFinder,
                productFinder,
                productPriceGetter
        );
    }

    public List<Order> getByCustomerId(UUID id) {
        Predicate<Order> matchesId = order -> order.getCustomer().getId().equals(id);

        return orders.stream()
                .filter(Order::isActive)
                .filter(matchesId)
                .collect(Collectors.toList());
    }

    public List<Order> filterTotalHigherThan(double amount) {
        Predicate<Order> totalHigherThanAmount = o -> o.getGrandTotal() > amount;

        return orders.stream()
                .filter(Order::isActive)
                .filter(totalHigherThanAmount)
                .collect(Collectors.toList());
    }

    public List<Order> filterTotalLowerThan(double amount) {
        Predicate<Order> totalLowerThanAmount = o -> o.getGrandTotal() < amount;

        return orders.stream()
                .filter(Order::isActive)
                .filter(totalLowerThanAmount)
                .collect(Collectors.toList());
    }

    private void saveOrdersToFile() throws IOException {
        FileUtils.writeToJson(FILE, orders);
    }

    private List<Order> loadOrdersFromFile() throws IOException {
        return FileUtils.readFromJson(FILE, Order.class);
    }
}
