//
//  HelpDialog.java
//  globeview
//
//  Created by Christopher Bruns on 3/4/05.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//
//  $Id$
//  $Header$
//  $Log$
//  Revision 1.1  2005/03/11 00:08:58  cmbruns
//  New help dialog when user clicks Help->Globeview Help
//

package org.bruns.asmodeus.globeview;

import org.bruns.asmodeus.globeview.*;
import java.awt.*;

public class HelpDialog extends InfoDialog {
	HelpDialog(Frame frame) {
		super(frame, "GlobeView Help", false); // not modal dialog
		
		addLine("Using GlobeView:");
		addLine("  CLICK the mouse to center on something");
		addLine("  DRAG the mouse to pan up/down and left/right");
		addLine("  SHIFT-DRAG (or far-click) to zoom in and out");
		addLine(" ");		
		addLine("Zoom out until you can see most of the earth.");
		addLine("Then experiment with the different options in the PROJECTION menu");
		addLine(" ");		
		addLine("For more information:");
		addLine("  READ http://bruns.homeip.net/~bruns/globeview.html");
		addLine("  CONTACT Chris Bruns cmbruns@comcast.net");
		
		setUndecorated(false);  // So we can drag it around

		finalizeDialog();
	}
}
