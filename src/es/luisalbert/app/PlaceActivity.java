package es.luisalbert.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import es.luisalbert.db.SQLiteInterface;
import es.luisalbert.entities.Place;

public class PlaceActivity extends Activity {
	private Place place;
	private boolean isNew;
	private TextView nameTextView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.place);
		
		// Id place
		int idPlace = getIntent().getIntExtra(Place.IDPLACE, 0);
		isNew = idPlace == 0;
		
		// Get place
		if (!isNew) {
			place = SQLiteInterface.getPlace(this, idPlace);
		} else {
			place = new Place(0, getString(R.string.new_place));
		}
		
		// Add text
		nameTextView = (TextView) findViewById(R.id.placeView);
		if(!isNew) {
			nameTextView.setText(place.getPlace());
		} 
		
		// Button
		Button recordButton = (Button) findViewById(R.id.recordButton);
		recordButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				openRecordDialog();
			}
		});
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
	 * @param i
	 */
	private void startRecord(int i) {
		Toast.makeText(this, "Record" + i, Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onPause() {		
		super.onPause();
		
		// Set name of place
		String name = nameTextView.getText().toString();
		if(name.length() == 0) {
			name = "No name";
		}
		place.setPlace(name);
		
		// Save to db
		if(isNew) {
			place = SQLiteInterface.savePlace(this, place);
			isNew = false;
		} else {
			SQLiteInterface.updatePlace(this, place);
		}	
	}
}