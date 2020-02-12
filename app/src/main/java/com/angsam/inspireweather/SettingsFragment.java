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

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsFragment extends PreferenceFragmentCompat{

    private SwitchPreferenceCompat si, hr;
    private SharedPreferences pref;
    SharedPreferences.Editor ed;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.prefs, rootKey);
        pref = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        si = findPreference("si units");
        hr = findPreference("twentyfourclock");

        /*

        When unit preference is changed, apply it to the preference manager.

         */

        si.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean isOn = (Boolean) newValue;
                ed = pref.edit();
                if(isOn){
                    ed.putBoolean("si units", true);
                    ed.apply();
                }
                else{
                    ed.putBoolean("si units", false);
                    ed.apply();
                }
                return true;
            }

        });

        /*

        When hour preference is changed, apply it to the preference manager.

         */

        hr.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean isOn = (Boolean) newValue;
                ed = pref.edit();
                if(isOn){
                    ed.putBoolean("hr", true);
                    ed.apply();
                }
                else{
                    ed.putBoolean("hr", false);
                    ed.apply();
                }
                return true;
            }

        });
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}
