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

create table if not exists card (
    id varchar(36) not null,
    user_id varchar(36) not null,
    card_holder_name varchar(100) not null,
    last_four_digits varchar(4) not null,
    card_type enum("VISA", "MASTERCARD", "AMEX") not null,
    expiry_month int NOT NULL,
    expiry_year int NOT NULL,
    token varchar(255) not null unique,
    is_default boolean not null default false,
    primary key (id),
    constraint fk_payment_card_user_id
        foreign key (user_id) references `user` (id)
        on delete cascade
);