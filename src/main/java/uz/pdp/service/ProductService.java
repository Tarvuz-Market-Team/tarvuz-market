package uz.pdp.service;

import uz.pdp.base.BaseService;
import uz.pdp.exception.InvalidNameException;
import uz.pdp.exception.InvalidProductException;
import uz.pdp.exception.InsufficientStockException;
import uz.pdp.model.Product;
import uz.pdp.model.User;
import uz.pdp.util.FileUtils;
import uz.pdp.xmlwrapper.UserList;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ProductService implements BaseService<Product> {
    private static final String FILE_NAME = "products.json";
    private final List<Product> products;
    public static final UUID ROOT_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public ProductService() throws IOException {
        products = readProductsFromFile();
    }

    @Override
    public void add(Product product) throws IOException, InvalidProductException, InvalidNameException {
        throwIfInvalid(product);

        products.add(product);
        product.touch();

        saveProductsToFile();
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return products.stream()
                .filter(Product::isActive)
                .filter(product -> product.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Product> getAll() {
        return products.stream()
                .filter(Product::isActive)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public boolean update(UUID id, Product product)
            throws IOException, InvalidProductException, InvalidNameException {

        Product existing = findById(id)
                .orElseThrow(() -> new InvalidProductException("Product with this ID does not exist: " + id));

        if (!existing.isActive()) {
            throw new InvalidProductException("Product is not active: " + id);
        }
        if (findByName(product.getName()).isPresent()) {
            throw new InvalidProductException("Product name  already used" + product.getName() + " ");
        }

        existing.setName(product.getName());
        existing.setQuantity(product.getQuantity());
        existing.setPrice(product.getPrice());
        existing.setCategoryId(product.getCategoryId());
        existing.touch();

        saveProductsToFile();
        return true;
    }

    @Override
    public void deactivate(UUID id) throws IOException, NoSuchElementException, InvalidProductException {
        Product product = findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product with this ID does not exist: " + id));

        if (!product.isActive()) {
            throw new InvalidProductException("Product is not active: " + id);
        }

        product.setActive(false);
        product.touch();

        saveProductsToFile();
    }

    @Override
    public void clearAndSave() throws IOException {
        products.clear();
        saveProductsToFile();
    }

    private void throwIfInvalid(Product product) throws InvalidProductException, InvalidNameException {
        if (findById(product.getId()).isPresent()) {
            throw new InvalidProductException("Product with this ID already exists: " + product.getId());
        }
        if (findByName(product.getName()).isPresent()) {
            throw new InvalidNameException("Product name already used: '" + product.getName() + "'");
        }
    }

    public List<Product> getByCategoryId(UUID categoryId) {
        return products.stream()
                .filter(Product::isActive)
                .filter(product -> product.getCategoryId().equals(categoryId))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Product> getBySellerId(UUID sellerId) {
        return products.stream()
                .filter(Product::isActive)
                .filter(product -> product.getSellerId().equals(sellerId))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Optional<Product> findByName(String productName) {
        return products.stream()
                .filter(Product::isActive)
                .filter(product -> product.getName().equalsIgnoreCase(productName))
                .findFirst();
    }

    public boolean isInStock(UUID productId, int amount) {
        return products.stream()
                .filter(Product::isActive)
                .filter(product -> product.getId().equals(productId))
                .anyMatch(product -> product.getQuantity() >= amount);
    }

    public boolean isCategoryEmpty(UUID id) {
        return products.stream()
                .filter(Product::isActive)
                .noneMatch(product -> product.getCategoryId().equals(id));
    }

    private void purchase(UUID productId, int amount)
            throws IOException,
            InvalidProductException,
            IllegalArgumentException,
            InsufficientStockException,
            NoSuchElementException {

        Product product = findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product with this ID does not exist: " + productId));

        if (!product.isActive()) {
            throw new InvalidProductException("Product is not active: " + productId);
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Purchase amount must be greater than zero: " + amount);
        }
        if (amount > product.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock for product: " + productId);
        }

        product.setQuantity(product.getQuantity() - amount);
        if (product.getQuantity() == 0) {
            product.setActive(false);
        }

        product.touch();
        saveProductsToFile();
    }

    public void renameProduct(UUID id, String newName)
            throws IOException, InvalidProductException, InvalidNameException, NoSuchElementException {

        Product product = findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product with this ID does not exist: " + id));

        if (!product.isActive()) {
            throw new InvalidProductException("Product is not active: " + id);
        }
        if (newName == null || newName.trim().isEmpty()) {
            throw new InvalidNameException("Product name cannot be null or empty");
        }
        if (findByName(newName).isPresent()) {
            throw new InvalidNameException("Product name already used" + newName + " ");
        }

        product.setName(newName);
        product.touch();

        saveProductsToFile();
    }

    public int getQuantity(UUID productId) throws IOException, NoSuchElementException {
        Product product = findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product with this ID does not exist: " + productId));

        return product.getQuantity();
    }

    public void updateStock(Map<UUID, Integer> stockUpdates) throws IOException {
        for (Map.Entry<UUID, Integer> entry : stockUpdates.entrySet()) {
            purchase(entry.getKey(), entry.getValue());
        }
    }

    private void saveProductsToFile() throws IOException {
        FileUtils.writeToJson(FILE_NAME, products);
    }

    private List<Product> readProductsFromFile() throws IOException {
        return FileUtils.readFromJson(FILE_NAME, Product.class);
    }
}
