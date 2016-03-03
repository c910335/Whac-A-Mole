package asia.tatsujin.whac_a_mole;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {

    final static int SPEED[] = {500, 300, 200, 50};
    boolean isStart = false, penalty, isPenalizing = false;
    int defaultTime, time, score = 0, speed, highScore;
    TextView timeText, scoreText;
    ImageButton startButton;
    LinearLayout molesView;
    List<Mole> moles;
    Random random;
    private Timer gameTimer;
    private Timer timeTimer;


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
        if (! isStart && item.getItemId() == R.id.action_game_method) {
            showRule();
            return true;
        }
        return false;
    }

    private void showRule() {
        ViewPager viewPager = (ViewPager) getLayoutInflater().inflate(R.layout.dialog_game_method, null);
        new AlertDialog.Builder(this)
                .setTitle(R.string.action_game_method)
                .setView(viewPager)
                .setNegativeButton("Close", null)
                .show();
        int[] step_ids = {R.mipmap.step1, R.mipmap.step2, R.mipmap.step3};
        final ArrayList<ImageView> imageViews = new ArrayList<>();
        for (int step_id : step_ids) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(step_id);
            imageViews.add(imageView);
        }
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return imageViews.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object)   {
                container.removeView((View) object);
            }

            @Override
             public Object instantiateItem(ViewGroup container, int position) {
                View view = imageViews.get(position);
                container.addView(view);
                return view;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        });
    }

    @Override
    protected void onPause() {
        try {
            gameTimer.cancel();
            timeTimer.cancel();
        } catch (Exception ignored) {}
        super.onPause();
    }

    private void initVariables() {
        speed = SPEED[getIntent().getIntExtra("difficulty", 0)];
        highScore = PreferenceManager.getDefaultSharedPreferences(this).getInt("high_score", 0);
        defaultTime = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("time", "10"));
        penalty = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("penalty", true);
        random = new Random();
        moles = new ArrayList<>();
    }

    private void initViews() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        timeText = (TextView) findViewById(R.id.text_time);
        timeText.setText(time + "");
        scoreText = (TextView) findViewById(R.id.text_score);
        molesView = (LinearLayout) findViewById(R.id.view_moles);

        for (int i = 0; i != 3; ++i) {
            LinearLayout molesRow = new LinearLayout(this);
            molesRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            molesRow.setLayoutParams(rowLayoutParams);
            for (int j = 0; j != 3; ++j) {
                Mole mole = new Mole(this);
                mole.setBackgroundResource(R.drawable.fig_grass);
                mole.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mole.setAdjustViewBounds(true);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);
                molesRow.addView(mole, layoutParams);
                moles.add(mole);
            }
            molesView.addView(molesRow);
        }
        startButton = (ImageButton) findViewById(R.id.button_start);
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
                            mole.hit();
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
        for (Mole mole : moles)
            mole.start();
        time = defaultTime;
        timeText.setText(time + "");
        scoreText.setText(score + "");
        gameTimer = new Timer();
        timeTimer = new Timer();
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
                        isPenalizing = false;
                        for (Mole mole : moles)
                            mole.resume();
                    }
                });
            }
        }, speed * 2);
    }

    private void end() {
        isStart = false;
        for (Mole mole : moles)
            mole.end();
        new AlertDialog.Builder(this)
                .setTitle("Score")
                .setMessage(score + "")
                .show();
        if (speed == SPEED[2] && penalty && defaultTime == 10 && score > highScore) {
            highScore = score;
            PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("high_score", score).commit();
        }
        time = defaultTime;
        score = 0;
        startButton.setClickable(true);
    }

    class Mole extends ImageButton {
        private boolean up;
        private boolean isHitting;
        public int life;

        public Mole(Context context) {
            super(context);
            isHitting = false;
        }

        public void popUp() {
            up = true;
            life = 2;
            setImageResource(R.drawable.fig_mole);
        }

        public void popDown() {
            up = false;
            life = 0;
            isHitting = false;
            if (! isStart)
                setImageDrawable(null);
            else
                setImageResource(R.drawable.fig_hole);
        }

        public void fallDown() {
            life--;
            if (life <= 0)
                popDown();
        }

        public void hit() {
            if (up && ! isHitting) {
                life = 1;
                isHitting = false;
                setImageResource(R.drawable.fig_angry_mole);
            }
        }

        public void penalize() {
            setBackgroundColor(Color.RED);
        }

        public void resume() {
            setBackgroundResource(R.drawable.fig_grass);
        }

        public void start() {
            setImageResource(R.drawable.fig_hole);
        }

        public void end() {
            if (! up && ! isPenalizing)
                setImageDrawable(null);
        }

        public boolean isUp() {
            return up;
        }
    }
}
