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
import android.view.View;
import android.widget.ImageButton;

public class LocationFailed extends AppCompatActivity {
    private ImageButton retryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_failed);
        retryButton = findViewById(R.id.retryButtonloc);

        //Retry main activity when retry button pressed
        retryButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent toMain = new Intent(LocationFailed.this, MainActivity.class);
                startActivity(toMain);
                finish();
            }
        });
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
