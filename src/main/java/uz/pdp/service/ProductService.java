package uz.pdp.service;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import net.bytebuddy.dynamic.DynamicType;
import uz.pdp.base.BaseService;
import uz.pdp.exception.InvalidCategoryException;
import uz.pdp.exception.InvalidNameException;
import uz.pdp.exception.InvalidProductException;
import uz.pdp.model.Product;
import uz.pdp.util.FileUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ProductService implements BaseService<Product> {
    private static final String FILE_NAME = "products.json";
    public static final UUID ROOT_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private final List<Product> products;

    public ProductService() throws IOException {
        products = readProductsFromFile();
    }


    @Override
    public void add(Product product) throws IOException {
        throwIfInvalid(product);

        products.add(product);

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
    public boolean update(UUID id, Product product) throws IOException {
        Optional<Product> optionalProduct = findById(id);
        if (optionalProduct.isPresent()) {
            Product existing = optionalProduct.get();
            if (existing.isActive()) {
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
        }
        return false;
    }

    @Override
    public void deactivate(UUID id) throws IOException {
       Optional<Product> optionalProduct = findById(id);
       if (optionalProduct.isPresent()){
           Product existing = optionalProduct.get();
           if (existing.isActive()){
               existing.setActive(false);
               existing.touch();

           }
       }
       saveProductsToFile();

    }

    @Override
    public void clearAndSave() throws IOException {
        products.clear();
        saveProductsToFile();
    }

    private void throwIfInvalid (Product product){
        if (findById(product.getId()).isPresent()) {
            throw new InvalidCategoryException("Product with this ID already exists: " + product.getId());
        }
        if (findByName(product.getName()).isPresent()) {
            throw new InvalidNameException("Category name already used: '" + product.getName() + "'");
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

    public Optional<Product> findByName (String productName){
        return products.stream()
                .filter(Product :: isActive)
                .filter(product -> product.getName().equalsIgnoreCase(productName))
                .findFirst();
    }

    public boolean isCategoryEmpty (UUID categoryId){
        return products.stream()
                .filter(Product :: isActive)
                .noneMatch(product -> product.getCategoryId().equals(categoryId));
    }


    public void purchaseProducts (UUID productId, int quantity) throws IOException {
        Optional<Product> optionalProduct = findById(productId);
        if (optionalProduct.isPresent()){
            Product existing = optionalProduct.get();
            if (existing.isActive()){
                if (quantity <= 0){
                    throw new InvalidProductException("Quantity must be positive");
                }
                if (quantity > existing.getQuantity()){
                    throw new InvalidProductException("Insufficient stock");
                }

                existing.setQuantity(existing.getQuantity()-quantity);

                if (existing.getQuantity() == 0){
                    existing.setActive(false);
                }

                existing.touch();
                saveProductsToFile();
            }
        }
    }

    public void renameProducts (UUID productName, String name) throws IOException {
        Optional<Product> productOptional = findById(productName);
        if (productOptional.isPresent()){
            Product existing = productOptional.get();
            if (existing.isActive()){
                if (findByName(name).isPresent()){
                    throw new InvalidProductException("Product name already used" + name + " ");
                }

                existing.setName(name);
                existing.touch();
            }
        }
        saveProductsToFile();
    }

    private void saveProductsToFile() throws IOException{
        FileUtils.writeToJson(FILE_NAME,products);
    }

    private List<Product> readProductsFromFile() throws IOException {
        return FileUtils.readFromJson(FILE_NAME, Product.class);
    }
}
