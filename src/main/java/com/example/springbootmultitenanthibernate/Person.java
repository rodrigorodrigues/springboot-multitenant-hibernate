package com.example.springbootmultitenanthibernate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "person")
@Getter
@ToString
@NoArgsConstructor
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "person_generator")
    @SequenceGenerator(name = "person_generator", sequenceName = "person_seq", allocationSize = 1)
    private Long id;

    @Setter
    private String name;

    @TenantId
    private String tenant;

    Person(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Person otherPerson)) {
            return false;
        }
        return id != null && id.equals(otherPerson.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
