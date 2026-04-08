create table if not exists category (
    id varchar(36) not null default (uuid()),
    name varchar(50) not null unique,
    description varchar(100),
    primary key (id)
);

create table if not exists product (
    id varchar(36) not null default (uuid()),
    name varchar(50) not null,
    description varchar(100),
    price decimal(10, 2) not null,
    active_status enum('ACTIVE','INACTIVE') default 'INACTIVE',
    stock_status enum('LOW_STOCK', 'OUT_OF_STOCK', 'IN_STOCK'),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    primary key (id)
);

create table if not exists product_category (
    product_id varchar(36) not null,
    category_id varchar(36) not null,
    primary key (product_id, category_id),
    constraint fk_product_category_product_id
        foreign key (product_id) references product (id)
        on delete cascade,
    constraint fk_product_category_category_id
        foreign key (category_id) references category (id)
        on delete cascade
);

create table if not exists product_image_urls (
    product_id varchar(36) not null,
    image_url varchar(100) not null,
    primary key (product_id, image_url),
    constraint fk_product_image_urls_product_id
        foreign key (product_id) references product (id)
        on delete cascade
);
