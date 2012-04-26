package es.luisalbert.db;

import java.util.List;

import es.luisalbert.app.R;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import es.luisalbert.entities.CellInfo;
import es.luisalbert.entities.Place;

public class SQLiteInterface {

	private static final String TAG_LOGGER = "Database";

	public static final String FOLLOWING = "following";
	public static final String ALL = "all";

	public static Place savePlace(Context context, Place place) {
		PlacesDBHelper dbHelper = new PlacesDBHelper(context);
		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getWritableDatabase();

			// Container for the values
			ContentValues contentValues = new ContentValues();

			// Values into content: places table
			contentValues.put(PlacesDBHelper.PLACE, place.getPlace());
			contentValues.put(PlacesDBHelper.VOLUME, 0);
			contentValues.put(PlacesDBHelper.VIBRATION, 0);

			// Save the place
			db.insertOrThrow(PlacesDBHelper.PLACES_TABLE, null, contentValues);
			Cursor lastIdCursor = db.rawQuery("SELECT last_insert_rowid()",
					null);
			if (lastIdCursor.moveToNext()) {
				place.setId(lastIdCursor.getInt(0));
			}

			Log.i("DATABASE", "Place inserted into db");
			return place;
		} catch (Exception e) {
			Log.e("DATABASE", "Error in db " + e);
			return null;
		} finally {
			// Always close the dbHelper
			dbHelper.close();
		}
	}

	public static void saveCell(Context context, int idPlace, CellInfo cell) {
		PlacesDBHelper dbHelper = new PlacesDBHelper(context);
		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getWritableDatabase();

			// Container for the values
			ContentValues contentValues = new ContentValues();

			// Values into content: cell table
			contentValues.put(PlacesDBHelper.IDPLACE, idPlace);
			contentValues.put(PlacesDBHelper.IDCELL, cell.getId());
			contentValues.put(PlacesDBHelper.LOCATION_AREA, cell.getAreaCode());

			// Save the place
			db.insertOrThrow(PlacesDBHelper.CELLS_TABLE, null, contentValues);

			Log.i("DATABASE", "Cell inserted into db");
		} catch (Exception e) {
			Log.e("DATABASE", "Error in db " + e);
		} finally {
			// Always close the dbHelper
			dbHelper.close();
		}
	}

	public static void addCells(Activity activity, Place place) {
		// Get the helper
		PlacesDBHelper dbHelper = new PlacesDBHelper(activity);
		// Clear the list
		place.getCells().clear();

		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Log.d(TAG_LOGGER, "Database obtained");

			Cursor cursor = db.query(PlacesDBHelper.CELLS_TABLE, null, 
					PlacesDBHelper.IDPLACE + " = " + place.getId(),
					null, null, null, null);

			activity.startManagingCursor(cursor);
			Log.d(TAG_LOGGER, "Query for cells executed");

			// Rest from db
			addCellsFromCursor(place.getCells(), cursor, activity);
			Log.d(TAG_LOGGER, "Cells added to list: " + place.getCells().size());

		} finally {
			// Always close the helper
			dbHelper.close();
		}
		Log.i(TAG_LOGGER, place.getCells().size() + " places from db");
	}

	public static void updatePlace(Context context, Place place) {
		PlacesDBHelper dbHelper = new PlacesDBHelper(context);
		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getWritableDatabase();

			// Container for the values
			ContentValues contentValues = new ContentValues();

			// Values into content: places table
			contentValues.put(PlacesDBHelper.PLACE, place.getPlace());
			contentValues.put(PlacesDBHelper.VOLUME, 0);
			contentValues.put(PlacesDBHelper.VIBRATION, 0);

			// Update the message
			db.update(PlacesDBHelper.PLACES_TABLE, contentValues,
					PlacesDBHelper.IDPLACE + "=" + place.getId(), null);

			Log.i("DATABASE", "Place updated");
		} finally {
			// Always close the dbHelper
			dbHelper.close();
		}
	}

	public static void addPlaces(Activity activity, List<Place> places) {
		// Get the helper
		PlacesDBHelper dbHelper = new PlacesDBHelper(activity);
		// Clear the list
		places.clear();

		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Log.d(TAG_LOGGER, "Database obtained");

			String orderBy = PlacesDBHelper.IDPLACE;
			Cursor cursor = db.query(PlacesDBHelper.PLACES_TABLE, null, null,
					null, null, null, orderBy);

			activity.startManagingCursor(cursor);
			Log.d(TAG_LOGGER, "Query for places executed");

			// Add fake place for creating
			places.add(new Place(0, activity.getString(R.string.new_place)));

			// Rest from db
			addPlacesFromCursor(places, cursor, activity);
			Log.d(TAG_LOGGER, "Places added to list");

		} finally {
			// Always close the helper
			dbHelper.close();
		}
		Log.i(TAG_LOGGER, places.size() + " places from db");
	}

	private static void addPlacesFromCursor(List<Place> places, Cursor cursor,
			Activity activity) {

		while (cursor.moveToNext()) {
			// Add the place
			places.add(new Place(cursor.getInt(0), cursor.getString(1)));
		}
	}
	
	private static void addCellsFromCursor(List<CellInfo> cells, Cursor cursor,
			Activity activity) {

		while (cursor.moveToNext()) {
			// Add the place
			cells.add(new CellInfo(cursor.getInt(2), cursor.getInt(3)));
		}
	}

	public static Place getPlace(Activity activity, int idPlace) {
		// Get the helper
		PlacesDBHelper dbHelper = new PlacesDBHelper(activity);

		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Log.d("DATABASE", "Database obtained");

			// Execute the query
			Cursor cursor = db.query(PlacesDBHelper.PLACES_TABLE, null,
					PlacesDBHelper.IDPLACE + " = " + idPlace, null, null, null,
					null);
			activity.startManagingCursor(cursor);
			Log.d("DATABASE", "Query for place executed");

			// Extract the results
			if (cursor.moveToNext()) {
				Log.d("DATABASE", "Place returned");
				return new Place(cursor.getInt(0), cursor.getString(1));
			}
		} finally {
			// Always close the subjectsData
			dbHelper.close();
		}
		return null;
	}
}
