package es.luisalbert.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import es.luisalbert.db.SQLiteInterface;
import es.luisalbert.entities.CellInfo;
import es.luisalbert.entities.Place;

public class PlaceActivity extends Activity {
	private Place place;
	private boolean isNew;
	private TextView nameTextView;

	private ListView listView;
	private ArrayAdapter<CellInfo> adapter;
	private LearningService service;


	private final static int RESTART_LIST_CELLS = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.place);

		// Id place
		int idPlace = getIntent().getIntExtra(Place.IDPLACE, 0);
		isNew = (idPlace == 0);

		// Get place
		if (!isNew) {
			place = SQLiteInterface.getPlace(this, idPlace);
			SQLiteInterface.addCells(this, place);
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

		// Button
		Button learnButton = (Button) findViewById(R.id.learnButton);
		learnButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				openRecordDialog();
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
				// Get the view
				View itemView;
				if (convertView == null) {
					itemView = inflater.inflate(R.layout.cell_item, null);
				} else {
					itemView = convertView;
				}

				// Get the views
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
	}

	/**
	 * Ask the user how to record
	 **/
	private void openRecordDialog() {
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
	 * 
	 * @param i
	 */
	private void startLearning(int i) {
		// Save first the place (otherwise we won't have id if it's new)
		updatePlace();

		if (i == RESTART_LIST_CELLS) {
			SQLiteInterface.deleteCells(this, place);
			place.getCells().clear();
			Toast.makeText(this, getString(R.string.cells_deleted),
					Toast.LENGTH_LONG).show();
		}

		service.getCellLocation();


		
		adapter.notifyDataSetChanged();
	}

	private Place createAndSavePlace() {
		return SQLiteInterface.savePlace(this, new Place(0, getString(R.string.new_place)));
	}

	private void updatePlace() {
		// Set name of place
		String name = nameTextView.getText().toString();
		if (name.length() == 0) {
			name = "No name";
		}
		place.setPlace(name);
		SQLiteInterface.updatePlace(this, place);
	}

	@Override
	public void onPause() {
		super.onPause();
		updatePlace();
	}

	private ServiceConnection serviceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.i("LOG", "Service connected to CompetitionTabActivity");

			// Set the webService
			service = ((LearningService.LearningBinder) binder)
					.getService();
			service.setPlace(place);
			
			
			// // Set the competition
			// webService.setCompetition(competition);
			//
			// // Download the athletes and circuit
			// webService.downloadAthletesAndCircuit();
			//
			// // Broadcast that the connection is established (Details Activity
			// // will be able to show the competition details
			// sendBroadcast(new Intent(WebService.CONNECTION_ESTABLISHED));

		}

		public void onServiceDisconnected(ComponentName className) {
			Log.i("LOG", "Service disconnected from CompetitionTabActivity");
			// webService = null;
		}
	};
}