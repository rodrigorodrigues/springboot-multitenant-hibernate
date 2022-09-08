package com.example.springbootmultitenanthibernate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PersonController {
    final PersonRepository personRepository;

    @GetMapping("/person")
    @Transactional(readOnly = true)
    public Page<PersonDto> person(Pageable pageable, @RequestParam(name = "name", required = false) String name) {
        log.info("pageable: {}", pageable);
        if (StringUtils.hasLength(name)) {
            return personRepository.findPersonByName(name, pageable)
                    .map(p -> new PersonDto(p.getId(), p.getName(), p.getTenant()));
        } else {
            return personRepository.people(pageable);
        }
    }

    @PostMapping("/person")
    @Transactional
    public void addPerson(@RequestBody PersonDto p) {
        personRepository.save(new Person(p.getName()));
    }
}
