CREATE TABLE restaurants (
    id INTEGER PRIMARY KEY,
    city TEXT NOT NULL,
    address TEXT NOT NULL,
    manager TEXT NOT NULL
);

CREATE TABLE vacancies (
    id INTEGER PRIMARY KEY,
    restaurant_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    shift_name TEXT NOT NULL,
    salary TEXT NOT NULL,
    experience_months INTEGER NOT NULL,
    priority TEXT NOT NULL,
    status TEXT NOT NULL,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);

CREATE TABLE candidates (
    id INTEGER PRIMARY KEY,
    full_name TEXT NOT NULL,
    age INTEGER NOT NULL,
    target_role TEXT NOT NULL,
    experience_months INTEGER NOT NULL,
    availability TEXT NOT NULL,
    rating REAL NOT NULL,
    phone TEXT NOT NULL,
    city TEXT NOT NULL,
    status TEXT NOT NULL
);

CREATE TABLE applications (
    id INTEGER PRIMARY KEY,
    vacancy_id INTEGER NOT NULL,
    candidate_id INTEGER NOT NULL,
    score INTEGER NOT NULL,
    interview_date TEXT NOT NULL,
    stage TEXT NOT NULL,
    FOREIGN KEY (vacancy_id) REFERENCES vacancies(id),
    FOREIGN KEY (candidate_id) REFERENCES candidates(id)
);

CREATE INDEX idx_vacancies_restaurant_id ON vacancies(restaurant_id);
CREATE INDEX idx_vacancies_status_priority ON vacancies(status, priority);
CREATE INDEX idx_candidates_status_city ON candidates(status, city);
CREATE INDEX idx_applications_vacancy_id ON applications(vacancy_id);
CREATE INDEX idx_applications_candidate_id ON applications(candidate_id);

INSERT INTO restaurants (id, city, address, manager) VALUES
(1, 'Москва', 'ул. Тверская, 12', 'Ирина Петрова'),
(2, 'Санкт-Петербург', 'Невский пр., 44', 'Максим Орлов'),
(3, 'Казань', 'ул. Баумана, 19', 'Алина Галимова'),
(4, 'Екатеринбург', 'ул. Малышева, 51', 'Дмитрий Климов'),
(5, 'Новосибирск', 'Красный проспект, 71', 'Светлана Белова');

INSERT INTO vacancies (id, restaurant_id, title, shift_name, salary, experience_months, priority, status) VALUES
(1, 1, 'Менеджер смены', '2/2, день', '85 000 ₽', 12, 'Высокий', 'Открыта'),
(2, 1, 'Кассир', 'Гибкий график', '62 000 ₽', 3, 'Средний', 'Открыта'),
(3, 2, 'Повар-универсал', '5/2, вечер', '74 000 ₽', 6, 'Высокий', 'Открыта'),
(4, 2, 'Сотрудник зала', '2/2, утро', '58 000 ₽', 0, 'Низкий', 'Открыта'),
(5, 3, 'Директор ресторана', '5/2', '120 000 ₽', 24, 'Высокий', 'Открыта'),
(6, 4, 'Курьер', 'Свободный график', '68 000 ₽', 2, 'Средний', 'Приостановлена'),
(7, 4, 'Бариста-кассир', '2/2, вечер', '64 000 ₽', 4, 'Средний', 'Открыта'),
(8, 5, 'Менеджер доставки', '5/2', '82 000 ₽', 10, 'Высокий', 'Открыта');

INSERT INTO candidates (id, full_name, age, target_role, experience_months, availability, rating, phone, city, status) VALUES
(1, 'Анна Соколова', 24, 'Кассир', 8, 'Может выйти через 3 дня', 4.6, '+7 900 100-10-10', 'Москва', 'Скрининг'),
(2, 'Роман Егоров', 31, 'Менеджер смены', 18, 'Доступен сразу', 4.8, '+7 900 100-20-20', 'Москва', 'Интервью'),
(3, 'Полина Ким', 27, 'Повар-универсал', 15, 'Через неделю', 4.7, '+7 900 100-30-30', 'Санкт-Петербург', 'Интервью'),
(4, 'Тимур Набиуллин', 22, 'Сотрудник зала', 5, 'Доступен сразу', 4.3, '+7 900 100-40-40', 'Казань', 'Новый'),
(5, 'Ольга Власова', 35, 'Директор ресторана', 42, 'Через 2 недели', 4.9, '+7 900 100-50-50', 'Казань', 'Финал'),
(6, 'Сергей Миронов', 26, 'Курьер', 7, 'На следующей неделе', 4.2, '+7 900 100-60-60', 'Екатеринбург', 'Скрининг'),
(7, 'Екатерина Ли', 23, 'Бариста-кассир', 9, 'Доступна сразу', 4.5, '+7 900 100-70-70', 'Екатеринбург', 'Интервью'),
(8, 'Никита Павлов', 29, 'Менеджер доставки', 14, 'Через 4 дня', 4.4, '+7 900 100-80-80', 'Новосибирск', 'Скрининг');

INSERT INTO applications (id, vacancy_id, candidate_id, score, interview_date, stage) VALUES
(1, 1, 2, 94, '2026-07-02 11:00', 'Интервью'),
(2, 2, 1, 89, '2026-07-03 14:30', 'Скрининг'),
(3, 3, 3, 92, '2026-07-01 16:00', 'Интервью'),
(4, 4, 4, 78, '2026-07-05 10:00', 'Скрининг'),
(5, 5, 5, 97, '2026-07-04 13:00', 'Финал'),
(6, 6, 6, 80, '2026-07-06 09:30', 'Скрининг'),
(7, 7, 7, 88, '2026-07-02 15:00', 'Интервью'),
(8, 8, 8, 91, '2026-07-03 12:00', 'Интервью');
