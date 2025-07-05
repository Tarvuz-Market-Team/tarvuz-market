package uz.pdp.dto;

import lombok.Data;
import lombok.Getter;
import uz.pdp.model.Product;

@Getter
public class ProductDto {
    public ProductDto(Product product) {
        name = product.getName();
        price = product.getPrice();
    }

    private final String name;
    private final double price;
}
