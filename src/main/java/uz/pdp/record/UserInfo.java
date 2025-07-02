package uz.pdp.record;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    private UUID id;
    private String username;
    private String fullName;
}
