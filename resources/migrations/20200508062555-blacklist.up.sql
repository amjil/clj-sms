create table user_black_list (
  id serial primary key,
  phone text not null,
  reason text not null,
  status integer not null default 1,
  created_at timestamp default now(),
  updated_at timestamp default now()
);
--;;
create index blacklist_phone_index on user_black_list(phone);
