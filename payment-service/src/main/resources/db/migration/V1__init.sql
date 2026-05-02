create table if not exists payment (
    id varchar(36) not null,
    order_id varchar(36) not null,
    user_id varchar(36) not null,
    transaction_id varchar(255),
    card_token varchar(255),
    amount decimal(10,2) not null,
    payment_status enum("PROCESSING", "SUCCESS", "FAILED") default "PROCESSING",
    created_at timestamp not null default current_timestamp,
    note varchar(255),
    primary key (id)
);