create table user_sms_log (
  id serial primary key,
  phone text not null,
  sms text not null,
  status integer not null default 0,
  created_at timestamp default now(),
  updated_at timestamp default now()
);
--;;
create index user_sms_log_phone_index on user_sms_log(phone);
