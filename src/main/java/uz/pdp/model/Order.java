package uz.pdp.model;

import lombok.*;
import uz.pdp.base.BaseModel;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Order extends BaseModel {
    private UUID cartId;
    private Customer customer;
    private List<BoughtItem> boughtItems;
    private double grandTotal;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Customer {
        UUID id;
        String fullName;
        String username;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class BoughtItem {
        Seller seller;
        UUID productId;
        String product;
        int amountBought;
        double pricePerPsc;
        double totalPaid;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Seller {
        UUID id;
        String fullName;
        String username;
        boolean active;
    }
}
