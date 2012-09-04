package com.triplaud;

import com.google.android.maps.GeoPoint;

/**
 * @author pradeep
 * A class to remember two points for every friend, the starting location and the current location.
 *
 */
public class TrackerPoint {
	private GeoPoint initialPoint;
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public GeoPoint getInitialPoint() {
		return initialPoint;
	}
	public void setInitialPoint(GeoPoint initialPoint) {
		this.initialPoint = initialPoint;
	}
	public GeoPoint getCurrentPoint() {
		return currentPoint;
	}
	public void setCurrentPoint(GeoPoint currentPoint) {
		this.currentPoint = currentPoint;
	}
	private GeoPoint currentPoint;
	
	

}
