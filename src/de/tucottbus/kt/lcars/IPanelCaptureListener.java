package de.tucottbus.kt.lcars;

import java.awt.image.BufferedImage;
import java.rmi.Remote;
import java.util.concurrent.FutureTask;

public interface IPanelCaptureListener extends Remote
{
	void onScreenUpdate(FutureTask<BufferedImage> screenCapture);
}
