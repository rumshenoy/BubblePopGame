package com.example.BubblePopGame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.Random;

public class BubblePopActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    private final static int RANDOM =0;
    private final static int SINGLE = 1;
    private final static int STILL = 2;
    private static int speedMode = RANDOM;

    private RelativeLayout mainFrame;
    private Bitmap mBitMap;
    private AudioManager mAudioManager;
    private float mStreamVolume;
    private  int mSoundID;
    private SoundPool mSoundPool;
    private GestureDetector mGestureDetector;


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

        mStreamVolume = (float) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                setUpGestureDetector();
            }
        });

        mSoundID = mSoundPool.load(this, R.raw.bubble_pop, 1);

    }

    private void setUpGestureDetector() {
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return super.onFling(e1, e2, velocityX, velocityY);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                for(int i =0; i < mainFrame.getChildCount(); i++){
                    BubbleView childBubbleView = (BubbleView) mainFrame.getChildAt(i);
                    if(childBubbleView.intersects(e.getX(), e.getY())){
                        childBubbleView.stop(true);
                        return true;
                    }
                }

                BubbleView bubbleView = new BubbleView(getApplicationContext(), e.getX(), e.getY());
                bubbleView.start();
                mainFrame.addView(bubbleView);
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onPause() {
        if(mSoundPool != null){
            mSoundPool.unload(mSoundID);
            mSoundPool.release();
            mSoundPool = null;
        }

        super.onPause();
    }

    private class BubbleView extends View {

        private static final int BITMAP_SIZE = 64;
        private static final int REFRESH_RATE = 40;
        private int mScaledBitmapWidth;
        private  Bitmap mScaledBitmap;

        private float mXPos, mYPos, mDx, mDy;
        private long mRotate, mDRotate;

        public BubbleView(Context context, float x, float y) {
            super(context);

            Random r = new Random();

            createScaledBitmap(r);
        }

        private void createScaledBitmap(Random r) {
            if(speedMode == RANDOM){

            }else{

            }
        }

        public boolean intersects(float rawX, float rawY) {
            return false;
        }

        public void stop(boolean poppedByUser) {

        }

        public void start() {

            
        }
    }
}
