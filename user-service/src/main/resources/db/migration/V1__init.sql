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

create table if not exists address (
    id varchar(36) not null default (uuid()),
    user_id varchar(36) not null,
    number varchar(50) not null,
    street varchar(50) not null,
    city varchar(50) not null,
    state varchar(50) not null,
    country varchar(50) not null,
    postal_code varchar(50) not null,
    is_primary bit default 0,
    primary key (id),
    constraint fk_address_user_id
        foreign key (user_id) references `user` (id),
    unique (user_id, number, street, city, state, postal_code)
);