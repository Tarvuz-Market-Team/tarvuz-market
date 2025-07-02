package uz.pdp.model;

import lombok.*;
import uz.pdp.base.BaseModel;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Category extends BaseModel {
    private String name;
    private UUID parentId;
    private boolean isLast = true;
}
