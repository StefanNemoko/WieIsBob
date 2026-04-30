create table bob_groups
(
    id         int auto_increment
        primary key,
    name       varchar(64)                        not null,
    created_at datetime default CURRENT_TIMESTAMP not null,
    updated_at datetime default CURRENT_TIMESTAMP not null
);

create table users
(
    id         int auto_increment
        primary key,
    email      varchar(128)                       not null,
    name       varchar(64)                        not null,
    password   varchar(255)                       not null,
    created_at datetime default CURRENT_TIMESTAMP not null,
    updated_at datetime default CURRENT_TIMESTAMP not null
);

create table bob_assignments
(
    id          int auto_increment
        primary key,
    user_id     int                                not null,
    group_id    int                                not null,
    assigned_at datetime                           not null comment 'The date the user has to be ''bob''',
    created_at  datetime default CURRENT_TIMESTAMP not null,
    constraint bob_assignments_bob_groups_id_fk
        foreign key (group_id) references bob_groups (id)
            on update cascade on delete cascade,
    constraint bob_assignments_users_id_fk
        foreign key (user_id) references users (id)
            on update cascade on delete cascade
);

create table group_users
(
    group_id int not null,
    user_id  int not null,
    constraint group_users___bob_groups
        foreign key (group_id) references bob_groups (id)
            on update cascade on delete cascade,
    constraint group_users__users
        foreign key (user_id) references users (id)
            on update cascade on delete cascade
);

create table tokens
(
    id         int auto_increment
        primary key,
    token      varchar(255)                       not null,
    user_id    int                                not null,
    created_at datetime default CURRENT_TIMESTAMP not null,
    expires_at datetime                           not null,
    constraint tokens___users
        foreign key (user_id) references users (id)
            on update cascade on delete cascade
);

