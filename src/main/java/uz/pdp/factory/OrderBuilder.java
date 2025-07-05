package uz.pdp.factory;

import lombok.*;
import uz.pdp.exception.InvalidOrderException;
import uz.pdp.model.Cart;
import uz.pdp.model.Cart.Item;
import uz.pdp.model.Order;
import uz.pdp.model.Order.Seller;
import uz.pdp.model.Order.BoughtItem;
import uz.pdp.model.Product;
import uz.pdp.model.User;
import uz.pdp.dto.UserDto;
import uz.pdp.util.CartUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;


@RequiredArgsConstructor
public class OrderBuilder {
    private final Cart cart;

    public Order buildNewOrder(
            UserDto userDto,
            Function<UUID, User> IgnoreActiveSellerGetter,
            Function<UUID, Product> getProductById,
            Function<UUID, Double> productPriceGetter
    ) throws InvalidOrderException {
        Order order = new Order();

        order.setCartId(cart.getId());
        order.setCustomer(buildeOrderCustomer(userDto));
        order.setBoughtItems(buildOrderBoughtItems(getProductById, IgnoreActiveSellerGetter));
        order.setGrandTotal(CartUtils.calculatePrice(cart.getItems(), productPriceGetter));
        return order;
    }

    private Order.Customer buildeOrderCustomer(UserDto userDto) {
        Order.Customer customer = new Order.Customer();

        customer.setUsername(userDto.getUsername());
        customer.setFullName(userDto.getFullName());
        customer.setId(userDto.getId());

        return customer;
    }

    private List<BoughtItem> buildOrderBoughtItems(
            Function<UUID, Product> getProductById,
            Function<UUID, User> getIgnoreActiveById

    ) throws InvalidOrderException {
        List<Item> items = validateItemList(cart.getItems());

        List<BoughtItem> boughtItems = new ArrayList<>();

        items.forEach(i-> {

            Product product = getProductById.apply(i.getProductId());
            User userSeller = getIgnoreActiveById.apply(product.getSellerId());

            BoughtItem boughtItem = buildBoughtItem(product, i.getAmount());
            boughtItem.setSeller(buildBoughtItemSeller(userSeller));

            boughtItems.add(boughtItem);

        });

        return boughtItems;
    }

    private Seller buildBoughtItemSeller(User userSeller) {
        Seller seller = new Seller();

        seller.setId(userSeller.getId());
        seller.setActive(userSeller.isActive());
        seller.setFullName(userSeller.getFullName());
        seller.setUsername(userSeller.getUsername());

        return seller;
    }

    private BoughtItem buildBoughtItem(Product product, int itemQuantity) {
        BoughtItem boughtItem = new BoughtItem();

        boughtItem.setProductId(product.getId());
        boughtItem.setProduct(product.getName());
        boughtItem.setAmountBought(itemQuantity);
        boughtItem.setPricePerPsc(product.getPrice());
        boughtItem.setTotalPaid(product.getPrice() * itemQuantity);

        return boughtItem;
    }


    private List<Item> validateItemList(Set<Item> items) {
        if(items.isEmpty()) {
            throw new InvalidOrderException("Cannot create order from an empty cart.");
        }

        return new ArrayList<>(items);
    }
}
