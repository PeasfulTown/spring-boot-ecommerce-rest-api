create table if not exists inventory (
    id varchar(36) not null,
    product_id varchar(36) not null,
    stock int unsigned not null default 0,
    primary key (id),
    unique key (product_id)
);