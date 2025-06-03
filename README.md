# Java-TrainingHub

## Overview

Workout Java-TrainingHub is a Java-based application designed to help trainees manage their workouts and enable personal trainers to guide their clients effectively. Trainees can log their workout sessions, view personalized plans created by their trainers, and track their progress. Personal trainers can manage their trainees, create and assign tailored workout plans, monitor trainee activity, and also use the app for their personal fitness tracking.

The application emphasizes clear communication and progress tracking between personal trainers and their trainees, featuring real-time notifications for plan updates.

##  Key Features

### User Profiles
* **Trainee:** Can perform actions related to their personal workout journey.
* **Personal Trainer (PT):** Has all Trainee functionalities and additional features to manage and guide their clients. A Personal Trainer can also be a Trainee, using the app for their own workouts.

### Trainee Features
* **View Workout Records:** Access a complete history of all logged workout sessions.
* **View Assigned Workout Plan:** See the detailed workout plan assigned by their personal trainer.
* **Workout Plan Update Notifications:** Receive real-time notifications when their personal trainer modifies their assigned workout plan.
* **Upload Completed Workouts:** Log details of each workout session performed.


### Personal Trainer Features
* **All Trainee Features:** Can use the app for their personal workouts just like any trainee.
* **Create & Manage Workout Plans:** Design new workout plans, edit existing ones, and delete plans.
* **Assign Plans to Trainees:** Link specific workout plans to their followed trainees.
* **View Trainee Workout Records:** Monitor the workout history and progress of each trainee they follow.
* **Manage Trainee Roster:** Add or remove trainees from their client list.

## Conceptual Model & Design Insights

The application's design incorporates several key concepts and patterns:

* **Core Entities:** The system revolves around `Users` (Trainees, PersonalTrainers), `WorkoutPlans`, `WorkoutRecords`, individual `Workouts` (like `Workout4Plan` for planned workouts and `Workout4Record` for logged sessions), and `Exercises`.
* **User Interaction:**
    * Trainees and Personal Trainers have distinct roles and capabilities, managed under `UserManagement`.
    * Personal Trainers can follow multiple Trainees.
* **Workout Structure:**
    * `WorkoutPlan`s are composed of several planned workout sessions (`Workout4Plan`).
    * `WorkoutRecord`s accumulate a user's logged workout sessions (`Workout4Record`).
* **Observer Pattern:** Used to notify Trainees when their `WorkoutPlan` is updated by a Personal Trainer, ensuring they always have the latest version.
* **Strategy Pattern:** Exercises can have different intensity or execution strategies (e.g., Strength, Hypertrophy, Endurance). This pattern allows for flexible definition and application of these strategies to exercises.
* **Factory Patterns:** Used for the creation of complex objects like different types of workouts or exercises, promoting loose coupling and easier extension (e.g., `WorkoutFactory`, `Workout4PlanFactory`, `ExerciseStrategyFactory`).

## Technologies Used (Tech Stack)

* **Language:** Java (Version 11 or as specified in `pom.xml`)
* **Build Tool & Dependency Management:** Apache Maven
* **Database:** PostgreSQL
* **Testing Frameworks:**
    * JUnit 5 (for unit and integration testing)
    * Mockito (for creating mock objects in tests)

## Project Structure

The project follows the standard Maven directory layout:
* `pom.xml`: Maven project configuration, including dependencies.
* `src/main/java/`: Main application source code, organized by packages (e.g., `ORM`, `Model`, `BusinessLogic`).
* `src/main/resources/`: Resource files for the main application.
* `src/test/java/`: Test source code, mirroring the main package structure.
* `Design/`: Contains project design documents, including class diagrams, ER models, and use case specifications.
* `target/`: Directory where Maven places compiled code and packaged artifacts (not version controlled).

## Getting Started (For Developers)

### Prerequisites
* Java Development Kit (JDK) - Version 11 or as specified in `pom.xml`.
* Apache Maven.
* PostgreSQL database server installed and running.
* Git (for cloning the repository).

### Setup & Installation
1.  **Clone the repository:**
    ```bash
    git clone [URL_OF_YOUR_REPOSITORY]
    cd [PROJECT_DIRECTORY_NAME]
    ```
2.  **Database Setup:**
    * Ensure your PostgreSQL server is running.
    * Create a dedicated database for the application.
    * Configure the database connection details. (`DatabaseManager.java` needs manual setup. Tables creation file can be found at src/main/resources/sql)
  
3.  **Build the project using Maven:**
    ```bash
    mvn clean install
    ```
    This command will compile the code, run tests, and package the application (e.g., into a JAR file in the `target/` directory).

### Running the Application
* **(if run via IntelliJ IDEA):**
    * Import the project as a Maven project.
    * Locate the main class and run it.

## Running Tests

All tests can be executed directly within your Integrated Development Environment (IDE), such as IntelliJ IDEA.

* **In IntelliJ IDEA:**
    * Navigate to a specific test class (e.g., in `src/test/java/`).
    * Right-click on the test class or a specific test method.
    * Select "Run 'YourTestClassName'" or "Run 'yourTestMethodName()'".
    * Alternatively, you can run all tests in the project by right-clicking on the `src/test/java` directory and selecting "Run 'All Tests'".
## Future Enhancements

We are continuously looking to evolve Java-TrainingHub to offer even more value to trainees and personal trainers. Planned future enhancements include:

* **Graphical User Interface (GUI):** Development of a user-friendly graphical interface to enhance usability and provide a more intuitive experience, moving beyond the current command-line interface.
* **AI-Powered Workout Plan Generation:** Integration of artificial intelligence to suggest or generate personalized workout plans based on trainee progress, goals, historical performance, and common fitness principles.
* **Advanced Analytics & Reporting:** Implementation of more sophisticated data visualization and reporting features for trainees and trainers to track long-term progress, identify trends, and make data-driven decisions.
* **External API Integrations:** Exploring integrations with popular fitness trackers (e.g., wearable devices) or nutrition tracking services to provide a more holistic view of a user's health and fitness journey.
* **Cloud Deployment:** Investigating options for deploying the application to a cloud platform to enable broader accessibility and scalability.
