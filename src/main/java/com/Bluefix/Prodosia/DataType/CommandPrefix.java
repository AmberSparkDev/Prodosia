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

package com.Bluefix.Prodosia.DataType;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Storage Object for command prefixes.
 *
 * A command prefix is a certain pattern that must occur before a command is called.
 * This allows the bot to distinguish command-comments from non-command-comments.
 */
public class CommandPrefix
{
    /**
     * The type of service that incorporates this command prefix.
     */
    public enum Type
    {
        IMGUR(0),
        DISCORD(1);

        private int value;

        Type(int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return value;
        }

        public static Type parseType(int value)
        {
            switch (value)
            {
                case 0:
                    return IMGUR;
                case 1:
                    return DISCORD;
            }

            throw new IllegalArgumentException("The command prefix type was not recognized.");
        }
    }

    private Type type;

    /**
     * The pattern expression that will recognize a command within a string.
     */
    private String regex;

    public CommandPrefix(Type type, String regex)
    {
        this.type = type;
        this.regex = regex;
    }

    public Type getType()
    {
        return type;
    }

    public String getRegex()
    {
        return regex;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandPrefix that = (CommandPrefix) o;
        return type == that.type;
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(type);
    }

    /**
     * Retrieve the index after a command prefix. This index is where the
     * command and the arguments will start.
     *
     * Returns -1 if no command was recognized.
     * @param comment The comment with a potential command on it.
     * @return The index after the prefix, or -1 if no command was detected.
     */
    private int matchIndex(String comment)
    {
        Pattern p = Pattern.compile(this.regex);

        Matcher matcher = p.matcher(comment);

        if (matcher.find())
            return matcher.end();

        return -1;
    }


}
