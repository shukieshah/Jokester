package com.example.shuka.jokester;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by shuka on 1/7/2017.
 */

public class FavoritesActivity extends Activity {

    private EditText favoritesEditText;
    private String favoritesEditTextString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        favoritesEditText = (EditText) findViewById(R.id.favoritesEditText);

        SharedPreferences favorites = getSharedPreferences("Favorites", Context.MODE_PRIVATE);
        favoritesEditTextString = favorites.getString("favorites", "Empty");

        if (!favoritesEditTextString.equals("Empty")) {
            favoritesEditText.setText(favoritesEditTextString);
        }
    }

    public void onBack(View view) {

        SharedPreferences favorites = getSharedPreferences("Favorites",
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = favorites.edit();
        editor.putString("favorites", favoritesEditText.getText().toString());
        editor.commit();
        Intent goingBack = new Intent();
        setResult(RESULT_OK, goingBack);
        finish();
    }
}
