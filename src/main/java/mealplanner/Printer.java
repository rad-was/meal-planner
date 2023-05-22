package mealplanner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Printer {
    @SuppressWarnings("unused")
    static void printAllMeals() {
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

    static void printMealsByCategory(String validCategory) {
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
