package edu.gvsu.cis.greenmbr.stedmane.SteamMeet;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.parse.Parse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class MyActivity extends Activity implements View.OnClickListener {
    // Called when the activity is first created.
    private TextView profile, user, emailText, state;
    private String userSave, profileSave, stateSave,
                   emailSave;
    private ImageView avatar;
    private String profNumber, emailAddress, emailPreference;
    private Button eventButton, linkEmail, createEvent;
    public static final String PREFS2 = "MyPrefsFile2";
    private Intent intented;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profileview);
        Parse.initialize(this, "2JUDU3NzfMd4QN1KY2HiKWFpAG9nSiAyeWM4aQNg",
                "YZazw5idYfUiivovNxZFUezRSBznPGbmTlvYDkZW");
        user = (TextView) findViewById(R.id.persona);
        profile = (TextView) findViewById(R.id.profileSite);
        state = (TextView) findViewById(R.id.activeState);
        emailText = (TextView) findViewById(R.id.emailText);
        avatar = (ImageView) findViewById(R.id.imageView);
        eventButton = (Button) findViewById(R.id.toEvents);
        createEvent = (Button) findViewById(R.id.create);
        linkEmail = (Button) findViewById(R.id.link);
        intented = getIntent();
        profNumber = intented.getStringExtra("storage");
        emailAddress = intented.getStringExtra("emailAddress");
        eventButton.setOnClickListener(this);
        linkEmail.setOnClickListener(this);
        createEvent.setOnClickListener(this);
        if(emailAddress != null){
            SharedPreferences emailLink = this.getSharedPreferences(PREFS2, 0);
            SharedPreferences.Editor editor = emailLink.edit();
            editor.putString("emailPref", emailAddress);
            editor.commit();
        }
        SharedPreferences settings = this.getSharedPreferences(PREFS2, 0);
        emailPreference = settings.getString("emailPref", null);
        if(emailPreference != null){
            emailText.setText("E-Mail: " + emailPreference);
            linkEmail.setVisibility(-1);
        }
        if(savedInstanceState != null){
            userSave = savedInstanceState.getString("UserSave");
            profileSave = savedInstanceState.getString("ProfileSave");
            stateSave = savedInstanceState.getString("StateSave");
            profNumber = savedInstanceState.getString("ProfSave");
            emailSave = savedInstanceState.getString("EmailLink");
            user.setText(userSave);
            profile.setText(profileSave);
            emailText.setText(emailSave);
            state.setText(stateSave);
            new imageTask().execute();
        }
        else{
            new ProfileTask().execute();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("UserSave", user.getText().toString());
        outState.putString("ProfileSave", profile.getText().toString());
        outState.putString("StateSave", state.getText().toString());
        outState.putString("ProfSave", profNumber);
        outState.putString("EmailLink", emailText.getText().toString());
    }

    @Override
    public void onClick(View v) {
        if(v == eventButton){
            Intent toEvents = new Intent(this, EventsMain.class);
            toEvents.putExtra("storage", profNumber);
            toEvents.putExtra("emailAddress", emailPreference);
            toEvents.putExtra("lat", intented.getDoubleExtra("lat", 0));
            toEvents.putExtra("lon", intented.getDoubleExtra("lon", 0));
            startActivity(toEvents);
        }
        if(v == linkEmail){
            Intent toEmail = new Intent(this, EmailLogin.class);
            toEmail.putExtra("storage", profNumber);
            startActivity(toEmail);
        }
        if(v == createEvent){
            Intent toCreate = new Intent(this, CreatePlaces.class);
            toCreate.putExtra("emailAddress", emailPreference);
            toCreate.putExtra("storage", profNumber);
            startActivity(toCreate);
        }
    }

    private class ProfileTask extends AsyncTask<Void, Integer, JSONObject> {
        private Drawable avatarImg;
        @Override
        protected JSONObject doInBackground(Void... params) {
            URL profileURL;
            try {
                profileURL = new URL("http://api.steampowered.com/" +
                        "ISteamUser/GetPlayerSummaries/v00" +
                        "02/?key=A35259FADACBD1E99D1101AD8" +
                        "4321147&steamids=" + profNumber);
                String out = "";
                HttpURLConnection conn = (HttpURLConnection) profileURL.openConnection();
                Scanner scan = new Scanner(conn.getInputStream());
                while (scan.hasNextLine()) {
                    out += scan.nextLine();
                }
                JSONObject arr = new JSONObject(out);
                JSONArray obj = (arr.getJSONObject("response")).getJSONArray("players");
                JSONObject profileObj = obj.getJSONObject(0);
                String avatarString = profileObj.getString("avatarfull");
                URL avatarURL = new URL(avatarString);
                avatarImg = Drawable.createFromStream(avatarURL.openStream(), "Picture");
                return profileObj;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject profileData) {
            try {
                String userTemp = profileData.getString("personaname");
                String profTemp = profileData.getString("profileurl");
                String clanTemp = profileData.getString("primaryclanid");
                int stateTemp = profileData.getInt("personastate");
                avatar.setImageDrawable(avatarImg);

                user.setText("User Name: " + userTemp);
                profile.setText("Steam Profile: " + profTemp);
                if (stateTemp == 1)
                    state.setText("State: Online");
                else if (stateTemp == 2) {
                    state.setText("State: Busy");
                } else if (stateTemp == 3) {
                    state.setText("State: Away");
                } else if (stateTemp == 4) {
                    state.setText("State: Snooze");
                } else if (stateTemp == 5) {
                    state.setText("State: Looking to Play");
                } else if (stateTemp == 6) {
                    state.setText("State: Looking to Trade");
                } else
                    state.setText("State: Offline");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class imageTask extends AsyncTask<Void, Integer, Void> {
        Drawable avatar2;
        @Override
        protected Void doInBackground(Void... params) {
            URL profileURL;
            try {
                profileURL = new URL("http://api.steampowered.com/" +
                        "ISteamUser/GetPlayerSummaries/v00" +
                        "02/?key=A35259FADACBD1E99D1101AD8" +
                        "4321147&steamids=" + profNumber);
                String out = "";
                HttpURLConnection conn = (HttpURLConnection) profileURL.openConnection();
                Scanner scan = new Scanner(conn.getInputStream());
                while (scan.hasNextLine()) {
                    out += scan.nextLine();
                }
                JSONObject arr = new JSONObject(out);
                JSONArray obj = (arr.getJSONObject("response")).getJSONArray("players");
                JSONObject profileObj = obj.getJSONObject(0);
                String avatarString = profileObj.getString("avatarfull");
                URL avatarURL = new URL(avatarString);
                avatar2 = Drawable.createFromStream(avatarURL.openStream(), "Picture");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            avatar.setImageDrawable(avatar2);
        }
    }
}
