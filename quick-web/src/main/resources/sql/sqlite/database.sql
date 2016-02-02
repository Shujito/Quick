create table user_groups (
	_id integer primary key on conflict fail autoincrement,
	name text not null on conflict fail
);

insert into user_groups(name) values('users');

create table users (
	_id integer primary key on conflict fail autoincrement,
	created_at integer not null default (cast(((julianday('now') - julianday('1970-01-01')) * 86400000) as integer)),
	updated_at integer not null default (cast(((julianday('now') - julianday('1970-01-01')) * 86400000) as integer)),
	deleted_at integer,
	username text not null on conflict fail unique on conflict fail,
	display_name text not null on conflict fail,
	email text not null on conflict fail unique on conflict fail,
	group_id integer not null on conflict fail default 1,
	foreign key (group_id) references user_groups(_id)
);

create table user_permissions (
	_id integer primary key on conflict fail autoincrement,
	name text not null on conflict fail,
	description text not null on conflict fail
);

create table group_permissions (
	id_group integer not null on conflict fail,
	id_permission integer not null on conflict fail,
	primary key (id_group, id_permission) on conflict fail
);

create table users_passwords (
	user_id integer not null on conflict fail,
	password blob not null on conflict fail,
	salt blob not null on conflict fail,
	foreign key (user_id) references users(_id),
	primary key (user_id) on conflict replace
);

create table sessions (
	_id integer primary key on conflict fail autoincrement,
	user_id integer not null on conflict fail,
	access_token blob not null on conflict fail default (randomblob(32)),
	accessed_at integer not null on conflict ignore default (cast(((julianday('now') - julianday('1970-01-01')) * 86400000) as integer)),
	expires_at integer not null on conflict ignore default (cast(((julianday('now','+15 minutes') - julianday('1970-01-01')) * 86400000) as integer)),
	user_agent text not null on conflict fail,
	foreign key (user_id) references users(_id)
);

create table quicks (
	_id integer primary key on conflict fail autoincrement,
	created_at integer not null default (cast(((julianday('now') - julianday('1970-01-01')) * 86400000) as integer)),
	updated_at integer not null default (cast(((julianday('now') - julianday('1970-01-01')) * 86400000) as integer)),
	expires_at integer not null on conflict ignore default (cast(((julianday('now','+7 days') - julianday('1970-01-01')) * 86400000) as integer)),
	deleted_at integer,
	user_id integer,
	contents blob not null on conflict fail,
	content_type text not null on conflict fail,
	name text not null on conflict fail,
	description text not null on conflict fail,
	is_public integer not null on conflict fail,
	foreign key (user_id) references users(_id)
);

create table reports (
	_id integer primary key on conflict fail autoincrement,
	quick_id integer not null on conflict fail,
	created_at integer not null default (cast(((julianday('now') - julianday('1970-01-01')) * 86400000) as integer)),
	updated_at integer not null default (cast(((julianday('now') - julianday('1970-01-01')) * 86400000) as integer)),
	deleted_at integer,
	resolved_at integer,
	description text not null on conflict fail
);

--------------------------------------------------------------------------------
-- Constraints
--------------------------------------------------------------------------------

create trigger check_users before insert on users
	begin
		select case
			when length(new.username) < 2 or length(new.username) > 24 then
				raise(fail,'Username must be between 2 and 24 characters')
			when new.email not like '%_@__%.__%' then
				raise(fail,'Invalid email format')
			when length(new.email) < 8 or length(new.email) > 254 then
				raise(fail,'Email must be between 8 and 24 characters')
			end;
	end;

create trigger check_user_groups before insert on user_groups
	begin
		select case
			when length(new.name) > 25 then
				raise(fail,'Name must not be longer than 25 characters')
			end;
	end;

create trigger check_user_permissions before insert on user_permissions
	begin
		select case
			when length(new.name) > 25 then
				raise(fail,'Name must not be longer than 25 characters')
			when length(new.description) > 25 then
				raise(fail,'Description must not be longer than 25 characters')
			end;
	end;

create trigger check_quicks before insert on quicks
	begin
		select case
			when length(new.name) = 0 then
				raise(fail,'Name cannot be empty')
			when length(new.name) > 50 then
				raise(fail,'Name must not be longer than 50 characters')
			when length(new.description) > 145 then
				raise(fail,'Description must not be longer than 145 characters')
			end;
	end;

create trigger check_reports before insert on reports
	begin
		select case
			when length(new.description) > 145 then
				raise(fail,'Description must not be longer than 145 characters')
			end;
	end;

--------------------------------------------------------------------------------
-- Soft deletes
--------------------------------------------------------------------------------

create trigger delete_user before delete on users
	begin
		update users set
			deleted_at = (select unixtime from unixtime)
		where
			users.deleted_at is null
			and users._id=old._id;
		select raise(ignore);
	end;

create trigger delete_quicks before delete on quicks
	begin
		update quicks set
			deleted_at = (select unixtime from unixtime),
			contents = X''
		where
			quicks.deleted_at is null
			and quicks._id=old._id;
		select raise(ignore);
	end;

--------------------------------------------------------------------------------
-- Views
--------------------------------------------------------------------------------

create view unixtime as
	select
		cast(((julianday('now') - julianday('1970-01-01')) * 86400000) as integer) as unixtime;

create view passwords as
	select
		users_passwords.user_id as user_id,
		users_passwords.password as password,
		users_passwords.salt as salt,
		users.username as username,
		users.email as email
	from users
	inner join users_passwords
		on users._id=users_passwords.user_id;

create view active_sessions as
	select
		sessions.access_token,
		users.*
	from users
	left join sessions
		on sessions.user_id=users._id
	where
		users.deleted_at is null
		and datetime(sessions.expires_at/1000,'unixepoch') > datetime('now');

create view active_quicks as
	select
		*
	from quicks
	where
		quicks.deleted_at is null
		and datetime(quicks.expires_at/1000,'unixepoch') > datetime('now')
