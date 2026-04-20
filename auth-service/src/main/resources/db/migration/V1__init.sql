create table if not exists `account` (
    id varchar(36) not null default (uuid()),
    email varchar(50) not null unique,
    password varchar(100) not null,
    role enum('ADMIN', 'USER') default 'USER',
    primary key (id)
);

create table if not exists refresh_token (
    id varchar(36) not null default (uuid()),
    account_id varchar(36) not null,
    token varchar(36) not null,
    created_at timestamp not null default current_timestamp,
    expires_at timestamp not null,
    revoked bit default 0,
    primary key (id),
    constraint fk_refresh_token_account_id
        foreign key (account_id) references `account` (id)
);
