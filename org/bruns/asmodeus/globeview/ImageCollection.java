//
//  ImageCollection.java
//  globeview
//
//  Created by Christopher Bruns on 3/2/05.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//
//  $Id$
//  $Header$
//  $Log$
//  Revision 1.3  2005/03/13 22:06:03  cmbruns
//  Changes to keep track of which images are most recently used, and to delete the stalest ones.
//  Changes to blur the pixel boundaries at hyper zoomed zoom levels.
//
//  Revision 1.2  2005/03/11 00:10:30  cmbruns
//  Added printline about image count for debugging memory
//  Removed reference to MapBlitter.rawImage in check
//
//  Revision 1.1  2005/03/05 00:08:58  cmbruns
//  New data structure for painting multiple satellite images.
//  Incorporates the loop the goes over all canvas pixels,
//  which used to be in GeoCanvas, and also used to be in
//  MapBlitter.
//
//
// A set of bitmap images
package org.bruns.asmodeus.globeview;

import java.util.*; // Date
import java.text.*; // SimpleDateFormat
import java.awt.*;
import org.bruns.asmodeus.globeview.*;

public class ImageCollection extends GeoCollection {

	GeoCanvas canvas;
	double twilightWidth = 0.03; // size of the twilight zone, in radians
	volatile boolean keepDrawing = true;
		
	ImageCollection(GeoCanvas geoCanvas) {
		canvas = geoCanvas;
	}
	
	// Delete some of the image information to conserve memory
	int maxImagesToKeep = 15;  // Delete images if there are more than this
	int minImagesToKeep = 5; // Do not delete images if there are less than this
	void flushSomeImages() {
		// consider destroying some images

		Vector sortedImages = sortImagesByTime(); // Oldest images are first
		int activeImageCount = sortedImages.size();
		// System.out.println(""+activeImageCount+" images with data loaded");
		// if (activeImageCount <= minImagesToKeep) return;

		long currentTime = (new Date()).getTime();
		for (int i = 0; i < sortedImages.size(); i++) {
			MapBlitter image = (MapBlitter) sortedImages.elementAt(i);
			double secondsSinceUse = (currentTime - image.timeOfPreviousUse) / 1000.0;
			// System.out.println("  Time of last use = "+secondsSinceUse);
			// if ((sortedImages.size() - i) <= minImagesToKeep) return; // Remaining images are keepers
			if ((sortedImages.size() - i) > maxImagesToKeep) { // Over limit - definitely remove
				image.pixelArray = null;
				continue;
			}
			// More rules for intermediate images that are on probation
			if (secondsSinceUse > 1200) { // Unused for 20 minutes
				image.pixelArray = null;
				continue;
			}
		}
	}

	// Return non-null images sorted by time of last use - oldest first
	Vector sortImagesByTime() {
		Vector images = new Vector();
		for (int i = 0; i < subObject.size(); i++) {
			MapBlitter image = (MapBlitter) subObject.elementAt(i);
			if (image.pixelArray != null) {
				images.addElement(image);
			}
		}
		return quickSortImages(images);
	}
	
	Vector quickSortImages(Vector images) {
		if (images.size() <= 1) return images;

		Vector greaterVector = new Vector();
		Vector equalVector = new Vector();
		Vector lessVector = new Vector();

		// TODO choose pivot more randomly
		MapBlitter pivot = (MapBlitter) images.elementAt(0);

		for (int i = 0; i < images.size(); i++) {
			MapBlitter image = (MapBlitter) images.elementAt(i);
			if (image.timeOfPreviousUse == pivot.timeOfPreviousUse)
				equalVector.addElement(image);
			else if (image.timeOfPreviousUse > pivot.timeOfPreviousUse)
				greaterVector.addElement(image);
			else // (image.timeOfPreviousUse < pivot.timeOfPreviousUse)
				lessVector.addElement(image);
		}
		
		// sort subVectors
		lessVector = quickSortImages(lessVector);
		greaterVector = quickSortImages(greaterVector);
		
		// Combine into one vector
		lessVector.addAll(equalVector);
		lessVector.addAll(greaterVector);
		return lessVector;
	}
	
