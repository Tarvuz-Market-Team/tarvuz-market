package uz.pdp.model;

import lombok.*;
import uz.pdp.base.BaseModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BaseModel {
    private String fullName;
    private String username;
    private String password;
    private UserRole userRole;

    public enum UserRole {
        ADMIN,
        SELLER,
        CUSTOMER,
    }
}
