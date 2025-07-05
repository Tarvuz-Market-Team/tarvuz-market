package uz.pdp.service;

import uz.pdp.base.BaseService;
import uz.pdp.exception.InvalidNameException;
import uz.pdp.exception.InvalidProductException;
import uz.pdp.exception.InvalidUserException;
import uz.pdp.model.User;
import uz.pdp.util.FileUtils;

import java.io.IOException;
import java.util.*;

import java.util.stream.Collectors;

public class UserService implements BaseService<User> {
    private static final String FILE_NAME = "users.xml";
    private final List<User> users;

    public UserService() throws IOException {
        users = readUsersFromFile();
    }

    @Override
    public void add(User user) throws IOException, InvalidUserException, InvalidNameException {
        throwIfInvalid(user);

        user.setUsername(user.getUsername().toLowerCase(Locale.ENGLISH));
        users.add(user);
        user.touch();

        saveUsersToXml();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return users.stream()
                .filter(User::isActive)
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<User> getAll() {
        return users.stream()
                .filter(User::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public boolean update(UUID id, User user)
            throws IOException, InvalidUserException, InvalidNameException {
        User existing = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with this ID does not exist: " + id));

        if (!existing.isActive()) {
            throw new IllegalArgumentException("User is not active: " + id);
        }
        if (findByUsername(user.getUsername()).isPresent()) {
            throw new InvalidNameException("Username is already used: " + user.getUsername());
        }

        existing.setFullName(user.getFullName());
        existing.setUsername(user.getUsername().toLowerCase(Locale.ENGLISH));
        existing.setPassword(user.getPassword());
        existing.touch();

        saveUsersToXml();
        return true;
    }

    @Override
    public void deactivate(UUID id) throws IOException, InvalidUserException {
        User existing = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with this ID does not exist: " + id));

        if (!existing.isActive()) {
            throw new IllegalArgumentException("User is not active: " + id);
        }

        existing.setActive(false);
        existing.touch();

        saveUsersToXml();
    }

    @Override
    public void clearAndSave() throws IOException {
        users.clear();
        saveUsersToXml();
    }

    private void throwIfInvalid(User user) throws InvalidUserException, InvalidNameException {
        if (findByUsername(user.getUsername()).isPresent() ) {
            throw new InvalidNameException("Username is already used: " + user.getUsername());
        }
        if(findById(user.getId()).isPresent()){
            throw new InvalidUserException("User with this ID already exists: " + user.getId());
        }
    }

    public Optional<User> login(String username, String password) {
        return users.stream()
                .filter(User::isActive)
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .filter(user -> user.getPassword().equals(password))
                .findFirst();
    }

    public void changeUsername(UUID userId, String newUsername)
            throws IOException, InvalidUserException, NoSuchElementException, InvalidNameException {
        User user = findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User with this ID does not exist: " + userId));

        if (!user.isActive()) {
            throw new InvalidUserException("User is not active: " + userId);
        }
        if (newUsername == null || newUsername.trim().isEmpty()) {
            throw new InvalidNameException("Username cannot be null or blank.");
        }
        if (findByUsername(newUsername).isPresent()) {
            throw new InvalidNameException("Username is already used: " + newUsername);
        }

        user.setUsername(newUsername.toLowerCase(Locale.ENGLISH));
        user.touch();

        saveUsersToXml();
    }

    public Optional<User> findByUsername(String username) {
        return users.stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public Optional<User> findIgnoreActiveSeller(UUID sellerId) {
        return users.stream()
                .filter(user -> user.getUserRole() == User.UserRole.SELLER)
                .filter(user -> user.getId().equals(sellerId))
                .findFirst();
    }

    private void saveUsersToXml() throws IOException {
        FileUtils.writeToXml(FILE_NAME, users);
    }

    private List<User> readUsersFromFile() throws IOException {
        return FileUtils.readFromXml(FILE_NAME, User.class);
    }
}