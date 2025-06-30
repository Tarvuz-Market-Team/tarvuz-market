package uz.pdp.base;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface BaseService {

    /**
     * Adds a new object.
     *
     * @param obj the object to add
     * @throws IOException if an I/O error occurs
     */
    void add(Object obj) throws IOException;

    /**
     * Retrieves an object by its UUID.
     *
     * @param id the UUID of the object to retrieve
     * @return the object with the given UUID
     */
    Object get(UUID id);

    /**
     * Retrieves all stored objects.
     *
     * @return a list of all objects
     */
    List<Object> getAll();

    /**
     * Updates an existing object.
     *
     * @param id  the UUID of the object to update
     * @param obj the updated object
     * @return true if the update was successful, false otherwise
     * @throws IOException if an I/O error occurs
     */
    boolean update(UUID id, Object obj) throws IOException;

    /**
     * Removes an object by its UUID.
     *
     * @param id the UUID of the object to remove
     * @throws IOException if an I/O error occurs
     */
    void remove(UUID id) throws IOException;

    /**
     * Clears the current storage and saves all objects again.
     *
     * @throws IOException if an I/O error occurs
     */
    void clearAndSave() throws IOException;
}
