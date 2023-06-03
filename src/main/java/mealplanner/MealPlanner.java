package mealplanner;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MealPlanner {
    public static void main(String[] args) {
        MealPlanner mealPlanner = new MealPlanner();
        mealPlanner.run();
    }

    public void run() {
        Connection connection = DbConnection.connect();
        createTablesIfNotExists();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("What would you like to do (add, show, plan, save, exit)?");
            String operation = scanner.nextLine();

            if (operation.strip().equalsIgnoreCase("exit")) {
                System.out.println("Bye!");
                break;
            } else if (operation.strip().equalsIgnoreCase("add")) {
                addMeal();
            } else if (operation.strip().equalsIgnoreCase("plan")) {
                plan();
            } else if (operation.strip().equalsIgnoreCase("save")) {
                // Check if weekly plan was created
                try {
                    ResultSet numberOfMealsQuery =
                            connection.createStatement().executeQuery(SQLQueries.countMealsInPlan());
                    if (numberOfMealsQuery.next()) {
                        int numberOfMeals = numberOfMealsQuery.getInt("count");
                        if (numberOfMeals == (3 * 7)) {  // 3 meals every day of the week
                            save();
                        } else {
                            System.out.println("Unable to save. Plan your meals first.");
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else if (operation.strip().equalsIgnoreCase("show")) {
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

    private void createTablesIfNotExists() {
        Connection connection = DbConnection.connect();
        try {
            for (String table : List.of("meals", "ingredients", "plan")) {
                ResultSet rs = connection.createStatement().executeQuery(SQLQueries.checkIfTableExists(table));
                if (rs.next()) {
                    if (!rs.getBoolean("exists")) {
                        switch (table) {
                            case "meals" ->
                                    connection.createStatement().executeUpdate(SQLQueries.createMealsTable());
                            case "ingredients" ->
                                    connection.createStatement().executeUpdate(SQLQueries.createIngredientsTable());
                            case "plan" ->
                                    connection.createStatement().executeUpdate(SQLQueries.createPlanTable());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
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

                try (ResultSet rs = connection.createStatement().executeQuery(
                        SQLQueries.getMealsByCategory(mealCategory))) {
                    if (!rs.isBeforeFirst()) {
                        System.out.println("No meals");
                    } else {
                        while (rs.next()) {
                            System.out.println(rs.getString("name"));
                        }

                        String mealName = "";
                        boolean nameExistsInDb = false;
                        System.out.println("Choose the " + mealCategory + " for "
                                + day + " from the list above:");
                        while (!nameExistsInDb) {
                            mealName = scanner.nextLine();

                            // Check if meal name is in database
                            PreparedStatement nameCheck =
                                    connection.prepareStatement(SQLQueries.countMealsByName());
                            nameCheck.setString(1, mealName);

                            ResultSet numberOfNames = nameCheck.executeQuery();
                            if (numberOfNames.next()) {
                                if (numberOfNames.getInt(1) == 0) {
                                    System.out.println("This meal doesn't exist. Choose a meal from the list above.");
                                } else {
                                    nameExistsInDb = true;
                                }
                            }
                        }

                        PreparedStatement insertStatement = connection.prepareStatement(SQLQueries.insertIntoTable(
                                "plan", "meal_category", "meal_name", "meal_id"));

                        insertStatement.setString(1, mealCategory);
                        insertStatement.setString(2, mealName);

                        ResultSet mealIdQuery = connection.createStatement()
                                .executeQuery(SQLQueries.getMealIdByMealName(mealName));
                        int mealId;
                        if (mealIdQuery.next()) {
                            mealId = mealIdQuery.getInt("id");
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
        try (ResultSet rs = connection.createStatement().executeQuery(SQLQueries.countMeals())) {
            if (rs.next()) {
                mealCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            PreparedStatement mealStmt = connection.prepareStatement(SQLQueries.insertIntoTable(
                    "meals", "category", "name"));
            PreparedStatement ingredientStmt = connection.prepareStatement(SQLQueries.insertIntoTable(
                    "ingredients", "name", "meal_id"));

            mealStmt.setString(1, mealCategory.toLowerCase());
            mealStmt.setString(2, mealName.toLowerCase());
            mealStmt.executeUpdate();

            for (String ingredient : ingredients) {
                ingredientStmt.setString(1, ingredient);
                ingredientStmt.setInt(2, mealCount + 1);
                ingredientStmt.executeUpdate();
            }
            System.out.println("The meal has been added!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        DbConnection.finalize(connection);
    }

    private void save() {
        Connection connection = DbConnection.connect();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Input a filename:");
        String filename = scanner.nextLine();

        try {
            File file = new File(filename);
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));

            ResultSet ingredientAndCountQuery = connection.createStatement()
                    .executeQuery(SQLQueries.getIngredientsAndCountFromPlan());
            while (ingredientAndCountQuery.next()) {
                String ingredient = ingredientAndCountQuery.getString("name");
                int ingredientCount = ingredientAndCountQuery.getInt("count");

                if (ingredientCount == 1) {
                    bw.write(ingredient);
                    bw.newLine();
                } else {
                    bw.write(ingredient + " x" + ingredientCount);
                    bw.newLine();
                }
            }
            bw.close();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Saved!");
        DbConnection.finalize(connection);
    }
}
