package es.gsmprofiler.app;

import es.gsmprofiler.db.SQLiteInterface;
import es.gsmprofiler.entities.CellInfo;
import es.gsmprofiler.entities.Place;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

/**
 * Service that will learn in the background the cells that the user can view
 * from his/her defined places
 * 
 * @author luis
 */
public class LearningService extends Service {

	private TelephonyManager telephonyManager;
	private Place learnPlace;
	private Place detectPlace;
	private int lastDetectedIdCell;
	private boolean isLearning;
	private boolean isDetecting;
	private PhoneStateListener phoneStateListener;

	// Broadcasting
	public static final String CELL_LEARNT = "cell_learnt";
	public static final String DELETE_PLACE = "delete_place";
	public static final String PLACE_DETECTED = "place_detected";
	private LearningServiceBroadcastReceiver broadcastReceiver;

	@Override
	public void onCreate() {
		Log.i("LocalService", "On create");

		// Telephony manager
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		// The system is not still learning nor detecting
		isLearning = false;
		isDetecting = false;

		// Listener for learning
		phoneStateListener = new PhoneStateListener() {
			@Override
			public void onCellLocationChanged(CellLocation location) {
				GsmCellLocation gsmLocation = (GsmCellLocation) location;
				lastDetectedIdCell = gsmLocation.getCid();
				if(isLearning) {
					saveCellLocation(gsmLocation);
					sendBroadcast(new Intent(CELL_LEARNT));
				} 
				if(isDetecting) {
					checkIfNewDetection(lastDetectedIdCell);
				}
			}
		};

		// Set the broadcast receiver
		this.broadcastReceiver = new LearningServiceBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(LearningService.DELETE_PLACE);
		registerReceiver(broadcastReceiver, intentFilter);
	}
	
	/**
	 * Forces checking. To use it for example when the priorities have changed
	 * or a place have been deleted
	 */
	public void forceCheckDetection() {
		if(isDetecting) {
			checkIfNewDetection(lastDetectedIdCell);
		}
	}
	
	private void checkIfNewDetection(int idCell) {
		// Get detected place from DB
		Place lastDetPlace = SQLiteInterface.detectPlace(LearningService.this, idCell);
		
		// First component includes if one is null and the other one is not
		if(detectPlace != lastDetPlace || !detectPlace.equals(lastDetPlace)) {
			detectPlace = lastDetPlace;
			if(detectPlace == null) {
				Toast.makeText(this, "Out of the defined places", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "You are in: " + detectPlace.getPlace(), Toast.LENGTH_LONG).show();
			}
		}	
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
		unregisterReceiver(broadcastReceiver);
	}

	/**
	 * Set the place we are learning about
	 * 
	 * @param place
	 */
	public void setPlace(Place place) {
		this.learnPlace = place;
	}

	public Place getPlace() {
		return learnPlace;
	}

	private void saveCellLocation(GsmCellLocation cellLocation) {
		// Save cell info
		CellInfo cell = new CellInfo(cellLocation.getCid(),
				cellLocation.getLac());

		// Save in DB
		SQLiteInterface.deleteCell(this, learnPlace.getId(), cell.getId());
		SQLiteInterface.saveCell(this, learnPlace.getId(), cell);
	}

	public boolean isLearning() {
		return isLearning;
	}
	
	public boolean isDetecting() {
		return isDetecting;
	}

	public void startLearning() {
		isLearning = true;
		// Register listener
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_CELL_LOCATION);
	}

	public void stopLearning() {
		isLearning = false;
		// Unregister listener
		if(!isDetecting) {
			telephonyManager.listen(phoneStateListener,
					PhoneStateListener.LISTEN_NONE);
		}
	}
	
	public void startDetecting() {
		isDetecting = true;
		// Register listener
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_CELL_LOCATION);
	}

	public void stopDetecting() {
		isDetecting = false;
		// Unregister listener
		if(!isLearning) {
			telephonyManager.listen(phoneStateListener,
					PhoneStateListener.LISTEN_NONE);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// Object that receives interactions from clients
	private final IBinder mBinder = new LearningBinder();

	/**
	 * Class for clients to access the service. We will return a reference to
	 * the Service itself
	 */
	public class LearningBinder extends Binder {
		LearningService getService() {
			return LearningService.this;
		}
	}

	private class LearningServiceBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// Place deleted
			int idPlace = intent.getIntExtra(Place.IDPLACE, 0);

			// Check if it's our place, even if it's not learning
			if (learnPlace != null && learnPlace.getId() == idPlace) {
				stopLearning();
				learnPlace = null;
			}
			// Same for detecting
			if (detectPlace != null && detectPlace.getId() == idPlace) {
				detectPlace = null;
				forceCheckDetection();
			}
		}
	}
}