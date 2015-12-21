package com.example.BubblePopGame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.*;
import android.widget.RelativeLayout;

import java.util.Random;
import java.util.concurrent.*;

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

    private static final int MENU_STILL = Menu.FIRST;
    private static final int MENU_SINGLE_SPEED = Menu.FIRST + 1;
    private static final int MENU_RANDOM_SPEED = Menu.FIRST + 2;

    private int mDisplayWidth, mDisplayHeight;


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
                for(int i =0; i < mainFrame.getChildCount(); i++){
                    BubbleView childBubbleView = (BubbleView) mainFrame.getChildAt(i);
                    if(childBubbleView.intersects(e1.getX(), e1.getY())){
                        childBubbleView.deflect(velocityX, velocityY);
                        return true;
                    }
                }
                return false;
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            mDisplayHeight = mainFrame.getHeight();
            mDisplayWidth = mainFrame.getWidth();
        }
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
        private final Paint mPaint = new Paint();
        private ScheduledFuture<?> mMoverFuture;


        private float mXPos, mYPos, mDx, mDy;
        private long mRotate, mDRotate;

        public BubbleView(Context context, float x, float y) {
            super(context);

            Random r = new Random();

            createScaledBitmap(r);

            mXPos = x - mScaledBitmapWidth/2;
            mYPos = y - mScaledBitmapWidth/2;

            setSpeedAndDirection(r);

            setRotation(r);

            mPaint.setAntiAlias(true);
        }

        private void setRotation(Random r) {
            if(speedMode == RANDOM){
                //rotation speed [1..3]
                mDRotate = r.nextInt(3) + 1; // [r.nextInt(high-low+1) + low] if high is inclusive
            }else{
                mDRotate = 0;
            }
        }

        private void setSpeedAndDirection(Random r) {
            switch (speedMode){
                case SINGLE:
                    //fixed

                    mDx =10;
                    mDy = 10;
                    break;

                case STILL:
                    mDx = 0;
                    mDy = 0;
                    break;

                default:
                    // Limit movement speed in the x and y
                    // direction to [-3..3].

                    //// (3 - (-3) + 1)
                    mDx = r.nextInt(7) - 3;
                    mDy = r.nextInt(7) - 3;
                    break;
            }
        }

        private void createScaledBitmap(Random r) {
            if(speedMode == RANDOM){
                mScaledBitmapWidth = BITMAP_SIZE *3;
            }else{
                // set scaled bitmap size in range [1..3] * BITMAP_SIZE
                mScaledBitmapWidth = (r.nextInt(3) +1 ) * BITMAP_SIZE;
            }

            this.mScaledBitmap = Bitmap.createScaledBitmap(mBitMap, mScaledBitmapWidth, mScaledBitmapWidth, false);
        }

        public boolean intersects(float x, float y) {
            //Return true if the BubbleView intersects position (x,y)
            // To do this as a circle instead of a rectangle

            final int radius = (mScaledBitmapWidth/2);
            final double deltaX = (mXPos + radius) - x;
            final double deltaY = (mYPos + radius) - y;
            final double distance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
            return distance <=radius;
        }

        public void stop(boolean poppedByUser) {

            if(mMoverFuture != null && mMoverFuture.cancel(true)){
                final BubbleView childView= this;
                mainFrame.post(new Runnable() {
                    @Override
                    public void run() {
                        mainFrame.removeView(childView);
                        if(poppedByUser){
                            mSoundPool.play(mSoundID, mStreamVolume, mStreamVolume, 1, 0, 1.0f);
                        }
                    }
                });
            }
        }

        public void start() {
            //creating a worker thread
            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

            // Execute the run() in Worker Thread every REFRESH_RATE
            // milliseconds
            // Save reference to this job in mMoverFuture
            mMoverFuture = executorService.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    // Each time this method is run the BubbleView should
                    // move one step. If the BubbleView exits the display,
                    // stop the BubbleView's Worker Thread.
                    // Otherwise, request that the BubbleView be redrawn.

                    if(BubbleView.this.moveWhileOnScreen()){
                        BubbleView.this.stop(false);
                    }else{
                        BubbleView.this.postInvalidate();
                    }
                }
            }, 0, REFRESH_RATE, TimeUnit.MILLISECONDS);

        }

        private boolean moveWhileOnScreen() {
            // Move the BubbleView
            // Returns true if the BubbleView has exited the screen

            mXPos += mDx;
            mYPos += mDy;
            if(isOutOfView()){
                return true;
            }else{
                return false;
            }
        }

        private boolean isOutOfView() {
            if(mXPos < 0 - mScaledBitmapWidth || mXPos > mDisplayWidth + mScaledBitmapWidth || mYPos < 0- mScaledBitmapWidth || mYPos > mDisplayHeight +mScaledBitmapWidth){
                return true;
            }else{
                return false;
            }
        }

        public void deflect(float velocityX, float velocityY) {
            mDx = velocityX/ REFRESH_RATE;
            mDy = velocityY/ REFRESH_RATE;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // Draw the Bubble at its current location

            canvas.save();
            mRotate +=mDRotate;
            canvas.rotate(mRotate, mXPos +mScaledBitmapWidth/2, mYPos + mScaledBitmapWidth/2);
            canvas.drawBitmap(mScaledBitmap, mXPos, mYPos, mPaint);
            canvas.restore();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_STILL:
                speedMode = STILL;
                return true;
            case MENU_SINGLE_SPEED:
                speedMode = SINGLE;
                return true;
            case MENU_RANDOM_SPEED:
                speedMode = RANDOM;
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(Menu.NONE, MENU_STILL, Menu.NONE, "Still Mode");
        menu.add(Menu.NONE, MENU_SINGLE_SPEED, Menu.NONE, "Single Speed Mode");
        menu.add(Menu.NONE, MENU_RANDOM_SPEED, Menu.NONE, "Random Speed Mode");

        return true;
    }
}
