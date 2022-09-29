create schema if not exists pivotal;
create schema if not exists vmware;
create schema if not exists test;

create sequence if not exists pivotal.person_seq start with 1 increment by 50;
create table if not exists pivotal.person (id bigint not null, name varchar(255), tenant varchar(255) not null, primary key (id));

create sequence if not exists vmware.person_seq start with 1 increment by 50;
create table if not exists vmware.person (id bigint not null, name varchar(255), tenant varchar(255) not null, primary key (id));

create sequence if not exists test.person_seq start with 1 increment by 50;
create table if not exists test.person (id bigint not null, name varchar(255), tenant varchar(255) not null, primary key (id, tenant));

insert into test.person (id, tenant, name) values(NEXTVAL('test.person_seq'), 'CustomerA', 'Test Customer A - H2');
insert into test.person (id, tenant, name) values(NEXTVAL('test.person_seq'), 'CustomerB', 'Test Customer B - H2');
