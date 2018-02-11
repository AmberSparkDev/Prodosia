package com.Bluefix.Prodosia.DataType;

/**
 * Data class for a single Taglist.
 */
public class Taglist
{


    //region Data Setters and Getters
    private long id;
    private String abbreviation;
    private String description;

    public String getAbbreviation()
    {
        return abbreviation;
    }

    public String getDescription()
    {
        return description;
    }

    //endregion

    //region Constructor

    public Taglist(String abbreviation, String description)
    {
        this.abbreviation = abbreviation;
        this.description = description;
    }

    //endregion





}
