package uz.pdp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.base.BaseModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseModel {
    public enum userRole {
        SELLER,
        CUSTOMER,
        ADMIN,
    }

    private String fullName;
    private String username;
    private String password;
    private String userRole;
}
