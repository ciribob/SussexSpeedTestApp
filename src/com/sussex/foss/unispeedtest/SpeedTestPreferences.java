package com.sussex.foss.unispeedtest;



import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * 
 * @author Ciaran
 */
public class SpeedTestPreferences extends PreferenceActivity
{
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferencescreen_view);

	}

}
