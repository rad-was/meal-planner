# meal-planner

The Meal Planner Application is a Java program that allows users to save and manage meals with their properties, such as category, name, and necessary ingredients. The application stores all the meal data in a Postgres database, ensuring that the information is persisted after closing and reopening the app.

### Features
- **Add New Meals:** Users can add new meals by providing the category, name, and necessary ingredients. The data is stored in the database for future access.
- **Show Meals:** Users can view the meals stored in the database. The "show" command allows users to display meals based on the chosen category, making it easier to find specific types of meals.
- **Plan Meals for the Week:** Users can utilize the "plan" command to generate a daily meal plan for the whole week. The application selects meals from the saved options and creates a plan for each day.
- **Generate Shopping List:** The application generates a shopping list containing all the required ingredients for the planned meals. The shopping list is saved to a file along with the weekly meal plan, making it convenient for users to reference when shopping for ingredients.

### How to Use
- Make sure you have Docker and a JDK installed.
- Navigate to the project directory.
- Build the Docker image and start the database container:
`docker-compose up -d`
- Wait for the Docker container to start, then run the application with the following command:
`java -jar target/*-jar-with-dependencies.jar`
- The application will launch and display the menu in the terminal.
- Use the available commands to add new meals, show meals, filter by category, plan meals, and generate the shopping list.
- Follow the prompts and instructions provided by the application to interact with the various features.
- To stop the container, enter:
`docker-compose stop`
