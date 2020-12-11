package ru.draftplace.santanizer;

import java.util.*;

public class PairSelector
{
    private final Set<Person> candidates;

    public PairSelector(Set<Person> candidates)
    {
        if (candidates.size() < 2) {
            throw new RuntimeException("List of participants must contain at least 2 entries.");
        }

        this.candidates = candidates;
    }

    public Set<Pair> select()
    {
        // пары
        Set<Pair> result = new HashSet<>();
        // кто будет поздравлять
        List<Person> santas = new LinkedList<>(candidates);
        Collections.shuffle(santas);
        // кого будут поздравлять
        List<Person> participants = new LinkedList<>(santas);

        int index = 0;

        while (santas.size() > 0 || participants.size() > 0) {
            int participantIndex = participants.size() - 1 == index ? index : index + 1;
            // сбор пары
            result.add(new Pair(santas.get(index), participants.get(participantIndex)));
            // сокращение списков
            santas.remove(index);
            participants.remove(participantIndex);
        }

        return result;
    }
}
