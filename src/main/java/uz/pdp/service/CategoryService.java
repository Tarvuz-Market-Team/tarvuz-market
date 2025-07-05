package uz.pdp.service;

import uz.pdp.base.BaseService;
import uz.pdp.exception.InvalidCategoryException;
import uz.pdp.exception.InvalidNameException;
import uz.pdp.model.Category;
import uz.pdp.util.FileUtils;
import uz.pdp.xmlwrapper.CategoryList;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import java.util.UUID;

public class CategoryService implements BaseService<Category> {
    private static final String FILE_NAME = "categories.xml";
    public static final UUID ROOT_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private final List<Category> categories;

    public CategoryService() throws IOException {
        categories = readCategoriesToFile();
    }

    @Override
    public void add(Category category) throws IOException, InvalidCategoryException, InvalidNameException {
        throwIfInvalid(category);

        categories.add(category);
        category.touch();

        makeCategoryNotLast(category.getParentId());

        saveCategoriesToFile();
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return categories.stream()
                .filter(Category::isActive)
                .filter(category -> category.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Category> getAll() {
        return categories.stream()
                .filter(Category::isActive)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public boolean update(UUID id, Category category)
            throws IOException, NoSuchElementException, InvalidCategoryException, InvalidNameException {

        Category existing = findById(id)
                .orElseThrow(() -> new NoSuchElementException("Category with this ID does not exist: " + id));

        if (!existing.isActive()) {
            throw new InvalidCategoryException("Category is not active: " + id);
        }
        if (findByName(category.getName()).isPresent()) {
            throw new InvalidNameException("Category name already used: '" + category.getName() + "'");
        }

        existing.setName(category.getName());
        existing.setParentId(category.getParentId());
        existing.setLast(category.isLast());
        existing.touch();

        makeCategoryNotLast(category.getParentId());

        saveCategoriesToFile();
        return false;

    }

    @Override
    public void deactivate(UUID id) throws IOException, NoSuchElementException {
        Category existing = findById(id)
                .orElseThrow(() -> new NoSuchElementException("Category with this ID does not exist: " + id));

        if (!existing.isActive()) {
            throw new InvalidCategoryException("Category is already inactive: " + id);
        }

        Set<UUID> toDeactivate = new HashSet<>();

        collectDescendants(id, toDeactivate);

        categories.stream()
                .filter(category -> toDeactivate.contains(category.getId()))
                .forEach(category -> {
                    category.setActive(false);
                    category.touch();
                });

        makeCategoryLastIfHasNoDescendants(existing.getParentId());

        saveCategoriesToFile();
    }

    private void throwIfInvalid(Category category) throws InvalidCategoryException, InvalidNameException {
        if (findById(category.getId()).isPresent()) {
            throw new InvalidCategoryException("Category with this ID already exists: " + category.getId());
        }
        if (findByName(category.getName()).isPresent()) {
            throw new InvalidNameException("Category name already used: '" + category.getName() + "'");
        }
    }

    public Optional<Category> findByName(String name) {
        return categories.stream()
                .filter(Category::isActive)
                .filter(category -> category.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public void renameCategory(Category category, String newName) throws IOException, InvalidNameException {
        findByName(newName)
                .orElseThrow(() -> new InvalidNameException("Category name already used: '" + category.getName() + "'"));

        category.setName(newName);
        category.touch();

        saveCategoriesToFile();
    }

    private void collectDescendants(UUID id, Set<UUID> collected) {
        if (categories.stream()
                .noneMatch(category -> category.getId().equals(id))) return;

        collected.add(id);
        categories.stream()
                .filter(Category::isActive)
                .filter(category -> category.getParentId().equals(id))
                .forEach(category -> collectDescendants(category.getId(), collected));
    }

    @Override
    public void clearAndSave() throws IOException {
        categories.clear();
        saveCategoriesToFile();
    }

    public List<Category> getLastCategories() {
        return categories.stream()
                .filter(Category::isActive)
                .filter(Category::isLast)
                .collect(Collectors.toList());
    }

    public List<Category> getCategoriesEmptyOfProducts(Predicate<Category> isEmptyOfProducts) {
        return getLastCategories().stream()
                .filter(isEmptyOfProducts)
                .collect(Collectors.toList());
    }

    public boolean isAvailableForSubcategory(UUID id, Predicate<Category> isEmptyOfProducts) {
        return findById(id)
                .map(isEmptyOfProducts::test)
                .orElseThrow(() -> new NoSuchElementException("Category with this ID does not exist: " + id));
    }

    public boolean isAvailableForProduct(UUID id) {
        return findById(id)
                .map(Category::isLast)
                .orElseThrow(() -> new NoSuchElementException("Category with this ID does not exist: " + id));
    }

    private void makeCategoryNotLast(UUID id) {
        findById(id)
                .ifPresent(category -> {

                    category.setLast(false);
                    category.touch();
                });
    }

    private void makeCategoryLastIfHasNoDescendants(UUID id) {
        if (categories.stream()
                .filter(Category::isActive)
                .noneMatch(category -> category.getParentId().equals(id))) {

            findById(id)
                    .ifPresent(category -> category.setLast(true));
        }
    }

    private void saveCategoriesToFile() throws IOException {
        CategoryList categoryList = new CategoryList(categories);
        FileUtils.writeToXml(FILE_NAME, categoryList);
    }

    private List<Category> readCategoriesToFile() throws IOException {
        return FileUtils.readFromXml(FILE_NAME, Category.class);
    }
}