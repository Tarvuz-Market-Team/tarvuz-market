package uz.pdp.renderer;

import uz.pdp.model.Category;

import java.util.*;
import java.util.stream.Collectors;

public final class CategoryRenderer {

    public static String render(List<Category> categories) {
        StringBuilder sb = new StringBuilder();

        Map<UUID, List<Category>> parentMap = categories.stream()
                .filter(Category::isActive)
                .collect(Collectors.groupingBy(Category::getParentId));

        List<Category> roots = categories.stream()
                .filter(Category::isActive)
                .filter(CategoryRenderer::isRoot)
                .collect(Collectors.toList());

        String rootNames = roots.stream()
                .map(Category::getName)
                .collect(Collectors.joining(", "));
        sb.append("Root --> ").append(rootNames).append("\n");

        roots.forEach(root -> renderRecursive(root, parentMap, sb));

        return sb.toString();
    }

    public static String render(Category rootCategory, List<Category> categories) {
        StringBuilder sb = new StringBuilder();

        Map<UUID, List<Category>> parentMap = categories.stream()
                .filter(Category::isActive)
                .collect(Collectors.groupingBy(Category::getParentId));

        renderRecursive(rootCategory, parentMap, sb);
        return sb.toString();
    }

    private static void renderRecursive(Category parent, Map<UUID, List<Category>> parentMap, StringBuilder sb) {
        List<Category> children = parentMap.getOrDefault(parent.getId(), Collections.emptyList());

        if (!children.isEmpty()) {
            sb.append(parent.getName())
                    .append(" --> ")
                    .append(children.stream().map(Category::getName).collect(Collectors.joining(", ")))
                    .append("\n");

            children.forEach(child -> renderRecursive(child, parentMap, sb));
        }
    }

    private static boolean isRoot(Category category) {
        UUID pid = category.getParentId();
        return pid == null || pid.equals(new UUID(0, 0));
    }
}
