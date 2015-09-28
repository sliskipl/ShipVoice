package com.example.pawelkrysa.shipvoice;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by pawel.krysa on 2015-09-25.
 */
public class Boxes extends GameObject {

    private Bitmap image;

    public Boxes(Bitmap res, int x, int y) {
        height = 100;
        width = 40;

        this.x = x;
        this.y = y;

        dx = GamePanel.MOVE_SPEED;
        image = Bitmap.createBitmap(res, 0, 0, width, height);
    }

    public void update() {
        x += dx;
    }

    public void draw(Canvas canvas) {
        try {
            canvas.drawBitmap(image, x, y, null);
        } catch (Exception e) {
        }
    }

}
