package uz.pdp.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public abstract class BaseModel {
    private final UUID id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private final Instant createdAt = Instant.now();
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant updatedAt = Instant.now();

    private boolean active = true;

    public BaseModel() {
        id = UUID.randomUUID();
    }

    public void touch() {
        updatedAt = Instant.now();
    }
}
