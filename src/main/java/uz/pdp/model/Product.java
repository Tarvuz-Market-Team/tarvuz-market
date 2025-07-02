package uz.pdp.model;

import lombok.*;
import uz.pdp.base.BaseModel;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseModel {
    private int quantity;
    private String name;
    private Double price;
    private UUID sellerId;
}
