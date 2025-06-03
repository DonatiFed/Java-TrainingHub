-- Users Table (Trainees & PTs)
CREATE TABLE AppUser (
    user_id SERIAL PRIMARY KEY,
    user_name VARCHAR(50) NOT NULL,
    user_age INT,
    is_pt BOOLEAN NOT NULL DEFAULT FALSE
);

-- Personal Trainers Table (Subset of Users)
CREATE TABLE Personal_Trainer (
    pt_id INT PRIMARY KEY REFERENCES AppUser(user_id) ON DELETE CASCADE
);

-- Workout Plans Table
CREATE TABLE WorkoutPlans (
    wp_id SERIAL PRIMARY KEY,
    last_edit_date DATE NOT NULL
);

-- Workout4Plans Table (Represents a specific workout session within a plan)
CREATE TABLE Workout4Plan (
    w4p_id SERIAL PRIMARY KEY,
    strategy VARCHAR(50),
    day VARCHAR(50)
);

-- Exercises Table
CREATE TABLE Exercises (
    ex_id SERIAL PRIMARY KEY,
    exercise_name VARCHAR(50) NOT NULL,
    exercise_description VARCHAR(255),
    exercise_equipment VARCHAR(50),
    exercise_N_sets INT,
    exercise_N_reps INT,
    exercise_weight INT,
    exercise_strategy VARCHAR(50)
);

-- Workout Records Table
CREATE TABLE WorkoutRecords (
    wr_id SERIAL PRIMARY KEY,
    last_edit_date DATE NOT NULL,
    N_workouts INT
);

-- Workout4Record Table (Represents a specific workout session that was performed)
CREATE TABLE Workout4Record (
    w4r_id SERIAL PRIMARY KEY, -- Corrected typo from "w4r id" to "w4r_id" for consistency
    wr_id INT REFERENCES WorkoutRecords(wr_id) ON DELETE CASCADE,
    date DATE NOT NULL
);

-- Step 4: Relationship Tables (Many-to-Many and linking tables)

-- Workout Plans Assigned to PTs & Trainees (Linking WorkoutPlan to a Trainee, assigned by a PT)
CREATE TABLE WorkoutPlans_PersonalTrainer_AppUser (
    wp_id INT REFERENCES WorkoutPlans(wp_id) ON DELETE CASCADE,
    pt_id INT REFERENCES Personal_Trainer(pt_id) ON DELETE CASCADE,
    trainee_id INT REFERENCES AppUser(user_id) ON DELETE CASCADE,
    PRIMARY KEY (wp_id, trainee_id) -- Ensures each trainee has only one instance of a specific plan directly assigned this way.
                                     -- If a trainee can have multiple different plans or the same plan multiple times (e.g. historical), this PK might need adjustment or this table represents current assignment.
);

-- Link Workout4Plans to WorkoutPlans (Many-to-many: A WorkoutPlan can have many Workout4Plan sessions, a Workout4Plan session can be part of many WorkoutPlans - though less likely for the latter)
CREATE TABLE WorkoutPlans_Workout4Plans (
    wp_id INT REFERENCES WorkoutPlans(wp_id) ON DELETE CASCADE,
    w4p_id INT REFERENCES Workout4Plan(w4p_id) ON DELETE CASCADE, -- Corrected to reference Workout4Plan
    PRIMARY KEY (wp_id, w4p_id)
);

-- Link Exercises to Workout4Plans (Many-to-many: A Workout4Plan session contains many Exercises, an Exercise can be in many Workout4Plan sessions)
CREATE TABLE Workout4Plan_Exercises (
    w4p_id INT REFERENCES Workout4Plan(w4p_id) ON DELETE CASCADE,
    ex_id INT REFERENCES Exercises(ex_id) ON DELETE CASCADE,
    PRIMARY KEY (w4p_id, ex_id)
);

-- Link Exercises to Workout4Records (Many-to-many: A Workout4Record session contains many Exercises, an Exercise can be in many Workout4Record sessions)
CREATE TABLE Workout4Record_Exercises (
    w4r_id INT REFERENCES Workout4Record(w4r_id) ON DELETE CASCADE,
    ex_id INT REFERENCES Exercises(ex_id) ON DELETE CASCADE,
    PRIMARY KEY (w4r_id, ex_id)
);

-- Link Workout4Records to WorkoutRecords (Many-to-many or one-to-many based on PK. The PDF structure implies one Workout4Record belongs to one WorkoutRecord via FK in Workout4Record table itself. This linking table suggests a Workout4Record could link to multiple WorkoutRecords, which seems unusual given the direct FK already. Clarification might be needed on its exact purpose, or if Workout4Record.wr_id already handles this.)
--It's just a redundant table
CREATE TABLE Workout4Record_WorkoutRecords (
    w4r_id INT REFERENCES Workout4Record(w4r_id) ON DELETE CASCADE,
    wr_id INT REFERENCES WorkoutRecords(wr_id) ON DELETE CASCADE,
    PRIMARY KEY (w4r_id, wr_id)
);

-- Link WorkoutRecords to AppUsers (Many-to-many or one-to-many based on PK. If one WorkoutRecord belongs to one AppUser, an FK in WorkoutRecords table is more common. This table allows a WorkoutRecord to be associated with multiple users or a user to have multiple WorkoutRecords, the latter is normal.)
-- Assuming a user has one main WorkoutRecords aggregate. If WorkoutRecords.wr_id is already user-specific (e.g. if AppUser has a FK to WorkoutRecords), this table might be for a different relationship or sharing.
-- The PDF schema implies wr_id is a general ID for a record log, and this table links which user owns which log.
--Also this is useless
CREATE TABLE WorkoutRecords_AppUser (
    wr_id INT REFERENCES WorkoutRecords(wr_id) ON DELETE CASCADE,
    user_id INT REFERENCES AppUser(user_id) ON DELETE CASCADE, -- Corrected typo from "user id" to "user_id"
    PRIMARY KEY (wr_id, user_id) -- This implies a WorkoutRecord can only be linked to a specific user once. Usually (wr_id) would be PK if it's one-to-one with user, or user_id would be part of PK if a user has many records. This PK (wr_id, user_id) suggests wr_id is unique and it's linked to a unique user_id. If a user has one WorkoutRecords entry, then user_id should be the PK here, or wr_id should be unique.
                                  -- Given WorkoutRecords.wr_id is SERIAL PK, and a user has one WorkoutRecords aggregate, this table should probably be:
                                  -- user_id INT PRIMARY KEY REFERENCES AppUser(user_id) ON DELETE CASCADE,
                                  -- wr_id INT UNIQUE REFERENCES WorkoutRecords(wr_id) ON DELETE CASCADE
                                  -- Or simply add user_id to WorkoutRecords table with a FK.

);