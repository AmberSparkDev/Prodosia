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

package com.Bluefix.Prodosia.ImportExport;

import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataHandler.TrackerHandler;
import com.Bluefix.Prodosia.DataHandler.UserHandler;
import com.Bluefix.Prodosia.DataType.Archive.Archive;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.DataType.Tracker.TrackerPermissions;
import com.Bluefix.Prodosia.DataType.User.User;
import com.Bluefix.Prodosia.DataType.User.UserSubscription;
import com.Bluefix.Prodosia.DataHandler.ArchiveHandler;
import com.Bluefix.Prodosia.GUI.GuiUpdate;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

public class ImportExportHandler
{
    public enum ImportPolicy
    {
        MERGE_THEIRS,
        MERGE_OURS,
        OVERWRITE
    }



    public enum ImportResult
    {
        OK,
        FILE_MISSING,
        FILE_INCORRECT,
        NO_TAGLISTS_SUPPLIED
    }


    public static ImportResult importFromFile(File file, ImportPolicy policy) throws Exception
    {
        JsonData data;

        try
        {
            data = readJsonFromFile(file);
        }
        catch (FileNotFoundException ex)
        {
            return ImportResult.FILE_MISSING;
        }
        catch (Exception e)
        {
            return ImportResult.FILE_INCORRECT;
        }


        // incorporate the information depending on the import policy.
        //
        // first, retrieve all the taglists and map their 'id' to the actual
        // id in the system here.
        Map<Long, Long> taglistIdMap = importTaglistData(data.getTaglists(), policy);
        if (taglistIdMap == null)
            return ImportResult.NO_TAGLISTS_SUPPLIED;
        GuiUpdate.updateTaglists();

        // import the tracker data.
        importTrackerData(data.getTrackers(), taglistIdMap, policy);
        GuiUpdate.updateTrackers();

        // import the user data
        importUserData(data.getUsers(), taglistIdMap, policy);
        GuiUpdate.updateUsers();

        // import the archive data
        importArchiveData(data.getArchives(), taglistIdMap, policy);
        GuiUpdate.updateArchives();




        return ImportResult.OK;
    }


    private static JsonData readJsonFromFile(File file) throws IOException
    {
        StringBuilder sb = new StringBuilder();

        try (Stream<String> lines = Files.lines(file.toPath()))
        {
            lines.forEach(l -> sb.append(l));
        }

        return (new Gson()).fromJson(sb.toString(), JsonData.class);
    }


    /**
     * Import the taglists into the system. This method will return a map that maps the
     * taglist-id's from the import-data to the actual taglist-id the taglists now have in the system.
     * @param taglists The taglists to be incorporated.
     * @return A Map that maps the import-taglist-id's to the actual taglist-id's in the system.
     */
    private static Map<Long, Long> importTaglistData(List<JsonTaglist> taglists, ImportPolicy policy) throws Exception
    {
        if (taglists == null)
            return null;

        // initialize the map
        Map<Long, Long> idMap = new HashMap<>();

        // retrieve all currently known taglists.
        ArrayList<Taglist> dbTaglists = TaglistHandler.handler().getAll();

        // loop through all taglists to be imported.
        for (JsonTaglist tl : taglists)
        {
            // first check to see if there exists a taglist with duplicate abbreviation
            Taglist dup = null;

            for (Taglist tmpTl : dbTaglists)
            {
                if (tmpTl.getAbbreviation().equals(tl.getAbbreviation()))
                {
                    dup = tmpTl;
                    break;
                }
            }

            if (dup == null)
            {
                // since there was no duplicate, simply add the taglist and map the id.
                Taglist newTl = new Taglist(tl.getAbbreviation(), tl.getDescription(), tl.hasRatings);
                TaglistHandler.handler().set(newTl);

                idMap.put(tl.getId(), newTl.getId());
            }
            else
            {
                // depending on the import policy, we take action.
                if (policy == ImportPolicy.OVERWRITE ||
                    policy == ImportPolicy.MERGE_THEIRS)
                {
                    // only if we have to overwrite or prioritize our data, set the new taglist.
                    Taglist newTl = new Taglist(tl.getAbbreviation(), tl.getDescription(), tl.hasRatings);
                    TaglistHandler.handler().set(newTl);
                }

                idMap.put(tl.getId(), dup.getId());
            }
        }

        return idMap;
    }


