package mealplanner;

import org.apache.commons.lang3.EnumUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class MealPlanner {
    public void run() {
        createTablesIfNotExist();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("What would you like to do (add, show, exit)?");
            String operation = scanner.nextLine();

            if (operation.equalsIgnoreCase("exit")) {
                System.out.println("Bye!");
                break;
            } else if (operation.equalsIgnoreCase("add")) {
                addMeal();
            } else if (operation.equalsIgnoreCase("show")) {
                System.out.println("Which category do you want to print (breakfast, lunch, dinner)?");
                boolean isValidCategory = false;
                String category = "";

                while (!isValidCategory) {
                    category = scanner.nextLine().strip().toUpperCase();
                    if (EnumUtils.isValidEnum(MealCategory.class, category)) {
                        category = category.toLowerCase();
                        isValidCategory = true;
                    } else {
                        System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
                    }
                }
                printMealsByCategory(category);
            }
        }
    }

    private enum MealCategory {
        BREAKFAST, LUNCH, DINNER
    }

    private void createTablesIfNotExist() {
        try {
            Connection connection = DbConnection.connect();
            connection.createStatement().executeUpdate("""
                    CREATE TABLE IF NOT EXISTS meals (
                        category VARCHAR,
                        meal VARCHAR,
                        meal_id INTEGER,
                        CONSTRAINT unique_meals UNIQUE (meal, meal_id)
                    );
                                        
                    CREATE TABLE IF NOT EXISTS ingredients (
                        ingredient VARCHAR,
                        ingredient_id INTEGER,
                        meal_id INTEGER
                    );
                    """);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addMeal() {
        Scanner scanner = new Scanner(System.in);

        String mealCategory;
        System.out.println("Which meal do you want to add (breakfast, lunch, dinner)?");
        do {
            mealCategory = scanner.nextLine();
            if (!EnumUtils.isValidEnum(MealCategory.class, mealCategory.toUpperCase())) {
                System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
            }
        } while (!EnumUtils.isValidEnum(MealCategory.class, mealCategory.toUpperCase()));

        String mealName;
        System.out.println("Input the meal's name:");
        while (true) {
            mealName = scanner.nextLine();
            if (mealName.isEmpty() || mealName.isBlank() ||
                    !Arrays.stream(mealName.strip().split(" "))
                            .allMatch(s -> s.chars().allMatch(Character::isLetter))) {
                System.out.println("Wrong format. Use letters only!");
            } else {
                break;
            }
        }

        String ingredientsToSplit;
        ArrayList<String> ingredients;
        System.out.println("Input the ingredients:");
        while (true) {
            ingredientsToSplit = scanner.nextLine();
            if (ingredientsToSplit.isEmpty()) {
                System.out.println("Wrong format. Use letters only!");
            } else {
                ingredients = new ArrayList<>(Arrays.asList(ingredientsToSplit.split(",")));
                ingredients.replaceAll(String::strip);
                if (ingredients.stream().noneMatch(String::isEmpty) &&
                        ingredients.stream().noneMatch(String::isBlank) &&
                        ingredients.stream().allMatch(s -> s.matches("[a-zA-Z\\s]+"))) {
                    break;
                } else {
                    System.out.println("Wrong format. Use letters only!");
                }
            }
        }

        int mealCount = 0;
        try (ResultSet rs = DbConnection.connect().createStatement().executeQuery("SELECT COUNT(meal_id) FROM meals;")) {
            if (rs.next()) {
                mealCount = rs.getInt(1); // Assuming the count is in the first column
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        int ingredientId;
        String mealQuery = "INSERT INTO meals (category, meal, meal_id) VALUES (?, ?, ?)";
        String ingredientQuery = "INSERT INTO ingredients (ingredient, ingredient_id, meal_id) VALUES (?, ?, ?)";

        try (Connection connection = DbConnection.connect()) {
            try (PreparedStatement mealStmt = connection.prepareStatement(mealQuery);
                 PreparedStatement ingredientStmt = connection.prepareStatement(ingredientQuery)) {

                mealStmt.setString(1, mealCategory.toLowerCase());
                mealStmt.setString(2, mealName.toLowerCase());
                mealStmt.setInt(3, mealCount + 1);
                mealStmt.executeUpdate();

                for (String ingredient : ingredients) {
                    ingredientStmt.setString(1, ingredient);
                    ingredientStmt.setInt(3, mealCount + 1);

                    // Check if the ingredient already exists in the database
                    String ingredientCheckQuery = "SELECT ingredient_id FROM ingredients WHERE ingredient LIKE ?";
                    try (PreparedStatement ingredientCheckStmt = connection.prepareStatement(ingredientCheckQuery)) {
                        ingredientCheckStmt.setString(1, ingredient);
                        ResultSet ingredientCheckResult = ingredientCheckStmt.executeQuery();

                        if (ingredientCheckResult.next()) {
                            // Ingredient with the same name exists, use the existing ingredient_id
                            ingredientId = ingredientCheckResult.getInt(1);
                        } else {
                            // Generate a new ingredient_id
                            ResultSet ingredientCountResult = connection.createStatement()
                                    .executeQuery("SELECT COUNT(DISTINCT ingredient) FROM ingredients");
                            int ingredientCount = 0;
                            if (ingredientCountResult.next()) {
                                ingredientCount = ingredientCountResult.getInt(1);
                            }
                            ingredientId = ingredientCount + 1;
                        }
                    }
                    ingredientStmt.setInt(2, ingredientId);
                    ingredientStmt.executeUpdate();
                }
                System.out.println("The meal has been added!");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    private static void printAllMeals() {
        try (Connection connection = DbConnection.connect();
             Statement statement = connection.createStatement()) {
            String query = "SELECT m.meal, m.category, i.ingredient, i.meal_id " +
                    "FROM meals m " +
                    "JOIN ingredients i ON m.meal_id = i.meal_id " +
                    "ORDER BY m.meal_id";
            ResultSet resultSet = statement.executeQuery(query);

            String previousMealName = "";
            String previousCategory = "";
            ArrayList<String> ingredientsList = new ArrayList<>();

            while (resultSet.next()) {
                String mealName = resultSet.getString("meal");
                String category = resultSet.getString("category");
                String ingredient = resultSet.getString("ingredient");

                if (!mealName.equals(previousMealName)) {
                    if (!previousMealName.isEmpty()) {
                        printMealDetails(previousMealName, previousCategory, ingredientsList);
                        System.out.println();
                    }
                    previousMealName = mealName;
                    previousCategory = category;
                    ingredientsList = new ArrayList<>();
                }

                ingredientsList.add(ingredient);
            }

            if (!previousMealName.isEmpty()) {
                printMealDetails(previousMealName, previousCategory, ingredientsList);
            } else {
                System.out.println("\nNo meals saved. Add a meal first.\n");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void printMealsByCategory(String validCategory) {
        try (ResultSet rs = DbConnection.connect().createStatement().executeQuery(
                "SELECT m.meal, m.category, i.ingredient, i.meal_id " +
                        "FROM meals m " +
                        "JOIN ingredients i ON m.meal_id = i.meal_id " +
                        "WHERE m.category LIKE '" + validCategory + "'" +
                        "ORDER BY m.meal_id;")) {
            if (!rs.isBeforeFirst()) {
                System.out.println("No meals found.");
                return;
            }
            System.out.println("Category: " + validCategory + "\n");
            String previousMealName = "";
            ArrayList<String> ingredientsList = new ArrayList<>();

            while (rs.next()) {
                String mealName = rs.getString("meal");
                String ingredient = rs.getString("ingredient");

                if (!mealName.equals(previousMealName)) {
                    if (!previousMealName.isEmpty()) {
                        printMealDetails(previousMealName, ingredientsList);
                        System.out.println();
                    }
                    previousMealName = mealName;
                    ingredientsList = new ArrayList<>();
                }
                ingredientsList.add(ingredient);
            }

            if (!previousMealName.isEmpty()) {
                printMealDetails(previousMealName, ingredientsList);
                System.out.println();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void printMealDetails(String mealName, ArrayList<String> ingredientsList) {
        System.out.println("Name: " + mealName);
        System.out.println("Ingredients:");
        for (String ingredient : ingredientsList) {
            System.out.print(ingredient + "\n");
        }
    }

    private static void printMealDetails(String mealName, String category, ArrayList<String> ingredientsList) {
        System.out.println("Category: " + category);
        System.out.println("Name: " + mealName);
        System.out.println("Ingredients:");
        for (String ingredient : ingredientsList) {
            System.out.print(ingredient + "\n");
        }
    }
}
