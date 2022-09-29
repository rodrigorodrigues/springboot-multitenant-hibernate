create sequence person_seq start with 1 increment by 1;
create table person (id number not null, name varchar(255), tenant varchar(255) not null, primary key (id));

insert into person (id, tenant, name) values(person_seq.nextval, 'CustomerA', 'Test Customer A - Oracle');
insert into person (id, tenant, name) values(person_seq.nextval, 'CustomerB', 'Test Customer B - Oracle');
