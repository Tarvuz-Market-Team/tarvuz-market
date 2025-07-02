package uz.pdp;
import uz.pdp.model.Category;
import uz.pdp.renderer.CategoryRenderer;
import uz.pdp.service.CategoryService;

import java.io.IOException;

public class Testing {
    public static void main(String[] args) throws IOException {
        CategoryService service = new CategoryService();

        // Faylni tozalab boshlaymiz
        service.clearAndSave();

        // Rootlar
        Category phone = new Category("Phone", CategoryService.ROOT_UUID, true);
        Category food = new Category("Food", CategoryService.ROOT_UUID, true);
        Category car = new Category("Car", CategoryService.ROOT_UUID, true);

        service.add(phone);
        service.add(food);
        service.add(car);

        // Phone bolalari
        Category samsung = new Category("Samsung", phone.getId(), true);
        Category iphone = new Category("iPhone", phone.getId(), true);
        service.add(samsung);
        service.add(iphone);

        // Food bolalari
        Category pizza = new Category("Pizza", food.getId(), true);
        Category palov = new Category("Palov", food.getId(), true);
        service.add(pizza);
        service.add(palov);

        // BYD bolalari
        Category kia = new Category("KIA", car.getId(), true);
        Category bmw = new Category("BMW", car.getId(), true);
        service.add(kia);
        service.add(bmw);

        // Barchasini renderer bilan chiqaramiz
        String tree = CategoryRenderer.render(service.getAll());
        System.out.println(tree);
    }
}