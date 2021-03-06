package com.shortstack.hackertracker.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import com.shortstack.hackertracker.Adapter.TweetAdapter;
import com.shortstack.hackertracker.Model.Key;
import com.shortstack.hackertracker.R;
import com.shortstack.hackertracker.Utils.Connectivity;
import com.shortstack.hackertracker.Utils.PullToRefresh;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;

public class TwitterFeed extends Activity {

    public PullToRefresh tweetListView;
    public List<twitter4j.Status> tweets;
    public twitter4j.Status tweetData[];
    public TweetAdapter adapter;
    public ProgressDialog progress;

    public interface Callback {

        void run(Object result);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twitter);

        if (Connectivity.isConnected(this.getApplicationContext()) || Connectivity.isConnectedMobile(this.getApplicationContext()) || Connectivity.isConnectedWifi(this.getApplicationContext())) {

            progress = new ProgressDialog(this);
            progress.setTitle("Searching Tweets");
            progress.setMessage("Please wait...");


            getTweets request = new getTweets(new Callback() {
               public void run(Object result) {


                    if (!(((List<twitter4j.Status>) result).size() < 1)) {
                        tweets = (List<twitter4j.Status>) result;

                        tweetData = tweets.toArray(new twitter4j.Status[tweets.size()]);

                        adapter = new TweetAdapter(TwitterFeed.this, R.layout.tweet, tweetData);

                        tweetListView = (PullToRefresh) findViewById(R.id.listView);

                        tweetListView.setAdapter(adapter);

                        tweetListView.setOnRefreshListener(new PullToRefresh.OnRefreshListener() {

                            @Override
                            public void onRefresh() {

                                if (Connectivity.isConnected(TwitterFeed.this) || Connectivity.isConnectedMobile(TwitterFeed.this) || Connectivity.isConnectedWifi(TwitterFeed.this)) {

                                    progress = new ProgressDialog(TwitterFeed.this);
                                    progress.setTitle("Searching Tweets");
                                    progress.setMessage("Please wait...");


                                    getTweets request = new getTweets(new Callback() {
                                        public void run(Object result) {


                                            if (!(((List<twitter4j.Status>) result).size() < 1)) {
                                                tweets = (List<twitter4j.Status>) result;

                                                tweetData = tweets.toArray(new twitter4j.Status[tweets.size()]);

                                                adapter = new TweetAdapter(TwitterFeed.this, R.layout.tweet, tweetData);

                                                tweetListView = (PullToRefresh) findViewById(R.id.listView);

                                                tweetListView.setAdapter(adapter);
                                            }

                                        }});
                                    request.execute();
                                } else {
                                    Toast.makeText(getApplicationContext(),"No Network Connection", Toast.LENGTH_SHORT).show();
                                    finish();
                                }


                                tweetListView.postDelayed(new Runnable() {


                                    @Override
                                    public void run() {
                                        tweetListView.onRefreshComplete();
                                    }
                                }, 2000);
                            }
                        });

                    }

               }});
            request.execute();
        } else {
            Toast.makeText(getApplicationContext(),"No Network Connection", Toast.LENGTH_SHORT).show();
            finish();
        }



    }



    public class getTweets extends AsyncTask<Void, Void, Object> {

        public Object tweetList;

        Callback callback;
        public getTweets(Callback callback){
            this.callback = callback;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            progress.show();
        }

        @Override
        protected Object doInBackground(Void... arg0) {

            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey(Key.consumerKey)
                    .setOAuthConsumerSecret(Key.consumerSecret)
                    .setOAuthAccessToken(Key.OAuthAccessToken)
                    .setOAuthAccessTokenSecret(Key.OAuthAccessTokenSecret);
            TwitterFactory tf = new TwitterFactory(cb.build());
            Twitter twitter = tf.getInstance();
            try {
                Query query = new Query("(#defcon) OR (@_defcon_) OR (#dc21) OR (#defcon21)");
                query.setResultType("MIXED");
                query.setCount(20);
                QueryResult result;
                result = twitter.search(query);
                tweetList = result.getTweets();
            } catch (TwitterException te) {
                te.printStackTrace();
                Log.v("tweet","Failed to search tweets: " + te.getMessage());
            }


            progress.dismiss();
            return tweetList;
        }

        protected void onPostExecute(Object result) {
            callback.run(result);
            progress.dismiss();
        }

    }




}