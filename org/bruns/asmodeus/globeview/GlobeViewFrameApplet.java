//
// $Id$
// $Header$
// $Log$
// Revision 1.3  2005/03/02 01:55:11  cmbruns
// Added loading of new ParameterFile
// Improved thrown error checking
// Converted projection parameter to lower case before checking
//
// Revision 1.2  2005/03/01 02:13:14  cmbruns
// added cvs headers
//
//

package org.bruns.asmodeus.globeview;

import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import org.bruns.asmodeus.globeview.*;
import java.net.*; // URL
import java.lang.reflect.*; // Field

public class GlobeViewFrameApplet extends Applet 
    implements ActionListener
{
    GlobeView frame;
    Button button;

    Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    Cursor moveCursor = new Cursor(Cursor.MOVE_CURSOR);
    Cursor crosshairCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
    Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);

    public void init() {
        // Avoid default gray behind launch button
        Color buttonBackgroundColor = stringToColor(getParameter("button_background"));
        if (buttonBackgroundColor != null) setBackground(buttonBackgroundColor);
        else setBackground(Color.white);

	button = new Button("Launch GlobeView");
	button.setActionCommand("Launch GlobeView");
	button.addActionListener(this);
	add(button);
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand() == "Launch GlobeView") {
	    // frame.pack();  // Doesn't help IE get the initial size
	    if (frame == null) {

		// Shut down the button
		button.setLabel("Loading Data...");
		setCursor(waitCursor);
		button.setCursor(waitCursor);
		button.removeActionListener(this);

		// Create a whole new Application
		URL parameterURL = null;
		URL mapURL = null;
		URL siteURL = null;
		URL borderURL = null;

		String parameterString = getParameter("parameters");
		if (parameterString != null) {
			try {parameterURL = new URL(parameterString);
			} catch (java.net.MalformedURLException ex) {
				System.out.println("Problem with URL for parameters. " + 
								   ex +
								   "  URL: " + parameterURL);
				parameterURL = null;
			}
		}
		
		String sitesString = getParameter("siteLabels");
		if (sitesString != null) {
			try {siteURL = new URL(sitesString);
			} catch (java.net.MalformedURLException ex) {
				System.out.println("Problem with URL for site labels. " + 
								   ex +
								   "  URL: " + siteURL);
				siteURL = null;
			}
		}
		
		String imageString = getParameter("imagemap");
		if (imageString != null) {
			try {mapURL = new URL(getParameter("imagemap"));
			} catch (java.net.MalformedURLException ex) {
				System.out.println("Problem with URL for satelite map. " + 
								   ex +
								   "  URL: " + mapURL);
				mapURL = null;
			}
		}
		
		String borderString = getParameter("boundaries");
		if (borderString != null) {
			try {borderURL = new URL(borderString);
			} catch (java.net.MalformedURLException ex) {
				System.out.println("Problem with URL for boundaries. " + 
								   ex +
								   "  URL: " + borderURL);
				borderURL = null;
			}
		}
		
		frame = new GlobeView(mapURL, siteURL, borderURL, parameterURL);

		    // Check for projection option
                    // TODO - this is causing some kind of permission error
                    // frame.setProjection(Projection.AZIMUTHALEQUIDISTANT);
  		    String projectionName = getParameter("projection");
			if (projectionName != null) {
				projectionName = projectionName.toLowerCase(); // Convert to lower case
				if (projectionName.equals("azimuthal equidistant"))
					frame.setProjection(Projection.AZIMUTHALEQUIDISTANT);
				else if (projectionName.equals("orthographic")) 
					frame.setProjection(Projection.ORTHOGRAPHIC);
				else if (projectionName.equals("azimuthal equal area")) 
					frame.setProjection(Projection.AZIMUTHALEQUALAREA);
				else if (projectionName.equals("mercator")) 
					frame.setProjection(Projection.MERCATOR);
				else if (projectionName.equals("perspective")) 
					frame.setProjection(Projection.PERSPECTIVE);
				else if (projectionName.equals("equirectangular")) 
					frame.setProjection(Projection.EQUIRECTANGULAR);
				else if (projectionName.equals("gnomonic")) 
					frame.setProjection(Projection.GNOMONIC);
				else if (projectionName.equals("sinusoidal")) 
					frame.setProjection(Projection.SINUSOIDAL);
				else if (projectionName.equals("stereographic"))
					frame.setProjection(Projection.STEREOGRAPHIC);
			}
			//
//		} catch (Exception exception) {
//		    System.out.println(e);
//		    setCursor(defaultCursor);
//		    button.setCursor(defaultCursor);
//		    button.setLabel("Globeview failed");
//		    System.exit(1);
//		}

		// Re-activate the button
		button.addActionListener(this);
		setCursor(defaultCursor);
		button.setCursor(defaultCursor);
		button.setLabel("Launch GlobeView");

	    }
	    frame.show();
	}
    }

    public void start() {
	if (button == null) {
	    button = new Button("Launch GlobeView");
	    button.setActionCommand("Launch GlobeView");
	    button.addActionListener(this);
	    add(button);
	}
    }

    public void stop() {
		if (frame != null) frame.hide();
		frame = null;
    }

    // Returns a Color based on 'colorName' which must be one
    // of the predefined colors in java.awt.Color.
    // Returns null if colorName is not valid.
    public Color stringToColor(String colorName) {
        try {
            // Find the field and value of colorName
            Field field = Class.forName("java.awt.Color").getField(colorName);
            return (Color)field.get(null);
        } catch (Exception e) {
            return null;
        }
    }


}