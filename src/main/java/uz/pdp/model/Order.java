package uz.pdp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uz.pdp.base.BaseModel;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Order extends BaseModel {
    private transient Cart cart;

    private UUID cartId;
    private Customer customer;
    private List<BoughtItem> boughtItems;
    private double grandTotal;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Customer {
        transient User objCustomer;

        UUID id;
        String fullName;
        String username;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class BoughtItem {
        transient Cart.Item item;

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
        transient User objSeller;

        UUID id;
        String fullName;
        String username;
        boolean active;
    }
}
