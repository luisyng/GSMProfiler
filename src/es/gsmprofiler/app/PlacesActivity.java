package es.gsmprofiler.app;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import es.gsmprofiler.db.SQLiteInterface;
import es.gsmprofiler.entities.Place;
import es.gsmprofiler.app.R;

public class PlacesActivity extends Activity {

	// Entities
	private List<Place> places;

	// Views
	private ListView listView;
	private ArrayAdapter<Place> adapter;

	// Location manager
	private LocationManagerInterface locationInterface;

	// Broadcast receiver
	private PlacesActivityBroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;

	// Menu actions
	private static final int DELETE = 0;
	private static final int INCREASE_PRIORITY = 1;
	private static final int DECREASE_PRIORITY = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.places);

		// Initialize places
		places = new ArrayList<Place>();

		// Location Interface null until connected to service
		locationInterface = null;

		// Bind to the service
		getApplicationContext().bindService(
				new Intent(this, LocationService.class), serviceConnection,
				Context.BIND_AUTO_CREATE);

		// Get the list view
		listView = (ListView) this.findViewById(R.id.placesListView);

		// Listener for the list items
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView av, View v, int index, long arg3) {
				// Go to PlaceActivity
				Intent intent = new Intent(PlacesActivity.this,
						PlaceActivity.class);

				// Pass the idPlace to the PlaceActivity
				int idPlace = places.get(index).getId();
				intent.putExtra(Place.IDPLACE, idPlace);
				startActivity(intent);
			}
		});

		// Inflater (it will be used inside the adapter)
		final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Adapter
		adapter = new ArrayAdapter<Place>(this, R.layout.place_item, places) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// Get the item view
				View itemView;
				if (convertView == null) {
					itemView = inflater.inflate(R.layout.place_item, null);
				} else {
					itemView = convertView;
				}

				// Get the views
				TextView nameTextView = (TextView) itemView
						.findViewById(R.id.nameView);

				// Get place
				Place place = getItem(position);

				// Place name
				if (locationInterface != null && locationInterface.isLearning()
						&& place.equals(locationInterface.getLearnPlace())) {
					nameTextView.setText(place.getPlace() + " (L)");
				} else {
					nameTextView.setText(place.getPlace());
				}

				// Change color of detected place
				if (locationInterface != null
						&& place.equals(locationInterface.getDetectPlace())) {
					itemView.setBackgroundResource(R.color.detected_place);
				} else {
					itemView.setBackgroundResource(android.R.color.transparent);
				}

				// Return the view
				return itemView;
			}
		};
		listView.setAdapter(adapter);

		// Long press menu
		registerForContextMenu(listView);

		// Set the broadcast receiver
		this.broadcastReceiver = new PlacesActivityBroadcastReceiver();
		this.intentFilter = new IntentFilter();
		this.intentFilter.addAction(LocationService.PLACE_DETECTED);
	}

	/**
	 * Menu to ask for deletion or changing priority
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		menu.setHeaderTitle(getString(R.string.long_press_title));
		String[] menuItems = getResources().getStringArray(R.array.long_press);
		for (int i = 0; i < menuItems.length; i++) {
			menu.add(Menu.NONE, i, i, menuItems[i]);
		}
	}

	/**
	 * Response to the menu
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// Action selected
		int idAction = item.getItemId();

		// Place previously selected
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		Place place = places.get(info.position);

		// Different actions
		if (idAction == DELETE) {

			// Delete place
			SQLiteInterface.deletePlace(this, place.getId());
			places.remove(info.position);

			// Inform the service in case it was learning
			Intent intent = new Intent(LocationService.DELETE_PLACE);
			intent.putExtra(Place.IDPLACE, place.getId());
			sendBroadcast(intent);

		} else if (idAction == INCREASE_PRIORITY) {
			increasePriority(info.position);
			locationInterface.forceCheckDetection();
		} else if (idAction == DECREASE_PRIORITY) {
			decreasePriority(info.position);
			locationInterface.forceCheckDetection();
		}
		SQLiteInterface.updatePriorities(this, places);
		adapter.notifyDataSetChanged();
		return true;
	}

	/**
	 * Increase the priority of a place
	 * 
	 * @param position
	 *            of the item
	 */
	private void increasePriority(int position) {
		if (position == 1) {
			return;
		}
		Place p = places.remove(position);
		places.add(position - 1, p);
	}

	/**
	 * Decrease the priority of a place
	 * 
	 * @param position
	 *            of the item
	 */
	private void decreasePriority(int position) {
		if (position == places.size() - 1) {
			return;
		}
		Place p = places.remove(position);
		places.add(position + 1, p);
	}

	@Override
	public void onResume() {
		super.onResume();

		// Add places from database
		SQLiteInterface.addPlaces(this, places);
		adapter.notifyDataSetChanged();

		// Register receiver
		registerReceiver(this.broadcastReceiver, this.intentFilter);
	}

	@Override
	public void onPause() {
		super.onPause();

		// Unregister receiver
		unregisterReceiver(this.broadcastReceiver);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		MenuInflater inflater = getMenuInflater();
		if(!locationInterface.isDetecting()) {
			inflater.inflate(R.menu.detect, menu);		
		} else {
			inflater.inflate(R.menu.stop_detection, menu);
		}
		return true;
	}
	      
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		if (locationInterface.isDetecting()) {
			locationInterface.stopDetecting();
		} else {
			locationInterface.startDetecting();
		}
		return true;
	}

	private ServiceConnection serviceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.i("LOG", "Service connected to PlaceActivity");

			// Set the webService
			locationInterface = ((LocationService.LocationBinder) binder)
					.getLocationInterface();
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.i("LOG", "Service disconnected from PlaceActivity");
		}
	};

	private class PlacesActivityBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			adapter.notifyDataSetChanged();
		}
	}
}