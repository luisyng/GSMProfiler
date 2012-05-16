package es.gsmprofiler.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import es.gsmprofiler.db.SQLiteInterface;
import es.gsmprofiler.entities.CellInfo;
import es.gsmprofiler.entities.Place;


/**
 * Activity to edit the details of a place and 
 * tell the service to start learning the corresponding
 * cells
 * 
 * @author luis
 */
public class PlaceActivity extends Activity {
	private Place place;
	private boolean isNew;
	private TextView nameTextView;
	private ListView listView;
	private Button learnButton;
	private ArrayAdapter<CellInfo> adapter;
	private LearningService service;
	private final static int RESTART_LIST_CELLS = 1;

	private PlaceActivityBroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.place);

		// Id place
		int idPlace = getIntent().getIntExtra(Place.IDPLACE, 0);
		isNew = (idPlace == 0);

		// Get place from DB if it's not new
		if (!isNew) {
			place = SQLiteInterface.getPlace(this, idPlace);
			SQLiteInterface.addCells(this, place);
		// Otherwise create it and save it in DB
		} else {
			place = createAndSavePlace();
		}
		
		// Bind to the service
		getApplicationContext().bindService(
				new Intent(this, LearningService.class), serviceConnection,
				Context.BIND_AUTO_CREATE);

		// Add text
		nameTextView = (TextView) findViewById(R.id.placeView);
		if (!isNew) {
			nameTextView.setText(place.getPlace());
		}
		
		// Learning button
		learnButton = (Button) findViewById(R.id.learnButton);
		learnButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
						
				// Dismiss keyboard
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(nameTextView.getWindowToken(), 0);
				
				// Start or stop learning
				if(!service.isLearning()) {
					openLearningDialog();
				} else {
					service.stopLearning();
					learnButton.setText(getString(R.string.learn));
				}
			}
		});

		// Get the list view
		listView = (ListView) this.findViewById(R.id.cellsListView);

		// Inflater (it will be used inside the adapter)
		final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Adapter
		adapter = new ArrayAdapter<CellInfo>(this, R.layout.cell_item,
				place.getCells()) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// Get the item view
				View itemView;
				if (convertView == null) {
					itemView = inflater.inflate(R.layout.cell_item, null);
				} else {
					itemView = convertView;
				}

				// Get the individual views
				TextView cellIDTextView = (TextView) itemView
						.findViewById(R.id.cellIDText);
				TextView locationAreaTextView = (TextView) itemView
						.findViewById(R.id.locationAreaText);
				TextView timestampTextView = (TextView) itemView
						.findViewById(R.id.timestampText);

				// Add data
				CellInfo cell = getItem(position);
				cellIDTextView.setText(String.valueOf(cell.getId()));
				locationAreaTextView
						.setText(String.valueOf(cell.getAreaCode()));
				timestampTextView.setText(cell.getTimestamp());

				// Return the view
				return itemView;
			}
		};
		listView.setAdapter(adapter);
		
		// Set the broadcast receiver
		this.broadcastReceiver = new PlaceActivityBroadcastReceiver();
		this.intentFilter = new IntentFilter();
		this.intentFilter.addAction(LearningService.CELL_LEARNT);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// Register receiver
		registerReceiver(this.broadcastReceiver, this.intentFilter);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		// Unregister receiver
		unregisterReceiver(this.broadcastReceiver);
		
		// Update place
		updatePlace();
	}

	/**
	 * Ask the user how to record
	 **/
	private void openLearningDialog() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.how_to_learn)
				.setItems(R.array.how_to_learn,
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialoginterface, int i) {
								startLearning(i);
							}
						}).show();
	}

	/**
	 * Start learning with the chosen policy
	 * @param i
	 */
	private void startLearning(int i) {
		// Save first the place (otherwise we won't have id if it's new)
		updatePlace();

		// Delete previous cells if requested
		if (i == RESTART_LIST_CELLS) {
			SQLiteInterface.deleteCells(this, place.getId());
			place.getCells().clear();
			Toast.makeText(this, getString(R.string.cells_deleted),
					Toast.LENGTH_LONG).show();
		}
		service.startLearning();
		learnButton.setText(getString(R.string.stop_learning));
	}

	/**
	 * Create a new place and save it in the DB
	 * @return place
	 */
	private Place createAndSavePlace() {
		return SQLiteInterface.savePlace(this, new Place(0, getString(R.string.new_place)));
	}

	/**
	 * Update the place in the DB
	 */
	private void updatePlace() {
		// Set name of place
		String name = nameTextView.getText().toString();
		if (name.length() == 0) {
			name = "No name";
		}
		place.setPlace(name);
		SQLiteInterface.updatePlace(this, place);
	}

	private ServiceConnection serviceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.i("LOG", "Service connected to PlaceActivity");

			// Set the webService
			service = ((LearningService.LearningBinder) binder)
					.getService();
			
			// Learning the current place
			if(service.isLearning() && service.getPlace().equals(place)) {
				learnButton.setText(getString(R.string.stop_learning));
				learnButton.setEnabled(true);
			// Learning another place
			} else if(service.isLearning() && !service.getPlace().equals(place)) {
				Toast.makeText(PlaceActivity.this, getString(R.string.already_learning), Toast.LENGTH_LONG).show();
				learnButton.setEnabled(false);
				// Not learning
			} else {
				learnButton.setEnabled(true);
				service.setPlace(place);
			}	
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.i("LOG", "Service disconnected from PlaceActivity");
		}
	};
	
	private class PlaceActivityBroadcastReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			SQLiteInterface.addCells(PlaceActivity.this, place);
			adapter.notifyDataSetChanged();
		}
	}
}