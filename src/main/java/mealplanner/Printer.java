package mealplanner;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.ArrayList;

public class Printer {
    @SuppressWarnings("unused")
    static void printAllMeals() {
        Connection connection = DbConnection.connect();
        try {
            ResultSet resultSet = connection.createStatement()
                    .executeQuery(SQLQueries.getMealsAndIngredients());
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
        DbConnection.finalize(connection);
    }

    static void printMealsByCategory(String validCategory) {
        Connection connection = DbConnection.connect();
        try (ResultSet rs = connection.createStatement()
                .executeQuery(SQLQueries.getMealsAndIngredientsByCategory(validCategory))) {
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
        DbConnection.finalize(connection);
    }

    static void printPlan() {
        Connection connection = DbConnection.connect();
        try {
            ResultSet rs = connection.createStatement().executeQuery(SQLQueries.getCategoriesMealsFromPlan());
            int dayCounter = 1;
            while (rs.next()) {
                String category = rs.getString("category");
                String meal = rs.getString("meal");

                if (category.equalsIgnoreCase("breakfast")) {
                    System.out.println();
                    System.out.println(DayOfWeek.of(dayCounter++));
                }
                System.out.println(StringUtils.capitalize(category) + ": " + meal);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        DbConnection.finalize(connection);
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
