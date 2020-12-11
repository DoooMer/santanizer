package ru.draftplace.santanizer;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PairSelectorTest
{

    @Test
    void select()
    {
        Set<Person> persons = new HashSet<>();

        assertThrows(RuntimeException.class, () -> new PairSelector(persons));

        persons.add(new Person("Bob", "bob@example.com"));
        persons.add(new Person("Alice", "alice@example.com"));
        persons.add(new Person("Frank", "frank@example.com"));
        persons.add(new Person("David", "david@example.com"));

        for (int i = 0; i < 10; i++) {
            Set<Pair> pairs = (new PairSelector(persons)).select();
            assertEquals(persons.size(), pairs.size());
        }

    }
}