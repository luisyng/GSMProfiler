package es.luisalbert.app;

import es.luisalbert.db.SQLiteInterface;
import es.luisalbert.entities.CellInfo;
import es.luisalbert.entities.Place;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

public class LearningService extends Service {

	private TelephonyManager telephonyManager;
	private Place place;

	@Override
	public void onCreate() {
		Log.i("LocalService", "On create");
		Toast.makeText(this, "Create", Toast.LENGTH_SHORT).show();

		// Telephony manager
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("LocalService", "Received start id " + startId + ": " + intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// Tell the user we stopped.
		Toast.makeText(this, "Destroy", Toast.LENGTH_SHORT).show();
	}

	public void setPlace(Place place) {
		this.place = place;
	}

	public void getCellLocation() {
		Toast.makeText(this, "I will get cell", Toast.LENGTH_SHORT).show();

		// Obtain cell location
		GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager
				.getCellLocation();

		// Save cell info
		CellInfo cell = new CellInfo(cellLocation.getCid(),
				cellLocation.getLac());
		// Save in DB
		SQLiteInterface.deleteCell(this, place.getId(), cell.getId());
		SQLiteInterface.saveCell(this, place.getId(), cell);
		// Update object for views
		removeCell(cell.getId());
		place.getCells().add(cell);
	}

	private void removeCell(int id) {
		CellInfo found = null;
		for (CellInfo cell : place.getCells()) {
			if (cell.getId() == id) {
				found = cell;
				break;
			}
		}
		place.getCells().remove(found);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LearningBinder();

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LearningBinder extends Binder {
		LearningService getService() {
			return LearningService.this;
		}
	}
}