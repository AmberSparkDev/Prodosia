package com.Bluefix.Prodosia.DataType;

/**
 * Generic conversion for data types
 */
public class Data
{
    public static String ToString(Rating rating)
    {
        switch (rating)
        {

            case ALL:
                return "All";
            case SAFE:
                return "Safe";
            case QUESTIONABLE:
                return "Questionable";
            case EXPLICIT:
                return "Explicit";
            case UNKNOWN:
                return "unknown";

            default:
                throw new IllegalArgumentException("This rating does not exist");
        }
    }

}
