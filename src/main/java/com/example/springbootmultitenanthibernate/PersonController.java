package com.example.springbootmultitenanthibernate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PersonController {
    final PersonRepository personRepository;

    @GetMapping(value = "/person", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public Page<PersonDto> person(Pageable pageable, @RequestParam(name = "name", required = false) String name) {
        log.info("pageable: {}", pageable);
        if (StringUtils.hasLength(name)) {
            return personRepository.findPersonByName(name, pageable)
                    .map(p -> new PersonDto(p.getId(), p.getName(), p.getTenant()));
        } else {
            return new PageImpl<>(personRepository.findAll(pageable).stream().map(p -> new PersonDto(p.getId(), p.getName(), p.getTenant())).collect(Collectors.toList()));
        }

/*
        Function<Person, PersonDto> converter = p -> new PersonDto(p.getId(), p.getName(), p.getTenant());
        if (StringUtils.hasLength(name)) {
            return new PageImpl<>(personRepository.findPersonByName(name).stream().map(converter).collect(Collectors.toList()));
        } else {
            return new PageImpl<>(personRepository.findAll().stream().map(converter).collect(Collectors.toList()));
        }
*/

    }

    @PostMapping("/person")
    @Transactional
    public void addPerson(@RequestBody PersonDto p) {
        personRepository.save(new Person(p.getName()));
    }
}
