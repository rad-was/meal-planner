package mealplanner;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class MealPlanner {
    public void run() {
        Connection connection = DbConnection.connect();
        try {
            connection.createStatement().executeUpdate(SQLQueries.createTablesIfNotExist());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("What would you like to do (add, show, plan, exit)?");
            String operation = scanner.nextLine();

            if (operation.equalsIgnoreCase("exit")) {
                System.out.println("Bye!");
                break;
            } else if (operation.equalsIgnoreCase("add")) {
                addMeal();
            } else if (operation.equalsIgnoreCase("plan")) {
                plan();
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
                Printer.printMealsByCategory(category);
            }
        }
        DbConnection.finalize(connection);
    }

    private void plan() {
        Connection connection = DbConnection.connect();
        Scanner scanner = new Scanner(System.in);

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            String day = StringUtils.capitalize(dayOfWeek.toString().toLowerCase());
            System.out.println(day);

            for (MealCategory mc : MealCategory.values()) {
                final String mealCategory = mc.toString().toLowerCase();

                try (ResultSet rs = connection.createStatement().executeQuery
                        (SQLQueries.getMealsByCategory(mealCategory))) {
                    if (!rs.isBeforeFirst()) {
                        System.out.println("No meals");  // edit later
                    } else {
                        while (rs.next()) {
                            System.out.println(rs.getString("meal"));
                        }

                        String mealName = "";
                        boolean nameExistsInDb = false;
                        System.out.println("Choose the " + mealCategory + " for "
                                + day + " from the list above:");
                        while (!nameExistsInDb) {
                            mealName = scanner.nextLine();

                            // Check if meal name is in database
                            PreparedStatement nameCheck =
                                    connection.prepareStatement(SQLQueries.getNumberOfMealsByName());
                            nameCheck.setString(1, mealName);
                            ResultSet numberOfNames = nameCheck.executeQuery();
                            numberOfNames.next();
                            if (numberOfNames.getInt("count") == 0) {
                                System.out.println("This meal doesn't exist. Choose a meal from the list above.");
                            } else {
                                nameExistsInDb = true;
                            }
                        }

                        PreparedStatement insertStatement =
                                connection.prepareStatement(SQLQueries.insertIntoPlan());
                        insertStatement.setString(1, mealCategory);
                        insertStatement.setString(2, mealName);

                        ResultSet mealIdQuery = connection.createStatement()
                                .executeQuery(SQLQueries.getMealIdByMealName(mealName));
                        int mealId;
                        if (mealIdQuery.next()) {
                            mealId = mealIdQuery.getInt(SQLQueries.MEAL_ID);
                        } else {
                            throw new RuntimeException();
                        }

                        insertStatement.setInt(3, mealId);
                        insertStatement.executeUpdate();
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("Yeah! We planned the meals for " + day + ".");
        }
        Printer.printPlan();
        DbConnection.finalize(connection);
    }

    private enum MealCategory {
        BREAKFAST, LUNCH, DINNER
    }

    private void addMeal() {
        Connection connection = DbConnection.connect();
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
        try (ResultSet rs = connection.createStatement().executeQuery(SQLQueries.getNumberOfMeals())) {
            if (rs.next()) {
                mealCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        int ingredientId;
        try (PreparedStatement mealStmt = connection.prepareStatement(SQLQueries.insertIntoMeals());
             PreparedStatement ingredientStmt = connection.prepareStatement(SQLQueries.insertIntoIngredients())) {

            mealStmt.setString(1, mealCategory.toLowerCase());
            mealStmt.setString(2, mealName.toLowerCase());
            mealStmt.setInt(3, mealCount + 1);
            mealStmt.executeUpdate();

            for (String ingredient : ingredients) {
                ingredientStmt.setString(1, ingredient);
                ingredientStmt.setInt(3, mealCount + 1);

                // Check if the ingredient already exists in the database
                try (PreparedStatement ingredientCheckStmt =
                             connection.prepareStatement(SQLQueries.getIngredientIdByName())) {

                    ingredientCheckStmt.setString(1, ingredient);
                    ResultSet ingredientCheckResult = ingredientCheckStmt.executeQuery();

                    if (ingredientCheckResult.next()) {
                        // Ingredient with the same name exists, use the existing ingredient_id
                        ingredientId = ingredientCheckResult.getInt(1);
                    } else {
                        // Generate a new ingredient_id
                        ResultSet ingredientCountResult = connection.createStatement()
                                .executeQuery(SQLQueries.getNumberOfIngredients());
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        DbConnection.finalize(connection);
    }
}
