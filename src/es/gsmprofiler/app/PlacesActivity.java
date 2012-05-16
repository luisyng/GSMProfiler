package es.gsmprofiler.app;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
	private ListView listView;
	private List<Place> places;
	private ArrayAdapter<Place> adapter;
	private LearningService service;

	private static final int DELETE = 0;
	private static final int INCREASE_PRIORITY = 1;
	private static final int DECREASE_PRIORITY = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.places);

		// Initialize places
		places = new ArrayList<Place>();

		// Bind to the service
		getApplicationContext().bindService(
				new Intent(this, LearningService.class), serviceConnection,
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

				// Add data
				Place place = getItem(position);
				nameTextView.setText(place.getPlace());

				// Return the view
				return itemView;
			}
		};
		listView.setAdapter(adapter);

		// Long press menu
		registerForContextMenu(listView);
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
			Intent intent = new Intent(LearningService.DELETE_PLACE);
			intent.putExtra(Place.IDPLACE, place.getId());
			sendBroadcast(intent);
			
		} else if (idAction == INCREASE_PRIORITY) {
			increasePriority(info.position);
			service.forceCheckDetection();
		} else if (idAction == DECREASE_PRIORITY) {
			decreasePriority(info.position);	
			service.forceCheckDetection();
		}
		SQLiteInterface.updatePriorities(this, places);
		adapter.notifyDataSetChanged();
		return true;
	}

	/**
	 * Increase the priority of a place
	 * @param position of the item
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
	 * @param position of the item
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
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		if(service.isDetecting()) {
			service.stopDetecting();
		} else {
			service.startDetecting();
		}
		return true;
	}
	
	private ServiceConnection serviceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.i("LOG", "Service connected to PlaceActivity");

			// Set the webService
			service = ((LearningService.LearningBinder) binder)
					.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.i("LOG", "Service disconnected from PlaceActivity");
		}
	};
}