package com.example.shuka.jokester;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class JokeActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView jokeTextView;
    private Locale currentSpokenLang = Locale.US;
    //Synthesizes text to speech
    private TextToSpeech textToSpeech;
    private String favoritesEditTextString;
    private boolean nextJokeGenerated = true;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("JOKE", jokeTextView.getText().toString());
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joke);
        jokeTextView = (TextView) findViewById(R.id.jokeTextView);
        textToSpeech = new TextToSpeech(this, this);

        SharedPreferences favorites = getSharedPreferences("Favorites",
                Context.MODE_PRIVATE);
        favoritesEditTextString = favorites.getString("favorites", "Empty");

        if (favoritesEditTextString.equals("Empty")) {
            favoritesEditTextString = "";
        }

        // Allows use to track when an intent with the id TRANSACTION_DONE is executed
        // We can call for an intent to execute something and then tell use when it finishes
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ReadingService.TRANSACTION_DONE);

        // Prepare the main thread to receive a broadcast and act on it
        registerReceiver(downloadReceiver, intentFilter);
        if (savedInstanceState != null) {
            String joke = savedInstanceState.getString("JOKE");
            jokeTextView.setText(joke);
        } else {
            initializeJoke();
        }
    }

    public void initializeJoke() {
        // Create an intent to run the IntentService in the background
        Intent intent = new Intent(this, ReadingService.class);

        // Pass the URL that the IntentService will download from
        intent.putExtra("url", "http://www.randomjoke.com/topic/oneliners.php");

        // Start the intent service
        this.startService(intent);
    }

    public void onSpeak(View view) {
        //set the voice to use
        textToSpeech.setLanguage(currentSpokenLang);

        if (jokeTextView.getText().toString().length() > 0) {
            textToSpeech.speak(jokeTextView.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
        } else {
            Toast.makeText(this, "No Joke Available", Toast.LENGTH_SHORT).show();
        }
    }

    public void onFavorite(View view) {

        if (nextJokeGenerated) {
            SharedPreferences favorites = getSharedPreferences("Favorites",
                    Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = favorites.edit();
            editor.putString("favorites", favoritesEditTextString + jokeTextView.getText().toString() + "\n");
            editor.commit();

            favoritesEditTextString = favorites.getString("favorites", "Empty");
            Toast.makeText(this, "The joke was added to your favorites", Toast.LENGTH_LONG).show();
            System.out.println(favoritesEditTextString);
            nextJokeGenerated = false;
        } else {
            Toast.makeText(this, "This joke has already been added", Toast.LENGTH_LONG).show();
        }
    }

    public void onNext(View view) {
        // Create an intent to run the IntentService in the background
        Intent intent = new Intent(this, ReadingService.class);

        // Pass the URL that the IntentService will download from
        intent.putExtra("url", "http://www.randomjoke.com/topic/oneliners.php");

        // Start the intent service
        this.startService(intent);

        nextJokeGenerated = true;
    }

    public void onGoToFavorites(View view) {
        Intent favoritesIntent = new Intent(this,
                FavoritesActivity.class);
        final int result = 1;
        startActivityForResult(favoritesIntent, result);
    }

    // Is alerted when the IntentService broadcasts TRANSACTION_DONE
    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {

        // Called when the broadcast is received
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.e("FileService", "Service Received");

            showFileContents();

        }
    };

    // Will read our local file and put the text in the EditText
    public void showFileContents(){

        // Will build the String from the local file
        StringBuilder sb;

        try {
            // Opens a stream so we can read from our local file
            FileInputStream fis = this.openFileInput("jokeFile");

            // Gets an input stream for reading data
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

            // Used to read the data in small bytes to minimize system load
            BufferedReader bufferedReader = new BufferedReader(isr);

            // Read the data in bytes until nothing is left to read
            sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            String html = sb.toString();
            Document doc = Jsoup.parse(html);
            String text = doc.body().text();
            text = text.substring(text.indexOf("appropriate") + 13, text.indexOf("Over"));
            // Put downloaded text into the EditText
            //downloadedEditText.setText(sb.toString());
            jokeTextView.setText(text);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInit(int status) {
        //Check if TextToSpeech is available
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(currentSpokenLang);

            if (result == textToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language Not Supported", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Text To Speech Failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SharedPreferences favorites = getSharedPreferences("Favorites",
                Context.MODE_PRIVATE);
        favoritesEditTextString = favorites.getString("favorites", "Empty");

        if (favoritesEditTextString.equals("Empty")) {
            favoritesEditTextString = "";
        }
    }
}
