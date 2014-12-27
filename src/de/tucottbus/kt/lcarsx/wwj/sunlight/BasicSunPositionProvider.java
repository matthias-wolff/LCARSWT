package de.tucottbus.kt.lcarsx.wwj.sunlight;

import gov.nasa.worldwind.geom.LatLon;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author Michael de Hoog
 * @version $Id$
 */
public class BasicSunPositionProvider implements SunPositionProvider
{
	private LatLon position;
	private Calendar calendar;

	public BasicSunPositionProvider()
	{
		calendar = new GregorianCalendar();
		updatePosition();

		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				while (true)
				{
					try
					{
						Thread.sleep(60000);
					}
					catch (InterruptedException ignore)
					{
					}
					TimeZone timeZone = TimeZone.getDefault();
					calendar.setTimeInMillis(System.currentTimeMillis()+timeZone.getDSTSavings());
					updatePosition();
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	private synchronized void updatePosition()
	{
		//position = SunCalculator.subsolarPoint(calendar);
		position = AdvancedSunCalculator.subsolarPoint(calendar);		
	}

	public synchronized LatLon getPosition()
	{
		return position;
	}
	
	public synchronized Calendar getCalendar()
	{
		return calendar;
	}
}
