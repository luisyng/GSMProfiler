package es.gsmprofiler.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * User defined place
 * 
 * @author luis
 */
public class Place {
	private int id;
	private String place;
	private List<CellInfo> cells;
	public static final String IDPLACE = "idplace";
	
	public Place(int id, String place, List<CellInfo> cells) {
		this.id = id;
		this.place = place;
		this.cells = cells;
	}
	
	public Place(int id, String place) {
		this.id = id;
		this.place = place;
		this.cells = new ArrayList<CellInfo>();
	}
	
	public int getId() {
		return id;
	}
	
	public String getPlace() {
		return place;
	}
	
	public List<CellInfo> getCells() {
		return cells;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public void setCells(List<CellInfo> cells) {
		this.cells = cells;
	}
}
