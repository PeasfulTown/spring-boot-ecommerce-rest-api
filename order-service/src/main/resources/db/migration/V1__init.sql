create table if not exists `order` (
    id varchar(36) not null,
    user_id varchar(36) not null,
    email varchar(50) not null,
    phone varchar(10) not null,
    number varchar(10) not null,
    street varchar(50) not null,
    city varchar(50) not null,
    state varchar(50) not null,
    country varchar(50) not null,
    postal_code varchar(50) not null,
    total_price decimal(10, 2) not null,
    item_count int unsigned not null,
    status enum("PROCESSING", "SHIPPED", "OUT_FOR_DELIVERY", "COMPLETED", "CANCELLED") not null,
    primary key (id)
);

create table if not exists order_item (
    id varchar(36) not null,
    product_id varchar(36) not null,
    product_name varchar(50) not null,
    product_price decimal(10, 2) not null,
    quantity int unsigned not null,
    subtotal decimal(10, 2) not null,
    order_id varchar(36) not null,
    primary key (id),
    constraint fk_order_item_order_id
        foreign key (order_id) references `order` (id)
);