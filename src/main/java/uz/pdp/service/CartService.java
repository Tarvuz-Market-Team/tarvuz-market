package uz.pdp.service;

import uz.pdp.base.BaseModel;
import uz.pdp.base.BaseService;
import uz.pdp.model.Cart;
import uz.pdp.model.User;
import uz.pdp.util.FileUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class CartService implements BaseService<Cart> {
    private static final String FILE_NAME = "carts.json";
    private List<Cart> carts;

    public CartService() throws IOException {
        carts = readCartsFromFile();
    }

    @Override
    public void add(Cart cart) throws IOException, IllegalArgumentException {
        if (findById(cart.getId()).isPresent()) {
            throw new IllegalArgumentException("Cart with this ID already exists.");
        }

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



    private List<Cart> readCartsFromFile() throws IOException {
        return FileUtils.readFromJson(FILE_NAME, Cart.class);
    }

    private void saveCartsToFile() throws IOException {
        FileUtils.writeToJson(FILE_NAME, carts);
    }
}
