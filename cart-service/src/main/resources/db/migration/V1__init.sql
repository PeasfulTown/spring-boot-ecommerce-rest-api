create table if not exists cart (
    id varchar(36) not null default (uuid()),
    user_id varchar(36) not null unique,
    primary key (id)
);

create table if not exists cart_item (
    id varchar(36) not null default (uuid()),
    cart_id varchar(36) not null,
    product_id varchar(36) not null,
    quantity int not null default 1,
    primary key (id),
    unique key (cart_id, product_id),
    constraint fk_cart_item_cart_id
        foreign key (cart_id) references cart (id)
        on delete cascade
);
