package uz.pdp.base;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BaseService<T> {
    void add(T t) throws IOException;

    Optional<T> findById(UUID id);

    List<T> getAll();

    boolean update(UUID id, T t) throws IOException;

    void deactivate(UUID id) throws IOException;

    void clearAndSave() throws IOException;
}
