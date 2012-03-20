package org.ubilab.payment.twitter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Desktop;
import javax.swing.JOptionPane;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterException;
import twitter4j.StatusUpdate;
import twitter4j.GeoLocation;
import twitter4j.auth.RequestToken;
import twitter4j.auth.AccessToken;

/**
 *
 * @author atsushi-o
 */
public class TwitterPost {
    private static final Logger LOG;
    static {
        LOG = Logger.getLogger(TwitterPost.class.getName());
    }

    private TwitterPost(){}

    /**
     * Twitterの認証を行う
     * @return 認証済みトークン
     */
    public static String[] auth() {
        String[] ret = null;
        Twitter twitter = new TwitterFactory().getInstance();
        AccessToken accessToken = null;
        try {
            RequestToken requestToken = twitter.getOAuthRequestToken();
            if (Desktop.isDesktopSupported()) {
                Desktop dt = Desktop.getDesktop();

                dt.browse(new java.net.URI(requestToken.getAuthorizationURL()));

                String pin = JOptionPane.showInputDialog("Please input PIN");

                accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                ret = new String[]{accessToken.getToken(), accessToken.getTokenSecret()};
                JOptionPane.showMessageDialog(null, "Twitter Authentication complate.", "twitter Auth", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (TwitterException e) {
            if (401 == e.getStatusCode()) {
                LOG.log(Level.WARNING, "Unable to get the access token.", e);
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
        } catch (java.net.URISyntaxException ex) {
            LOG.log(Level.WARNING, null, ex);
        }

        return ret;
    }

    public static void post(String message) {
        Twitter twitter = new TwitterFactory().getInstance();
        StatusUpdate update = new StatusUpdate(message);
        update.setLocation(new GeoLocation(34.731557, 135.734187));

        try {
            twitter.updateStatus(update);
        } catch (TwitterException ex) {
            LOG.log(Level.WARNING, "Twitter status update failed.", ex);
        }
    }
}
