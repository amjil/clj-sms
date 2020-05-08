create table blacklist (
  id serial primary key,
  phone text not null,
  reason text not null,
  status integer not null default 0,
  created_at timestamp default now(),
  updated_at timestamp default now()
);
--;;
create unique index blacklist_phone_index on blacklist(phone);
