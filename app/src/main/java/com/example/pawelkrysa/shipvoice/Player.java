package com.example.pawelkrysa.shipvoice;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by pawel.krysa on 2015-09-01.
 */
public class Player extends GameObject {
    private Bitmap spritesheet;
    private int score;
    private int up = 0;
    private boolean playing = false;
    private Animation animation = new Animation();
    private long startTime;

    public Player(Bitmap res, int w, int h, int numFrames) {
        x = 100;
        y = (int) GamePanel.HEIGHT / 2;
        dy = 0;
        score = 0;
        height = h;
        width = w;

        Bitmap[] image = new Bitmap[numFrames];
        spritesheet = res;

        for (int i = 0; i < image.length; i++) {
            image[i] = Bitmap.createBitmap(spritesheet, i * width, 0, width, height);
        }

        animation.setFrames(image);
        animation.setDelay(10);
        startTime = System.nanoTime();
    }

    public void setUp(int b) {
        y+=b;
    }

    public void update() {
        long elapsed = (System.nanoTime() - startTime) / 1000000;
        if (elapsed > 100) {
            score++;
            startTime = System.nanoTime();
        }
        animation.update();

        if (dy > 14) dy = 14;
        if (dy < -14) dy = -14;

        //up = 0;

        //y += dy * 2;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(animation.getImage(), x, y, null);
    }

    public void setPlaying(boolean b) {
        playing = b;
    }

    public boolean getPlaying() {
        return playing;
    }

    public void resetDY() {
        dy = 0;
    }

    public void resetScore() {
        score = 0;
    }

    public int getScore(){
        return score;
    }

}
