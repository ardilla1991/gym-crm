-- Executed by Hibernate right after the tables are created (see jpa.properties).
-- One statement per line.

-- Training types: a constant list, ids are fixed.
INSERT INTO training_type (id, training_type_name) VALUES (1, 'FITNESS');
INSERT INTO training_type (id, training_type_name) VALUES (2, 'YOGA');
INSERT INTO training_type (id, training_type_name) VALUES (3, 'ZUMBA');
INSERT INTO training_type (id, training_type_name) VALUES (4, 'STRETCHING');
INSERT INTO training_type (id, training_type_name) VALUES (5, 'RESISTANCE');

-- Trainees
INSERT INTO users (first_name, last_name, username, password, is_active) VALUES ('Emily', 'Johnson', 'Emily.Johnson', 'Hq5sW1dF6y', true);
INSERT INTO trainee (date_of_birth, address, user_id) VALUES ('1995-04-12', '12 Oak Street, Boston', (SELECT id FROM users WHERE username = 'Emily.Johnson'));

INSERT INTO users (first_name, last_name, username, password, is_active) VALUES ('Michael', 'Brown', 'Michael.Brown', 'Jp2gK8mT4n', true);
INSERT INTO trainee (date_of_birth, address, user_id) VALUES ('1990-09-30', '45 Pine Avenue, Chicago', (SELECT id FROM users WHERE username = 'Michael.Brown'));

-- Trainers
INSERT INTO users (first_name, last_name, username, password, is_active) VALUES ('David', 'Miller', 'David.Miller', 'Xk3mP9qL2w', true);
INSERT INTO trainer (specialization_id, user_id) VALUES (2, (SELECT id FROM users WHERE username = 'David.Miller'));

INSERT INTO users (first_name, last_name, username, password, is_active) VALUES ('Jessica', 'Wilson', 'Jessica.Wilson', 'Rt7vN4bZ8c', true);
INSERT INTO trainer (specialization_id, user_id) VALUES (1, (SELECT id FROM users WHERE username = 'Jessica.Wilson'));

-- Trainee-trainer assignments
INSERT INTO trainee_trainer (trainee_id, trainer_id) VALUES ((SELECT t.id FROM trainee t JOIN users u ON t.user_id = u.id WHERE u.username = 'Emily.Johnson'), (SELECT t.id FROM trainer t JOIN users u ON t.user_id = u.id WHERE u.username = 'David.Miller'));
INSERT INTO trainee_trainer (trainee_id, trainer_id) VALUES ((SELECT t.id FROM trainee t JOIN users u ON t.user_id = u.id WHERE u.username = 'Michael.Brown'), (SELECT t.id FROM trainer t JOIN users u ON t.user_id = u.id WHERE u.username = 'Jessica.Wilson'));

-- Trainings
INSERT INTO training (trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) VALUES ((SELECT t.id FROM trainee t JOIN users u ON t.user_id = u.id WHERE u.username = 'Emily.Johnson'), (SELECT t.id FROM trainer t JOIN users u ON t.user_id = u.id WHERE u.username = 'David.Miller'), 'Morning yoga', 2, '2026-10-05', 60);
INSERT INTO training (trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) VALUES ((SELECT t.id FROM trainee t JOIN users u ON t.user_id = u.id WHERE u.username = 'Michael.Brown'), (SELECT t.id FROM trainer t JOIN users u ON t.user_id = u.id WHERE u.username = 'Jessica.Wilson'), 'Full body workout', 1, '2026-10-07', 45);