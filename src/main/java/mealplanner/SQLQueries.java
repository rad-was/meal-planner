package mealplanner;

import java.text.MessageFormat;

class SQLQueries {
    static String getMealsByCategory(String mealCategory) {
        return MessageFormat.format(
                "SELECT meal, meal_id FROM meals WHERE category LIKE ''{0}'' ORDER BY meal;",
                mealCategory);
    }

    static String countMeals() {
        return "SELECT COUNT(meal_id) FROM meals;";
    }

    static String countMealsByName() {
        return "SELECT COUNT(meal) FROM meals WHERE meal LIKE ?;";
    }

    static String getCategoriesMealsFromPlan() {
        return "SELECT category, meal FROM plan;";
    }

    static String countIngredients() {
        return "SELECT COUNT(DISTINCT ingredient) FROM ingredients;";
    }

    static String getIngredientIdByName() {
        return "SELECT ingredient_id FROM ingredients WHERE ingredient LIKE ?;";
    }

    static String getMealIdByMealName(String mealName) {
        return MessageFormat.format(
                "SELECT meal_id FROM meals WHERE meal LIKE ''{0}'';", mealName);
    }

    static String getMealsAndIngredients() {
        return "SELECT m.meal, m.category, i.ingredient, i.meal_id " +
                "FROM meals m JOIN ingredients i ON m.meal_id = i.meal_id " +
                "ORDER BY m.meal_id;";
    }

    static String getMealsAndIngredientsByCategory(String category) {
        return MessageFormat.format(
                "SELECT m.meal, m.category, i.ingredient, i.meal_id " +
                        "FROM meals m JOIN ingredients i ON m.meal_id = i.meal_id " +
                        "WHERE m.category LIKE ''{0}'' ORDER BY m.meal_id;", category);
    }

    static String countMealsInPlan() {
        return "SELECT COUNT(meal) FROM plan;";
    }

    static String getIngredientsAndCountFromPlan() {
        return "SELECT i.ingredient, COUNT(*) AS count " +
                "FROM ingredients i " +
                "JOIN meals m ON i.meal_id = m.meal_id " +
                "JOIN plan p ON m.meal_id = p.meal_id " +
                "GROUP BY i.ingredient;";
    }

    @SuppressWarnings("SameParameterValue")
    static String insertIntoTable(String tableName, String column1, String column2, String column3) {
        return MessageFormat.format(
                "INSERT INTO {0} ({1}, {2}, {3}) VALUES (?, ?, ?);",
                tableName, column1, column2, column3);
    }

    static String checkIfTableExists(String tableName) {
        return MessageFormat.format(
                """
                        SELECT EXISTS (
                            SELECT 1
                            FROM information_schema.tables
                            WHERE table_schema = ''public''
                                AND table_name = ''{0}''
                        );
                        """, tableName);
    }

    static String createMealsTable() {
        return """
                 CREATE TABLE meals (
                     category VARCHAR(10) NOT NULL,
                     meal VARCHAR(50) NOT NULL,
                     meal_id INT NOT NULL
                 );
                """;
    }

    static String createIngredientsTable() {
        return """
                CREATE TABLE ingredients (
                    ingredient VARCHAR(50) NOT NULL,
                    ingredient_id INT NOT NULL,
                    meal_id INT NOT NULL
                );
                """;
    }

    static String createPlanTable() {
        return """
                CREATE TABLE plan (
                    category VARCHAR(10) NOT NULL,
                    meal VARCHAR(50) NOT NULL,
                    meal_id INT NOT NULL
                );
                """;
    }
}
