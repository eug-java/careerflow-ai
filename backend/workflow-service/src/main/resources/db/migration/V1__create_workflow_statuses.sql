create table workflow_statuses (
    process_instance_key bigint primary key,
    process_id varchar(255) not null,
    status varchar(50) not null,
    message text,
    owner_id uuid,
    updated_at timestamp not null
);

create index idx_workflow_statuses_owner_id on workflow_statuses(owner_id);
