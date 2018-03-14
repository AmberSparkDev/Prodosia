/*
 * Copyright (c) 2018 J.S. Boellaard
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.Bluefix.Prodosia.DataHandler;

import com.Bluefix.Prodosia.SQLite.SqlBuilder;
import com.Bluefix.Prodosia.SQLite.SqlDatabase;

import java.sql.PreparedStatement;
import java.util.ArrayList;

/**
 * A base class that allows for a Handler to have a local storage.
 */
public abstract class LocalStorageHandler <T>
{
    //region Variables and constructor

    private ArrayList<T> data;

    public LocalStorageHandler()
    {
        data = null;
        this.useLocalStorage = true;
    }

    /**
     * Initialize a Local Storage Handler, indicating whether the local storage should be used.
     * @param useLocalStorage boolean indication whether local storage should be used.
     */
    public LocalStorageHandler(boolean useLocalStorage)
    {
        data = null;
        this.useLocalStorage = useLocalStorage;
    }

    //endregion

    //region use local storage

    private boolean useLocalStorage;

    /**
     * Indicate whether the Handler class should use local storage for data speedup.
     * @param val The boolean value. True means local storage is used for speedup, at the cost of memory.
     */
    public void setLocalStorage(boolean val)
    {
        this.useLocalStorage = val;

        // delete the local storage if it was disabled.
        if (!this.useLocalStorage)
            data = null;
    }

    //endregion




    //region Data handling


    /**
     * Add an item to the collection.
     * @param t The item to be added.
     */
    public void add(T t) throws Exception
    {
        // if the local storage is used, add the item to the data.
        if (useLocalStorage)
        {
            // initialize the local storage if necessary.
            if (data == null)
                data = getAllItems();

            // remove the item if it already existed and add it to the list.
            remove(t);
            data.add(t);
        }

        addItem(t);
    }


    /**
     * Remove an item from the collection.
     * @param t The item to be removed.
     */
    public void remove(T t) throws Exception
    {
        // if the local storage is in use, remove the item
        if (useLocalStorage && data != null)
            data.remove(t);

        // remove the item from the database.
        removeItem(t);
    }


    /**
     * Update the old item, replacing it with the new one.
     * @param oldT The old item, to be deleted.
     * @param newT The new item that will replace the old item.
     */
    public void update(T oldT, T newT) throws Exception
    {
        // remove the old item and add the new one.
        remove(oldT);
        add(newT);
    }

    /**
     * Retrieve all items from the list.
     * @return An arraylist with all items currently stored by the handler.
     */
    public ArrayList<T> getAll() throws Exception
    {
        // if the local storage is used,
        if (useLocalStorage)
        {
            if (data == null)
                data = getAllItems();

            return data;
        }

        // if the local storage wasn't used, simply return the data call.
        return getAllItems();
    }


    //endregion


    //region Abstract methods

    /**
     * Retrieve the prepared statements necessary for adding an item.
     * @param t
     */
    protected abstract void addItem(T t) throws Exception;

    /**
     * Remove an item from the storage.
     * @param t
     */
    protected abstract void removeItem(T t) throws Exception;

    /**
     * Retrieve all items from the storage in no particular order.
     * @return
     */
    protected abstract ArrayList<T> getAllItems() throws Exception;

    //endregion
}
