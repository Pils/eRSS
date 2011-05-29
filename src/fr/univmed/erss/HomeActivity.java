package fr.univmed.erss;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HomeActivity extends ListActivity {

	private final String LOG_TAG = "HomeActivity";//Utile pour debug
	
	private ArrayAdapter<CharSequence> aAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(LOG_TAG, "onCreate()");
		
		aAdapter = ArrayAdapter.createFromResource(
                this, R.array.activities_list, android.R.layout.simple_list_item_1);
		setListAdapter(aAdapter);
		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		Intent intent;
		switch(position)
		{
		case 0 : intent = new Intent(this, FluxActivity.class);
    			 startActivity(intent);
    			 break;
		case 1 : intent = new Intent(this, EventActivity.class);
    			 startActivity(intent);
    			 break;
    	default : break;
		}
		
	}
	
}
