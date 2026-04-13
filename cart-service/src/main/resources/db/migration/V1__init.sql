create table if not exists cart (
    id varchar(36) not null default (uuid()),
    user_id varchar(36) not null unique,
    total_items int default 0,
    total_price decimal(10, 2) default 0.0,
    primary key (id)
);

create table if not exists cart_item (
    id varchar(36) not null default (uuid()),
    cart_id varchar(36) not null,
    product_id varchar(36) not null,
    product_name varchar(50) not null,
    product_price decimal(10, 2) not null,
    quantity int not null default 1,
    subtotal decimal(10, 2) not null,
    primary key (id),
    unique key (cart_id, product_id),
    constraint fk_cart_item_cart_id
        foreign key (cart_id) references cart (id)
        on delete cascade
);
