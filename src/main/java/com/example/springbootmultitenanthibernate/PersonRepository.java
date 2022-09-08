package com.example.springbootmultitenanthibernate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

interface PersonRepository extends JpaRepository<Person, Long> {
    @Query("select new com.example.springbootmultitenanthibernate.PersonDto(p.id, p.name, p.tenant) from Person p")
    Page<PersonDto> people(Pageable pageable);

    Page<Person> findPersonByName(@Param("name") String name, Pageable pageable);

    @Query(nativeQuery = true,
           value = "SELECT * FROM person WHERE name = :name")
    Person findSqlByName(String name);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM person")
    int deleteAllRegardlessOfTenant();
}
