package mealplanner;

import org.apache.commons.lang3.EnumUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class MealPlanner {
    private final ArrayList<Meal> meals;

    public MealPlanner() {
        meals = new ArrayList<>();
    }

    public void run() {
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
                printAllMeals();
            }
        }
    }

    public void addMeal() {
        Scanner scanner = new Scanner(System.in);

        String mealCategory;
        System.out.println("Which meal do you want to add (breakfast, lunch, dinner)?");
        do {
            mealCategory = scanner.nextLine();
            if (!EnumUtils.isValidEnum(MealCategory.class, mealCategory.toUpperCase())) {
                System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
            }
        } while (!EnumUtils.isValidEnum(MealCategory.class, mealCategory.toUpperCase()));
        MealCategory mealCategoryEnum = MealCategory.valueOf(mealCategory.toUpperCase());

        String mealName;
        System.out.println("Input the meal's name:");
        while (true) {
            mealName = scanner.nextLine();
            if (mealName.isEmpty() || mealName.isBlank() ||
                    !Arrays.stream(mealName.strip().split(" ")).allMatch(s -> s.chars().allMatch(Character::isLetter))) {
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
        meals.add(new Meal(mealCategoryEnum, mealName, ingredients));
        System.out.println("The meal has been added!");
    }

    public void printAllMeals() {
        System.out.println();
        if (meals.isEmpty()) {
            System.out.println("No meals saved. Add a meal first.");
        } else {
            for (Meal meal : meals) {
                System.out.println(meal);
                System.out.println();
            }
        }
    }
}
