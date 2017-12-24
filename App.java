package CSSR;

import java.util.logging.*;
import java.util.logging.Logger;
import java.io.IOException;
import java.util.Random;
import twitter4j.*;

public class App
{
    final static int GOAL = 1000;
    final static int ERROR_TOLERANCE = 100;
    static String pitch = "Hello! I am a research tool!  Please follow me?  Read all about me at www.clevelandsocialresearch.wordpress.com";
    static int accountsFollowed = 0;
    static int errors = 0;
    static int blockingDM = 0;
    static int askedNicely = 0;
    final static String MY_ACCOUNT = "@AbbotMcFly";

    final static java.util.logging.Logger LOGGER = Logger.getLogger("CSSR.App");

    static String targetAccount = "@OmegaNix";

    public static void main( String[] args ) throws InterruptedException {

        Twitter twitter = TwitterFactory.getSingleton();
        configLogging();
        postTweet(twitter, "Abbot the robot reindeer! www.clevelandsocialresearch.wordpress.com");
        do
        {
            getNextUser(twitter);
            followUser(twitter, targetAccount);
            Thread.sleep(60000);

        } while (errors < ERROR_TOLERANCE && accountsFollowed < GOAL);

       collectData(twitter);
    }
    public static void collectData(Twitter twitter)
    {
        try
        {
            int success = 0;
            int followerCount = 0;
            long cursor = -1;

            IDs followerIDs  = twitter.getFollowersIDs(MY_ACCOUNT, cursor, 1000);
            long [] followerIDArray = new long[1000];
            followerIDArray = followerIDs.getIDs();

            IDs friendIDs  = twitter.getFriendsIDs(MY_ACCOUNT, cursor, 1000);
            long [] friendIDArray = new long[1000];
            friendIDArray = friendIDs.getIDs();

            for( int i = 0; i < followerIDArray.length; ++i)
            {
                if (followerIDArray[i] != 0)
                    ++followerCount;
                    LOGGER.info(followerCount + " followers.");

                for( int x = 0; x < friendIDArray.length; ++x)
                {
                    if (i == x && x + i != 0)
                    {
                        ++success;
                        LOGGER.info(twitter.showUser(followerIDArray[i]).getScreenName() + " followed you!");
                    }
                }
            }
            LOGGER.info("Total followers gained from asking: " + success);
            LOGGER.info("Total follower count: " + followerCount);
            LOGGER.info("Accounts followed: " + accountsFollowed);
            LOGGER.info("DMs sent: " + askedNicely);
            LOGGER.info("DMs blocked: " + blockingDM);
            LOGGER.info("Errors: " + errors);
        }
        catch (TwitterException ex) {
            LOGGER.warning(ex.getErrorMessage());
        }
    }
    public static void configLogging()
    {  //set up logging to external file for review afterwards
        try
        {
            Handler fileHandler = new FileHandler("./Experiment1_1_0.log");
            LOGGER.addHandler(fileHandler);
            fileHandler.setLevel(Level.ALL);
            LOGGER.config("Logger Configured!");

        } catch(IOException ex) {
            LOGGER.log(Level.SEVERE, "Error in FileHandler.", ex);
            errors = ERROR_TOLERANCE;  // prevents program from ever running without a log
        }
    }
    public static void followUser (Twitter twitter, String targetAccount)
    {
        //follows a user if they are not a friend or have a pending friend request
        try
        {

            if(twitter.showUser(targetAccount).isFollowRequestSent())
            {
                LOGGER.info(targetAccount + " was already sent a follow request.  Fetching new user.");
            }
            else
            {
                twitter.createFriendship(targetAccount);
                ++accountsFollowed;
                sendMessage(twitter, targetAccount);
                LOGGER.info(targetAccount + " followed! " + accountsFollowed + " total accounts followed!");
            }
        } catch (TwitterException ex) {
            LOGGER.warning(ex.getErrorMessage());
            ++errors;
        }
    }
    public static void getNextUser (Twitter twitter)
    {// takes a Twitter handle as a parameter, returns a follower handle selected at random from the first page of 5000.
        try {
            try {
                final int PAGE_MAX = 5000;

                long cursor = -1;
                int followerCount = twitter.showUser(targetAccount).getFollowersCount();
                IDs ids = twitter.getFollowersIDs(targetAccount, cursor, PAGE_MAX);
                long[] idArray = new long[5000];
                idArray = ids.getIDs();
                Random rn = new Random();
                int randomID;
                if (followerCount < PAGE_MAX)
                    randomID = rn.nextInt(followerCount);
                else
                    randomID = rn.nextInt(PAGE_MAX);
                long nextUserID = idArray[randomID];
                LOGGER.info("Next user is " + nextUserID);
                targetAccount = twitter.showUser(nextUserID).getScreenName();

            } catch (TwitterException ex) {

                LOGGER.warning(ex.getErrorMessage());
                ++errors;
            }
        } catch (NullPointerException npex)
        {
            ++errors;
            LOGGER.warning("403 Unauthorized - null result returned");
        }
    }
    public static void postTweet(Twitter twitter, String msg)
    { //nothing special.  Update status and log it.
        try {
            Status status = twitter.updateStatus(msg);
            LOGGER.info("Tweeted " + status.getText());

        } catch (TwitterException ex) {
            LOGGER.warning(ex.getErrorMessage());
            ++errors;
        }
    }
    public static void sendMessage (Twitter twitter, String targetAccount)
    { // sends the pitch to target user, increments accountsMessaged
        try
        {
            twitter.sendDirectMessage(targetAccount, pitch);
            ++askedNicely;
            LOGGER.info("Asked " + targetAccount + " nicely.");
        } catch (TwitterException ex) {
            LOGGER.info(ex.getErrorMessage());
            ++blockingDM;
        }
    }
}