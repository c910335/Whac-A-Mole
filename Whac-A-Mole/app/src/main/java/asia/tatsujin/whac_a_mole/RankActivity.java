package asia.tatsujin.whac_a_mole;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static asia.tatsujin.whac_a_mole.R.id.button_login;


public class RankActivity extends AppCompatActivity {

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private GraphRequest graphRequest;
    private ArrayList<Score> scores;
    private LinearLayout scoresView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        scoresView = (LinearLayout) findViewById(R.id.view_scores);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(button_login);
        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        for (String scope : loginResult.getRecentlyGrantedPermissions())
                            if (scope.equals("publish_actions")) {
                                updateRank();
                                break;
                            }
                    }

                    @Override
                    public void onCancel() {
                        finish();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.w("GG", "G_G");
                    }
                });
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("user_games_activity", "user_friends"));
        LoginManager.getInstance().logInWithPublishPermissions(this, Collections.singletonList("publish_actions"));
    }

    private synchronized void updateRank() {
        scores = new ArrayList<>();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        JSONObject object = new JSONObject();
        try {
            object.put("score", PreferenceManager.getDefaultSharedPreferences(this).getInt("high_score", 0));
        } catch (JSONException e) {
            Log.w("GG", "G_G");
        }
        graphRequest = GraphRequest.newPostRequest(accessToken, "me/scores", object, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                if (response == null || response.getError() != null)
                    Log.w("GG", "G_G");
                else {
                    Log.w("www", response.getRawResponse());
                    JSONObject jsonObject = response.getJSONObject();
                    try {
                        if (jsonObject.getBoolean("success"))
                            getRank();
                        else
                            Log.w("GG", "G_G");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        graphRequest.executeAsync();
    }

    private void getRank() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        graphRequest = GraphRequest.newGraphPathRequest(accessToken, getString(R.string.facebook_app_id) + "/scores", new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                if (response == null || response.getError() != null)
                    Log.w("GG", "G_G");
                else {
                    Log.w("www", response.getRawResponse());
                    JSONObject jsonObject = response.getJSONObject();
                    try {
                        JSONArray data = jsonObject.getJSONArray("data");
                        for (int i = 0; i != data.length(); ++i) {
                            JSONObject score = data.getJSONObject(i);
                            scores.add(new Score(score.getJSONObject("user").getString("name"), score.getInt("score")));
                        }
                        showRank();
                    } catch (JSONException e) {
                        Log.w("GG", "G_G");
                    }
                }
            }
        });
        graphRequest.executeAsync();
    }

    private void showRank() {
        scoresView.removeAllViews();
        int i = 1;
        for (Score score: scores) {
            RelativeLayout scoreRow = (RelativeLayout) getLayoutInflater().inflate(R.layout.row_score, null);
            ((TextView) scoreRow.findViewById(R.id.text_name)).setText(i++ + ". " + score.getName());
            ((TextView) scoreRow.findViewById(R.id.text_score)).setText(score.getScore() + "");
            scoresView.addView(scoreRow);
        }
    }

    @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class Score {
        private String name;
        private int score;

        public Score(String name, int score) {
            this.name = name;
            this.score = score;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }
    }
}
