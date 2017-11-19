package de.tucottbus.kt.lcars;

/**
 * Interface for listeners to panel timer events.
 * 
 * @author Matthias Wolff
 */
public interface IPanelTimerListener
{

  /**
   * Called 25 times per second. Derived classes may override this method to
   * perform periodic actions. It is <em>not</em> recommended to start own
   * threads for that purpose.
   * 
   * @see Panel#fps25()
   */
  public void fps25();

  /**
   * Called 10 times per second. Derived classes may override this method to
   * perform periodic actions. It is <em>not</em> recommended to start own
   * threads for that purpose.
   * 
   * @see Panel#fps10()
   */
  public void fps10();

  /**
   * Called twice per second. Derived classes may override this method to
   * perform periodic actions. It is <em>not</em> recommended to start own
   * threads for that purpose.
   * 
   * @see Panel#fps2()
   */
  public void fps2();

  /**
   * Called once per second. Derived classes may override this method to perform
   * periodic actions. It is <em>not</em> recommended to start own threads for
   * that purpose.
   * 
   * @see Panel#fps1()
   */
  public void fps1();

}

// EOF
