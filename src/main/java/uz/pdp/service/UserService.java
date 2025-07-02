package uz.pdp.service;

import uz.pdp.base.BaseService;
import uz.pdp.exception.InvalidUserException;
import uz.pdp.model.User;
import uz.pdp.util.FileUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import java.util.UUID;
import java.util.stream.Collectors;

public class UserService implements BaseService<User> {
    private static final String FILE_NAME = "users.xml";
    private final List<User> users;

    public UserService() throws IOException {
        users = readUsersFromFile();
    }

    @Override
    public void add(User user) throws IOException {
        throwIfInvalid(user);

        user.setUsername(user.getUsername().toLowerCase(Locale.ENGLISH));
        user.touch();

        users.add(user);
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
    public boolean update(UUID id, User user) throws IOException {
        Optional<User> optionalUser = findById(id);
        if (!optionalUser.isPresent()) {
            throw new IllegalArgumentException("User with this ID does not exist.");
        }

        User existing = optionalUser.get();
        existing.setFullName(user.getFullName());
        existing.setUsername(user.getUsername());
        existing.setPassword(user.getPassword());
        existing.setUserRole(user.getUserRole());
        existing.touch();

        saveUsersToXml();
        return true;
    }

    @Override
    public void deactivate(UUID id) throws IOException {
        Optional<User> optionalUser = findById(id);
        if (!optionalUser.isPresent()) {
            throw new IllegalArgumentException("User with this ID does not exist");
        }

        User user = optionalUser.get();
        user.setActive(false);
        user.touch();

        saveUsersToXml();
    }

    @Override
    public void clearAndSave() throws IOException {
        users.clear();
        saveUsersToXml();
    }

    private void throwIfInvalid(User user) throws InvalidUserException {
        if (findByUsername(user.getUsername()).isPresent() ) {
            throw new InvalidUserException("User is not valid.");
        }
        if(findById(user.getId()).isPresent()){
            throw new InvalidUserException("User is not valid.");
        }
    }

    private void saveUsersToXml() throws IOException {
        FileUtils.writeToXml(FILE_NAME, users);
    }

    private List<User> readUsersFromFile() throws IOException {
        return FileUtils.readFromXml(FILE_NAME, User.class);
    }


    public Optional<User> login(String username, String password) {
        return users.stream()
                .filter(User::isActive)
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .filter(user -> user.getPassword().equals(password))
                .findFirst();
    }

    public Optional<User> findByUsername(String username) {
        return users.stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }
}
