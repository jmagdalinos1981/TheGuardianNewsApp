package com.example.android.newsapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Activity for Settings
 */

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
    }

    // Fragment containing the preferences
    public static class NewsPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Use settings_main as preference resource file
            addPreferencesFromResource(R.xml.settings_main);

            // Select one-by-one each preference, set OnPreferenceChangeListener and show value
            // Search Query
            Preference searchQuery = findPreference(getString(R.string.settings_search_query_key));
            bindPreferenceToValue(searchQuery);
            // Order by
            Preference orderBy = findPreference(getString(R.string.settings_order_by_list_key));
            bindPreferenceToValue(orderBy);
            // Section Selection
        }

        private void bindPreferenceToValue(Preference preference) {
            // Set OnPreferenceChangeListener
            preference.setOnPreferenceChangeListener(this);
            // Get an instance of SharedPreferences
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences
                    (preference.getContext());
            // Retrieve default value
            String preferenceString = sharedPreferences.getString(preference.getKey(), "");
            // Show value
            onPreferenceChange(preference, preferenceString);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            // Store new value
            String stringValue = newValue.toString();
            // If this is a list preference, use values instead of keys to update summary
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >=0 ) {
                    CharSequence[] labels = listPreference.getEntries();
                    preference.setSummary(labels[prefIndex]);
                }
            }
            // Set new value as summary
            preference.setSummary(stringValue);
            return true;
        }
    }
}
