set schema test;
insert into test.person (id, tenant, name) values(NEXTVAL('test.person_seq'), 'CustomerA', 'Test Customer A - H2');
insert into test.person (id, tenant, name) values(NEXTVAL('test.person_seq'), 'CustomerB', 'Test Customer B - H2');