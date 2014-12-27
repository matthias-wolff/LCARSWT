package de.tucottbus.kt.lcarsx.wwj.sunlight;

import gov.nasa.worldwind.geom.LatLon;

/**
 * Copyright (C) 2001 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 * @author Michael de Hoog
 * @version $Id$
 */
public interface SunPositionProvider
{
	public LatLon getPosition();
}
