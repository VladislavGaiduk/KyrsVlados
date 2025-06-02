-- Roles Table
CREATE TABLE Roles (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE
);

-- Persons Table
CREATE TABLE Persons (
                         id SERIAL PRIMARY KEY,
                         first_name VARCHAR(50) NOT NULL,
                         email VARCHAR(50),
                         last_name VARCHAR(50) NOT NULL,
                         patronomic VARCHAR(50)
);

-- Users Table
CREATE TABLE Users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role_id INT NOT NULL,
                       person_id INT NOT NULL UNIQUE,
                       FOREIGN KEY (role_id) REFERENCES Roles(id) ON DELETE CASCADE ON UPDATE CASCADE,
                       FOREIGN KEY (person_id) REFERENCES Persons(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Genres Table
CREATE TABLE genres (
                        id SERIAL PRIMARY KEY,
                        name VARCHAR(255) NOT NULL UNIQUE CHECK (TRIM(name) <> '')
);

-- Movies Table
CREATE TABLE movies (
                        id SERIAL PRIMARY KEY,
                        title VARCHAR(255) NOT NULL CHECK (TRIM(title) <> ''),
                        rating FLOAT NOT NULL CHECK (rating >= 0 AND rating <= 10.0),
                        year INT NOT NULL CHECK (year >= 1888 AND year <= EXTRACT(YEAR FROM CURRENT_DATE)),
    genre_id INT NOT NULL,
    duration_minutes INT NOT NULL DEFAULT 120,
    FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE,
    CONSTRAINT unique_movie UNIQUE (title, year)
);

-- Halls Table
CREATE TABLE halls (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL UNIQUE,
                       capacity INT NOT NULL CHECK (capacity > 0),
                       description TEXT
);

-- Sessions Table
CREATE TABLE sessions (
                          id SERIAL PRIMARY KEY,
                          movie_id INT NOT NULL,
                          hall_id INT NOT NULL,
                          start_time TIMESTAMP NOT NULL,
                          end_time TIMESTAMP NOT NULL,
                          price FLOAT NOT NULL CHECK (price >= 0),
                          FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
                          FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE,
                          CONSTRAINT valid_session_time CHECK (end_time > start_time)
);

-- Tickets Table
CREATE TABLE tickets (
                         id SERIAL PRIMARY KEY,
                         session_id INT NOT NULL,
                         user_id INT NOT NULL,
                         seat_number INT NOT NULL,
                         purchase_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE,
                         FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
                         UNIQUE(session_id, seat_number)
);

-- Indexes for better performance
CREATE INDEX idx_movie_title ON movies(title);
CREATE INDEX idx_movie_year ON movies(year);
CREATE INDEX idx_movie_genre_id ON movies(genre_id);
CREATE INDEX idx_sessions_movie_id ON sessions(movie_id);
CREATE INDEX idx_sessions_hall_id ON sessions(hall_id);
CREATE INDEX idx_sessions_time ON sessions(start_time, end_time);
CREATE INDEX idx_tickets_session_id ON tickets(session_id);
CREATE INDEX idx_tickets_user_id ON tickets(user_id);

-- Insert sample data for genres
INSERT INTO genres (name) VALUES
                              ('Драма'),
                              ('Комедия'),
                              ('Фантастика'),
                              ('Боевик'),
                              ('Триллер')
    ON CONFLICT (name) DO NOTHING;

-- Insert sample data for movies
INSERT INTO movies (title, rating, year, genre_id, duration_minutes) VALUES
                                                                         ('Побег из Шоушенка', 9.3, 1994, (SELECT id FROM genres WHERE name = 'Драма'), 142),
                                                                         ('Зеленая миля', 8.6, 1999, (SELECT id FROM genres WHERE name = 'Драма'), 189),
                                                                         ('Начало', 8.8, 2010, (SELECT id FROM genres WHERE name = 'Фантастика'), 148),
                                                                         ('Матрица', 8.7, 1999, (SELECT id FROM genres WHERE name = 'Боевик'), 136),
                                                                         ('Криминальное чтиво', 8.9, 1994, (SELECT id FROM genres WHERE name = 'Триллер'), 154)
    ON CONFLICT (title, year) DO NOTHING;

-- Insert sample data for halls
INSERT INTO halls (name, capacity, description) VALUES
                                                    ('Зал 1', 100, 'Основной зал с 3D проекцией'),
                                                    ('Зал 2', 50, 'Малый зал'),
                                                    ('VIP зал', 30, 'Премиум зал с диванами')
    ON CONFLICT (name) DO NOTHING;

-- Insert sample sessions
INSERT INTO sessions (movie_id, hall_id, start_time, end_time, price) VALUES
                                                                          (1, 1, '2025-06-03 12:00:00', '2025-06-03 14:22:00', 300.0),
                                                                          (3, 2, '2025-06-03 15:00:00', '2025-06-03 17:28:00', 350.0),
                                                                          (4, 3, '2025-06-03 18:00:00', '2025-06-03 20:16:00', 500.0);

-- Create database user and grant privileges
CREATE ROLE kino_user WITH LOGIN PASSWORD 'password123';
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO kino_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO kino_user;