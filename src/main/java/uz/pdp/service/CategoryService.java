package uz.pdp.service;

import uz.pdp.base.BaseService;
import uz.pdp.model.Category;
import uz.pdp.util.FileUtils;
import uz.pdp.xmlwrapper.CategoryList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CategoryService implements BaseService {

    private static final String FILE_NAME = "categories.xml";
    public static final UUID ROOT_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private List<Category> categories;

    public CategoryService() {
        try {
            categories = loadFromFile();
        } catch (IOException e) {
            categories = new ArrayList<>();
            System.out.println("Failed to load categories from file: " + e.getMessage());
        }
    }

    @Override
    public void add(Object obj) throws IOException {

    }

    @Override
    public Object get(UUID id) {
        return null;
    }

    @Override
    public List<Object> getAll() {
        return Collections.emptyList();
    }

    @Override
    public boolean update(UUID id, Object obj) throws IOException {
        return false;
    }

    @Override
    public void remove(UUID id) throws IOException {

    }

    @Override
    public void clearAndSave() throws IOException {

    }

    private void save() throws IOException {
        CategoryList categoryList = new CategoryList(categories);
        FileUtils.writeToXml(FILE_NAME, categoryList);
    }

    private List<Category> loadFromFile() throws IOException {
        return FileUtils.readFromXml(FILE_NAME, Category.class);
    }
}
