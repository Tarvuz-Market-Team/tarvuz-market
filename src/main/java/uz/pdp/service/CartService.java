package uz.pdp.service;

import uz.pdp.base.BaseService;
import uz.pdp.exception.InvalidCartException;
import uz.pdp.exception.InsufficientStockException;
import uz.pdp.exception.InvalidProductException;
import uz.pdp.model.Cart;
import uz.pdp.model.Cart.Item;
import uz.pdp.util.FileUtils;

import java.io.IOException;
import java.util.*;
import java.util.function.BiPredicate;
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
        cart.touch();

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
    public boolean update(UUID id, Cart cart) throws IOException, InvalidCartException, NoSuchElementException {
        Cart existing = findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cart with this ID does not exist: " + id));

        if (!existing.isActive()) {
            throw new InvalidCartException("Cart is not active: " + id);
        }

        saveCartsToFile();
        return false;
    }

    @Override
    public void deactivate(UUID id) throws IOException, NoSuchElementException, InvalidCartException {
        Cart cart = findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cart with this ID does not exist: " + id));

        if (!cart.isActive()) {
            throw new InvalidCartException("Cart is already inactive: " + id);
        }

        cart.setActive(false);
        cart.touch();

        saveCartsToFile();
    }

    @Override
    public void clearAndSave() throws IOException {
        carts.clear();
        saveCartsToFile();
    }

    private void throwIfInvalid(Cart cart) throws NoSuchElementException {
        if (findById(cart.getId()).isPresent()) {
            throw new NoSuchElementException("Cart with this ID already exists.");
        }
    }

    public Optional<Cart> findByCustomerId(UUID customerId) {
        return carts.stream()
                .filter(Cart::isActive)
                .filter(cart -> cart.getCustomerId().equals(customerId))
                .findFirst();
    }

    public void purchaseCart(UUID cartId,
                             Consumer<Map<UUID, Integer>> stockUpdater)
            throws IOException,
            NoSuchElementException {

        Cart cart = findById(cartId)
                .orElseThrow(() -> new NoSuchElementException("Cart not found: " + cartId));

        Map<UUID, Integer> newQuantityById = cart.getItems().stream()
                .collect(Collectors.toMap(
                        Item::getProductId,
                        Item::getAmount
                ));

        stockUpdater.accept(newQuantityById);

        cart.setOrdered(true);
        cart.touch();

        saveCartsToFile();
    }

    public void updateOutOfStockItemsToMax(UUID cartId,
                                           Function<UUID, Integer> quantityGetter)
            throws IOException,
            NoSuchElementException {

        Cart cart = findById(cartId)
                .orElseThrow(() -> new NoSuchElementException("Cart not found: " + cartId));

        for (Item item : new HashSet<>(cart.getItems())) {
            int amount = quantityGetter.apply(item.getProductId());
            if (amount != 0) {
                item.setAmount(amount);
            } else {
                removeItem(cartId, item.getProductId());
            }
        }
        cart.touch();

        saveCartsToFile();
    }

    public void addItem(UUID cartId,
                        UUID productId,
                        int amount,
                        BiPredicate<UUID, Integer> stockChecker)
            throws IOException,
            IllegalArgumentException,
            NoSuchElementException,
            InsufficientStockException {

        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");

        Cart cart = findById(cartId)
                .orElseThrow(() -> new NoSuchElementException("Cart not found: " + cartId));

        Item item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseGet(() -> new Item(productId));

        int newTotalAmount = item.getAmount() + amount;

        if (!stockChecker.test(productId, newTotalAmount)) {
            throw new InsufficientStockException("Cannot add " + amount + " units of product " + productId +
                    " to cart " + cartId + ": insufficient stock.");
        }

        item.setAmount(newTotalAmount);
        cart.getItems().add(item);
        cart.touch();

        saveCartsToFile();
    }

    /**
     * Returns an {@link Optional} containing the set of items in the cart identified by {@code cartId}.
     *
     * <p><strong>Mutability Warning:</strong> The returned {@code Set<Item>} is a live reference
     * to the internal state of the cart. Modifying this set (e.g., adding or removing items)
     * will directly affect the contents of the cart.
     *
     * <p>Consumers should not assume immutability or thread-safety. If you require a snapshot,
     * consider copying the set before use.
     *
     * @param cartId the ID of the cart
     * @return an {@code Optional} containing the live item set, or {@code Optional.empty()} if the cart is not found
     */
    public Optional<Set<Item>> getItems(UUID cartId) throws NoSuchElementException {
        findById(cartId)
                .orElseThrow(() -> new NoSuchElementException("Cart not found: " + cartId));

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
     * @param cartId    the ID of the cart
     * @param productId the ID of the product
     * @return an {@code Optional<Item>} if found; otherwise, {@code Optional.empty()}
     */
    public Optional<Item> findItem(UUID cartId, UUID productId) throws NoSuchElementException {
        return getItems(cartId)
                .flatMap(items -> items.stream()
                        .filter(item -> item.getProductId().equals(productId))
                        .findFirst());
    }

    public Optional<Set<UUID>> extractProductIds(UUID cartId) throws NoSuchElementException {
        return getItems(cartId)
                .map(items -> items.stream()
                        .map(Item::getProductId)
                        .collect(Collectors.toSet()));
    }

    public void removeItem(UUID cartId, UUID productId) throws IOException, NoSuchElementException, InvalidProductException {
        Cart cart = findById(cartId)
                .orElseThrow(() -> new NoSuchElementException("Cart not found: " + cartId));

        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));

        if (!removed) {
            throw new InvalidProductException("Item not found for product ID: " + productId);
        } else {
            cart.touch();
            saveCartsToFile();
        }
    }

    private List<Cart> readCartsFromFile() throws IOException {
        return FileUtils.readFromJson(FILE_NAME, Cart.class);
    }

    private void saveCartsToFile() throws IOException {
        FileUtils.writeToJson(FILE_NAME, carts);
    }
}
