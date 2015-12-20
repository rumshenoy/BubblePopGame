package com.example.BubblePopGame;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.RelativeLayout;

public class BubblePopActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    private RelativeLayout mainFrame;
    private Bitmap mBitMap;
    private AudioManager mAudioManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mainFrame = (RelativeLayout) findViewById(R.id.frame);
        mBitMap = BitmapFactory.decodeResource(getResources(), R.drawable.b64);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        

    }
}
