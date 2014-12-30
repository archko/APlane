package com.me.microblog.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.me.microblog.R;
import com.me.microblog.gif.GifView2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 11-12-4
 */
public class TestGifActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "TestGifActivity";
    private GifView2 gf2;
    private boolean flag = true;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.gif_layout2);

        gf2 = (GifView2) findViewById(R.id.gif2);
        gf2.setGifImageType(GifView2.GifImageType.ANIMATION);

        try {
            File file = new File("/sdcard/test.gif");
            FileInputStream fis = null;

            fis = new FileInputStream(file);
            System.out.println("fis:" + fis.available());
            gf2.setGifImage(fis, false);

            /*FileInputStream fileInputStream=new FileInputStream(file);
            byte[] bytes=new byte[fileInputStream.available()];
            fileInputStream.read(bytes);
            gf2.setGifImage(bytes);*/
            gf2.setOnClickListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        gf2.startAnimate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gf2.stopAnimate();
    }
}
