package ru.draftplace.santanizer;

public enum FormNextAction
{
    ADD("add"),
    NEXT("next");

    private final String value;

    FormNextAction(String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return value;
    }
}
