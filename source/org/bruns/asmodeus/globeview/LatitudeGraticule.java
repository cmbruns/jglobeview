/*
 * Copyright (c) 2005, Christopher Bruns. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions
 * are met: 
 *  - Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer. 
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution. 
 *  - Neither the name of Christopher Bruns nor the names other 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE. 
 */
//
//  LatitudeGraticule.java
//  globeview
//
//  Created by Christopher Bruns on 2/17/05.
//  Copyright 2005 Christopher Bruns. All rights reserved.
//
//
// $Id$
// $Header$
// $Log$
// Revision 1.4  2005/03/14 05:06:57  cmbruns
// Changed autocreated copyright text from __MyCompanyName__ to Christopher Bruns
//
// Revision 1.3  2005/03/13 22:06:32  cmbruns
// Minor comment change
//
// Revision 1.2  2005/03/01 02:13:14  cmbruns
// added cvs headers
//
//

package org.bruns.asmodeus.globeview;

import org.bruns.asmodeus.globeview.*;
import java.util.*;
import java.awt.*;

public class LatitudeGraticule 
extends GeoCollection
{
	LatitudeGraticule(
					  double graticuleInterval,
					  GeoObject resolutionObject
					   ) {

		if (graticuleInterval < 0) graticuleInterval = -graticuleInterval;
		
		double graticuleStep = 0.100;
		if (resolutionObject.minResolution > 0.020) { // hack
			graticuleStep = 0.020;
		}
			
		// TODO - resolution limits do not work well nor hierarchically now
		setResolution(resolutionObject);
		Color graticuleColor = new Color(30,30,150);
		Color equatorColor = new Color(160,50,50);

		// Create graticule objects
		GeoPath path;
		double latitude, longitude;
		
		// Break everything into 30 degree segments
		double graticuleSegmentSize = Math.PI / 2.9999;

		// Equator
		latitude = 0;
		double maxAngle = 2.0 * Math.PI;
		for (double segmentStart = 0; 
			 segmentStart <= maxAngle;
			 segmentStart += graticuleSegmentSize) {
			double segmentEnd = segmentStart + graticuleSegmentSize;
			if (segmentEnd > maxAngle) segmentEnd = maxAngle;
			 
			path = new GeoPath();
			for (longitude = segmentStart; 
				 longitude <= segmentEnd;
				 longitude += graticuleStep) {
				if (longitude > (segmentEnd - graticuleStep)) 
					longitude = segmentEnd; // neaten end
				path.addPoint(longitude, latitude);
			}
			path.setColor(equatorColor);
			path.setResolution(resolutionObject);
			addElement(path);
		}
				
		// Other Parallels
		//  Northern hemisphere
		// How many latitude lines between equator and north pole?
		int latitudeCount = 0;
		for (latitude = graticuleInterval;
			 latitude < (Math.PI/2.0 - graticuleInterval/2);
			 latitude += graticuleInterval) {

			latitudeCount ++;

			for (double segmentStart = 0; 
				 segmentStart <= maxAngle;
				 segmentStart += graticuleSegmentSize) {
				double segmentEnd = segmentStart + graticuleSegmentSize;
				if (segmentEnd > maxAngle) segmentEnd = maxAngle;
				
				path = new GeoPath();
				for (longitude = segmentStart; 
					 longitude <= segmentEnd;
					 longitude += graticuleStep) {
					if (longitude > (segmentEnd - graticuleStep)) 
						longitude = segmentEnd; // neaten end
					path.addPoint(longitude, latitude);
				}
				path.setColor(graticuleColor);
				path.setResolution(resolutionObject);
				addElement(path);
			}
		}
		//  Southern hemisphere
		for (latitude = -graticuleInterval;
			 latitude > (-Math.PI/2.0 + graticuleInterval/2);
			 latitude -= graticuleInterval) {
			path = new GeoPath();
			for (double segmentStart = 0; 
				 segmentStart <= maxAngle;
				 segmentStart += graticuleSegmentSize) {
				double segmentEnd = segmentStart + graticuleSegmentSize;
				if (segmentEnd > maxAngle) segmentEnd = maxAngle;
				
				path = new GeoPath();
				for (longitude = segmentStart; 
					 longitude <= segmentEnd;
					 longitude += graticuleStep) {
					if (longitude > (segmentEnd - graticuleStep)) 
						longitude = segmentEnd; // neaten end
					path.addPoint(longitude, latitude);
				}
				path.setColor(graticuleColor);
				path.setResolution(resolutionObject);
				addElement(path);
			}
		}
		
		// Meridians (including the prime meridian)
		for (longitude = 0;
			 longitude <= (2 * Math.PI - (graticuleInterval / 2));
			 longitude += graticuleInterval) {

			// What is the latitude of the highest latitude line?
			double maxLatitude = latitudeCount * graticuleInterval;
			double minLatitude = -maxLatitude;
			
			// Only two meridians go all the way to the pole
			double poleMeridians = Math.PI / 2.0; // go to pole every 90 degrees
			double longSection = longitude / poleMeridians; // Is this close to an integer?
			double sectionOffset = longSection - (int) longSection;
			while (sectionOffset > 0.5) sectionOffset -= 1.0;
			while (sectionOffset < -0.5) sectionOffset += 1.0;
			if (sectionOffset < 0) sectionOffset = -sectionOffset; // Make positive
			sectionOffset *= poleMeridians; // convert back to radians
			if (sectionOffset < (graticuleInterval / 2.0)) {
				// Go to poles
				maxLatitude = Math.PI / 2.0;
				minLatitude = -Math.PI / 2.0;
			}
			// System.out.println("min latitude = " + minLatitude * 180/Math.PI);
			// System.out.println("max latitude = " + maxLatitude * 180/Math.PI);

			// North half
			path = new GeoPath();
			for (latitude = 0; 
				 latitude < maxLatitude;
				 latitude += graticuleStep) {
				if (latitude > (maxLatitude - graticuleStep)) latitude = maxLatitude; // neaten end
				path.addPoint(longitude, latitude);
			}
			path.setColor(graticuleColor);
			path.setResolution(resolutionObject);
			addElement(path);

			// South half
			path = new GeoPath();
			for (latitude = 0; 
				 latitude > minLatitude; 
				 latitude -= graticuleStep) {
				if (latitude < (minLatitude + graticuleStep)) latitude = minLatitude; // neaten end
				path.addPoint(longitude, latitude);
			}
			path.setColor(graticuleColor);
			path.setResolution(resolutionObject);
			addElement(path);
		}
	}
}
