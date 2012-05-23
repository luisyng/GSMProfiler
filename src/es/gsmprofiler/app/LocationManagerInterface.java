package es.gsmprofiler.app;

import es.gsmprofiler.entities.Place;

public interface LocationManagerInterface {
	public void setLearnPlace(Place place);

	public Place getLearnPlace();

	public Place getDetectPlace();

	public boolean isLearning();

	public boolean isDetecting();

	public void startLearning();

	public void stopLearning();

	public void startDetecting();

	public void stopDetecting();

	public void forceCheckDetection();
}
