package uz.pdp.model;

import lombok.*;
import uz.pdp.base.BaseModel;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Cart extends BaseModel {
    @Setter(AccessLevel.NONE)
    private UUID customerId;
    private boolean ordered = false;
    private final Set<Item> items = new HashSet<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        @Setter(AccessLevel.NONE)
        UUID productId;
        int amount;

        public Item(UUID productId) {
            this.productId = productId;
        }
    }

    public Cart(UUID customerId) {
        super();
        this.customerId = customerId;
    }
}
