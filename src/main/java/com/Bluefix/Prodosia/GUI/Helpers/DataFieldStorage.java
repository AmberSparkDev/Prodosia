/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.Bluefix.Prodosia.GUI.Helpers;

/**
 * Helper class that takes a variable amount of strings and
 * stores them in the proper order. These can be retrieve at a later date.
 *
 * The class creates copies of the stored data.
 *
 * In order to create a DataFieldStorage object, call its static `store` method.
 */
public class DataFieldStorage
{
    private String[] data;

    /**
     * Private constructor
     */
    private DataFieldStorage(String[] data)
    {
        this.data = data;
    }


    /**
     * Create a new DataFieldStorage object that stores the specified fields.
     * @param fields The fields to be stored.
     * @return A new DataFieldStorage object.
     */
    public static DataFieldStorage store(String... fields)
    {
        String[] data = new String[fields.length];

        int counter = 0;

        for (String field : fields)
        {
            data[counter++] = new String(field);
        }

        return new DataFieldStorage(data);
    }

    /**
     * Retrieve the stored fields.
     * @return The fields that were stored in this DataFieldStorage object.
     */
    public String[] retrieve()
    {
        return data;
    }



}
