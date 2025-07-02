package uz.pdp.renderer;

import uz.pdp.model.Category;

import java.util.*;
import java.util.stream.Collectors;

public final class CategoryRenderer {

    // Render all tree from ROOTs
    public static String render(List<Category> categories) {
        StringBuilder sb = new StringBuilder();

        Map<UUID, List<Category>> parentMap = categories.stream()
                .filter(Category::isActive)
                .collect(Collectors.groupingBy(Category::getParentId));

        categories.stream()
                .filter(Category::isActive)
                .filter(CategoryRenderer::isRoot)
                .forEach(root -> renderRecursive(root, parentMap, sb));

        return sb.toString();
    }

    // Render from specific category
    public static String render(Category rootCategory, List<Category> categories) {
        StringBuilder sb = new StringBuilder();

        Map<UUID, List<Category>> parentMap = categories.stream()
                .filter(Category::isActive)
                .collect(Collectors.groupingBy(Category::getParentId));

        renderRecursive(rootCategory, parentMap, sb);

        return sb.toString();
    }

    private static void renderRecursive(Category parent,
                                        Map<UUID, List<Category>> parentMap,
                                        StringBuilder sb) {
        List<Category> children = getChildrenOf(parent, parentMap);
        if (!children.isEmpty()) {
            String childNames = children.stream()
                    .map(Category::getName)
                    .collect(Collectors.joining(", "));

            sb.append(parent.getName()).append(" --> ").append(childNames).append("\n");

            children.forEach(child -> renderRecursive(child, parentMap, sb));
        }
    }

    private static List<Category> getChildrenOf(Category parent,
                                                Map<UUID, List<Category>> parentMap) {
        return parentMap.getOrDefault(parent.getId(), Collections.emptyList());
    }

    private static boolean isRoot(Category category) {
        UUID pid = category.getParentId();
        return pid == null || pid.equals(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }
}
