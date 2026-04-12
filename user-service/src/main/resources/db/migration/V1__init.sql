create table if not exists `user` (
    id varchar(36) not null,
    email varchar(50) not null unique,
    first_name varchar(50) not null,
    last_name varchar(50) not null,
    phone varchar(10) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    primary key (id)
);
