package fr.univmed.erss;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class HomeActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		       
		Intent intent = new Intent(this, FluxActivity.class);
		startActivity(intent);
	}

}
