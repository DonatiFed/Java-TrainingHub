package Model.WorkoutManagement.Tests;

import Model.WorkoutManagement.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class WorkoutManagementTest {
        @Test
        void testStrengthStrategy() {
            ExerciseIntensitySetter strength = ExerciseStrategyFactory.createStrategy("strength");
            assertEquals(5, strength.setNSets());
            assertEquals(3, strength.setNReps());
        }

        @Test
        void testHypertrophyStrategy() {
            ExerciseIntensitySetter hypertrophy = ExerciseStrategyFactory.createStrategy("hypertrophy");
            assertEquals(4, hypertrophy.setNSets());
            assertTrue(hypertrophy.setNReps() >= 8 && hypertrophy.setNReps() <= 12);
        }

        @Test
        void testEnduranceStrategy() {
            ExerciseIntensitySetter endurance = ExerciseStrategyFactory.createStrategy("endurance");
            assertEquals(3, endurance.setNSets());
            assertTrue(endurance.setNReps() >= 15);
        }

        @Test
        void testUnknownStrategy() {
            assertThrows(IllegalArgumentException.class, () -> ExerciseStrategyFactory.createStrategy("invalid"));
        }

        @Test
        void testWorkout4PlanFactory() {
            Workout4PlanFactory factory = new Workout4PlanFactory("Monday", "strength", 1);
            Workout4Plan plan = factory.createWorkout();
            assertEquals("Monday", plan.getDay());
            assertNotNull(plan.getStrategy());
            assertInstanceOf(StrengthExerciseSetter.class, plan.getStrategy());
            assertEquals(1, plan.getId());
        }

        @Test
        void testWorkout4RecordFactory() {
            Workout4RecordFactory factory = new Workout4RecordFactory("2025-05-25", 2);
            Workout4Record record = factory.createWorkout();
            assertEquals("2025-05-25", record.getDate());
            assertEquals(2, record.getId());
        }

        @Test
        void testExerciseCreationWithStrategy() {
            ExerciseIntensitySetter strengthStrategy = new StrengthExerciseSetter();
            Exercise exercise = new Exercise(1, "Squat", "Barbell squat", "Barbell", strengthStrategy);
            assertEquals(5, exercise.getN_sets());
            assertEquals(3, exercise.getN_reps());
            assertEquals(strengthStrategy, exercise.getStrategy());
        }

        @Test
        void testConfigureIntensity() {
            ExerciseIntensitySetter strengthStrategy = new StrengthExerciseSetter();
            Exercise exercise = new Exercise(1, "Bench Press", "Barbell bench", "Barbell", strengthStrategy);
            ExerciseIntensitySetter enduranceStrategy = new EnduranceExerciseSetter();
            exercise.setStrategy(enduranceStrategy);
            exercise.configureIntensity();
            assertEquals(3, exercise.getN_sets());
            assertTrue(exercise.getN_reps() >= 15);
            assertEquals(enduranceStrategy, exercise.getStrategy());
        }

        @Test
        void testWorkout4PlanInitialization() {
            Workout4Plan plan = new Workout4Plan("Tuesday", "hypertrophy", 3);
            assertEquals("Tuesday", plan.getDay());
            assertNotNull(plan.getStrategy());
            assertInstanceOf(HypertrophyExerciseSetter.class, plan.getStrategy());
            assertEquals(3, plan.getId());
            assertNotNull(plan.getExercises());
            assertTrue(plan.getExercises().isEmpty());
        }

        @Test
        void testWorkout4RecordInitialization() {
            Workout4Record record = new Workout4Record("2025-05-26", 4);
            assertEquals("2025-05-26", record.getDate());
            assertEquals(4, record.getId());
            assertNotNull(record.getExercises());
            assertTrue(record.getExercises().isEmpty());
        }
    }
