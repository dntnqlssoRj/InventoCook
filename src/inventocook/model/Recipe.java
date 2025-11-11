package inventocook.model;

import java.util.List;

public class Recipe {
    public String title;
    public List<String> ingredients;
    public String instructions;

    public Recipe(String title, List<String> ingredients, String instructions) {
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }
}

