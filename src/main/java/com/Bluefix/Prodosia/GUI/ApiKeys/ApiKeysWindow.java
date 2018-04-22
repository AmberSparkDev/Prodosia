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

package com.Bluefix.Prodosia.GUI.ApiKeys;

import com.Bluefix.Prodosia.DataHandler.TrackerHandler;
import com.Bluefix.Prodosia.DataType.ImgurKey;
import com.Bluefix.Prodosia.Discord.DiscordManager;
import com.Bluefix.Prodosia.Exception.ExceptionHelper;
import com.Bluefix.Prodosia.GUI.Navigation.VistaNavigator;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
import com.Bluefix.Prodosia.Storage.CookieStorage;
import com.Bluefix.Prodosia.Storage.KeyStorage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.Optional;

public class ApiKeysWindow
{
    private static final String defaultImgurCallback = "https://imgur.com";

    @FXML public TextField tf_imgClientId;
    @FXML public TextField tf_imgClientSecret;
    @FXML public TextField tf_imgCallback;
    @FXML public TextField tf_discordToken;
    @FXML public CheckBox chk_callback;

    /**
     * Initialize the values of the UI window based on
     * what is available in the KeyStorage.
     */
    @FXML
    private void initialize()
    {
        // setup imgur credentials
        try
        {
            ImgurKey ik = KeyStorage.getImgurKey();

            if (ik == null)
            {
                tf_imgClientId.setText("");
                tf_imgClientSecret.setText("");
                tf_imgCallback.setText("");
                tf_imgCallback.setDisable(true);
                chk_callback.setSelected(false);
            }
            else
            {
                tf_imgClientId.setText(ik.getClientId());
                tf_imgClientSecret.setText(ik.getClientSecret());
                tf_imgCallback.setText(ik.getCallback());

                if (defaultImgurCallback.equals(ik.getCallback().toLowerCase()))
                {
                    tf_imgCallback.setDisable(true);
                    chk_callback.setSelected(false);
                }
                else
                {
                    tf_imgCallback.setDisable(false);
                    chk_callback.setSelected(true);
                }
            }

        } catch (IOException e)
        {
            tf_imgClientId.setText("");
            tf_imgClientSecret.setText("");
            tf_imgCallback.setText("");
        }

        // setup discord credentials
        try
        {
            String discordToken = KeyStorage.getDiscordToken();

            tf_discordToken.setText((discordToken == null ? "" : discordToken));

        } catch (IOException e)
        {
            tf_discordToken.setText("");
        }
    }


    /**
     * Reset the data to the original values.
     * @param actionEvent
     */
    public void reset(ActionEvent actionEvent)
    {
        this.initialize();
    }

    /**
     * The moment accept is clicked, the data in it is stored.
     */
    private ImgurKey acceptImgurKey;

    /**
     * The moment accept is clicked, the data in it is stored.
     */
    private String acceptDiscordToken;


    /**
     * When accepting, functioning Imgur API credentials are *required*
     * before access to the rest of the application is granted.
     * @param actionEvent
     */
    public void accept(ActionEvent actionEvent) throws Exception
    {
        // parse the current Credentials
        acceptImgurKey = parseImgurKey();
        acceptDiscordToken = parseDiscordToken();

        // check to see if the credentials were all valid.
        ValidationCheck imgurKeyValidation = imgurKeyIsValid();
        ValidationCheck discordTokenValidation = discordTokenIsValid();
        // Show the error message
        //
        // If only the discord token was invalid, give the user the option to continue anyways.
        // The discord token is only invalid if it had a value in the first place.
        if (imgurKeyValidation.isValid && !discordTokenValidation.isValid && acceptDiscordToken != null)
        {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Discord token invalid");
            alert.setHeaderText("Warning! Discord token is invalid.");
            alert.setContentText(msg_discordTokenInvalid(discordTokenValidation.errorMessage) +
            "\nPress ok to continue without the discord token or press cancel otherwise.");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == ButtonType.OK)
            {
                // set the keys and return.
                acceptDiscordToken = "";
                store();
                VistaNavigator.loadVista(VistaNavigator.AppStage.MAIN_MENU);
            }
            else
            {
                // Stay on this page.
                return;
            }

        }
        else if (!imgurKeyValidation.isValid)
        {
            // distinguish between the case the the discord token existed and is invalid or
            // the case that the discord token is valid.
            if (acceptDiscordToken != null && !discordTokenValidation.isValid)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Imgur and Discord credentials invalid");
                alert.setHeaderText("Warning! Imgur and Discord credentials are invalid.");
                alert.setContentText(
                        msg_imgurKeyInvalid(imgurKeyValidation.errorMessage) + "\n\n" +
                        msg_discordTokenInvalid(discordTokenValidation.errorMessage));

                alert.showAndWait();
                return;
            }
            else
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Imgur credentials invalid");
                alert.setHeaderText("Warning! Imgur credentials are invalid.");
                alert.setContentText(
                        msg_imgurKeyInvalid(imgurKeyValidation.errorMessage));

