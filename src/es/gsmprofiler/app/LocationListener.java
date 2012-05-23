package es.gsmprofiler.app;

import es.gsmprofiler.entities.CellInfo;
import es.gsmprofiler.entities.Place;

public interface LocationListener {
	public void onCellLearnt(CellInfo cell);
	public void onPlaceDetected(Place place);
	
}
