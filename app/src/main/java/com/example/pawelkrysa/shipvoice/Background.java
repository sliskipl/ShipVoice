package com.example.pawelkrysa.shipvoice;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by pawel.krysa on 2015-09-01.
 */
public class Background {
    private Bitmap image;
    private int x, y, dx;

    public Background(Bitmap res) {
        image = res;
        dx = GamePanel.MOVE_SPEED;
    }

    public void update() {
        x += dx;
        if (x < -GamePanel.WIDTH) {
            x = 0;
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(image, x, y, null);
        if (x < 0) {
            canvas.drawBitmap(image, x + GamePanel.WIDTH, y, null);
        }
    }

}
