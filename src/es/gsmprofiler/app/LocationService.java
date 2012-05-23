package es.gsmprofiler.app;

import es.gsmprofiler.entities.CellInfo;
import es.gsmprofiler.entities.Place;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Service that will learn in the background the cells that the user can view
 * from his/her defined places
 * 
 * @author luis
 */
public class LocationService extends Service implements LocationListener {

	// Location manager
	private LocationManager locationManager;

	// Broadcasting
	public static final String CELL_LEARNT = "cell_learnt";
	public static final String DELETE_PLACE = "delete_place";
	public static final String PLACE_DETECTED = "place_detected";

	// Binding
	private final IBinder mBinder = new LocationBinder();

	// --- Service life cycle ------------------------------------

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("LocalService", "On create");

		// Create location manager
		locationManager = new LocationManager(this, this);
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
		super.onDestroy();
		locationManager.onDestroy();
	}

	// --- Location notifications ------------------------------------

	public void onCellLearnt(CellInfo cell) {

		// Broadcast for PlaceActivity to paint again cells
		sendBroadcast(new Intent(CELL_LEARNT));
	}

	public void onPlaceDetected(Place detectPlace) {
		// Create intent for broadcasting
		Intent intent = new Intent(PLACE_DETECTED); 
		int idPlace = -1;
		
		// Notify by toast
		if (detectPlace == null) {
			Toast.makeText(this, "Out of the defined places", Toast.LENGTH_LONG)
					.show();
		} else {
			Toast.makeText(this, "You are in: " + detectPlace.getPlace(),
					Toast.LENGTH_LONG).show();
			idPlace = detectPlace.getId();
		}
		
		// Broadcast
		intent.putExtra(Place.IDPLACE, idPlace);
		sendBroadcast(intent);
	}

	// --- Binding with activities ------------------------------------

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	/**
	 * Class for clients to access the service. We will return a reference to
	 * the Service itself
	 */
	public class LocationBinder extends Binder {
		LocationManagerInterface getLocationInterface() {
			return locationManager;
		}
	}
}