    // Paint the texture map, simultaneously to two destinations
    // offScreenGraphics is the off-screen image
    // onScreenGraphics is the on-screen image
    // Try for faster version -
    //  No new objects
    //  No inner loop stack variables
    //  No function calls
    int stripWidth = 10; // How often to update progress on-screen
    Vector3D sunVector;
	void paint(Graphics onScreenGraphics, GenGlobe genGlobe, Projection projection, LensRegion viewLens) 
	{
		// System.out.println("" + elementCount() + " images loaded");
		
		// This is cheating, not using arguments to pass this Graphics, but I don't want to
		// change the paint signature for all GeoObjects
		Graphics offScreenGraphics = canvas.offScreenGraphics;
		
		// Clipping
		// if (!usableResolution(genGlobe)) return;  // still needed for day night?
		// if (!boundingBox.overlaps(viewLens)) return; // still needed for day night?
		// Which bitmaps might be drawn?
		Vector usableBitmaps = new Vector();
		if (canvas.drawSatelliteImage) {
IMAGES:
			for (int i = 0; i < subObject.size(); i++) {
				MapBlitter mapBlitter = (MapBlitter) subObject.elementAt(i);
				if ((mapBlitter != null) && 
					mapBlitter.usableResolution(genGlobe) &&
					mapBlitter.overlaps(viewLens)) {
						usableBitmaps.addElement(mapBlitter);
					}
			}
		}
		
		
		if (canvas.dayNight) {
			// Measure elapsed time
			// Date t1 = new Date();
			// sunVector = getSunVector2();
			Date currentDate = new Date();
			// try {
				SimpleDateFormat dateFormat = 
				new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a z");
				// currentDate = dateFormat.parse("01-Jun-2008 12:00:00 PM GMT");
				// currentDate = dateFormat.parse("09-May-2003 07:05:02 PM PST");	  
			// } catch (Exception e) {}
			sunVector = Sun.getVector(currentDate);
		}
		
		Vector3D tempv1 = new Vector3D(); // This is NEVER null
		Vector3D v1 = tempv1; // This may be null
		
		Color color;
		int pixel, darkPixel, shadePixel;
		int width = canvas.memOffScreenImage.getWidth(null);
		int height = canvas.memOffScreenImage.getHeight(null);
		
		int x, y; // Actual screen coordinates
		double cx, cy; // Scaled, translated screen coordinates
		
		int tx, ty; // Coordinates of texture map pixel
		
		Dimension d = canvas.getSize();
		int xMax = d.width;
		int yMax = d.height;
		
		double r = genGlobe.getPixelRadius();
		double rInv = 1.0 / r;
		
		double lambda, phi; // longitude, latitude
		
		int mapIndex;
		
		double sinSunElevation = 0;
		// "civil twilight" is six degrees below horizon (0.2 radians)
		double alpha; // Day/night ratio for twilight zone
		double fakeAngle;
		
		// keep track of which Bitmaps are actually used
		boolean[] usedBitmaps = new boolean[usableBitmaps.size()]; // initializes to false
		
		// For computing blur on hyper zoomed images
		boolean previousPlot = false; // Was the pixel above drawn from bitmap?
		Vector3D previousSpot = new Vector3D();

		for (x = 0; x < xMax; ++x) {
			cx = (x - genGlobe.centerX) * rInv;
			previousPlot = false;
			for (y = 0; y < yMax; ++y) {
				mapIndex = x + y * width;
				
				canvas.offScreenPixels[mapIndex] = 0xFF000000; // set to black?
				
				cy = (genGlobe.centerY - y) * rInv;
				
				v1 = projection.vec2DTo3D(cx, cy, tempv1);
				if (v1 == null) {
					previousPlot = false;
					continue; // Clipping (function failed)
				}
				
				// Oblique transform (centers globe on point of interest)
				v1 = genGlobe.unrotate(v1);

				// Might want to blur hyper zoomed pixels
				// Approximate angular distance between pixels
				double pixelSize = 0;
				if (previousPlot) pixelSize = (v1.minus(previousSpot)).length();
				previousSpot.copy(v1);
				
				pixel = 0;				
				// Look for pixel in each bitmap, starting with highest resolution
IMAGES:
				for (int i = usableBitmaps.size() - 1; i >= 0; i--) {
					MapBlitter mapBlitter = (MapBlitter) usableBitmaps.elementAt(i);
					try {
						pixel = mapBlitter.getPixel(v1, pixelSize);
					} catch (InterruptedException e) {
						return;
					}
					if (pixel != 0) {
						usedBitmaps[i] = true;
						break IMAGES;
					}
				}
				
				if (pixel == 0) {
					pixel = canvas.oceanColorInt;
					previousPlot = false;
				}
				else
					previousPlot = true;
				
				// Check for day/night
				if (canvas.dayNight) {
					sinSunElevation = 
					v1.x() * sunVector.x() +
					v1.y() * sunVector.y() +
					v1.z() * sunVector.z();
					// Make it bright everywhere sun shines 
					// (i.e. twilight is all in shadow)
					// if (sinSunElevation < twilightWidth ) { // not quite day
					if (sinSunElevation < 0) { // not quite day
						darkPixel = pixel >> 1; // Shift to darken
						darkPixel = darkPixel & 0xFF7F7F7F; // High bit zero in each color
						darkPixel = darkPixel | 0xFF000000; // Restore opacity
						
						if (sinSunElevation < -twilightWidth) { // definitely night
							pixel = darkPixel;
						}
						else { 
							// in twilight zone
							// Make Twilight Zone really smooth by using SINE function
							// fakeAngle = (sinSunElevation / twilightWidth)  * (Math.PI / 2.0);
							fakeAngle = (2*(sinSunElevation + twilightWidth/2)/ twilightWidth)  * (Math.PI / 2.0);
							alpha = (Math.sin(fakeAngle) + 1.0) / 2.0;
							// alpha = (sinSunElevation + twilightWidth) /
							//     (2.0 * twilightWidth);
							if (alpha > 1.0) alpha = 1.0;
							if (alpha < 0.0) alpha = 0.0;
							
							// Red
							shadePixel = (int) ((1.0 - alpha) * (darkPixel & 0x00FF0000) + 
												alpha * (pixel & 0x00FF0000));
							shadePixel = shadePixel & 0x00FF0000;
							pixel = pixel & 0xFF00FFFF; // clear red
							pixel = pixel + shadePixel; // add shaded red
							
							// Green
							shadePixel = (int) ((1.0 - alpha) * (darkPixel & 0x0000FF00) + 
												alpha * (pixel & 0x0000FF00));
							shadePixel = shadePixel & 0x0000FF00;
							pixel = pixel & 0xFFFF00FF; // clear green
							pixel = pixel + shadePixel; // add shaded green
							
							// Blue
							shadePixel = (int) ((1.0 - alpha) * (darkPixel & 0x000000FF) + 
												alpha * (pixel & 0x000000FF));
							shadePixel = shadePixel & 0x000000FF;
							pixel = pixel & 0xFFFFFF00; // clear blue
							pixel = pixel + shadePixel; // add shaded blue
							
						}
					}
				}
				
				canvas.offScreenPixels[mapIndex] = pixel;
				if (!keepDrawing) {return;} // for very fast abortion

				}
			
			if (!keepDrawing) {return;}
			
			// When bitmap is slow, update on-screen image
			// if (((x % stripWidth) == 0) && (x > 0)){
				// onScreenGraphics.drawImage(canvas.memOffScreenImage,
				//						   x - stripWidth,0, x,yMax,
				//						   x - stripWidth,0, x,yMax,
				//						   canvas);
				// canvas.offScreenSource.newPixels(x - stripWidth,0, stripWidth,yMax);
			// }
		}
		// canvas.offScreenSource.newPixels(0,0,width,height);
		
		// Update time stamp on those bitmaps which were used in this draw
		long timeStamp = (new Date()).getTime();
		for (int i = 0; i < usableBitmaps.size(); i++) {
			if (usedBitmaps[i]) {
				MapBlitter mapBlitter = (MapBlitter) usableBitmaps.elementAt(i);
				mapBlitter.timeOfPreviousUse = timeStamp;
			}
		}
	}
}
