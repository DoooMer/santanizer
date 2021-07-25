package ru.draftplace.santanizer.access.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class AccessRequest
{
    @Getter
    @Setter
    private String email;

    @Override
    public String toString()
    {
        return "," + email + ">";
    }
}
