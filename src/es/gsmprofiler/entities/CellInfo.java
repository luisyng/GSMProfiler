package es.gsmprofiler.entities;


import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * GSM Cell information
 * 
 * @author luis
 */
public class CellInfo {
	private int id;
	private int areaCode;
	private String timestamp;
	
	public CellInfo(int id, int areaCode, String timestamp) {
		this.id = id;
		this.areaCode = areaCode;
		this.timestamp = timestamp;
	}
	
	public CellInfo(int id, int areaCode) {
		this(id, areaCode, new SimpleDateFormat("dd-MM-yyyy, HH:mm:ss").format(new Date()));
	}

	public int getId() {
		return id;
	}

	public int getAreaCode() {
		return areaCode;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
}
