package fr.univmed.erss;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class HomeActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		       
		Intent intent = new Intent(this, PickActivity.class);
		startActivity(intent);
	}
}
