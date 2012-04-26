package es.luisalbert.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import es.luisalbert.db.SQLiteInterface;
import es.luisalbert.entities.CellInfo;
import es.luisalbert.entities.Place;

public class PlaceActivity extends Activity {
	private Place place;
	private boolean isNew;
	private TextView nameTextView;

	private ListView listView;
	private ArrayAdapter<CellInfo> adapter;

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
			place = new Place(0, getString(R.string.new_place));
		}

		// Add text
		nameTextView = (TextView) findViewById(R.id.placeView);
		if (!isNew) {
			nameTextView.setText(place.getPlace());
		}

		// Button
		Button recordButton = (Button) findViewById(R.id.recordButton);
		recordButton.setOnClickListener(new View.OnClickListener() {
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

				// Add data
				CellInfo cell = getItem(position);
				cellIDTextView.setText(String.valueOf(cell.getId()));
				locationAreaTextView
						.setText(String.valueOf(cell.getAreaCode()));

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
				.setTitle(R.string.how_to_record)
				.setItems(R.array.how_to_record,
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialoginterface, int i) {
								startRecord(i);
							}
						}).show();
	}

	/**
	 * Start record with the chosen policy
	 * 
	 * @param i
	 */
	private void startRecord(int i) {
		// Save first the place (otherwise we won't have id if it's new)
		savePlace();
		
		// Obtain cell location
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager
				.getCellLocation();
		
		// Save cell info
		CellInfo cell = new CellInfo(cellLocation.getCid(), cellLocation.getLac());
		SQLiteInterface.saveCell(this, place.getId(), cell);
		
		// Update views
		place.getCells().add(cell);
		adapter.notifyDataSetChanged();
	}

	private void savePlace() {
		// Set name of place
		String name = nameTextView.getText().toString();
		if (name.length() == 0) {
			name = "No name";
		}
		place.setPlace(name);

		// Save to db
		if (isNew) {
			place = SQLiteInterface.savePlace(this, place);
			isNew = false;
		} else {
			SQLiteInterface.updatePlace(this, place);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		savePlace();
	}
}