    private static void importTrackerData(List<JsonTracker> trackers, Map<Long, Long> taglistIdMap, ImportPolicy policy) throws Exception
    {
        if (trackers == null)
            return;

        // retrieve all currently known trackers.
        ArrayList<Tracker> dbTrackers = TrackerHandler.handler().getAll();

        // loop through all trackers to be imported.
        for (JsonTracker t : trackers)
        {
            // first check to see if there is a duplicate tagger.
            Tracker dup = null;

            for (Tracker myT : dbTrackers)
            {
                boolean hasImgurId = myT.getImgurName() != null && !myT.getImgurName().trim().isEmpty();
                boolean hasDiscordId = myT.getDiscordId() != null && !myT.getDiscordId().trim().isEmpty();

                if (    (myT.getDiscordId().equals(t.getDiscordId()) &&
                            myT.getImgurId() == t.getImgurId()) ||
                        (myT.getDiscordId().equals(t.getDiscordId()) && !hasImgurId) ||
                        (myT.getImgurId() == t.getImgurId() && !hasDiscordId))
                {
                    dup = myT;
                    break;
                }
            }

            if (dup == null)
            {
                // if there was no duplicate, simply add the new tracker
                Tracker newT = new Tracker(
                        t.getImgurId(),
                        t.getDiscordId(),
                        parseTrackerPermissions(t, taglistIdMap));

                TrackerHandler.handler().set(newT);
            }
            else
            {
                // depending on the import policy, we take action.

                if (policy == ImportPolicy.OVERWRITE)
                {
                    // overwrite the tracker.
                    Tracker newT = new Tracker(
                            t.getImgurId(),
                            t.getDiscordId(),
                            parseTrackerPermissions(t, taglistIdMap));

                    TrackerHandler.handler().update(dup, newT);
                }
                else if (policy == ImportPolicy.MERGE_THEIRS)
                {
                    // only overwrite if any of our entries are applicable.
                    boolean hasImgurId = t.getImgurId() > 0;
                    boolean hasDiscordId = t.getDiscordId() != null && !t.getDiscordId().trim().isEmpty();

                    long imgurId = dup.getImgurId();
                    String discordId = dup.getDiscordId();

                    if (hasImgurId)
                        imgurId = t.getImgurId();

                    if (hasDiscordId)
                        discordId = t.getDiscordId();

                    Tracker newT = new Tracker(
                            imgurId,
                            discordId,
                            parseTrackerPermissions(t, taglistIdMap));

                    TrackerHandler.handler().update(dup, newT);
                }
                else if (policy == ImportPolicy.MERGE_OURS)
                {
                    // only overwrite if no former value was known.
                    boolean hasImgurId = dup.getImgurId() > 0;
                    boolean hasDiscordId = dup.getDiscordId() != null && !dup.getDiscordId().trim().isEmpty();

                    long imgurId = t.getImgurId();
                    String discordId = t.getDiscordId();

                    if (hasImgurId)
                        imgurId = dup.getImgurId();

                    if (hasDiscordId)
                        discordId = dup.getDiscordId();

                    Tracker newT = new Tracker(
                            imgurId,
                            discordId,
                            parseTrackerPermissions(t, taglistIdMap));

                    TrackerHandler.handler().update(dup, newT);
                }
            }
        }
    }


    private static TrackerPermissions parseTrackerPermissions(JsonTracker t, Map<Long, Long> taglistIdMap) throws Exception
    {
        // split the taglists that were known and replace full values with their actual taglist values.
        String taglists = t.getTaglists();
        String[] split = taglists.split(";");
        StringBuilder taglistString = new StringBuilder();

        for (int i = 0; i < split.length; i++)
        {
            try
            {
                long value = Long.parseLong(split[i]);

                // get the actual taglist value.
                Long actualValue = taglistIdMap.get(value);

                if (actualValue != null)
                {
                    taglistString.append(actualValue + ";");
                }
            }
            catch (Exception ex)
            {
                // ignore
            }
        }

        return new TrackerPermissions(t.getType(), taglistString.toString());
    }




