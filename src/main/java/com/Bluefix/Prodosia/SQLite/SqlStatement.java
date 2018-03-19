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

package com.Bluefix.Prodosia.SQLite;

/**
 * Prepared SQL statements.
 */
public class SqlStatement
{
    //region Database creation

    public static String[] createDatabaseStatement()
    {
        return new String[]{
                /* --- Info Table --- */
                        "CREATE TABLE IF NOT EXISTS Info (" +
                        "version integer, " +
                        "created_by text); ",
                /* --- Api Credentials --- */
                        "CREATE TABLE IF NOT EXISTS ApiCredentials (" +
                        "imgurId text, " +
                        "imgurSecret text, " +
                        "discordToken text); ",
                /* --- Tracker --- */
                        "CREATE TABLE IF NOT EXISTS Tracker (" +
                        "id integer PRIMARY KEY, " +
                        "imgurId integer, " +
                        "imgurName text, " +
                        "discordId integer, " +
                        "discordName text, " +
                        "discordTag integer); ",
                /* --- Permission --- */
                        "CREATE TABLE IF NOT EXISTS Permission (" +
                        "trackerId integer PRIMARY KEY, " +
                        "isAdmin integer, " +
                        "taglists text); ",
                /* --- User --- */
                        "CREATE TABLE IF NOT EXISTS User (" +
                        "id integer primary key, " +
                        "name text, " +
                        "imgurId integer UNIQUE); ",
                /* --- UserSubscription --- */
                        "CREATE TABLE IF NOT EXISTS UserSubscription (" +
                        "userId integer, " +
                        "taglistId integer, " +
                        "ratings text, " +
                        "filters text); ",
                /* --- Taglist --- */
                        "CREATE TABLE IF NOT EXISTS Taglist (" +
                        "id integer primary key, " +
                        "abbreviation text NOT NULL UNIQUE, " +
                        "description text, " +
                        "hasRatings integer); ",
                /* --- Archive --- */
                        "CREATE TABLE IF NOT EXISTS Archive (" +
                        "taglistId integer, " +
                        "description text, " +
                        "channel integer, " +
                        "ratings text, " +
                        "filters text); ",
                /* --- TagQueue --- */
                        "CREATE TABLE IF NOT EXISTS TagQueue (" +
                        "imgurId text UNIQUE, " +
                        "parentComment integer, " +
                        "taglists text, " +
                        "rating integer, " +
                        "filters text); ",
                /* --- CommentDeletion --- */
                        "CREATE TABLE IF NOT EXISTS CommentDeletion (" +
                        "id integer PRIMARY KEY); ",
                /* --- CommentScanner --- */
                        "CREATE TABLE IF NOT EXISTS CommentScanner (" +
                        "imgurId integer PRIMARY KEY, " +
                        "lastCommentId integer, " +
                        "lastCommentTime integer); ",
                /* --- CommandPrefix --- */
                        "CREATE TABLE IF NOT EXISTS CommandPrefix (" +
                        "type integer PRIMARY KEY, " +
                        "regex text); ",
                /* --- CommentQueue --- */
                        "CREATE TABLE IF NOT EXISTS CommentQueue (" +
                        "imgurId text, " +
                        "parentId integer, " +
                        "lines text); "

        };
    }

    //endregion







}
