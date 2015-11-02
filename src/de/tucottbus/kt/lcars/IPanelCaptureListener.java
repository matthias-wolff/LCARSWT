package de.tucottbus.kt.lcars;

import java.awt.image.BufferedImage;
import java.util.concurrent.FutureTask;

public interface IPanelCaptureListener
{
	void onScreenUpdate(FutureTask<BufferedImage> screenCapture);
}
