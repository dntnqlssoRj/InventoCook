package inventocook.model;

import java.util.List;

public class Recipe {
    private final String name;
    private final List<String> ingredients;
    private final String description;

    public Recipe(String name, List<String> ingredients, String description) {
        this.name = name;
        this.ingredients = ingredients;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public String getDescription() {
        return description;
    }
}

