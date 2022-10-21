package com.example.customlistview_startrek;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;

import android.media.MediaParser;
import android.media.MediaPlayer;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

//Step-By-Step, Setting up the ListView

    private
    ListView lvEpisodes;     //Reference to the listview GUI component
    ListAdapter lvAdapter;   //Reference to the Adapter used to populate the listview.
    MediaPlayer liveLongProsper;
    MediaPlayer khanVideo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvEpisodes = (ListView)findViewById(R.id.lvEpisodes);
        lvAdapter = new MyCustomAdapter(this.getBaseContext());  //instead of passing the boring default string adapter, let's pass our own, see class MyCustomAdapter below!
        lvEpisodes.setAdapter(lvAdapter);

        liveLongProsper = MediaPlayer.create(MainActivity.this, getResources().getIdentifier("live_long_prosper", "raw", getPackageName()));
        khanVideo = MediaPlayer.create(MainActivity.this, getResources().getIdentifier("wrath_of_khan", "raw", getPackageName()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        return super.onCreateOptionsMenu(menu);   //get rid of default behavior.

        // Inflate the menu; this adds items to the action bar
        getMenuInflater().inflate(R.menu.my_test_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.mnu_one) {
            //Merchandise: open up the following page in an external browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://shop.startrek.com"));
            startActivity(browserIntent);
            return true;
        }
        if (id == R.id.mnu_two) {
            //Nuclear Wessel: pre-dial 1-800-startrk
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "1-800-startrek" ));
            startActivity(dialIntent);
            return true;
        }
        if (id == R.id.mnu_three) {
            //Phasers on stun: spawn SMS with the text "Ouch!"

            Intent smsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + "1-800-1020"));
            smsIntent.putExtra("sms_body", "Ouch!!");
            startActivity(smsIntent);
            return true;
        }
        if (id == R.id.mnu_four) {
            //Live Long and Prosper: play audio of this phrase
            liveLongProsper.start();
            return true;
        }
        if (id == R.id.mnu_five) {
            //Kahn!! : play video
            Intent videoIntent = new Intent(MainActivity.this, VideoActivity.class);
            startActivity(videoIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);  //if none of the above are true, do the default and return a boolean.
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("Rating Bar Preferences", Context.MODE_PRIVATE);

        for(int i = 0; i < lvEpisodes.getCount(); i++){
            float rating = prefs.getFloat("RatingBar" + i, 3.0f);
            Log.d("debugging::", "Rating" + i + " = " + rating);
            ((MyCustomAdapter)lvAdapter).setRating(i, rating);
        }

        Map<String, ?> x = prefs.getAll();
        Log.d("debugging::", x.toString());

    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences prefs = getSharedPreferences("Rating Bar Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        for (int i = 0; i < lvEpisodes.getCount(); i++) {
            editor.putFloat("RatingBar" + i, (Float) lvAdapter.getItem(i));
        }
        editor.commit();


    }

    @Override
    protected void onStop() {
        super.onStop();

        try{
            liveLongProsper.release();
            liveLongProsper = null;
        }catch (Exception e){
            Log.d("status", "no video");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences prefs = getSharedPreferences("Rating Bar Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        for (int i = 0; i < lvEpisodes.getCount(); i++) {
            editor.putFloat("RatingBar" + i, (Float) lvAdapter.getItem(i));
        }
        editor.commit();


//        SharedPreferences sharedPref = getSharedPreferences("ratingPref", MODE_PRIVATE);
//        SharedPreferences.Editor data = sharedPref.edit();
//
//        data.putFloat("rating", MyCustomAdapter.ratingbar.getRating());
//        data.apply();

    }
}


//***************************************************************//
//create a  class that extends BaseAdapter
//Hit Alt-Ins to easily implement required BaseAdapter methods.
//***************************************************************//
//STEP 1: Create references to needed resources for the ListView Object.  String Arrays, Images, etc.
class MyCustomAdapter extends BaseAdapter {

    private
    String episodes[];             //Keeping it simple.  Using Parallel arrays is the introductory way to store the List data.
    String  episodeDescriptions[];  //the "better" way is to encapsulate the list items into an object, then create an arraylist of objects.
    String episodeLinks[];  // website links of each episodes
    //     int episodeImages[];         //this approach is fine for now.
    ArrayList<Integer> episodeImages;  //Well, we can use one arrayList too...  Just mixing it up here, Arrays or Templated ArrayLists, you choose.

//    ArrayList<String> episodes;
//    ArrayList<String> episodeDescriptions;

    RatingBar ratingBar;     //Reference to the rating bar GUI component
    Float[] ratingBarValues;  // rating bar values for each episode

    Button btnRandom;
    Context context;   //Creating a reference to our context object, so we only have to get it once.  Context enables access to application specific resources.
    // Eg, spawning & receiving intents, locating the various managers.