    private static void importUserData(List<JsonUser> users, Map<Long, Long> taglistIdMap, ImportPolicy policy) throws Exception
    {
        if (users == null)
            return;

        // retrieve all currently known users.
        ArrayList<User> dbUsers = UserHandler.handler().getAll();

        for (JsonUser u : users)
        {
            // first check to see if there exists a duplicate user.
            User dup = null;

            for (User myU : dbUsers)
            {
                if (myU.getImgurId() == u.getImgurId())
                {
                    dup = myU;
                    break;
                }
            }

            if (dup == null)
            {
                // since there was no duplicate, simply add the user
                User newU = new User(u.getName(), u.getImgurId(), parseUserSubscriptions(u, taglistIdMap));

                UserHandler.handler().set(newU);
            }
            else
            {
                // depending on the policy, we take action.
                if (policy == ImportPolicy.OVERWRITE)
                {
                    User newU = new User(u.getName(), u.getImgurId(), parseUserSubscriptions(u, taglistIdMap));

                    UserHandler.handler().update(dup, newU);
                }
                else if (policy == ImportPolicy.MERGE_OURS ||
                        policy == ImportPolicy.MERGE_THEIRS)
                {
                    HashSet<UserSubscription> mainCollection;
                    HashSet<UserSubscription> additionCollection;

                    if (policy == ImportPolicy.MERGE_OURS)
                    {
                        mainCollection = new HashSet<>(dup.getSubscriptions());
                        additionCollection = parseUserSubscriptions(u, taglistIdMap);
                    }
                    else
                    {
                        mainCollection = parseUserSubscriptions(u, taglistIdMap);
                        additionCollection = new HashSet<>(dup.getSubscriptions());
                    }

                    for (UserSubscription uss : additionCollection)
                    {
                        boolean exists = false;

                        for (UserSubscription uss0 : mainCollection)
                        {
                            if (uss0.getTaglist().equals(uss.getTaglist()))
                            {
                                exists = true;
                                break;
                            }
                        }

                        // only add the item to the collection if it wasn't already in there
                        if (!exists)
                            mainCollection.add(uss);
                    }

                    User newU = new User(dup.getImgurName(), dup.getImgurId(), mainCollection);

                    UserHandler.handler().update(dup, newU);
                }
            }
        }
    }

    private static HashSet<UserSubscription> parseUserSubscriptions(JsonUser user, Map<Long, Long> taglistIdMap) throws Exception
    {
        HashSet<UserSubscription> result = new HashSet<>();

        for (JsonUserSubscription jus : user.getSubscriptions())
        {
            Long taglistId = taglistIdMap.get(jus.taglistId);

            if (taglistId == null)
                continue;

            UserSubscription us = new UserSubscription(taglistId, jus.getRatings(), jus.getFilters());
            result.add(us);
        }

        return result;
    }


    private static void importArchiveData(List<JsonArchive> archives, Map<Long, Long> taglistIdMap, ImportPolicy policy) throws Exception
    {
        if (archives == null)
            return;

        // retrieve all currently known Archives
        ArrayList<Archive> dbArchives = ArchiveHandler.handler().getAll();

        // loop through all archives to be imported.
        for (JsonArchive a : archives)
        {
            // parse the archive
            Archive newA = parseArchive(a, taglistIdMap);

            // first check to see if there exists a duplicate archive
            Archive dup = null;

            for (Archive tmpA : dbArchives)
            {
                if (tmpA.equals(newA))
                {
                    dup = tmpA;
                    break;
                }
            }

            if (dup == null)
            {
                // with no duplicate, simply store the archive.
                ArchiveHandler.handler().set(newA);
            }
            else
            {
                // dependent on import policy, either keep the old one or replace
                if (policy == ImportPolicy.OVERWRITE ||
                        policy == ImportPolicy.MERGE_THEIRS)
                {
                    ArchiveHandler.handler().update(dup, newA);
                }
            }
        }
    }


    private static Archive parseArchive(JsonArchive archive, Map<Long, Long> taglistIdMap) throws Exception
    {
        // get the proper taglist id.
        Long taglistId = taglistIdMap.get(archive.getTaglistId());

        if (taglistId == null)
            return null;

        return new Archive(
                taglistId,
                archive.getDescription(),
                archive.getChannel(),
                archive.getRatings(),
                archive.getFilters());
    }








    public enum ExportResult
    {
        OK,
        LOCATION_INCORRECT,
        ERROR
    }

    public static ExportResult exportToFile(String location) throws Exception
    {
        Gson g = new Gson();

        // retrieve all taglists.
        List<JsonTaglist> jsonTaglists = new LinkedList<>();
        ArrayList<Taglist> dbTaglists = TaglistHandler.handler().getAll();

        for (Taglist t : dbTaglists)
        {
            jsonTaglists.add(
                    new JsonTaglist(
                            t.getId(),
                            t.getAbbreviation(),
                            t.getDescription(),
                            t.hasRatings()));
        }

        // retrieve all trackers
        List<JsonTracker> jsonTrackers = new LinkedList<>();
        ArrayList<Tracker> dbTrackers = TrackerHandler.handler().getAll();

        for (Tracker t : dbTrackers)
        {
            // parse any taglist permissions
            StringBuilder taglistPermissions = new StringBuilder();

            for (Taglist tl : t.getPermissions().getTaglists())
                taglistPermissions.append(tl.getId() + ";");

            jsonTrackers.add(
                    new JsonTracker(
                            t.getImgurId(),
                            t.getDiscordId(),
                            t.getPermissions().dbGetType(),
                            taglistPermissions.toString()));
        }

        // retrieve the users.
        List<JsonUser> jsonUsers = new LinkedList<>();
        ArrayList<User> dbUsers = UserHandler.handler().getAll();

        for (User u : dbUsers)
        {
            // parse the user-subscriptions.
            List<JsonUserSubscription> jsonUserSubscriptions = new LinkedList<>();

            for (UserSubscription us : u.getSubscriptions())
            {
                jsonUserSubscriptions.add(
                        new JsonUserSubscription(
                                us.getTaglist().getId(),
                                us.getDbRating(),
                                us.getFilters()));
            }

            jsonUsers.add(new JsonUser(u.getImgurName(), u.getImgurId(), jsonUserSubscriptions));
        }

        // retrieve the archives.
        List<JsonArchive> jsonArchives = new LinkedList<>();
        ArrayList<Archive> dbArchives = ArchiveHandler.handler().getAll();

        for (Archive a : dbArchives)
        {
            jsonArchives.add(
                    new JsonArchive(
                            a.getTaglist().getId(),
                            a.getDescription(),
                            a.getChannelId(),
                            a.dbGetRatings(),
                            a.getFilters()));
        }




        String jsonData = g.toJson(new JsonData(jsonTaglists, jsonTrackers, jsonUsers, jsonArchives));

        // write to file.
        try (PrintWriter out = new PrintWriter(location))
        {
            out.println(jsonData);


        } catch (FileNotFoundException e)
        {
            return ExportResult.LOCATION_INCORRECT;
        }


        return ExportResult.OK;
    }



