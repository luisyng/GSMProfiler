package es.luisalbert.app;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import es.luisalbert.db.SQLiteInterface;
import es.luisalbert.entities.Place;

public class PlacesActivity extends Activity {
	private ListView listView;
	private List<Place> places;
	private ArrayAdapter<Place> adapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.places);

		// Initialize places
		places = new ArrayList<Place>();
		
		// Get the list view
		listView = (ListView) this.findViewById(R.id.placesListView);

		// Listener for the list items
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView av, View v, int index, long arg3) {
				Intent intent = new Intent(PlacesActivity.this, PlaceActivity.class);
				int idPlace = places.get(index).getId();
				intent.putExtra(Place.IDPLACE, idPlace);
				startActivity(intent);
			}
		});

		// Inflater (it will be used inside the adapter)
		final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		// Adapter
		adapter = new ArrayAdapter<Place>(this,
				R.layout.place_item, places) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// Get the view
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
	}
	
	@Override
	public void onResume() {
		super.onResume();

		// Add messages from database
		SQLiteInterface.addPlaces(this, places);
		adapter.notifyDataSetChanged();
	}
}