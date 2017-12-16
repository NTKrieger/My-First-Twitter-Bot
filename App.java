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

    final static java.util.logging.Logger LOGGER = Logger.getLogger("CSSR.App");

    static String targetAccount = "@MaxKriegerVG";


    public static void main( String[] args ) throws InterruptedException
    {
        configLogging();
        postTweet("One small step for man.  1,000 small steps for bot.");
        do
        {
            followUser(targetAccount);
            getNextUser(targetAccount);
            Thread.sleep(60000);

        } while (errors > ERROR_TOLERANCE && accountsFollowed > GOAL);
        collectData();
    }
    public static void collectData ()
    {
        try
        {
            Twitter twitter = TwitterFactory.getSingleton();
            int success = 0;
            int followerCount = 0;
            long cursor = -1;

            IDs followerIDs  = twitter.getFollowersIDs(targetAccount, cursor, 1000);
            long [] followerIDArray = new long[1000];
            followerIDArray = followerIDs.getIDs();

            IDs friendIDs  = twitter.getFriendsIDs(targetAccount, cursor, 1000);
            long [] friendIDArray = new long[1000];
            friendIDArray = friendIDs.getIDs();

            for( int i = 0; i < followerIDArray.length; ++i)
            {
               if (followerIDArray[i] != 0)
                    ++followerCount;

                for( int x = 0; i < friendIDArray.length; ++x)
                {
                    if (i == x)
                    {
                        ++success;
                        LOGGER.info(twitter.showUser(followerIDArray[i]).getScreenName() + " followed you!");
                    }
                }
            }
            LOGGER.info("Total followers gained from asking: " + success);
            LOGGER.info("Unasked followers gained: " + (followerCount - success) );
        }
        catch (TwitterException ex) {
            LOGGER.warning(ex.getErrorMessage());
        }
    }
    public static void configLogging()
    {  //set up logging to external file for review afterwards
        try
        {
            Handler fileHandler = new FileHandler("./Experiment1.log");
            LOGGER.addHandler(fileHandler);
            fileHandler.setLevel(Level.ALL);
            LOGGER.config("Logger Configured!");

        } catch(IOException ex) {
            LOGGER.log(Level.SEVERE, "Error in FileHandler.", ex);
            errors = ERROR_TOLERANCE;  // prevents program from ever running without a log
        }
    }
    public static void followUser (String targetAccount)
    {
        //follows a user if they do not have a pending follow request.  asks for another user if they do.
        try
        {
            Twitter twitter = TwitterFactory.getSingleton();
            if(twitter.showUser(targetAccount).isFollowRequestSent())
            {
                LOGGER.info(targetAccount + " was already sent a follow request.  Fetching new user.");
                followUser(getNextUser(targetAccount));
            }
            else
            {
                twitter.createFriendship(targetAccount);
                ++accountsFollowed;
                sendMessage(targetAccount);
                LOGGER.info(targetAccount + " followed! " + accountsFollowed + " total accounts followed!");
            }
        } catch (TwitterException ex) {
            LOGGER.warning(ex.getErrorMessage());
            ++errors;
        }
    }
    public static String getNextUser (String targetAccount)
    {// takes a Twitter handle as a parameter, returns a follower handle selected at random from the first page of 5000.
        try
        {
            final int PAGE_MAX = 5000;
            Twitter twitter = TwitterFactory.getSingleton();
            long cursor = -1;
            int followerCount = twitter.showUser(targetAccount).getFollowersCount();
            IDs ids  = twitter.getFollowersIDs(targetAccount, cursor, PAGE_MAX);
            long [] idArray = new long[5000];
            idArray = ids.getIDs();
            Random rn = new Random();
            int randomID;
            if (followerCount < PAGE_MAX)
                randomID = rn.nextInt(followerCount);
            else
                randomID = rn.nextInt(PAGE_MAX);
            long nextUserID = idArray[randomID];
            LOGGER.info("Next user is " + nextUserID);
            return twitter.showUser(nextUserID).getScreenName();

        } catch (TwitterException ex) {

            LOGGER.warning(ex.getErrorMessage());
            ++errors;
            return (ex.getErrorMessage());
        }
    }
    public static void postTweet(String msg)
    { //nothing special.  Update status and log it.
        Twitter twitter = TwitterFactory.getSingleton();
        try {
            Status status = twitter.updateStatus(msg);
            LOGGER.info("Tweeted " + status.getText());

        } catch (TwitterException ex) {
            LOGGER.warning(ex.getErrorMessage());
            ++errors;
        }
    }
    public static void sendMessage (String targetAccount)
    { // sends the pitch to target user, increments accountsMessaged
        try
        {
            Twitter twitter = TwitterFactory.getSingleton();
            twitter.sendDirectMessage(targetAccount, pitch);
            LOGGER.info("Asked " + targetAccount + " nicely.");
        } catch (TwitterException ex) {
            LOGGER.warning(ex.getErrorMessage());
            ++errors;
        }
    }
}

