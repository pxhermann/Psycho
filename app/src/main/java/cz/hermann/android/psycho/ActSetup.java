package cz.hermann.android.psycho;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class ActSetup extends PreferenceActivity implements OnSharedPreferenceChangeListener 
{
	private ListPreference listMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(	savedInstanceState);
		
		addPreferencesFromResource(R.xml.setup);

		ListPreference listSize = (ListPreference)getPreferenceScreen().findPreference(getString(R.string.keySize));
		listSize.setSummary(listSize.getEntry());
//		listSize.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
//			@Override
//			public boolean onPreferenceChange(Preference preference, Object newValue) 
//			{
//				listSize.setSummary(listSize.getEntries()[listSize.findIndexOfValue(newValue.toString())]);
//				return true;
//			}
//		});

		listMode = (ListPreference) getPreferenceScreen().findPreference(getString(R.string.keyMode));
		listMode.setSummary(listMode.getEntry());
		
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) 
	{
		Preference pref = findPreference(key);

	    if (pref instanceof ListPreference) 
	    {
	        ListPreference listPref = (ListPreference) pref;
	        pref.setSummary(listPref.getEntry());
	    }		
	}	
}
