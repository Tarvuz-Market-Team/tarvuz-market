package uz.pdp.model;

import lombok.*;
import uz.pdp.base.BaseModel;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseModel {
    @Setter(AccessLevel.NONE)
    private UUID sellerId;
    private UUID categoryId;
    private String name;
    private int quantity;
    private double price;
}