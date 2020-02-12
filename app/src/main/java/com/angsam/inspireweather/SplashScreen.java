/*

MIT License

Copyright (c) 2020 Angel Samuel Mendez

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

 */

package com.angsam.inspireweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

public class SplashScreen extends AppCompatActivity {
    private static int splashTimeOut, maskTime;
    ImageView logo, mask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        splashTimeOut = 2500;
        maskTime = 1680;
        final Intent toMain = new Intent(SplashScreen.this, MainActivity.class);


        //Start main activity after splash animation finished
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                startActivity(toMain);
                finish();
            }
        }, splashTimeOut);
        logo = findViewById(R.id.logoTop);
        mask = findViewById(R.id.logoMask);
        logo.animate().rotation(360).setStartDelay(1000).setDuration(1000);

        //mask to hide to the logo rays while they rotate from underneath the clouds (bottom part of logo)
        mask.animate().alpha(0).setStartDelay(maskTime).setDuration(0);

    }

    /*

    Override default transition

     */

    @Override
    protected void onPause(){
        super.onPause();
        if(isFinishing()){
            overridePendingTransition(R.anim.snap_in, R.anim.snap_out);
        }
    }
}
