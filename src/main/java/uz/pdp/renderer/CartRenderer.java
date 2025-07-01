package uz.pdp.renderer;

import uz.pdp.model.Cart;
import uz.pdp.model.Cart.Item;
import uz.pdp.record.ProductNameAndPrice;
import uz.pdp.util.TimeUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public final class CartRenderer {
    public static String render(Cart cart,
                                Function<UUID, Optional<ProductNameAndPrice>> getProductNameAndPrice,
                                Function<UUID, Double> getProductPrice) {
        StringBuilder sb = new StringBuilder();

        List<Item> items = cart.getItems();
        for (Item item : items) {
            UUID productId = item.getProductId();
            Optional<ProductNameAndPrice> optionalProduct = getProductNameAndPrice.apply(productId);
            if (optionalProduct.isPresent()) {
                ProductNameAndPrice product = optionalProduct.get();
                sb.append(String.format("%-10s $%-7.2f amt: %-3d\n",
                        product.name, product.price, item.getAmount()));
            } else {
                sb.append(String.format("Product with ID %s not found.\n", productId));
            }
        }

        sb.append(String.format("Total: $%.2f\n",
                cart.calculateTotalPrice(getProductPrice)));
        sb.append(String.format("Last modified: %s\n",
                TimeUtils.format(cart.getUpdatedAt())));

        return sb.toString();
    }
}
