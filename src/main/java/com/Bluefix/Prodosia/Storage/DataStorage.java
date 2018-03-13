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

package com.Bluefix.Prodosia.Storage;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Generic data storage class for easily storing and retrieving data from text files.
 */
public abstract class DataStorage
{
    /**
     * Item class used for data storage. Item name should not contain spaces, item data can be any arrangement of symbols.
     */
    public static class Item
    {
        private String name;
        private String data;

        public Item(String name, String data)
        {
            if (name == null)
            {
                throw new IllegalArgumentException("The item name can not be null.");
            }
            else if (name.trim().contains(" "))
            {
                throw new IllegalArgumentException("The item name should not contain any spaces.");
            }
            else if (name.trim().isEmpty())
            {
                throw new IllegalArgumentException("The item name should not be empty.");
            }

            this.name = name;
            this.data = data;
        }

        /**
         * Retrieve the name of the data item.
         * @return The name of the data item.
         */
        public String getName()
        {
            return name;
        }

        /**
         * Retrieve the data of the data item.
         * @return The data of the data item.
         */
        public String getData()
        {
            return data;
        }
    }


    //region Write

    /**
     * Store the supplied items and prepend the file with a message.
     * @param filepath The relative/absolute filepath to write away to.
     * @param items The items to be written away.
     * @param message The message to be prepended to the data.
     */
    public static void storeItems(String filepath, Iterable<Item> items, String message) throws IOException
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, false)))
        {
            // prepend every line of the sentence with a # if necessary.
            Scanner msgScanner = new Scanner(message);

            while (msgScanner.hasNextLine())
            {
                String s = msgScanner.nextLine();

                if (s == null)
                {
                    // skip
                }
                else if (s.isEmpty())
                {
                    writer.write(s + "\n");
                }
                else
                {
                    writer.write("# " + s + "\n");
                }
            }

            // store the items in the file.
            storeItems(writer, items);
        }
    }


    /**
     * Store the supplied items
     * @param filepath The relative/absolute filepath to write away to.
     * @param items The items to be written away.
     */
    public static void storeItems(String filepath, Iterable<Item> items) throws IOException
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, false)))
        {
            // store the items in the file.
            storeItems(writer, items);
        }
    }


    /**
     * Store the individual list item.s
     * @param bw the buffered writer to be used.
     * @param items the items to be written away.
     * @throws IOException
     */
    private static void storeItems(BufferedWriter bw, Iterable<Item> items) throws IOException
    {
        if (items == null)
            return;

        for (Item i :items)
        {
            bw.write(i.name + " " + i.data + "\n");
        }
    }

    //endregion

    //region Read

    /**
     * Read all items from the indicated filepath.
     * @param filepath The relative/absolute filepath to read from.
     * @return The items contained in the file, or null if no such file was found.
     */
    public static ArrayList<Item> readItems(String filepath)
    {
        ArrayList<Item> result = new ArrayList<>();

        try (Scanner in = new Scanner(new FileReader(filepath)))
        {
            while (in.hasNextLine())
            {
                String s = in.nextLine();

                // if the string starts with a hash or is empty, ignore it.
                if (s == null || s.isEmpty() || s.startsWith("#"))
                {
                    // ignore
                }
                else
                {
                    // find the first space (if it exists) and split the name and data of the current line.
                    int index = s.indexOf(' ');

                    if (index < 0)
                    {
                        result.add(new Item(s, null));
                    }
                    else
                    {
                        String name = s.substring(0, index);
                        String data = s.substring(index+1);
                        result.add(new Item(name, data));
                    }
                }
            }

            return result;

        } catch (FileNotFoundException e)
        {
            return null;
        }
    }


    //endregion










}
