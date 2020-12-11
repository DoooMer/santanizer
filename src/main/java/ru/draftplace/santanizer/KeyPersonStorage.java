package ru.draftplace.santanizer;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class KeyPersonStorage
{
    Map<String, Set<Person>> keyPersons = new HashMap<>();

    public void register(String key)
    {
        keyPersons.put(key, new HashSet<>());
    }

    public void add(String key, Person person)
    {
        keyPersons.get(key).add(person);
    }

    public int size(String key)
    {
        Set<Person> persons = keyPersons.get(key);

        if (persons == null) {
            return 0;
        }

        return persons.size();
    }

    public Set<Person> get(String key)
    {
        return keyPersons.get(key);
    }

    public void forget(String key)
    {
        keyPersons.remove(key);
    }
}
