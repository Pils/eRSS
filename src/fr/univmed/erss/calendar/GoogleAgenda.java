package fr.univmed.erss.calendar;

import fr.univmed.erss.object.Item;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;


/**
 * Classe pour ajouter un évènement à GoogleCalendar
 * @author xavier
 *
 */

public class GoogleAgenda {

	public static String LOG_TAG = "GoogleAgenda";
	
	private String CALENDAR_NAME = "FLUX PLANNING";
	private String sync_account = "xavier.christienne@gmail.com";
	private Activity activity;
	private String Location = "Région marseillaise";

	
	public GoogleAgenda(Activity activity) {
		this.activity = activity;
	}
	
	public void createNewCalendar(String name, String displayName) {
		ContentValues calendar = new ContentValues();
		
		calendar.put("_sync_account", sync_account); // My account
		calendar.put("_sync_account_type","com.google");
		calendar.put("name", name);
		calendar.put("displayName",displayName);
		calendar.put("hidden",0);
		calendar.put("color",0xFF008080);
		calendar.put("access_level", 700);
		calendar.put("sync_events", 1);
		calendar.put("timezone", "Europe/Paris");
		calendar.put("ownerAccount", sync_account);
		Uri calendarUri = Uri.parse(getCalendarUriBase() + "calendars");
		activity.getContentResolver().insert(calendarUri, calendar);		
	}
	
	private Cursor getCalendarManagedCursor(String[] projection, String selection, String path) {
        Uri calendars = Uri.parse("content://calendar/" + path);

        Cursor managedCursor = null;
        try {
            managedCursor = activity.managedQuery(calendars, projection, selection, null, null);
        } catch (IllegalArgumentException e) {
            Log.w(LOG_TAG, "Failed to get provider at [" + calendars.toString() + "]");
        }

        if (managedCursor == null) {
            // try again
            calendars = Uri.parse("content://com.android.calendar/" + path);
            try {
                managedCursor = activity.managedQuery(calendars, projection, selection,  null, null);
            } catch (IllegalArgumentException e) {
                Log.w(LOG_TAG, "Failed to get provider at ["  + calendars.toString() + "]");
            }
        }
        return managedCursor;
	}
	
	 public Uri createEvent(int calId, Item item) {
        ContentValues eventValues = new ContentValues();
        
        eventValues.put("calendar_id", calId);
        eventValues.put("title", item.getTitle());
        eventValues.put("description", item.getDescription());
        eventValues.put("eventLocation", Location);
        
        long startTime = System.currentTimeMillis() + 1000 * 60 * 60;
        long endTime = System.currentTimeMillis() + 1000 * 60 * 60 * 2;

        eventValues.put("dtstart", startTime);
        eventValues.put("dtend", endTime);

        eventValues.put("allDay", 1); // 0 for false, 1 for true
        eventValues.put("eventStatus", 1);
        eventValues.put("visibility", 0);
        eventValues.put("transparency", 0);
        eventValues.put("hasAlarm", 0); // 0 for false, 1 for true
        
        System.out.println("USING SYNC ACCOUNT " + sync_account);
        
        eventValues.put("_sync_account_type", sync_account);

        Uri eventsUri = Uri.parse(getCalendarUriBase()+"events");

        Uri insertedUri = activity.getContentResolver().insert(eventsUri, eventValues);
        return insertedUri;
    }
	 
	 public int findUpdateCalendar() {
	        int result = -1;
	        
	        String[] projection = new String[] { "_id", "name" };
	        String selection = "selected=1";
	        String path = "calendars";

	        Cursor managedCursor = getCalendarManagedCursor(projection, selection,
	                path);

	        if (managedCursor != null && managedCursor.moveToFirst()) {

	            Log.i(LOG_TAG, "Listing Selected Calendars Only");

	            int nameColumn = managedCursor.getColumnIndex("name");
	            int idColumn = managedCursor.getColumnIndex("_id");

	            do {
	                String calName = managedCursor.getString(nameColumn);
	                String calId = managedCursor.getString(idColumn);
	                Log.i(LOG_TAG, "Found Calendar '" + calName + "' (ID=" + calId + ")");
	                if (calName != null && calName.equals(CALENDAR_NAME)) {
	                	result = Integer.parseInt(calId);
	                }
	            } while (managedCursor.moveToNext());
	        } else {
	            Log.i(LOG_TAG, "No Calendars");
	        }

	        return result;

	    }
	 
	 
	 private String getCalendarUriBase() {
		   	
	        String calendarUriBase = null;
	        Uri calendars = Uri.parse("content://calendar/calendars");
	        Cursor managedCursor = null;
	        try {
	            managedCursor = activity.managedQuery(calendars, null, null, null, null);
	        } catch (Exception e) {
	            // eat
	        }

	        if (managedCursor != null) {
	            calendarUriBase = "content://calendar/";
	        } else {
	            calendars = Uri.parse("content://com.android.calendar/calendars");
	            try {
	                managedCursor = activity.managedQuery(calendars, null, null, null, null);
	            } catch (Exception e) {
	                // eat
	            }

	            if (managedCursor != null) {
	                calendarUriBase = "content://com.android.calendar/";
	            }

	        }

	        managedCursor.close();
	        
	        return calendarUriBase;
	    }
	
}
