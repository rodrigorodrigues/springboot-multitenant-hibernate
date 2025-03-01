package com.example.springbootmultitenanthibernate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class})
class TenantIdentifierResolverTest {
    static final String PIVOTAL = "PIVOTAL";
    static final String VMWARE = "VMWARE";

    @Autowired
    PersonRepository personRepository;
    @Autowired
    TransactionTemplate txTemplate;
    @Autowired
    TenantIdentifierResolver tenantIdentifierResolver;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantInfo(PIVOTAL);
        personRepository.deleteAll();
        TenantContext.setTenantInfo(VMWARE);
        personRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        personRepository.deleteAllRegardlessOfTenant();
    }

    @Test
    void saveAndLoadPerson() {
        Person adam = createPerson(PIVOTAL, "Adam");
        Person eve = createPerson(VMWARE, "Eve");

        assertThat(adam.getTenant()).isEqualTo(PIVOTAL);
        assertThat(eve.getTenant()).isEqualTo(VMWARE);

        TenantContext.setTenantInfo(VMWARE);
        assertThat(personRepository.findAll()).extracting(Person::getName).containsExactly("Eve");

        TenantContext.setTenantInfo(PIVOTAL);
        assertThat(personRepository.findAll()).extracting(Person::getName).containsExactly("Adam");
    }

    private Person createPerson(String schema, String name) {
        TenantContext.setTenantInfo(schema);

        Person person = txTemplate.execute(tx -> personRepository.save(new Person(name)));

        assertThat(person).isNotNull();
        assertThat(person.getId()).isNotNull();

        return person;
    }

    @Test
    void findById() {
        Person adam = createPerson(PIVOTAL, "Adam");
        Person vAdam = createPerson(VMWARE, "Adam");

        TenantContext.setTenantInfo(VMWARE);
        var person = personRepository.findById(vAdam.getId());

        Assertions.assertTrue(person.isPresent());
        assertThat(person.get().getTenant()).isEqualTo(VMWARE);
        assertThat(personRepository.findById(adam.getId())).isEmpty();
    }

    @Test
    void queryJPQL() {
        createPerson(PIVOTAL, "Adam");
        createPerson(VMWARE, "Adam");
        createPerson(VMWARE, "Eve");

        TenantContext.setTenantInfo(VMWARE);
        assertThat(personRepository.findPersonByName("Adam", PageRequest.ofSize(10)).getContent().get(0).getTenant()).isEqualTo(VMWARE);

        TenantContext.setTenantInfo(PIVOTAL);
        assertThat(personRepository.findPersonByName("Eve", PageRequest.ofSize(10)).getContent()).isEmpty();
    }

    @Test
    void querySQL() {
        createPerson(PIVOTAL, "Adam");
        createPerson(VMWARE, "Adam");

        TenantContext.setTenantInfo(VMWARE);
        assertThatThrownBy(() -> personRepository.findSqlByName("Adam"))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }
}