                alert.showAndWait();
                return;
            }
        }
        else
        {
            // both the imgur key and discord key were valid. Return back to the main menu.
            store();
            VistaNavigator.loadVista(VistaNavigator.AppStage.MAIN_MENU);
        }
    }


    private void store() throws Exception
    {
        if (acceptImgurKey == null)
            throw new Exception("The Imgur key cannot be null before storage");

        // if the imgur key has changed at all, delete its cookie.
        ImgurKey curKey = KeyStorage.getImgurKey();

        if (!acceptImgurKey.equals(curKey))
        {
            CookieStorage.setRefreshToken(null);

            KeyStorage.setImgurKey(
                    acceptImgurKey.getClientId(),
                    acceptImgurKey.getClientSecret(),
                    acceptImgurKey.getCallback());

            ImgurManager.update();
        }

        KeyStorage.setDiscordToken(acceptDiscordToken);
        DiscordManager.update();
    }

    /**
     * Parse an imgur key from the API credentials textfields
     * @return A valid ImgurKey object, or null otherwise.
     */
    private ImgurKey parseImgurKey()
    {
        String clientId = tf_imgClientId.getText().trim();
        String clientSecret = tf_imgClientSecret.getText().trim();
        String callback;

        if (chk_callback.isSelected())
        {
            callback = tf_imgCallback.getText().trim();
        }
        else
        {
            callback = defaultImgurCallback;
        }

        // update the GUI for each item that is erroneous.
        if (clientId.isEmpty())
            tf_imgClientId.setId("invalidClientId");
        else
            tf_imgClientId.setId("");

        if (clientSecret.isEmpty())
            tf_imgClientSecret.setId("invalidClientSecret");
        else
            tf_imgClientSecret.setId("");

        if (callback.isEmpty())
            tf_imgCallback.setId("invalidCallback");
        else
            tf_imgCallback.setId("");

        // if any entry was empty, return null.
        if (clientId.isEmpty() || clientSecret.isEmpty() || callback.isEmpty())
            return null;

        // otherwise, parse the imgur key
        return new ImgurKey(clientId, clientSecret, callback);
    }

    /**
     * Parse the discord token from the API credentials textfields
     * @return A discord token if applicable, or null otherwise.
     */
    private String parseDiscordToken()
    {
        String discordToken = tf_discordToken.getText().trim();

        tf_discordToken.setId("");

        if (discordToken.isEmpty())
            return null;

        return discordToken;
    }



    //region Sanitation check

    /**
     * Struct for validation checks.
     */
    private static class ValidationCheck
    {
        private boolean isValid;
        private String errorMessage;

        public ValidationCheck(boolean isValid, String errorMessage)
        {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }
    }

    private ValidationCheck imgurKeyIsValid()
    {
        // if the data was incomplete, return false
        if (acceptImgurKey == null)
            return new ValidationCheck(false, "The Imgur credentials were incomplete.");

        try
        {
            boolean v = ImgurManager.checkValidity(acceptImgurKey);

            if (!v)
                markImgurValues();

            return new ValidationCheck(v, "");
        }
        catch (Exception e)
        {
            markImgurValues();
            return new ValidationCheck(false, e.getMessage());
        }
    }

    /**
     * Mark the imgur credential textfields to be invalid.
     */
    private void markImgurValues()
    {
        tf_imgClientId.setId("invalidClientId");
        tf_imgClientSecret.setId("invalidClientSecret");

        if (chk_callback.isSelected())
            tf_imgCallback.setId("invalidCallback");
        else
            tf_imgCallback.setId("");
    }

    private ValidationCheck discordTokenIsValid()
    {
        // if the data was incomplete, return false
        if (acceptDiscordToken == null)
            return new ValidationCheck(false, "");

        try
        {
            boolean v = DiscordManager.discordTokenIsValid(acceptDiscordToken);

            if (!v)
                tf_discordToken.setId("invalidDiscordToken");

            return new ValidationCheck(v, "");
        }
        catch (Exception e)
        {
            tf_discordToken.setId("invalidDiscordToken");
            return new ValidationCheck(false, e.getMessage());
        }
    }

    private static String msg_imgurKeyInvalid(String errorMessage)
    {
        return "The imgur credentials that were given could not be used to gain access, " +
                "please check the credentials. " +
                "Until the application has valid Imgur Credentials it cannot function. \n" +
                "Imgur error message:\n" + errorMessage + "\n";
    }

    private static String msg_discordTokenInvalid(String errorMessage)
    {
        return "The discord token that was provided appears to be erroneous.\n" +
                "Discord error message:\n" + errorMessage + "\n";
    }

    //endregion



    /**
     * Disable the callback textfield depending on the value of the checkbox.
     * @param actionEvent
     */
    public void callbackCheckbox(ActionEvent actionEvent)
    {
        // disable the callback textfield if no callback is used.
        if (chk_callback.isSelected())
            tf_imgCallback.setDisable(false);
        else
            tf_imgCallback.setDisable(true);
    }
}
