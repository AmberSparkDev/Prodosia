/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.Bluefix.Prodosia.DataType;

public class User
{
    //region Data

    public String getImgurName()
    {
        return imgurName;
    }

    public long getImgurId()
    {
        return imgurId;
    }

    private String imgurName;
    private long imgurId;
    private UserRating rating;

    //endregion

    //region Constructor

    public User(String imgurName, long imgurId, UserRating rating)
    {
        this.imgurName = imgurName;
        this.imgurId = imgurId;
        this.rating = rating;
    }

    //endregion



}
