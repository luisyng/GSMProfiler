package es.luisalbert.entities;

public class CellInfo {
	private int id;
	private int areaCode;
	
	public CellInfo(int id, int areaCode) {
		this.id = id;
		this.areaCode = areaCode;
	}

	public int getId() {
		return id;
	}

	public int getAreaCode() {
		return areaCode;
	}
}
