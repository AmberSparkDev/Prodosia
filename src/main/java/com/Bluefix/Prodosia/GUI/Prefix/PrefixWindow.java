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

package com.Bluefix.Prodosia.GUI.Prefix;

import com.Bluefix.Prodosia.GUI.Navigation.VistaNavigator;
import com.Bluefix.Prodosia.DataHandler.CommandPrefixStorage;
import com.Bluefix.Prodosia.Prefix.CommandPrefix;
import com.Bluefix.Prodosia.Prefix.EditablePrefixList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class PrefixWindow
{
    @FXML private AnchorPane ap_imgur;
    @FXML private AnchorPane ap_discord;

    private EditablePrefixList imgurList;
    private EditablePrefixList discordList;




    public void accept(ActionEvent actionEvent) throws Exception
    {
        imgurList.store();

        if (discordList != null)
            discordList.store();

        VistaNavigator.loadVista(VistaNavigator.AppStage.MAIN_MENU);
    }

    public void reset(ActionEvent actionEvent)
    {
        imgurList.reset();
        discordList.reset();
    }
    
    
    
    @FXML
    private void initialize() throws Exception
    {
        // the imgur-token is obligated for the application to function, and as such we can
        // initialize it indefinitely.
        this.imgurList = new EditablePrefixList(CommandPrefix.Type.IMGUR, ap_imgur);
    }


    /**
     * Initialize the Prefix-Window for the components that are allowed to change.
     */
    public void init() throws Exception
    {
        // if the discord token was unavailable, disable setting up the discord token.
        CommandPrefix dPrefix = CommandPrefixStorage.getPrefixForType(CommandPrefix.Type.DISCORD);

        if (dPrefix == null)
        {
            ap_discord.getChildren().clear();
            ap_discord.setDisable(true);
            this.discordList = null;
        }
        else
        {
            ap_discord.setDisable(false);
            this.discordList = new EditablePrefixList(CommandPrefix.Type.DISCORD, ap_discord);
        }
    }
}
