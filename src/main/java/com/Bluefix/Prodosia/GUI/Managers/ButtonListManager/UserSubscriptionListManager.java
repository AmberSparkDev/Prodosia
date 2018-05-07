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

package com.Bluefix.Prodosia.GUI.Managers.ButtonListManager;

import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.DataType.User.User;
import com.Bluefix.Prodosia.DataType.User.UserSubscription;
import com.Bluefix.Prodosia.GUI.Managers.ListManager.GuiListManager;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * List Manager specifically created for User Subscriptions.
 *
 * All entries with subscription data will have a `+` prepended to them.
 */
public abstract class UserSubscriptionListManager extends GuiListManager<Button>
{
    private Button[] buttonItems;

    private ArrayList<Taglist> taglists;
    private HashSet<UserSubscription> subscriptions;


    /**
     * Create a new GuiListManager object that is linked to the root pane.
     * This list-manager will instantiate itself by filling the items
     * from `listItems()`
     *
     * @param vbox The root in which the items will be displayed.
     */
    public UserSubscriptionListManager(VBox vbox) throws Exception
    {
        super(vbox);
    }


    /**
     * Retrieve the list items for the specified user.
     * @return
     */
    @Override
    protected Button[] listItems() throws Exception
    {
        taglists = new ArrayList<>(TaglistHandler.handler().getAll());
        Button[] buttons = new Button[taglists.size()];

        for (int i = 0; i < taglists.size(); i++)
        {
            Taglist tl = taglists.get(i);

            Button button = new Button(tl.getAbbreviation());
            button.setMaxWidth(Double.MAX_VALUE);

            // set button action
            button.setOnAction(event ->
            {
                // find a UserSubscription that fits with the value.
                for (UserSubscription us : subscriptions)
                {
                    if (us.getTaglist().equals(tl))
                    {
                        // just in case, refresh the user-subscription taglist.
                        try
                        {
                            us.setTaglist(tl);
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        taglistSelected(tl, us);
                        return;
                    }
                }

                taglistSelected(tl, null);
            });

            buttons[i] = button;
        }

        this.buttonItems = buttons;

        return buttons;
    }

    /**
     * Requests the underlying subclass to dereference the current items.
     */
    @Override
    protected void dereference()
    {
        if (this.buttonItems == null || this.buttonItems.length == 0)
            return;

        for (Button b : this.buttonItems)
            b.setOnAction(null);

        this.buttonItems = null;
    }

    //region User Subscription handling

    /**
     * Initialize a user for this list manager.
     * @param user
     */
    public void initializeUser(User user)
    {
        if (user != null)
            this.subscriptions = new HashSet<>(user.getSubscriptions());
        else
            this.subscriptions = new HashSet<>();

        updateSubscriptionIdentifiers();
    }

    /**
     * Prepend a `+` before every taglist that has subscription data.
     */
    private void updateSubscriptionIdentifiers()
    {
        if (super.items == null)
            return;

        for (int i = 0; i < super.items.length; i++)
        {
            Taglist tl = taglists.get(i);

            if (this.subscriptions == null || this.subscriptions.isEmpty())
            {
                super.items[i].setText(tl.getAbbreviation());
            }
            else
            {
                boolean existed = false;

                Iterator<UserSubscription> usIt = subscriptions.iterator();

                while (usIt.hasNext() && !existed)
                    existed = usIt.next().getTaglist().equals(tl);

                if (existed)
                    super.items[i].setText("+ " + tl.getAbbreviation());
                else
                    super.items[i].setText(tl.getAbbreviation());
            }
        }
    }

    /**
     * Retrieve all known subscriptions.
     * @return
     */
    public HashSet<UserSubscription> getSubscriptions()
    {
        return this.subscriptions;
    }


    public UserSubscription getSubscriptionForTaglist(Taglist tl)
    {
        for (UserSubscription us : this.subscriptions)
        {
            if (us.getTaglist().equals(tl))
                return us;
        }

        return null;
    }


    public void setSubscriptions(HashSet<UserSubscription> usCol)
    {
        this.subscriptions = usCol;
        updateSubscriptionIdentifiers();
    }

    /**
     * This method indicates that the specified taglist has been selected.
     * Will also give a user-subscription object if it was already set.
     * @param tl The taglist that has been selected.
     * @param us The UserSubscription if it existed, or null otherwise.
     */
    public abstract void taglistSelected(Taglist tl, UserSubscription us);

    /**
     * Store the user-subscription in the collection.
     * @param us
     */
    public void updateUserSubscription(Taglist tl, UserSubscription us)
    {
        // replace the usersubscription
        this.subscriptions.removeIf(mUs -> mUs.getTaglist().equals(tl));

        if (us != null)
            this.subscriptions.add(us);

        updateSubscriptionIdentifiers();
    }

    /**
     * Clear the known user-subscription from the collection.
     * @param tl the taglist of the UserSubscription to be removed.
     */
    public void clearUserSubscription(Taglist tl)
    {
        this.subscriptions.removeIf(us -> us.getTaglist().equals(tl));
        updateSubscriptionIdentifiers();
    }

    //endregion
}
