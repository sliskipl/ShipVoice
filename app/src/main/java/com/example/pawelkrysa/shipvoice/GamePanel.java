package com.example.pawelkrysa.shipvoice;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

import android.speech.RecognizerIntent;


public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVE_SPEED = -3;
    private long smokeStartTime;
    private long missileStartTime;
    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<Smokepuff> smoke;
    private ArrayList<Missle> missiles;
    private ArrayList<TopBorder> topBorders;
    private ArrayList<BottomBorder> bottomBorders;
    private ArrayList<Boxes> boxes;
    private ArrayList<PlayerMissle> playerMissles;
    private Random rand = new Random();
    private int maxBorderHeight;
    private int minBorderHeight;
    private boolean topDown = true;
    private boolean botDown = true;
    private boolean newGameCreated;

    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean dissapear;
    private boolean started;
    private int best = 0;

    public GamePanel(Context context) {

        super(context);


        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);


        //make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        int counter = 0;
        while (retry && counter < 1000) {
            counter++;
            try {
                thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.grassbg1));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter), 65, 28, 3);
        smoke = new ArrayList<>();
        missiles = new ArrayList<>();
        topBorders = new ArrayList<>();
        bottomBorders = new ArrayList<>();
        smokeStartTime = System.nanoTime();
        missileStartTime = System.nanoTime();
        boxes = new ArrayList<>();
        playerMissles = new ArrayList<>();

        thread = new MainThread(getHolder(), this);
        //we can safely start the game loop
        thread.setRunning(true);
        thread.start();

    }

    public void command(String command) {

        if (command.equals("start")) {
            player.setPlaying(true);
            reset = false;
            started = false;
        }

        if (command.equals("góra") || command.equals("up")) {
            System.out.println("ide do gory");
            player.setUp(50);
        }

        if (command.equals("dó³") || command.equals("down")) {
            player.setUp(-50);
        }

        if (command.equals("bum")) {
            playerMissles.add(new PlayerMissle(BitmapFactory.decodeResource(getResources(), R.drawable.
                    playermissle), (int)player.getX(), (int)player.getY(), 45, 15, 13));
        }
    }

    public void update()

    {

        if (player.getPlaying()) {

            if (player.getScore() % 100 == 0 && player.getScore() != 0) {
                boxes.add(new Boxes(BitmapFactory.decodeResource(getResources(), R.drawable.
                        box), WIDTH + 10, 0));
                boxes.add(new Boxes(BitmapFactory.decodeResource(getResources(), R.drawable.
                        box), WIDTH + 10, 100));
                boxes.add(new Boxes(BitmapFactory.decodeResource(getResources(), R.drawable.
                        box), WIDTH + 10, 200));
                boxes.add(new Boxes(BitmapFactory.decodeResource(getResources(), R.drawable.
                        box), WIDTH + 10, 300));
                boxes.add(new Boxes(BitmapFactory.decodeResource(getResources(), R.drawable.
                        box), WIDTH + 10, 400));
                boxes.add(new Boxes(BitmapFactory.decodeResource(getResources(), R.drawable.
                        box), WIDTH + 10, 500));
            }

            if (bottomBorders.isEmpty()) {
                player.setPlaying(false);
                return;
            }
            if (topBorders.isEmpty()) {
                player.setPlaying(false);
                return;
            }

            bg.update();
            player.update();
            updateBoxes();
            updatePlayerMissles();

            //calculate the threshold of height the border can have based on the score
            //max and min border heart are updated, and the border switched direction when either max or
            //min is met

            int progressDenom = 20;
            maxBorderHeight = 30 + player.getScore() / progressDenom;
            //cap max border height so that borders can only take up a total of 1/2 the screen
            if (maxBorderHeight > HEIGHT / 4) maxBorderHeight = HEIGHT / 4;
            minBorderHeight = 5 + player.getScore() / progressDenom;

            //check bottom border collision
            for (int i = 0; i < bottomBorders.size(); i++) {
                if (collision(bottomBorders.get(i), player))
                    player.setPlaying(false);
            }

            for (int i = 0; i < boxes.size(); i++) {
                if (collision(boxes.get(i), player))
                    player.setPlaying(false);
            }

            for (int i = 0; i < boxes.size(); i++) {
                for(int j = 0; j < playerMissles.size(); j++){
                    if(collision(boxes.get(i),playerMissles.get(j)))
                    {
                        boxes.remove(i);
                        playerMissles.remove(i);
                    }
                }
            }

            //check top border collision
            for (int i = 0; i < topBorders.size(); i++) {
                if (collision(topBorders.get(i), player))
                    player.setPlaying(false);
            }

            //update top border
            this.updateTopBorder();

            //udpate bottom border
            this.updateBottomBorder();

            //add missiles on timer
            long missileElapsed = (System.nanoTime() - missileStartTime) / 1000000;
            if (missileElapsed > (2000 - player.getScore() / 4)) {


                //first missile always goes down the middle
                if (missiles.size() == 0) {
                    missiles.add(new Missle(BitmapFactory.decodeResource(getResources(), R.drawable.
                            missile), WIDTH + 10, HEIGHT / 2, 45, 15, player.getScore(), 13));
                } else {

                    missiles.add(new Missle(BitmapFactory.decodeResource(getResources(), R.drawable.missile),
                            WIDTH + 10, (int) (rand.nextDouble() * (HEIGHT - (maxBorderHeight * 2)) + maxBorderHeight), 45, 15, player.getScore(), 13));
                }

                //reset timer
                missileStartTime = System.nanoTime();
            }
            //loop through every missile and check collision and remove
            for (int i = 0; i < missiles.size(); i++) {
                //update missile
                missiles.get(i).update();

                if (collision(missiles.get(i), player)) {
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;

                }

                for(int j = 0; j < playerMissles.size(); j++){
                    if(collision(missiles.get(i),playerMissles.get(j)))
                    {
                        missiles.remove(i);
                        playerMissles.remove(i);
                    }
                }


                //remove missile if it is way off the screen
                if (missiles.get(i).getX() < -100) {
                    missiles.remove(i);
                    break;
                }
            }

            //add smoke puffs on timer
            long elapsed = (System.nanoTime() - smokeStartTime) / 1000000;
            if (elapsed > 120) {
                smoke.add(new Smokepuff(player.getX(), player.getY() + 10));
                smokeStartTime = System.nanoTime();
            }

            for (int i = 0; i < smoke.size(); i++) {
                smoke.get(i).update();
                if (smoke.get(i).getX() < -10) {
                    smoke.remove(i);
                }
            }
        } else {
            player.resetDY();
            if (!reset) {
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                dissapear = true;
                started = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.explosion), player.getX(),
                        player.getY() - 30, 100, 100, 25);
            }

            explosion.update();
            long resetElapsed = (System.nanoTime() - startReset) / 1000000;

            if (resetElapsed > 2500 && !newGameCreated) {
                newGame();
            }


        }

    }

    public boolean collision(GameObject a, GameObject b) {
        return Rect.intersects(a.getRectangle(), b.getRectangle());
    }

    @Override
    public void draw(Canvas canvas) {
        final float scaleFactorX = getWidth() / (WIDTH * 1.f);
        final float scaleFactorY = getHeight() / (HEIGHT * 1.f);

        if (canvas != null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            if (!dissapear) {
                player.draw(canvas);
            }
            //draw smokepuffs
            for (Smokepuff sp : smoke) {
                sp.draw(canvas);
            }
            //draw missiles
            for (Missle m : missiles) {
                m.draw(canvas);
            }

            for (PlayerMissle pm : playerMissles) {
                pm.draw(canvas);
            }

            for (Boxes b : boxes) {
                b.draw(canvas);
            }


            //draw topBorders
            for (TopBorder tb : topBorders) {
                tb.draw(canvas);
            }

            //draw bottomBorders
            for (BottomBorder bb : bottomBorders) {
                bb.draw(canvas);
            }
            //draw explosion
            if (started) {
                explosion.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);

        }
    }

    public void updateBoxes() {

        for (int i = 0; i < boxes.size(); i++) {
            boxes.get(i).update();
            if (boxes.get(i).getX() < -50) {
                boxes.remove(i);
            }
        }
    }

    public void updatePlayerMissles() {
        for (int i = 0; i < playerMissles.size(); i++) {
            playerMissles.get(i).update();
            if (playerMissles.get(i).getX() > WIDTH + 30) {
                playerMissles.remove(i);
            }
        }
    }

    public void updateTopBorder() {
        //every 50 points, insert randomly placed top blocks that break the pattern
        for (int i = 0; i < topBorders.size(); i++) {
            topBorders.get(i).update();
            if (topBorders.get(i).getX() < -20) {
                topBorders.remove(i);
                //remove element of arraylist, replace it by adding a new one

                //calculate topdown which determines the direction the border is moving (up or down)
                if (topBorders.get(topBorders.size() - 1).getHeight() >= maxBorderHeight) {
                    topDown = false;
                }
                if (topBorders.get(topBorders.size() - 1).getHeight() <= minBorderHeight) {
                    topDown = true;
                }
                //new border added will have larger height
                if (topDown) {
                    topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), topBorders.get(topBorders.size() - 1).getX() + 20,
                            0, topBorders.get(topBorders.size() - 1).getHeight() + 1));
                }
                //new border added wil have smaller height
                else {
                    topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), topBorders.get(topBorders.size() - 1).getX() + 20,
                            0, topBorders.get(topBorders.size() - 1).getHeight() - 1));
                }

            }
        }

    }

    public void updateBottomBorder() {
        //update bottom border
        for (int i = 0; i < bottomBorders.size(); i++) {
            bottomBorders.get(i).update();

            //if border is moving off screen, remove it and add a corresponding new one
            if (bottomBorders.get(i).getX() < -20) {
                bottomBorders.remove(i);


                //determine if border will be moving up or down
                if (bottomBorders.get(bottomBorders.size() - 1).getY() <= HEIGHT - maxBorderHeight) {
                    botDown = true;
                }
                if (bottomBorders.get(bottomBorders.size() - 1).getY() >= HEIGHT - minBorderHeight) {
                    botDown = false;
                }

                if (botDown) {
                    bottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                    ), bottomBorders.get(bottomBorders.size() - 1).getX() + 20, bottomBorders.get(bottomBorders.size() - 1
                    ).getY() + 1));
                } else {
                    bottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                    ), bottomBorders.get(bottomBorders.size() - 1).getX() + 20, bottomBorders.get(bottomBorders.size() - 1
                    ).getY() - 1));
                }
            }
        }
    }

    public void newGame() {
        dissapear = false;

        bottomBorders.clear();
        topBorders.clear();

        missiles.clear();
        smoke.clear();
        boxes.clear();
        playerMissles.clear();

        minBorderHeight = 5;
        maxBorderHeight = 30;

        player.resetDY();
        player.resetScore();
        player.setY(HEIGHT / 2);

        if (player.getScore() > best) {
            best = player.getScore();

        }

        //create initial borders

        //initial top border
        for (int i = 0; i * 20 < WIDTH + 40; i++) {
            //first top border create
            if (i == 0) {
                topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                ), i * 20, 0, 10));
            } else {
                topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                ), i * 20, 0, topBorders.get(i - 1).getHeight() + 1));
            }
        }
        //initial bottom border
        for (int i = 0; i * 20 < WIDTH + 40; i++) {
            //first border ever created
            if (i == 0) {
                bottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick)
                        , i * 20, HEIGHT - minBorderHeight));
            }
            //adding borders until the initial screen is filed
            else {
                bottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, bottomBorders.get(i - 1).getY() - 1));
            }
        }

        newGameCreated = true;


    }

    public void drawText(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DISTANCE: " + (player.getScore() * 3), 10, HEIGHT - 10, paint);
        canvas.drawText("BEST: " + best, WIDTH - 215, HEIGHT - 10, paint);

        if (!player.getPlaying() && newGameCreated && reset) {
            Paint paint1 = new Paint();
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH / 2 - 50, HEIGHT / 2, paint1);

            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH / 2 - 50, HEIGHT / 2 + 20, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH / 2 - 50, HEIGHT / 2 + 40, paint1);
        }
    }

}