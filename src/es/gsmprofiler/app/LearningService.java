package es.gsmprofiler.app;

import es.gsmprofiler.db.SQLiteInterface;
import es.gsmprofiler.entities.CellInfo;
import es.gsmprofiler.entities.Place;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

/**
 * Service that will learn in the background the cells that 
 * the user can view from his/her defined places
 * 
 * @author luis
 */
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
		// Tell the user we stopped
		Toast.makeText(this, "Destroy", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Set the place we are learning about
	 * @param place
	 */
	public void setPlace(Place place) {
		this.place = place;
	}

	public void getCellLocation() {
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

	/**
	 * Remove cell from DB if we find another one with the same ID
	 * To only the one with the latest timestamp
	 * @param id id of the cell
	 */
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

	// Object that receives interactions from clients
	private final IBinder mBinder = new LearningBinder();

	/**
	 * Class for clients to access the service. We will return 
	 * a reference to the Service itself
	 */
	public class LearningBinder extends Binder {
		LearningService getService() {
			return LearningService.this;
		}
	}
}