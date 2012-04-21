package es.luisalbert.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper class for working with the database. When the Android app tries to
 * access to the db, this class creates the database if its not already created,
 * and upgrades the version if a new one is available.
 */
public class PlacesDBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "places.db";
	private static final int DATABASE_VERSION = 2;

	// Places table
	public static final String PLACES_TABLE = "places";
	public static final String IDPLACE = "idplace";
	public static final String PLACE = "place";
	public static final String VOLUME = "volume";
	public static final String VIBRATION = "vibration";

	/** Create a helper object for the Places database */
	public PlacesDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + PLACES_TABLE + " (" + IDPLACE
				+ " INTEGER PRIMARY KEY, " + PLACE + " TEXT," + VOLUME
				+ " INTEGER," + VIBRATION + " BOOLEAN);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + PLACES_TABLE);
		onCreate(db);
	}
}