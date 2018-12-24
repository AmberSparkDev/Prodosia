/*
 * Copyright (c) 2018 RoseLaLuna
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

package com.Bluefix.Prodosia.Business.Algorithm;

import java.util.*;

/**
 * Helper class for the Partition problem.
 *
 * Incorporates a dynamic programming solution.
 */
public class Partition
{
    /**
     * Partition the entries provided into as few lines as possible without exceeding the maximum length.
     * @param entries The entries to be partitioned.
     * @param maxLength The maximum allowed length per line.
     * @return A list with all entries partitioned so that it does not exceed the maximum length.
     * @throws IllegalArgumentException if any entry is larger than the allowed max length.
     */
    public static LinkedList<String> partitionEntries(Iterable<String> entries, int maxLength)
    {
        // first, parse a list with all entry lengths and corresponding strings.
        LengthStruct ls = new LengthStruct(entries);

        // spread the known lengths into partitions.
        LinkedList<ArrayList<Integer>> partitionSplit = splitEntriesByLength(ls.getLength(), maxLength);

        // reconstruct the string partitions and return.
        return reconstructStringPartition(partitionSplit, ls.getLengthMap());
    }


    /**
     * Maintain a collection with all the available lengths and the
     * mapping for said length to a collection of strings.
     */
    private static class LengthStruct
    {
        private ArrayList<Integer> length;
        private HashMap<Integer, LinkedList<String>> lengthMap;

        public LengthStruct(Iterable<String> entries)
        {
            length = new ArrayList<>();
            lengthMap = new HashMap<>();

            parseEntries(entries);
        }

        private void parseEntries(Iterable<String> entries)
        {
            Iterator<String> it = entries.iterator();

            while (it.hasNext())
            {
                String entry = it.next();

                // if the entry does not exist or is empty, continue
                // keep in mind that we cannot trim the entry to check if it consists of whitespace,
                // since whitespace might be part of the entries.
                if (entry == null || entry.isEmpty())
                    continue;


                length.add(entry.length());

                // retrieve the list of entries.
                LinkedList<String> list = lengthMap.get(entry.length());

                // if the list was null, create it.
                if (list == null)
                {
                    list = new LinkedList<>();
                    lengthMap.put(entry.length(), list);
                }

                list.add(entry);
            }
        }

        public ArrayList<Integer> getLength()
        {
            return length;
        }

        public HashMap<Integer, LinkedList<String>> getLengthMap()
        {
            return lengthMap;
        }
    }



    //region Dynamic programming solution

    /**
     * Take the length of all entries and separate these in as few parts as possible.
     * Each comment cannot exceed the maximum length
     * @param entries The entries to be spread. This linkedlist will be emptied.
     * @param maxLength The maximum allowed length per partition
     * @return
     */
    private static LinkedList<ArrayList<Integer>> splitEntriesByLength(ArrayList<Integer> entries, int maxLength)
    {
        LinkedList<ArrayList<Integer>> output = new LinkedList<>();

        while (!entries.isEmpty())
        {
            output.add(dynamicFillSingleRow(entries, maxLength));
        }

        return output;
    }

    /**
     * Use the entries to fill a single arraylist with entries that come as close to the maximum length as possible.
     * @param entries
     * @param maxLength
     * @return
     */
    private static ArrayList<Integer> dynamicFillSingleRow(ArrayList<Integer> entries, int maxLength)
    {
        int[] dynamicBlock = dynamicFillSingleRowArray(entries, maxLength);


        // translate the dynamic block into a list with integers.
        ArrayList<Integer> output = new ArrayList<>();

        // find the last pivot
        int lastPivot = -1;

        for (int i = dynamicBlock.length - 1; i >= 0 && lastPivot < 0; i--)
        {
            if (dynamicBlock[i] > 0)
                lastPivot = i;
        }

        // if there was no last pivot, something is wrong with the algorithm logic
        if (lastPivot < 0)
            throw new IllegalArgumentException("ba1bbd0a-390b-4b4e-b7f2-b80d7e34eca5");

        // backtrack all values from the last pivot.
        while (lastPivot >= 0)
        {
            int pivotValue = dynamicBlock[lastPivot];

            if (pivotValue == 0)
                throw new IllegalArgumentException("8501419e-3f56-440d-a43d-876124a650a4");

            output.add(pivotValue);
            lastPivot -= pivotValue;

            // complete the pivot value from the entries.
            if (!entries.remove((Object)pivotValue))
                throw new IllegalArgumentException("5061367f-6801-40d8-b6c5-86bd3117a59d");
        }

        return output;
    }


    /**
     * Fill an array with pivots.
     *
     * The logic behind this is that any integer 'entry' is either part of the solution or not part of the
     * solution. This means that it can either serve as a pivot for another entry or not.
     *
     * if a spot in the block was already occupied, it means a different combination of pivots made that combination
     * possible.
     *
     * By indicating which value made the pivot possible, we can backtrack it afterwards.
     *
     * This algorithm automatically stops as soon as the last entry has found a pivot, since that means
     * we have an optimal solution.
     * @param entries
     * @param maxLength
     * @return
     */
    private static int[] dynamicFillSingleRowArray(ArrayList<Integer> entries, int maxLength)
    {
        int[] dynamicBlock = new int[maxLength];

        // loop through all entries and attempt to add them as pivots.
        for (Integer e : entries)
        {
            // if the entry is larger than is allowed, throw an exception
            if (e > maxLength)
                throw new IllegalArgumentException("The Partition algorithm was given an entry that exceeded the maximum length.");

            // keep track of our own pivot positions so we don't pivot on ourselves
            HashSet<Integer> newPivots = new HashSet<Integer>();

            // attempt to add the entry as a base pivot (first entry in the array.
            if (dynamicBlock[e-1] == 0)
            {
                dynamicBlock[e-1] = e;
                newPivots.add(e-1);
            }

            // loop through the dynamic block and attempt to pivot on any entries that aren't ours.
            for (int i = e; i < dynamicBlock.length; i++)
            {
                // if the current pivot position isn't empty, continue
                if (dynamicBlock[i] != 0)
                    continue;

                // If there is no former value we can pivot on, continue
                if (dynamicBlock[i-e] == 0)
                    continue;

                // if the former pivot position was ours, continue
                if (newPivots.contains(i-e))
                    continue;

                // we are allowed to pivot here.
                dynamicBlock[i] = e;
                newPivots.add(i);
            }

            // if the very last item of the collection has been set, return
            if (dynamicBlock[dynamicBlock.length -1] != 0)
                return dynamicBlock;
        }

        return dynamicBlock;
    }




    //endregion




    /**
     * Take all integers from the partitions and replace them with corresponding items from the map.
     * @param partitions The calculated partitions with the required lengths.
     * @param lengthMap The map that links length to a specific string item.
     * @return The String-partitioned list.
     */
    private static LinkedList<String> reconstructStringPartition(LinkedList<ArrayList<Integer>> partitions, HashMap<Integer, LinkedList<String>> lengthMap)
    {
        LinkedList<String> output = new LinkedList<>();

        for (ArrayList<Integer> list : partitions)
        {
            StringBuilder sb = new StringBuilder();

            // find the corresponding text for the specified length
            for (Integer i : list)
            {
                LinkedList<String> sList = lengthMap.get(i);

                if (sList.isEmpty())
                    throw new IllegalArgumentException("261a148b-035c-4ff4-9c2b-212605b1efd8");

                sb.append(sList.removeFirst());
            }

            output.add(sb.toString());
        }

        return output;
    }

}

















