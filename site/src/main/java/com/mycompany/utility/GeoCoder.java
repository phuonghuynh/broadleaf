package com.mycompany.utility;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.Geometry;
import com.google.maps.model.LatLng;

public class GeoCoder {
	
	public static LatLng getGeometry(String postcode) {
		GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyAhusK_WJtzAkNyBE4FBSAwesamPMr0cSw");
		GeocodingResult[] results = null;
		try {
			results = GeocodingApi.geocode(context, postcode).await();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Geometry geometry = results[0].geometry;
		LatLng latlong = geometry.location;
		return latlong;
	}
	
	/**
	 * Calculates the distance in km between two lat/long points
	 * using the haversine formula
	 */
	public static double haversine(
	        double lat1, double lng1, double lat2, double lng2) {
	    int r = 6371; // average radius of the earth in km
	    double dLat = Math.toRadians(lat2 - lat1);
	    double dLon = Math.toRadians(lng2 - lng1);
	    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
	       Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) 
	      * Math.sin(dLon / 2) * Math.sin(dLon / 2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	    double d = r * c;
	    return d;
	}
}