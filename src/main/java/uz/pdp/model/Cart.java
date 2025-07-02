package uz.pdp.model;

import lombok.*;
import uz.pdp.base.BaseModel;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Cart extends BaseModel {
    private final UUID customerId;
    private boolean ordered;
    private final Set<Item> items = new HashSet<>();

    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class Item {
        final UUID productId;
        int amount;
    }
}
