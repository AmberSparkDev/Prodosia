package com.Bluefix.Prodosia.DataHandler;

import com.Bluefix.Prodosia.DataType.Taglist;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Handler for taglist management.
 */
public class TaglistHandler
{


    /**
     * retrieve all taglists in the system.
     * @return All taglists in the system.
     */
    public static Taglist[] getTaglists()
    {
        Taglist t0 = new Taglist("fur", "furry taglist");
        Taglist t1 = new Taglist("mlp", "");
        Taglist t2 = new Taglist("yos", "");
        Taglist t3 = new Taglist("htm", "");
        Taglist t4 = new Taglist("twd", "");
        Taglist t5 = new Taglist("kot", "");
        Taglist t6 = new Taglist("ott", "");
        Taglist t7 = new Taglist("che", "");
        Taglist t8 = new Taglist("box", "");
        Taglist t9 = new Taglist("scp", "");
        Taglist t10 = new Taglist("pkm", "");

        return new Taglist[]{t0, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10};
    }

    /**
     * retrieve all the taglists sorted.
     * @return All taglists ordered by their abbreviation.
     */
    public static Taglist[] getTaglistsSorted()
    {
        Taglist[] tls = getTaglists();
        Arrays.sort(tls, new TaglistComparator());

        return tls;
    }

    /**
     * Comparator class for taglists. Compares by abbreviation.
     */
    static class TaglistComparator implements Comparator<Taglist>
    {
        @Override
        public int compare(Taglist o1, Taglist o2)
        {
            return o1.getAbbreviation().compareTo(o2.getAbbreviation());
        }
    }
}
