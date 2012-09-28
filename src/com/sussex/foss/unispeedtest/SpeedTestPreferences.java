package com.sussex.foss.unispeedtest;



import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

import com.sussex.foss.unispeedtest.R;

/**
 * 
 * @author Ciaran
 */
public class SpeedTestPreferences extends PreferenceActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferencescreen_view);

	}

}
