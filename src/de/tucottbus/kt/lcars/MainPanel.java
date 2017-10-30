package de.tucottbus.kt.lcars;

import de.tucottbus.kt.lcars.contributors.EPanelSelector;

/**
 * Main panels are displayed on the LCARS panel selector.
 * 
 * @see EPanelSelector
 * @author Matthias Wolff
 */
public abstract class MainPanel extends Panel
{
  /**
   * Abstract constructor of main panels.
   * 
   * @param iscreen
   *          The screen to display the panel on.
   */
  public MainPanel(IScreen screen)
  {
    super(screen);
  }
}

// EOF

