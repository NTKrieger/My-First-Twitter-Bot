package CSSR;

import java.util.*;
import twitter4j.*;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;


public class App
{
    public static void main( String[] args )
    {
        Twitter twitter = TwitterFactory.getSingleton();

        try
        {
            List<User> following  = twitter.getFollowersList();
            List<Status> statuses = twitter.getHomeTimeline();
            for (Status status : statuses) {
               // if(status.getUser().getScreenName().equals("MaxKriegerVG"))
                {

                    //twitter.createFavorite(status.getId());
                }
                System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
                if(status.getUser().getScreenName().equals("MaxKriegerVG"))
                {
                    twitter.createFavorite(status.getId());
                    System.out.println(twitter.getRateLimitStatus());
                }
            }
        }
        catch (TwitterException ex)
        {
            System.out.println(ex.getErrorMessage());
        }
    }
}
