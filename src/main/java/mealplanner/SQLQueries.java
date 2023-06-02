package mealplanner;

import java.text.MessageFormat;

class SQLQueries {
    static String getMealsByCategory(String mealCategory) {
        return MessageFormat.format(
                "SELECT name, id FROM meals WHERE category LIKE ''{0}'' ORDER BY name;",
                mealCategory);
    }

    static String countMeals() {
        return "SELECT COUNT(id) FROM meals;";
    }

    static String countMealsByName() {
        return "SELECT COUNT(name) FROM meals WHERE name LIKE ?;";
    }

    static String getCategoriesMealsFromPlan() {
        return "SELECT meal_category, meal_name FROM plan;";
    }

    static String getMealIdByMealName(String mealName) {
        return MessageFormat.format(
                "SELECT id FROM meals WHERE name LIKE ''{0}'';", mealName);
    }

    static String getMealsAndIngredients() {
        return """
                SELECT m.name "meal", m.category, i.name "ingredient", i.meal_id
                FROM meals m JOIN ingredients i ON m.id = i.meal_id
                ORDER BY m.id;
                """;
    }

    static String getMealsAndIngredientsByCategory(String category) {
        return MessageFormat.format(
                """
                        SELECT m.name "meal", m.category, i.name "ingredient", i.meal_id
                        FROM meals m JOIN ingredients i ON m.id = i.meal_id
                        WHERE m.category LIKE ''{0}'' ORDER BY m.id;
                        """, category);
    }

    static String countMealsInPlan() {
        return "SELECT COUNT(meal_name) FROM plan;";
    }

    static String getIngredientsAndCountFromPlan() {
        return """
                SELECT i.name, COUNT(*) AS count
                FROM ingredients i
                JOIN meals m ON i.meal_id = m.id
                JOIN plan p ON m.id = p.meal_id
                GROUP BY i.name;
                """;
    }

    static String insertIntoTable(String tableName, String column1, String column2) {
        return MessageFormat.format(
                "INSERT INTO {0} ({1}, {2}) VALUES (?, ?);",
                tableName, column1, column2);
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
                     name VARCHAR(50) NOT NULL,
                     id SERIAL PRIMARY KEY
                 );
                """;
    }

    static String createIngredientsTable() {
        return """
                CREATE TABLE ingredients (
                    name VARCHAR(50) NOT NULL,
                    meal_id INT,
                    PRIMARY KEY (name, meal_id),
                    FOREIGN KEY (meal_id) REFERENCES meals(id)
                );
                """;
    }

    static String createPlanTable() {
        return """
                CREATE TABLE plan (
                    meal_category VARCHAR(10) NOT NULL,
                    meal_name VARCHAR(50) NOT NULL,
                    meal_id INT,
                    FOREIGN KEY (meal_id) REFERENCES meals(id)
                );
                """;
    }
}
