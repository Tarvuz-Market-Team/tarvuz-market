package uz.pdp.util;

import uz.pdp.model.Cart.Item;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public final class CartUtils {
    private CartUtils() {
    }

    public static double calculatePrice(Set<Item> items, Function<UUID, Double> productPriceGetter) {
        return items.stream()
                .mapToDouble(item -> item.getAmount() * productPriceGetter.apply(item.getProductId()))
                .sum();
    }
}
