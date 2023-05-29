package mealplanner;

import java.text.MessageFormat;

class SQLQueries {
    // DB Table Names
    static final String MEALS = "meals";
    static final String INGREDIENTS = "ingredients";
    static final String PLAN = "plan";

    // DB Column Names
    static final String MEAL = "meal";
    static final String MEAL_ID = "meal_id";
    static final String CATEGORY = "category";
    static final String INGREDIENT = "ingredient";
    static final String INGREDIENT_ID = "ingredient_id";

    static String getMealsByCategory(String mealCategory) {
        return MessageFormat.format(
                "SELECT {0}, {1} FROM {2} WHERE {3} LIKE ''{4}'' ORDER BY {0};",
                MEAL, MEAL_ID, MEALS, CATEGORY, mealCategory);
    }

    static String getNumberOfMeals() {
        return MessageFormat.format(
                "SELECT COUNT({0}) FROM {1};", MEAL_ID, MEALS);
    }

    static String getNumberOfMealsByName() {
        return MessageFormat.format(
                "SELECT COUNT({0}) FROM {1} WHERE {0} LIKE ?;",
                MEAL, MEALS);
    }

    static String getMealsFromPlan() {
        return MessageFormat.format(
                "SELECT {0}, {1} FROM {2};", CATEGORY, MEAL, PLAN);
    }

    static String getNumberOfIngredients() {
        return MessageFormat.format(
                "SELECT COUNT(DISTINCT {0}) FROM {1};", INGREDIENT, INGREDIENTS);
    }

    static String getIngredientIdByName() {
        return MessageFormat.format(
                "SELECT {0} FROM {1} WHERE {2} LIKE ?;",
                INGREDIENT_ID, INGREDIENTS, INGREDIENT);
    }

    static String getMealIdByMealName(String mealName) {
        return MessageFormat.format(
                "SELECT {0} FROM {1} WHERE {2} LIKE ''{3}'';",
                MEAL_ID, MEALS, MEAL, mealName);
    }

    static String getMealsAndIngredients() {
        return MessageFormat.format(
                "SELECT m.{0}, m.{1}, i.{2}, i.{3} " +
                        "FROM {4} m " +
                        "JOIN {5} i ON m.{3} = i.{3} " +
                        "ORDER BY m.{3};",
                MEAL, CATEGORY, INGREDIENT, MEAL_ID, MEALS, INGREDIENTS);
    }

    static String getMealsAndIngredientsByCategory(String category) {
        return MessageFormat.format(
                "SELECT m.{0}, m.{1}, i.{2}, i.{3} " +
                        "FROM {4} m " +
                        "JOIN {5} i ON m.{3} = i.{3} " +
                        "WHERE m.{1} LIKE ''{6}'' " +
                        "ORDER BY m.{3};",
                MEAL, CATEGORY, INGREDIENT, MEAL_ID, MEALS, INGREDIENTS, category);
    }

    static String insertIntoMeals() {
        return MessageFormat.format(
                "INSERT INTO {0} ({1}, {2}, {3}) VALUES (?, ?, ?);",
                MEALS, CATEGORY, MEAL, MEAL_ID);
    }

    static String insertIntoIngredients() {
        return MessageFormat.format(
                "INSERT INTO {0} ({1}, {2}, {3}) VALUES (?, ?, ?);",
                INGREDIENTS, INGREDIENT, INGREDIENT_ID, MEAL_ID);
    }

    static String insertIntoPlan() {
        return MessageFormat.format(
                "INSERT INTO {0} ({1}, {2}, {3}) VALUES (?, ?, ?);",
                PLAN, CATEGORY, MEAL, MEAL_ID);
    }

    static String createTablesIfNotExist() {
        return MessageFormat.format(
                """
                        CREATE TABLE IF NOT EXISTS {0} (
                            {1} VARCHAR,
                            {2} VARCHAR,
                            {3} INTEGER,
                            CONSTRAINT unique_meals UNIQUE ({2}, {3})
                        );
                                                
                        CREATE TABLE IF NOT EXISTS {4} (
                            {5} VARCHAR,
                            {6} INTEGER,
                            {3} INTEGER
                        );
                                                
                        CREATE TABLE IF NOT EXISTS {7} (
                            {1} VARCHAR,
                            {2} VARCHAR,
                            {3} INTEGER
                        );
                        """,
                MEALS, CATEGORY, MEAL, MEAL_ID, INGREDIENTS,
                INGREDIENT, INGREDIENT_ID, PLAN);
    }
}
