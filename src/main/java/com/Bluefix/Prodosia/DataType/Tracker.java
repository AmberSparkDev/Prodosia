package com.Bluefix.Prodosia.DataType;

/**
 * A tracker data class.
 *
 * A tracker is someone with permission to use the bot
 * outside of its own GUI, either through imgur comments
 * or discord integration.
 *
 * It is possible to setup a tracker for either an imgur
 * account, a discord user or both.
 */
public class Tracker
{
    //region Data Getters and Setters
    private String imgurName;
    private long imgurId;

    private String discordName;
    private int discordTag;
    private long discordId;

    public String getImgurName()
    {
        return imgurName;
    }

    public long getImgurId()
    {
        return imgurId;
    }

    public String getDiscordName()
    {
        return discordName;
    }

    public int getDiscordTag()
    {
        return discordTag;
    }

    public long getDiscordId()
    {
        return discordId;
    }

    //endregion

    //region Constructor

    public Tracker(String imgurName, long imgurId, String discordName, int discordTag, long discordId)
    {
        this.imgurName = imgurName;
        this.imgurId = imgurId;
        this.discordName = discordName;
        this.discordTag = discordTag;
        this.discordId = discordId;
    }


    //endregion


    //region Helper Methods

    /**
     * Returns the name of the tracker depending on whether the imgur
     * name or the discord name were set. Prefers imgur name.
     * @return The name of the tracker.
     */
    public String getName()
    {
        if (imgurName != null)
            return imgurName;

        return discordName;
    }

    //endregion




}
