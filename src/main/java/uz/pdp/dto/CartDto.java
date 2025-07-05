package uz.pdp.dto;

import lombok.Data;
import lombok.Getter;
import uz.pdp.model.Cart;
import uz.pdp.model.Cart.Item;

import java.util.Set;
import java.util.UUID;

@Getter
public class CartDto {
    public CartDto(Cart cart) {
        id = cart.getId();
        items = cart.getItems();
    }

    private final UUID id;
    private final Set<Item> items;
}