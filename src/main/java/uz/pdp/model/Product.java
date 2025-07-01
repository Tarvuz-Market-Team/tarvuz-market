package uz.pdp.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import uz.pdp.base.BaseModel;

@Data
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseModel {
    private int quantity;
}
