create table processed_events (
    event_id uuid primary key,
    processed_at timestamp not null
);
