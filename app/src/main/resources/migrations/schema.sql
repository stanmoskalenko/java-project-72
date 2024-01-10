create table if not exists urls (
    id bigint generated always as identity primary key,
    name varchar(255) not null,
    created_at timestamp default now()
);

create table if not exists url_checks (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    status_code integer NOT NULL,
    title varchar NOT NULL,
    h1 varchar,
    description varchar,
    url_id bigint references urls(id) not null,
    created_at timestamp default now()
);
