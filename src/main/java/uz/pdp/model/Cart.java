package uz.pdp.model;

import lombok.*;
import uz.pdp.base.BaseModel;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Cart extends BaseModel {
    private final User customer;
    private boolean ordered;
    private final List<Item> items = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class Item {
        final Product product;
        int quantity;
    }
}
