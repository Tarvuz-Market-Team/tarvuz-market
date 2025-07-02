package uz.pdp.service;

import uz.pdp.base.BaseService;
import uz.pdp.builder.OrderBuilder;
import uz.pdp.exception.InvalidOrderException;
import uz.pdp.model.Cart;
import uz.pdp.model.Order;
import uz.pdp.model.Product;
import uz.pdp.model.User;
import uz.pdp.record.UserInfo;
import uz.pdp.util.FileUtils;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OrderService implements BaseService<Order> {
    private static final String FILE = "orders.json";
    private List<Order> orders;

    public OrderService() throws IOException {
        orders = loadOrdersFromFile();
    }


    @Override
    public void add(Order order) throws IOException {
        throwIfInvalid(order);

        orders.add(order);

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
    public void deactivate(UUID id) throws IOException {
        Optional<Order> optionalOrder = findById(id);

        if (!optionalOrder.isPresent()) {
            throw new IllegalArgumentException("Order with this ID does not exist.");
        }

        Order order = optionalOrder.get();
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
        if(findById(order.getId()).isPresent()) {
            throw new InvalidOrderException("Order with this ID already exists.");
        }
    }

    public Order buildNewOrder(
            Cart cart, User user,
            Function<UUID, User> getIgnoreActiveSellerById,
            Function<UUID, Product> getProductById
    ) throws IOException, InvalidOrderException {
        OrderBuilder orderBuilder = new OrderBuilder(cart);
        return orderBuilder.buildNewOrder(
                new UserInfo(user.getId(), user.getUsername(), user.getFullName()),
                getIgnoreActiveSellerById,
                getProductById);
    }

    public List<Order> getByCustomerId(UUID id) {
        Predicate<Order> matchesId = order -> order.getCustomer().getId().equals(id);

        return orders.stream()
                .filter(Order::isActive)
                .filter(matchesId)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Order> filterTotalHigherThan(double amount) {
        Predicate<Order> totalHigherThanAmount = o -> o.getGrandTotal() > amount;

        return orders.stream()
                .filter(Order::isActive)
                .filter(totalHigherThanAmount)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Order> filterTotalLowerThan(double amount) {
        Predicate<Order> totalLowerThanAmount = o -> o.getGrandTotal() < amount;

        return orders.stream()
                .filter(Order::isActive)
                .filter(totalLowerThanAmount)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void saveOrdersToFile() throws IOException {
        FileUtils.writeToJson(FILE, orders);
    }

    private List<Order> loadOrdersFromFile() throws IOException {
        return FileUtils.readFromJson(FILE, Order.class);
    }
}
