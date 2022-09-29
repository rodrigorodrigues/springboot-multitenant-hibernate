create database if not exists pivotal;
create database if not exists vmware;
create database if not exists test;

create table if not exists test.person (id bigint not null auto_increment, name varchar(255), tenant varchar(255) not null, primary key (id)) engine=InnoDB;

insert into test.person (tenant, name) values('CustomerA', 'Test Customer A - Mysql');
insert into test.person (tenant, name) values('CustomerB', 'Test Customer B - Mysql');