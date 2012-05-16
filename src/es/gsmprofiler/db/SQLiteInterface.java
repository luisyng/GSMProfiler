package es.gsmprofiler.db;

import java.util.ArrayList;
import java.util.List;

import es.gsmprofiler.app.R;
import es.gsmprofiler.entities.CellInfo;
import es.gsmprofiler.entities.Place;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SQLiteInterface {

	private static final String TAG_LOGGER = "Database";

	public static Place savePlace(Context context, Place place) {
		PlacesDBHelper dbHelper = new PlacesDBHelper(context);
		try {
			// Get number of places to put lowest priority
			List<Place> places = new ArrayList<Place>();		
			SQLiteInterface.addPlaces(context, places);
			// Priority goes from 0 to SIZE-1. New lowest = size
			int priority = places.size(); 
					
			// Get the database
			SQLiteDatabase db = dbHelper.getWritableDatabase();

			// Container for the values
			ContentValues contentValues = new ContentValues();

			// Values into content: places table
			contentValues.put(PlacesDBHelper.PLACE, place.getPlace());
			contentValues.put(PlacesDBHelper.PRIORITY, priority);
			contentValues.put(PlacesDBHelper.VOLUME, 0);
			contentValues.put(PlacesDBHelper.VIBRATION, 0);

			// Save the place
			db.insertOrThrow(PlacesDBHelper.PLACES_TABLE, null, contentValues);
			Cursor lastIdCursor = db.rawQuery("SELECT last_insert_rowid()",
					null);
			if (lastIdCursor.moveToNext()) {
				place.setId(lastIdCursor.getInt(0));
			}

			Log.i(TAG_LOGGER, "Place inserted into db");
			return place;
		} catch (Exception e) {
			Log.e(TAG_LOGGER, "Error in db " + e);
			return null;
		} finally {
			// Always close the dbHelper
			dbHelper.close();
		}
	}
	
	public static void updatePriorities(Context context, List<Place> places) {
		PlacesDBHelper dbHelper = new PlacesDBHelper(context);
		try {				
			// Get the database
			SQLiteDatabase db = dbHelper.getWritableDatabase();

			// Container for the values
			ContentValues contentValues = new ContentValues();

			// Update individually
			for(int i = 0; i < places.size(); i++) {
				contentValues.put(PlacesDBHelper.PRIORITY, i);				
				db.update(PlacesDBHelper.PLACES_TABLE, contentValues,
						PlacesDBHelper.IDPLACE + "=" + places.get(i).getId(), null);
			}
			Log.i(TAG_LOGGER, "Prioirities updated");
		} catch (Exception e) {
			Log.e(TAG_LOGGER, "Error in db " + e);
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
			contentValues.put(PlacesDBHelper.TIMESTAMP, cell.getTimestamp());

			// Save the place
			db.insertOrThrow(PlacesDBHelper.CELLS_TABLE, null, contentValues);

			Log.i(TAG_LOGGER, "Cell inserted into db");
		} catch (Exception e) {
			Log.e(TAG_LOGGER, "Error in db " + e);
		} finally {
			// Always close the dbHelper
			dbHelper.close();
		}
	}

	public static void addCells(Context context, Place place) {
		// Get the helper
		PlacesDBHelper dbHelper = new PlacesDBHelper(context);
		// Clear the list
		place.getCells().clear();

		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Log.d(TAG_LOGGER, "Database obtained");

			Cursor cursor = db.query(PlacesDBHelper.CELLS_TABLE, null, 
					PlacesDBHelper.IDPLACE + " = " + place.getId(),
					null, null, null, null);

			Log.d(TAG_LOGGER, "Query for cells executed");

			// Rest from db
			addCellsFromCursor(place.getCells(), cursor);
			Log.d(TAG_LOGGER, "Cells added to list: " + place.getCells().size());

		} finally {
			// Always close the helper
			dbHelper.close();
		}
		Log.i(TAG_LOGGER, place.getCells().size() + " places from db");
	}

	public static void deletePlace(Context context, int idPlace) {
		PlacesDBHelper dbHelper = new PlacesDBHelper(context);
		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getWritableDatabase();

			// Delete place
			db.delete(PlacesDBHelper.PLACES_TABLE, 
					PlacesDBHelper.IDPLACE + "=?", 
					new String[] { String.valueOf(idPlace) });
			
			// Delete its cells
			deleteCells(context, idPlace);
			
			Log.i(TAG_LOGGER, "Place deleted");
		} finally {
			// Always close the dbHelper
			dbHelper.close();
		}
	}
	
	public static void deleteCell(Context context, int idPlace, int idCell) {
		PlacesDBHelper dbHelper = new PlacesDBHelper(context);
		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getWritableDatabase();

			// Delete cells
			db.delete(PlacesDBHelper.CELLS_TABLE, 
					PlacesDBHelper.IDPLACE + "=? AND " + PlacesDBHelper.IDCELL + "=?", 
					new String[] { String.valueOf(idPlace), String.valueOf(idCell) });
			Log.i(TAG_LOGGER, "Cell deleted");
		} finally {
			// Always close the dbHelper
			dbHelper.close();
		}
	}
	
	public static void deleteCells(Context context, int idPlace) {
		PlacesDBHelper dbHelper = new PlacesDBHelper(context);
		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getWritableDatabase();

			// Delete cells
			db.delete(PlacesDBHelper.CELLS_TABLE, 
					PlacesDBHelper.IDPLACE + "=?", new String[] { String.valueOf(idPlace) });
			Log.i(TAG_LOGGER, "Cells deleted");
		} finally {
			// Always close the dbHelper
			dbHelper.close();
		}
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

			Log.i(TAG_LOGGER, "Place updated");
		} finally {
			// Always close the dbHelper
			dbHelper.close();
		}
	}

	public static void addPlaces(Context context, List<Place> places) {
		// Get the helper
		PlacesDBHelper dbHelper = new PlacesDBHelper(context);
		// Clear the list
		places.clear();

		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Log.d(TAG_LOGGER, "Database obtained");

			String orderBy = PlacesDBHelper.PRIORITY;
			Cursor cursor = db.query(PlacesDBHelper.PLACES_TABLE, null, null,
					null, null, null, orderBy);
			Log.d(TAG_LOGGER, "Query for places executed");

			// Add fake place for creating
			places.add(new Place(0, context.getString(R.string.new_place)));

			// Rest from db
			addPlacesFromCursor(places, cursor);
			Log.d(TAG_LOGGER, "Places added to list");

		} finally {
			// Always close the helper
			dbHelper.close();
		}
		Log.i(TAG_LOGGER, places.size() + " places from db");
	}

	private static void addPlacesFromCursor(List<Place> places, Cursor cursor) {
		while (cursor.moveToNext()) {
			// Add the place
			places.add(new Place(cursor.getInt(0), cursor.getString(1)));
		}
	}
	
	private static void addCellsFromCursor(List<CellInfo> cells, Cursor cursor) {

		while (cursor.moveToNext()) {
			// Add the place
			cells.add(new CellInfo(cursor.getInt(2), cursor.getInt(3), cursor.getString(4)));
		}
	}

	public static Place getPlace(Context context, int idPlace) {
		// Get the helper
		PlacesDBHelper dbHelper = new PlacesDBHelper(context);

		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Log.d(TAG_LOGGER, "Database obtained");

			// Execute the query
			Cursor cursor = db.query(PlacesDBHelper.PLACES_TABLE, null,
					PlacesDBHelper.IDPLACE + " = " + idPlace, null, null, null,
					null);
			Log.d(TAG_LOGGER, "Query for place executed");

			// Extract the results
			if (cursor.moveToNext()) {
				Log.d(TAG_LOGGER, "Place returned");
				return new Place(cursor.getInt(0), cursor.getString(1));
			}
		} finally {
			// Always close the subjectsData
			dbHelper.close();
		}
		return null;
	}
	
	public static Place detectPlace(Context context, int idCell) {
		// Get the helper
		PlacesDBHelper dbHelper = new PlacesDBHelper(context);

		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Log.d(TAG_LOGGER, "Database obtained");

			// Execute the query
			Cursor cursor = db.rawQuery("SELECT places.* FROM places, cells "
					+ "WHERE places.idplace = cells.idplace AND idcell=? ORDER BY priority LIMIT 1", 
					new String[] { String.valueOf(idCell) });
			Log.d(TAG_LOGGER, "Query for detect place executed");

			// Extract the results
			if (cursor.moveToNext()) {
				Log.d(TAG_LOGGER, "Place returned");
				return new Place(cursor.getInt(0), cursor.getString(1));
			}
		} finally {
			// Always close the subjectsData
			dbHelper.close();
		}
		return null;
	}
}