    private static class JsonData
    {
        private List<JsonTaglist> taglists;
        private List<JsonTracker> trackers;
        private List<JsonUser> users;
        private List<JsonArchive> archives;

        public JsonData(List<JsonTaglist> taglists, List<JsonTracker> trackers, List<JsonUser> users, List<JsonArchive> archives)
        {
            this.taglists = taglists;
            this.trackers = trackers;
            this.users = users;
            this.archives = archives;
        }

        public List<JsonTaglist> getTaglists()
        {
            return taglists;
        }

        public List<JsonTracker> getTrackers()
        {
            return trackers;
        }

        public List<JsonUser> getUsers()
        {
            return users;
        }

        public List<JsonArchive> getArchives()
        {
            return archives;
        }
    }

    private static class JsonTaglist
    {
        private long id;
        private String abbreviation;
        private String description;
        private boolean hasRatings;

        public JsonTaglist(long id, String abbreviation, String description, boolean hasRatings)
        {
            this.id = id;
            this.abbreviation = abbreviation;
            this.description = description;
            this.hasRatings = hasRatings;
        }

        public long getId()
        {
            return id;
        }

        public String getAbbreviation()
        {
            return abbreviation;
        }

        public String getDescription()
        {
            return description;
        }

        public boolean isHasRatings()
        {
            return hasRatings;
        }
    }

    private static class JsonTracker
    {
        private long imgurId;
        private String discordId;
        private int type;
        private String taglists;

        public JsonTracker(long imgurId, String discordId, int type, String taglists)
        {
            this.imgurId = imgurId;
            this.discordId = discordId;
            this.type = type;
            this.taglists = taglists;
        }

        public long getImgurId()
        {
            return imgurId;
        }

        public String getDiscordId()
        {
            return discordId;
        }

        public int getType()
        {
            return type;
        }

        public String getTaglists()
        {
            return taglists;
        }
    }

    private static class JsonUser
    {
        private String name;
        private long imgurId;
        private List<JsonUserSubscription> subscriptions;

        public JsonUser(String name, long imgurId, List<JsonUserSubscription> subscriptions)
        {
            this.name = name;
            this.imgurId = imgurId;
            this.subscriptions = subscriptions;
        }

        public String getName()
        {
            return name;
        }

        public long getImgurId()
        {
            return imgurId;
        }

        public List<JsonUserSubscription> getSubscriptions()
        {
            return subscriptions;
        }
    }

    private static class JsonUserSubscription
    {
        private long taglistId;
        private String ratings;
        private String filters;

        public JsonUserSubscription(long taglistId, String ratings, String filters)
        {
            this.taglistId = taglistId;
            this.ratings = ratings;
            this.filters = filters;
        }

        public long getTaglistId()
        {
            return taglistId;
        }

        public String getRatings()
        {
            return ratings;
        }

        public String getFilters()
        {
            return filters;
        }
    }

    private static class JsonArchive
    {
        private long taglistId;
        private String description;
        private String channel;
        private String ratings;
        private String filters;

        public JsonArchive(long taglistId, String description, String channel, String ratings, String filters)
        {
            this.taglistId = taglistId;
            this.description = description;
            this.channel = channel;
            this.ratings = ratings;
            this.filters = filters;
        }

        public long getTaglistId()
        {
            return taglistId;
        }

        public String getDescription()
        {
            return description;
        }

        public String getChannel()
        {
            return channel;
        }

        public String getRatings()
        {
            return ratings;
        }

        public String getFilters()
        {
            return filters;
        }
    }






}
























