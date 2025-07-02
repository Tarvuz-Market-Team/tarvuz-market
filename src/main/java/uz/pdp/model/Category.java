package uz.pdp.model;

import lombok.*;
import uz.pdp.base.BaseModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Category extends BaseModel {
    private String name;
    private Category parent;
    private boolean isLast = true;
}
