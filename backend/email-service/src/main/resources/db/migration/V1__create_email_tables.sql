create table email_accounts (
    id uuid primary key,
    owner_id uuid not null unique,
    email_address varchar(320) not null,
    imap_host varchar(255) not null,
    imap_port integer not null,
    smtp_host varchar(255) not null,
    smtp_port integer not null,
    use_ssl boolean not null default true,
    encrypted_password text not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table inbox_messages (
    id uuid primary key,
    owner_id uuid not null,
    message_uid bigint not null,
    internet_message_id varchar(512),
    folder varchar(128) not null,
    subject varchar(1024),
    from_address varchar(512),
    to_address varchar(512),
    body_preview text,
    body_text text,
    received_at timestamp not null,
    category varchar(64) not null,
    classification_reason varchar(512),
    replied_at timestamp,
    created_at timestamp not null,
    unique (owner_id, folder, message_uid)
);

create index idx_inbox_messages_owner_id on inbox_messages(owner_id);
create index idx_inbox_messages_category on inbox_messages(category);
create index idx_inbox_messages_received_at on inbox_messages(received_at desc);
