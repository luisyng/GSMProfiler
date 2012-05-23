package es.gsmprofiler.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import es.gsmprofiler.db.SQLiteInterface;
import es.gsmprofiler.entities.CellInfo;
import es.gsmprofiler.entities.Place;

public class LocationManager implements LocationManagerInterface {
	// Android system
	private Context context;
	private TelephonyManager telephonyManager;
	private PhoneStateListener phoneStateListener;
	private LocationBroadcastReceiver broadcastReceiver;

	// State
	private Place learnPlace;
	private Place detectPlace;
	private int lastDetectedIdCell;
	private boolean isLearning;
	private boolean isDetecting;

	// Log
	private static final String TAG_LOGGER = "LocationManager";

	// Notifications
	private LocationListener locationListener;

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param locationListener
	 */
	public LocationManager(Context context, LocationListener locationListener) {

		// Notifications
		this.locationListener = locationListener;

		// Android system
		this.context = context;
		telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		phoneStateListener = new OurPhoneStateListener();

		// Broadcast receiver
		broadcastReceiver = new LocationBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(LocationService.DELETE_PLACE);
		context.registerReceiver(broadcastReceiver, intentFilter);

		// State
		learnPlace = null;
		detectPlace = null;
		lastDetectedIdCell = -1;
		isLearning = false;
		isDetecting = false;
	}

	// --- Communication with service ---------------------------
	
	public void onDestroy() {
		context.unregisterReceiver(broadcastReceiver);
	}

	// --- Internal methods -------------------------------
	private void checkIfNewDetection() {
		// Get detected place from DB
		Place currentPlace = SQLiteInterface.detectPlace(context, lastDetectedIdCell);

//		if(detectPlace == null) {
//			Log.i(TAG_LOGGER, "Det: null");
//		} else {
//			Log.i(TAG_LOGGER, "Det" + detectPlace.getId());
//		}
//		if(lastDetPlace == null) {
//			Log.i(TAG_LOGGER, "Last: null");
//		} else {
//			Log.i(TAG_LOGGER, "Last" + lastDetPlace.getId());
//		}
		
		// First component includes if one is null and the other one is not
		if ((detectPlace == null && currentPlace != null) 
				|| !detectPlace.equals(currentPlace)) {		
			detectPlace = currentPlace;
			locationListener.onPlaceDetected(detectPlace);
		}
	}
	
	private void logState() {
		Log.i(TAG_LOGGER, "Learning: " + isLearning + ", Detecting: " + isDetecting);
	}
	
	// --- External interface -----------------------

	public void setLearnPlace(Place place) {
		this.learnPlace = place;
	}

	public Place getLearnPlace() {
		return learnPlace;
	}

	public Place getDetectPlace() {
		return detectPlace;
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
		// Log
		logState();
	}

	public void stopLearning() {
		isLearning = false;
		// Unregister listener
		if (!isDetecting) {
			telephonyManager.listen(phoneStateListener,
					PhoneStateListener.LISTEN_NONE);
		}
		// Log
		logState();
	}

	public void startDetecting() {
		isDetecting = true;
		// Register listener
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_CELL_LOCATION);
		// Log
		logState();
	}

	public void stopDetecting() {
		isDetecting = false;
		// Unregister listener
		if (!isLearning) {
			telephonyManager.listen(phoneStateListener,
					PhoneStateListener.LISTEN_NONE);
		}
		// Log
		logState();
	}
	
	/**
	 * Forces checking. To use it for example when the priorities have changed
	 * or a place have been deleted
	 */
	public void forceCheckDetection() {
		if (isDetecting) {
			checkIfNewDetection();
		}
	}
	

	/**
	 * Phone State Listener. Gets the GSM Cell Location and notifies if new cell
	 * learn or new place detected.
	 * 
	 * @author luis
	 */
	private class OurPhoneStateListener extends PhoneStateListener {
		@Override
		public void onCellLocationChanged(CellLocation location) {

			// Get gsm cell and id
			GsmCellLocation gsmLocation = (GsmCellLocation) location;
			lastDetectedIdCell = gsmLocation.getCid();

			// Log
			Log.i(TAG_LOGGER, "New cell: " + lastDetectedIdCell);

			// If learning
			if (isLearning) {
				// Create cellInfo
				CellInfo cell = new CellInfo(gsmLocation.getCid(),
						gsmLocation.getLac());
				
				// Save in DB
				SQLiteInterface.deleteCell(context, learnPlace.getId(),
						cell.getId());
				SQLiteInterface.saveCell(context, learnPlace.getId(), cell);
				
				// Notify
				locationListener.onCellLearnt(cell);
			}

			// If detecting
			if (isDetecting) {
				checkIfNewDetection();
			}
		}
	}

	/**
	 * Broadcast Receiver. Acts when the user deletes a place If it was
	 * learning, stops learning If it was detecting and we were in that place,
	 * forces a new detection.
	 * 
	 * @author luis
	 */
	public class LocationBroadcastReceiver extends BroadcastReceiver {

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
				checkIfNewDetection();
			}
		}
	}
}
