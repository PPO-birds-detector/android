package io.github.ppo_birds_detector.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.preference.PreferenceManager;

import detectors.BulkDetector;
import preferences.IntEditTextPreference;


public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }


    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            Preference pref = findPreference(key);
            if (pref instanceof IntEditTextPreference) {
                IntEditTextPreference etp = (IntEditTextPreference) pref;
                pref.setSummary(etp.getText());

                //update the algorithm
                int val = prefs.getInt(key, -1);
                if (val != -1) {
                    switch (key) {
                        case "diff_threshold":
                            BulkDetector.setDIFF_THRESHOLD(val);
                            break;
                        case "block_size":
                            BulkDetector.setBLOCK_SIZE(val);
                            break;
                        case "noise_threshold":
                            BulkDetector.setNOISE_THRESHOLD(val);
                            break;
                    }
                }
            }
        }
    }
}

