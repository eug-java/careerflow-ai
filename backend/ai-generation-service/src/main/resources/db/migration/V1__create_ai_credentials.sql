create table ai_credentials (
    id uuid primary key,
    owner_id uuid not null unique,
    provider varchar(32) not null default 'openai',
    encrypted_api_key text not null,
    preferred_model varchar(64) not null default 'gpt-4o-mini',
    created_at timestamp not null,
    updated_at timestamp not null
);

create index idx_ai_credentials_owner_id on ai_credentials (owner_id);
