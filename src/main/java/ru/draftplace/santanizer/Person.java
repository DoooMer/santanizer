package ru.draftplace.santanizer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class Person
{
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String email;

    @Override
    public String toString()
    {
        return name + "<" + email + ">";
    }

    public boolean isEmpty()
    {
        return name.isEmpty() || email.isEmpty();
    }
}
