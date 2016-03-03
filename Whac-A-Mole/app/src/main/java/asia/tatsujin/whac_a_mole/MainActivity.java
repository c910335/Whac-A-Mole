package asia.tatsujin.whac_a_mole;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.facebook.appevents.AppEventsLogger;

public class MainActivity extends AppCompatActivity {

    private ImageButton playButton;
    private ImageButton rankButton;
    private ImageButton exitButton;
    private MediaPlayer bgm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setListeners();

        bgm = MediaPlayer.create(this, R.raw.bgm_home);
        bgm.setLooping(true);
    }

    @Override
     protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
        bgm.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
        bgm.pause();
    }

    @Override
    protected void onDestroy() {
        bgm.release();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_about:
                showAboutDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        playButton = (ImageButton) findViewById(R.id.button_play);
        rankButton = (ImageButton) findViewById(R.id.button_rank);
        exitButton = (ImageButton) findViewById(R.id.button_exit);
    }

    private void setListeners() {
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });
        rankButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), RankActivity.class));
            }
        });
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showAboutDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.action_about)
                .setView(R.layout.dialog_about)
                .show();
        ((TextView) alertDialog.findViewById(R.id.contributors))
                .setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) alertDialog.findViewById(R.id.github))
                .setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void play() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.difficulty)
                .setItems(R.array.difficulties, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(MainActivity.this, GameActivity.class)
                            .putExtra("difficulty", which));
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
