package de.tucottbus.kt.lcars.swt;

import java.util.ArrayList;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.jfree.experimental.swt.SWTUtils;

import de.tucottbus.kt.lcars.PanelState;
import de.tucottbus.kt.lcars.elements.ElementData;
import de.tucottbus.kt.lcars.j2d.ElementState;
import de.tucottbus.kt.lcars.logging.Log;

public abstract class ElementDataComposite extends Composite
    implements PaintListener
{
  ElementData ed;
  PanelState ps;

  private final Display display;

  public ElementDataComposite(Composite parent, int style)
  {
    super(parent, style);
    display = parent.getDisplay();
    addPaintListener(this);
  }

  @Override
  public void paintControl(PaintEvent e)
  {
    ElementData elData = ed;
    PanelState pState = ps;
    if (elData == null)
      return;
    assert(pState != null);

    GC gc = e.gc;

    org.eclipse.swt.graphics.Rectangle bnds = getBounds();
    Transform t = getTransform(display, -bnds.x, -bnds.y);
    gc.setTransform(t);
    t.dispose();

    // TODO: Add global transform
    elData.render2D(gc, pState);
    // gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_RED));
    // gc.fillRectangle(bnds);
  }

  public void applyUpdate(ElementData elementData, PanelState panelState)
  {
    ArrayList<Runnable> updateQueue = new ArrayList<Runnable>(3);
    applyUpdate(elementData, panelState, updateQueue);
    display.asyncExec(() -> {
      updateQueue.forEach((update) -> {
        update.run();
      });
    });
  }

  public void applyUpdate(ElementData elementData, PanelState panelState,
      ArrayList<Runnable> updateQueue)
  {
    assert(elementData != null) : "elementData != null";
    assert(ed == null || elementData.serialNo == ed.serialNo);

    if (elementData.serialNo == 25)
      assert(elementData == null || elementData.serialNo == ed.serialNo);
    // update date and get update flags
    int u = 0;
    if (elementData != null)
    {
      u |= elementData.applyUpdate(ed, true);
      ed = elementData;
    }
    if (panelState != null)
    {
      u |= (ps != null ? panelState.getUpdateFlags(ps) : PanelState.FLAG_MASK);
      ps = panelState;
    }

    if (ed.serialNo == 25)
      Log.debug("#" + ed.serialNo + " u = " + u);

    boolean visible = elementData.state.isVisible();

    if ((u & ElementState.VISIBLE) != 0)
      updateQueue.add(() -> {
        setVisible(visible);
      });
    
    if ((u & (ElementData.GEOMETRY_FLAG | ElementState.BOUNDS)) != 0) 
    // bounds update check
    {
      Rectangle bnds = SWTUtils.toSwtRectangle(elementData.getBounds());
      updateQueue.add(() -> {
        setBounds(bnds);
      });
    }

    if (u != 0 && visible) // redraw check
      updateQueue.add(() -> {
        redraw();
      });
  }

  public void clear()
  {
    ed = null;
    ps = null;
  }

  @Override
  public void setVisible(boolean visible)
  {
    if (visible == getVisible())
      return;
    super.setVisible(visible);
    if (ed != null)
      ed.onVisibilityChanged(visible);
  }

  @Override
  public String toString()
  {
    return ElementDataComposite.class.getSimpleName() + "#" + ed.serialNo;
  }

  protected abstract Transform getTransform(Device device, int dx, int dy);
}
