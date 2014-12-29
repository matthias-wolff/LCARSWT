/*
 * Copyright © 2014, Terramenta. All rights reserved.
 *
 * This work is subject to the terms of either
 * the GNU General Public License Version 3 ("GPL") or 
 * the Common Development and Distribution License("CDDL") (collectively, the "License").
 * You may not use this work except in compliance with the License.
 * 
 * You can obtain a copy of the License at
 * http://opensource.org/licenses/CDDL-1.0
 * http://opensource.org/licenses/GPL-3.0
 */
package gov.nasa.worldwindx.sunlight;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 *
 * @author heidtmare
 */
public class SunLayer extends LensFlareLayer {

    /**
     *
     */
    public SunLayer() {
        setName("Sun");
        setPickEnabled(false);

        BufferedImage sunDisk = createHaloImage(64, new Color(1f, 1f, .8f), 2f);
        BufferedImage disk = createDiskImage(128, Color.WHITE);
        BufferedImage star = createStarImage(128, Color.WHITE);
        BufferedImage halo = createHaloImage(128, Color.WHITE);
        BufferedImage rainbow = createRainbowImage(128);
        BufferedImage rays = createRaysImage(128, 12, Color.WHITE);

        ArrayList<FlareImage> flares = new ArrayList<FlareImage>();
        flares.add(new FlareImage(rays, 4, 0, .05));
        flares.add(new FlareImage(star, 1.4, 0, .1));
        flares.add(new FlareImage(star, 2.5, 0, .04));
        flares.add(new FlareImage(sunDisk, .6, 0, .9));
        flares.add(new FlareImage(halo, 1.0, 0, .9));
        flares.add(new FlareImage(halo, 4, 0, .9));
        flares.add(new FlareImage(rainbow, 2.2, 0, .03));
        flares.add(new FlareImage(rainbow, 1.2, 0, .04));
        flares.add(new FlareImage(disk, .1, .4, .1));
        flares.add(new FlareImage(disk, .15, .6, .1));
        flares.add(new FlareImage(disk, .2, .7, .1));
        flares.add(new FlareImage(disk, .5, 1.1, .2));
        flares.add(new FlareImage(disk, .2, 1.3, .1));
        flares.add(new FlareImage(disk, .1, 1.4, .05));
        flares.add(new FlareImage(disk, .1, 1.5, .1));
        flares.add(new FlareImage(disk, .1, 1.6, .1));
        flares.add(new FlareImage(disk, .2, 1.65, .1));
        flares.add(new FlareImage(disk, .12, 1.71, .1));
        flares.add(new FlareImage(disk, 3, 2.2, .05));
        flares.add(new FlareImage(disk, .5, 2.4, .2));
        flares.add(new FlareImage(disk, .7, 2.6, .1));
        flares.add(new FlareImage(rainbow, 5, 3.0, .03));
        flares.add(new FlareImage(disk, .2, 3.5, .1));
        this.addRenderables(flares);
    }
}
