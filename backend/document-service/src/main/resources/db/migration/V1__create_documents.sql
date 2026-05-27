create table generated_documents (
                                     id uuid primary key,
                                     profile_id uuid not null,
                                     job_id uuid not null,
                                     document_type varchar(50) not null,
                                     file_name varchar(255) not null,
                                     content_type varchar(100) not null,
                                     storage_bucket varchar(255) not null,
                                     storage_key varchar(500) not null,
                                     created_at timestamp not null
);
