create table session_log (
  id serial primary key,
  uuid text not null,
  created_at timestamp default now()
);
--;;
create index session_log_uuid_index on session_log(uuid);
