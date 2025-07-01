package uz.pdp.model;

import lombok.*;
import uz.pdp.base.BaseModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Cart extends BaseModel {
    private final UUID customerId;
    private boolean ordered;
    @Getter(AccessLevel.NONE)
    private final List<Item> items = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class Item {
        final UUID productId;
        int amount;
    }

    public void addItem(UUID productId, int amount) {
        for (Item item : items) {
            if (item.getProductId().equals(productId)) {
                item.setAmount(item.getAmount() + amount);
                super.touch();
                return;
            }
        }

        items.add(new Item(productId, amount));
        super.touch();
    }

    public void updateItem(UUID productId, int amount) {
        for (Item item : items) {
            if (item.getProductId().equals(productId)) {
                item.setAmount(amount);
                super.touch();
                return;
            }
        }
    }

    public Optional<Item> getItem(UUID productId) {
        return items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }

    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    public List<UUID> getProductIds() {
        return items.stream()
                .map(Item::getProductId)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void removeItem(UUID productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
        super.touch();
    }
  
    public double calculateTotalPrice(Function<UUID, Double> getProductPrice) {
        return items.stream()
                .mapToDouble(item -> getProductPrice.apply(item.getProductId()) * item.getAmount())
                .sum();
    }
}
