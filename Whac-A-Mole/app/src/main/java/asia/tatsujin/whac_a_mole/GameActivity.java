package asia.tatsujin.whac_a_mole;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {

    final static int SPEED[] = {500, 300, 200, 50};
    boolean isStart = false, penalty, isPenalizing = false;
    int time, score = 0, speed;
    TextView timeText, scoreText;
    Button startButton;
    GridLayout molesGrid;
    List<Mole> moles;
    Random random;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        initVariables();
        initViews();
        setListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (! isStart && item.getItemId() == R.id.action_rule) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.action_rule)
                    .setMessage("Error 500 Not Implemented")
                    .show();
            return true;
        }
        return false;
    }

    private void initVariables() {
        speed = SPEED[getIntent().getIntExtra("difficulty", 0)];
        time = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("time", "10"));
        penalty = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("penalty", false);
        random = new Random();
        moles = new ArrayList<>();
    }

    private void initViews() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        timeText = (TextView) findViewById(R.id.text_time);
        timeText.setText(time + "");
        scoreText = (TextView) findViewById(R.id.text_score);
        molesGrid = (GridLayout) findViewById(R.id.grid_moles);
        for (int i = 0; i != 3; ++i) {
            for (int j = 0; j != 3; ++j) {
                Mole mole = new Mole(this);
                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                layoutParams.rowSpec = GridLayout.spec(i, 1, 1);
                layoutParams.columnSpec = GridLayout.spec(j, 1, 1);
                layoutParams.setGravity(Gravity.FILL);
                molesGrid.addView(mole, layoutParams);
                moles.add(mole);
            }
        }
        startButton = (Button) findViewById(R.id.button_start);
    }

    private void setListeners() {
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (isStart && ! isPenalizing) {
                        Mole mole = (Mole) v;
                        if (mole.isUp()) {
                            addScore();
                            mole.popDown();
                            mole.playSoundEffect(0);
                        } else if (penalty) {
                            penalize();
                            mole.playSoundEffect(0);
                        }
                    }
                }
                return true;
            }
        };
        for (Mole mole : moles)
            mole.setOnTouchListener(onTouchListener);
    }

    private void start() {
        startButton.setClickable(false);
        isStart = true;
        timeText.setText(time + "");
        scoreText.setText(score + "");
        final Timer gameTimer = new Timer();
        final Timer timeTimer = new Timer();
        final Runnable updateMoles = new Runnable() {
            @Override
            public void run() {
                int i = 0;
                List<Integer> upNums = new ArrayList<>();
                for (Mole mole : moles) {
                    if (mole.isUp()) {
                        mole.fallDown();
                        upNums.add(i);
                    }
                    i++;
                }
                if (isStart) {
                    int toPopUp = random.nextInt(9 - upNums.size());
                    for (int upNum : upNums) {
                        if (toPopUp >= upNum)
                            toPopUp++;
                        else
                            break;
                    }
                    moles.get(toPopUp).popUp();
                } else if (upNums.size() == 0)
                    gameTimer.cancel();
            }
        };
        gameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(updateMoles);
            }
        }, 0, speed);
        final Runnable updateTime = new Runnable() {
            @Override
            public void run() {
                time--;
                timeText.setText(time + "");
                if (time <= 0) {
                    timeTimer.cancel();
                    end();
                }
            }
        };
        timeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(updateTime);
            }
        }, 1000, 1000);
    }

    private void addScore() {
        score += 10;
        scoreText.setText(score + "");
    }

    private void penalize() {
        isPenalizing = true;
        for (Mole mole : moles)
            mole.penalize();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (Mole mole : moles)
                            mole.resume();
                        isPenalizing = false;
                    }
                });
            }
        }, speed * 2);
    }

    private void end() {
        isStart = false;
        new AlertDialog.Builder(this)
                .setTitle("Score")
                .setMessage(score + "")
                .show();
        time = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("time", "10"));
        score = 0;
        startButton.setClickable(true);
    }

    class Mole extends Button {
        private boolean up;
        private boolean isPenalizing;
        private Drawable background;
        public int life;

        public Mole(Context context) {
            super(context);
            background = getBackground();
            isPenalizing = false;
        }

        public void popUp() {
            up = true;
            life = 2;
            if (isPenalizing)
                setBackgroundColor(Color.MAGENTA);
            else
                setBackgroundColor(Color.CYAN);
        }

        public void popDown() {
            up = false;
            life = 0;
            if (isPenalizing)
                setBackgroundColor(Color.RED);
            else
                setBackground(background);
        }

        public void fallDown() {
            life--;
            if (life <= 0)
                popDown();
        }

        public void penalize() {
            isPenalizing = true;
            if (up)
                setBackgroundColor(Color.MAGENTA);
            else
                setBackgroundColor(Color.RED);
        }

        public void resume() {
            isPenalizing = false;
            if (up)
                setBackgroundColor(Color.CYAN);
            else
                setBackground(background);
        }

        public boolean isUp() {
            return up;
        }
    }
}
