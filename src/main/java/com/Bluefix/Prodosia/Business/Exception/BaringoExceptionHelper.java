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

package com.Bluefix.Prodosia.Business.Exception;

import com.github.kskelm.baringo.util.BaringoApiException;

/**
 * Helps identify exceptions from the Baringo dependency.
 *
 */
public class BaringoExceptionHelper
{

    /**
     * Returns true iff the exception indicates a Bad Request call.
     * @param ex The exception to check.
     * @return True iff the exception indicates a Bad Request (400) error. False otherwise.
     */
    public static boolean isBadRequest(BaringoApiException ex)
    {
        // pattern
        String pattern = "https://api\\.imgur\\.com/3/.+: Bad Request";

        return ex.getMessage().matches(pattern);
    }

    /**
     * Returns true iff the exception indicates Not Found.
     * @param ex The exception to check.
     * @return True iff the exception indicates Not Found (404). False otherwise.
     */
    public static boolean isNotFound(BaringoApiException ex)
    {
        // pattern
        String pattern = "https://api\\.imgur\\.com/3/.+: Not Found";

        return ex.getMessage().matches(pattern);
    }
}
