package uz.pdp.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import uz.pdp.base.BaseModel;

import java.util.UUID;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseModel {
    private String name;
    private int quantity;
    private double price;
    private UUID categoryId;
    private final UUID sellerId;
}