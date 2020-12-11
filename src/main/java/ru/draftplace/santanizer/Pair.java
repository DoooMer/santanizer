package ru.draftplace.santanizer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class Pair
{
    @Getter
    @Setter
    private Person santa;

    @Getter
    @Setter
    private Person person;

    @Override
    public String toString()
    {
        return santa + " >> " + person;
    }
}
