/*
 *    Copyright (C) 2011 Jeff Moyer
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.example.pawelkrysa.shipvoice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Game extends Activity {
    private static final String TAG = "ShipVoice";

    private SpeechRecognizer mSpeechRecognizer;
    private Intent mRecognizerIntent;
    private GamePanel gamePanel;
    private Timer timer;
    private TimerTask timerTask;

    final Handler handler = new Handler();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);


        //set full screeen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        gamePanel = new GamePanel(this);
        setContentView(gamePanel);


        //startTimer();
        getSpeechRecognizer(false);
    }

    private void getSpeechRecognizer(Boolean isListening) {

        if (!isListening) {

            mRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());

            if (mSpeechRecognizer != null) {
                mSpeechRecognizer.destroy();
                mSpeechRecognizer = null;
            }

            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mSpeechRecognizer.setRecognitionListener(mRecognitionListener);
            mSpeechRecognizer.startListening(mRecognizerIntent);
        }
    }

    private RecognitionListener mRecognitionListener = new RecognitionListener() {
        @Override
        public void onBufferReceived(byte[] buffer) {
            // TODO Auto-generated method stub
            //Log.d(TAG, "onBufferReceived");
        }

        @Override
        public void onError(int error) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onError: " + error);

            if(error == 5) {
                mSpeechRecognizer.destroy();
                getSpeechRecognizer(false);
            }
            if(error == 6) {
                mSpeechRecognizer.stopListening();
                getSpeechRecognizer(false);
            }
            if(error == 7) {
                getSpeechRecognizer(false);
            }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            // TODO Auto-generated method stub
            //Log.d(TAG, "onEvent");
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            // TODO Auto-generated method stub
            //Log.d(TAG, "onPartialResults");
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onReadyForSpeech");
        }

        @Override
        public void onResults(Bundle results) {

            ArrayList<String> matches = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            Log.d(TAG, "onResults");
            gamePanel.command(matches.get(0));
            getSpeechRecognizer(false);
            //startTimer();
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            // TODO Auto-generated method stub
            //Log.d(TAG, "onRmsChanged");
        }

        @Override
        public void onBeginningOfSpeech() {
            // TODO Auto-generated method stub
            //Log.d(TAG, "onBeginningOfSpeech");
            stopTimetTask();
        }

        @Override
        public void onEndOfSpeech() {
            // TODO Auto-generated method stub
            Log.d(TAG, "onEndOfSpeech");
        }

    };

    public void startTimer() {
        timer = new Timer();

        initializeTimerTask();

        timer.schedule(timerTask, 4000);
    }

    public void stopTimetTask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {

            public void run() {
                handler.post(new Runnable() {
                    public void run() {

                    }
                });
            }
        };
    }
}
