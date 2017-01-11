package com.example.shuka.jokester;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by shuka on 1/7/2017.
 */

public class ReadingService extends IntentService {

    // Used to identify when the IntentService finishes
    public static final String TRANSACTION_DONE = "com.example.TRANSACTION_DONE";

    public ReadingService() {
        super(ReadingService.class.getName());
    }


    public ReadingService(String name) {
        super(name);
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        Log.e("ReadingService", "Service Started");

        // Get the URL for the file to download
        String passedURL = intent.getStringExtra("url");

        downloadFile(passedURL);

        Log.e("ReadingService", "Service Stopped");

        // Broadcast an intent back to MainActivity when file is downloaded
        Intent i = new Intent(TRANSACTION_DONE);
        ReadingService.this.sendBroadcast(i);

    }

    protected void downloadFile(String theURL) {
        // The name for the file we will save data to
        String fileName = "jokeFile";

        try{

            // Create an output stream to write data to a file (private to everyone except our app)
            FileOutputStream outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);

            // Get File
            URL fileURL = new URL(theURL);

            // Create a connection we can use to read data from a url
            HttpURLConnection urlConnection = (HttpURLConnection) fileURL.openConnection();

            // We are using the GET method
            urlConnection.setRequestMethod("GET");

            // Set that we want to allow output for this connection
            urlConnection.setDoOutput(true);

            // Connect to the url
            urlConnection.connect();

            // Gets an input stream for reading data
            InputStream inputStream = urlConnection.getInputStream();

            // Define the size of the buffer
            byte[] buffer = new byte[1024];
            int bufferLength = 0;

            // read reads a byte of data from the stream until there is nothing more
            while ( (bufferLength = inputStream.read(buffer)) > 0 ){

                // Write the data received to our file
                outputStream.write(buffer, 0, bufferLength);

            }

            // Close our connection to our file
            outputStream.close();

            // Get File Done

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
