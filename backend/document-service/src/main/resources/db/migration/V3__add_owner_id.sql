alter table generated_documents add column if not exists owner_id uuid;
alter table generated_documents add column if not exists source_event_id uuid;

create unique index if not exists uq_generated_documents_source_event_id
    on generated_documents(source_event_id)
    where source_event_id is not null;

create index if not exists idx_generated_documents_owner_id on generated_documents(owner_id);
