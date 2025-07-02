package lc.model;

import lombok.*;
import uz.pdp.base.BaseModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Cart extends BaseModel {
    private final UUID customerId;
    private boolean ordered;
    private final List<Item> items = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class Item {
        final UUID productId;
        int quantity;
    }
}
