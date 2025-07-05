package uz.pdp.dto;

import lombok.Getter;
import uz.pdp.model.User;

import java.util.UUID;

@Getter
public class UserDto {
    public UserDto(User user) {
        id = user.getId();
        username = user.getUsername();
        fullName = user.getFullName();
    }

    private final UUID id;
    private final String username;
    private final String fullName;
}
