package mealplanner;

import java.util.ArrayList;

public class Meal {
    private final MealCategory category;
    private final String name;
    private final ArrayList<String> ingredients;

    public Meal(MealCategory category, String name, ArrayList<String> ingredients) {
        this.category = category;
        this.name = name;
        this.ingredients = ingredients;
    }

    @Override
    public String toString() {
        return "Category: " +
                category.toString().toLowerCase() +
                "\nName: " + name +
                "\nIngredients:\n" + String.join("\n", ingredients);
    }
}
