DROP TABLE IF EXISTS "Offer";
DROP TABLE IF EXISTS "Stack";
DROP TABLE IF EXISTS "Password";
DROP TABLE IF EXISTS "MUser";
DROP TABLE IF EXISTS "Card";

create table if not exists "MUser"
(
    username varchar(30) not null
        constraint "MUser_pkey"
            primary key,
    elo integer default 20 not null
);

alter table "MUser" owner to postgres;

create table if not exists "Password"
(
    id serial not null
        constraint id_pk
            primary key,
    password varchar(255) not null,
    username varchar(30) not null
        constraint password_muser__fk
            references "MUser"
            on delete cascade
);

alter table "Password" owner to postgres;

create table if not exists "Card"
(
    cardname varchar(30) not null
        constraint cardname_pk
            primary key,
    damage integer not null,
    type varchar(1) default 'n'::character varying not null,
    is_a varchar(1) default 'm'::character varying not null,
    occurance integer default 0
);

alter table "Card" owner to postgres;

create unique index if not exists card_name_uindex
    on "Card" (cardname);

create table if not exists "Offer"
(
    id integer not null,
    cardname varchar(30) not null
        constraint offer_card_cardname_fk
            references "Card"
            on delete cascade,
    username varchar(30) not null
        constraint offer_muser_username_fk
            references "MUser"
            on delete cascade,
    minimum_expected varchar(5) default 'M50'::character varying,
        constraint offer_pk
            unique (username, id, cardname)
);

alter table "Offer" owner to postgres;

create table if not exists "Stack"
(
    username varchar(30) not null
        constraint stack_muser_username_fk
            references "MUser"
            on delete cascade,
    cardname varchar(30) not null
        constraint stack_card_cardname_fk
            references "Card"
            on delete set null,
    in_market boolean default false,
    in_deck boolean default false,
    constraint stack_pk
        primary key (cardname, username)
);

alter table "Stack" owner to postgres;




