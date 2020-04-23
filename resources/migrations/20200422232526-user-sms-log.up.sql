create table user_sms_log (
  id serial primary key,
  phone text not null,
  sms text not null,
  /* id int(11) not null auto_increment primary key,
  phone varchar(50) not null,
  sms varchar(20) not null, */
  status integer not null default 0,
  created_at timestamp default now(),
  updated_at timestamp default now()
);
--;;
create index user_sms_log_phone_index on user_sms_log(phone);