    //STEP 2: Override the Constructor, be sure to:
    // grab the context, we will need it later, the callback gets it as a parm.
    // load the strings and images into object references.
    public MyCustomAdapter(Context aContext) {
//initializing our data in the constructor.
        context = aContext;  //saving the context we'll need it again.
        episodes =aContext.getResources().getStringArray(R.array.episodes);  //retrieving list of episodes predefined in strings-array "episodes" in strings.xml
        episodeDescriptions = aContext.getResources().getStringArray(R.array.episode_descriptions);

        episodeLinks = aContext.getResources().getStringArray(R.array.episode_links);


        ratingBarValues = new Float[episodes.length];  //initializing the rating bar values arraylist
        Arrays.fill(ratingBarValues, 3.0f);  //filling the rating bar values arraylist default value of 3.0f


//This is how you would do it if you were using an ArrayList, leaving code here for reference, though we could use it instead of the above.
//        episodes = (ArrayList<String>) Arrays.asList(aContext.getResources().getStringArray(R.array.episodes));  //retrieving list of episodes predefined in strings-array "episodes" in strings.xml
//        episodeDescriptions = (ArrayList<String>) Arrays.asList(aContext.getResources().getStringArray(R.array.episode_descriptions));  //Also casting to a friendly ArrayList.

        episodeImages = new ArrayList<Integer>();   //Could also use helper function "getDrawables(..)" below to auto-extract drawable resources, but keeping things as simple as possible.
        episodeImages.add(R.drawable.st_spocks_brain);
        episodeImages.add(R.drawable.st_arena__kirk_gorn);
        episodeImages.add(R.drawable.st_this_side_of_paradise__spock_in_love);
        episodeImages.add(R.drawable.st_mirror_mirror__evil_spock_and_good_kirk);
        episodeImages.add(R.drawable.st_platos_stepchildren__kirk_spock);
        episodeImages.add(R.drawable.st_the_naked_time__sulu_sword);
        episodeImages.add(R.drawable.st_the_trouble_with_tribbles__kirk_tribbles);
    }


    //STEP 3: Override and implement getCount(..),
// ListView uses this to determine how many rows to render.
    @Override
    public int getCount() {
//        return episodes.size(); //all of the arrays are same length, so return length of any... ick!  But ok for now. :)
        return episodes.length;   //all of the arrays are same length, so return length of any... ick!  But ok for now. :)
        //Q: How else could we have done this (better)? ________________
    }

    //STEP 4: Override getItem/getItemId, we aren't using these, but we must override anyway.
    @Override
    public Object getItem(int position) {
//        return episodes.get(position);  //In Case you want to use an ArrayList
        return ratingBarValues[position];        //really should be returning entire set of row data, but it's up to us, and we aren't using this call.
    }

//    public float getRating(int position) {
//        return ratingBarValues[position];
//    }

    @Override
    public long getItemId(int position) {
        return position;  //Another call we aren't using, but have to do something since we had to implement (base is abstract).
    }

    //THIS IS WHERE THE ACTION HAPPENS.  getView(..) is how each row gets rendered.
//STEP 5: Easy as A-B-C
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //position is the index of the row being rendered.
        //convertView represents the Row (it may be null),
        // parent is the layout that has the row Views.
        Log.d("debugging::", "getView() called for position: " + position);

//STEP 5a: Inflate the listview row based on the xml.
        View row;  //this will refer to the row to be inflated or displayed if it's already been displayed. (listview_row.xml)
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        row = inflater.inflate(R.layout.listview_row, parent, false);  //

//// Let's optimize a bit by checking to see if we need to inflate, or if it's already been inflated...
        if (convertView == null){  //indicates this is the first time we are creating this row.
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  //Inflater's are awesome, they convert xml to Java Objects!
            row = inflater.inflate(R.layout.listview_row, parent, false);
        }
        else
        {
            row = convertView;
        }

        ratingBar = (RatingBar) row.findViewById(R.id.rbEpisode);  //reference to the rating bar GUI component
        Log.d("debugging::", "ratingBarValues: " + Arrays.toString(ratingBarValues));
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                Toast.makeText(context, "Rating: " + rating, Toast.LENGTH_SHORT).show();
                ratingBarValues[position] = rating;
            }
        });

//STEP 5b: Now that we have a valid row instance, we need to get references to the views within that row and fill with the appropriate text and images.
        ImageView imgEpisode = (ImageView) row.findViewById(R.id.imgEpisode);  //Q: Notice we prefixed findViewByID with row, why?  A: Row, is the container.
        TextView tvEpisodeTitle = (TextView) row.findViewById(R.id.tvEpisodeTitle);
        TextView tvEpisodeDescription = (TextView) row.findViewById(R.id.tvEpisodeDescription);

        tvEpisodeTitle.setText(episodes[position]);
        tvEpisodeDescription.setText(episodeDescriptions[position]);
        imgEpisode.setImageResource(episodeImages.get(position).intValue());

        btnRandom = (Button) row.findViewById(R.id.btnRandom);
        final String randomMsg = ((Integer)position).toString() +": "+ episodeDescriptions[position];
        final String webLink = episodeLinks[position];
        btnRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, randomMsg, Toast.LENGTH_LONG).show();
                Log.d("debugging", "onClick: " + webLink);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(webLink));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

//STEP 5c: That's it, the row has been inflated and filled with data, return it.
        return row;  //once the row is fully constructed, return it.  Hey whatif we had buttons, can we target onClick Events within the rows, yep!
//return convertView;

    }

    public void setRating(int i, float rating) {
        ratingBarValues[i] = rating;
        Log.d("debugging::", "setRating: " + ratingBarValues[i]);
    }


    ///Helper method to get the drawables...///
    ///this might prove useful later...///

//    public ArrayList<Drawable> getDrawables() {
//        Field[] drawablesFields =com.example.sse.customlistview_sse.R.drawable.class.getFields();
//        ArrayList<Drawable> drawables = new ArrayList<Drawable>();
//
//        String fieldName;
//        for (Field field : drawablesFields) {
//            try {
//                fieldName = field.getName();
//                Log.i("LOG_TAG", "com.your.project.R.drawable." + fieldName);
//                if (fieldName.startsWith("animals_"))  //only add drawable resources that have our prefix.
//                    drawables.add(context.getResources().getDrawable(field.getInt(null)));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
