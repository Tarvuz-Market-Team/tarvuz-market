package uz.pdp.service;

import uz.pdp.base.BaseService;
import uz.pdp.exception.InvalidCartException;
import uz.pdp.exception.OutOfStockException;
import uz.pdp.model.Cart;
import uz.pdp.model.Cart.Item;
import uz.pdp.util.FileUtils;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CartService implements BaseService<Cart> {
    private static final String FILE_NAME = "carts.json";
    private final List<Cart> carts;

    public CartService() throws IOException {
        carts = readCartsFromFile();
    }

    @Override
    public void add(Cart cart) throws IOException, InvalidCartException {
        throwIfInvalid(cart);

        carts.add(cart);

        saveCartsToFile();
    }

    @Override
    public Optional<Cart> findById(UUID id) {
        return carts.stream()
                .filter(Cart::isActive)
                .filter(cart -> cart.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Cart> getAll() {
        return carts.stream()
                .filter(Cart::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public boolean update(UUID id, Cart cart) throws IOException {
        saveCartsToFile();
        return false;
    }

    @Override
    public void deactivate(UUID id) throws IOException {
        Optional<Cart> optionalCart = findById(id);
        if (!optionalCart.isPresent()) {
            throw new IllegalArgumentException("Cart with this ID does not exist.");
        }

        Cart cart = optionalCart.get();
        cart.setActive(false);
        cart.touch();

        saveCartsToFile();
    }

    @Override
    public void clearAndSave() throws IOException {
        carts.clear();
        saveCartsToFile();
    }

    private void throwIfInvalid(Cart cart) throws InvalidCartException {
        if (findById(cart.getId()).isPresent()) {
            throw new InvalidCartException("Cart with this ID already exists.");
        }
    }

    public Optional<Cart> findByCustomerId(UUID customerId) {
        return carts.stream()
                .filter(Cart::isActive)
                .filter(cart -> cart.getCustomerId().equals(customerId))
                .findFirst();
    }

    public void orderCart(UUID cartId, Consumer<Set<UUID>> stockProductUpdater) {
        Cart cart = findById(cartId)
                .orElseThrow(() -> new NoSuchElementException("Cart not found: " + cartId));

        Set<UUID> productIds = cart.getItems().stream()
                .map(Item::getProductId)
                .collect(Collectors.toSet());

        stockProductUpdater.accept(productIds);
        cart.setOrdered(true);
    }

    public void updateOutOfStockItemsToMax(UUID cartId, Function<UUID, Integer> maxQuantityGetter) {
        Cart cart = findById(cartId)
                .orElseThrow(() -> new NoSuchElementException("Cart not found: " + cartId));

        for (Item item : new HashSet<>(cart.getItems())) {
            int amount = maxQuantityGetter.apply(item.getProductId());
            if (amount != 0) {
                item.setAmount(amount);
            } else {
                removeItem(cartId, item.getProductId());
            }
        }
    }

    public void addItem(UUID cartId, UUID productId, int amount, Predicate<Integer> stockChecker) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");

        Cart cart = findById(cartId)
                .orElseThrow(() -> new NoSuchElementException("Cart not found: " + cartId));

        Item item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseGet(() -> {
                    Item newItem = new Item(productId);
                    cart.getItems().add(newItem);
                    return newItem;
                });

        int newTotalAmount = item.getAmount() + amount;

        if (stockChecker.test(newTotalAmount)) {
            item.setAmount(newTotalAmount);
        } else {
            throw new OutOfStockException("Cannot add " + amount + " units of product " + productId +
                    " to cart " + cartId + ": insufficient stock.");
        }
    }

    // todo Document that it's a live reference
    public Optional<Set<Item>> getItems(UUID cartId) {
        Optional<Cart> optionalCart = findById(cartId);
        return optionalCart.map(Cart::getItems);
    }

    /**
     * Returns an {@link Optional} containing the {@link Item} with the given product ID
     * from the cart identified by {@code cartId}, if it exists.
     *
     * <p><strong>Mutability Warning:</strong> The returned {@code Item} is a direct reference
     * to the internal state of the cart. Modifying this object (e.g., via {@code setAmount})
     * will affect the cart's contents directly.
     *
     * <p>If you need a read-only or isolated copy, consider cloning the item manually.
     *
     * @param cartId the ID of the cart
     * @param productId the ID of the product
     * @return an {@code Optional<Item>} if found; otherwise, {@code Optional.empty()}
     */

    public Optional<Item> findItem(UUID cartId, UUID productId) {
        return getItems(cartId)
                .flatMap(items -> items.stream()
                        .filter(item -> item.getProductId().equals(productId))
                        .findFirst());
    }

    public Optional<Set<UUID>> extractProductIds(UUID cartId) {
        return getItems(cartId)
                .map(items -> items.stream()
                        .map(Item::getProductId)
                        .collect(Collectors.toSet()));
    }

    public void removeItem(UUID cartId, UUID productId) {
        Cart cart = findById(cartId)
                .orElseThrow(() -> new NoSuchElementException("Cart not found: " + cartId));

        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));

        if (!removed) {
            throw new NoSuchElementException("Item not found for product ID: " + productId);
        }
    }

    private List<Cart> readCartsFromFile() throws IOException {
        return FileUtils.readFromJson(FILE_NAME, Cart.class);
    }

    private void saveCartsToFile() throws IOException {
        FileUtils.writeToJson(FILE_NAME, carts);
    }
}
