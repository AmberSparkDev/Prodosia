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

package com.Bluefix.Prodosia.Imgur.ImgurApi;

/**
 * Data class indicating what the hourly distribution for different modules is.
 *
 * At current, the daily limit is 12500
 *
 * Posting Comments (`CommentExecution`) can only be done 6 comments per minute and
 * we want it to work as unimpeded as possible. As such, among with several others things,
 * it is not limited in GET requests. It does however use them:
 *  - At least 2 per TagRequest, possibly more
 * Worst case scenario (since the comments per minute are limited):
 *   6 * 60 = 360 GET requests per hour (69.12% of total daily limit)
 * However, unless lots of very small taglists are called, this should not be a concern.
 *
 *  Additional GET requests:
 *  (0/1): subscriptions
 *    If the name of a user was not known, it will request the account base.
 *  (0/1): tag command
 *    If a parent-comment is created, the actual Comment must be retrieved by its ID.
 */
public class ApiDistribution
{
    /**
     * Comment-module uses 250 requests on average per hour.
     * This is 24 * 250 = 6000 per day (48%)
     *
     * The comment-module is guaranteed to use this cap unless no
     * Imgur Trackers are instated.
     */
    public static final int CommentModule = 250;


    /**
     * Deletion module uses 50 requests on average per hour.
     * This is 24 * 100 = 2400 per day (19.2%)
     *
     * The comment-deletion module doesn't necessarily use this cap,
     * only if it consistently has comments to delete.
     */
    public static final int DeletionModule = 100;

}
