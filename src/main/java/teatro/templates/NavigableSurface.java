package teatro.templates;


import teatro.core.backstage.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JPanel;
import javax.swing.Timer;


public class NavigableSurface extends JPanel implements Surface, ActionListener
{
    // I/O:
    private SurfaceListener _listener;
    // Navigation:
    private AffineTransform Scaler = new AffineTransform();
    private AffineTransform Translator = new AffineTransform();

    //Space search label:
    AbstractSpaceMap PanelMap = null;

    // Animations:
    private teatro.core.backstage.Animator _animator;

    private int scaleAnimationCounter = 160;
    private int lastSenseX;
    private int lastSenseY;

    private Timer GUITimer;

    // Sense Focus:
    private SurfaceObject FocusObject;

    public SurfaceObject getFocusObject() {
        return FocusObject;
    }

    public void setFocusObject(SurfaceObject object) {
        FocusObject = object;
    }

    @Override
    protected boolean requestFocusInWindow(boolean temporary) {
        return super.requestFocusInWindow(temporary);
    }

    private double CenterX = getWidth() / 2;
    private double CenterY = getHeight() / 2;

    private int[] LongPress;
    private int[] Swipe;
    private int[] Click;
    private int[] DoubleClick;
    private int[] Sense;
    private double[] Scaling;
    private int[] Drag;
    private int[] Press;

    public int[] getLongPress() {
        return LongPress;
    }

    public int[] getSwipe() {
        return Swipe;
    }

    public int[] getDoubleClick() {
        return DoubleClick;
    }

    public int[] getSense() {
        return Sense;
    }

    public double[] getScaling() {
        return Scaling;
    }

    public int[] getDrag() {
        return Drag;
    }

    public void setDrag(int[] newDragging) {
        Drag = newDragging;
    }

    public void setPress(int[] newPress) {
        Press = newPress;
    }

    public void setLongPress(int[] newPress) {
        LongPress = newPress;
    }

    public int[] getPress() {
        return Press;
    }

    public void setClick(int[] newClick) {
        Click = newClick;
    }

    public void setScaling(double[] newScaling) {
        Scaling = newScaling;
    }

    public void setDoubleClick(int[] newClick) {
        DoubleClick = newClick;
    }

    public void setMovement(int[] newMove) {
        Sense = newMove;
    }

    public void setSwipe(int[] newSwipe) {
        Swipe = newSwipe;
    }

    private boolean touchMode = true;

    private boolean drawRepaintSpaces = true;
    private boolean advancedRendering = true;
    private boolean mapRendering = false;

    private long frameStart;
    private int frameDelta;

    private double fps;
    private double smoothFPS;

    public interface SurfaceAction {
        void actOn(NavigableSurface panel);
    }

    public AffineTransform getScaler() {
        return Scaler;
    }

    public AffineTransform getTranslator() {
        return Translator;
    }

    public double getCenterX() {
        return CenterX;
    }

    public double getCenterY() {
        return CenterY;
    }

    public void setCenterX(double value) {
        CenterX = value;
    }

    public void setCenterY(double value) {
        CenterY = value;
    }

    public SurfaceListener getListener() {
        return _listener;
    }

    // Settings:
    public boolean isAntialiasing() {
        return advancedRendering;
    }

    public boolean isMaprendering() {
        return mapRendering;
    }

    public boolean isClipRendering() {
        return drawRepaintSpaces;
    }

    public boolean isInTouchMode() {
        return touchMode;
    }

    private SurfaceRepaintSpace _currentFrameSpace;

    @Override
    public SurfaceRepaintSpace getCurrentFrameSpace() {
        return _currentFrameSpace;
    }

    List<ObjectPainter>[] _layers = new List[]{
            new ArrayList<ObjectPainter>(),
            new ArrayList<ObjectPainter>(),
            new ArrayList<ObjectPainter>(),
            new ArrayList<ObjectPainter>(),
            new ArrayList<ObjectPainter>(),
            new ArrayList<ObjectPainter>(),
            new ArrayList<ObjectPainter>(),
            new ArrayList<ObjectPainter>(),
            new ArrayList<ObjectPainter>(),
            new ArrayList<ObjectPainter>(),
            new ArrayList<ObjectPainter>(),
            new ArrayList<ObjectPainter>(),
            new ArrayList<ObjectPainter>(),
            new ArrayList<ObjectPainter>()
    };

    public List<ObjectPainter>[] layers() {
        return _layers;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void setPressAction(SurfaceAction action) {
        applyPress = action;
    }

    SurfaceAction applyPress = Utility.DefaultPressAction;

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void setLongPressAction(SurfaceAction action) {
        applyLongPress = action;
    }

    SurfaceAction applyLongPress = Utility.DefaultLongPressAction;

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void setDoubleClickAction(SurfaceAction action) {
        applyDoubleClick = action;
    }

    SurfaceAction applyDoubleClick = Utility.DefaultDoubleClickAction;

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void setClickAction(SurfaceAction action) {
        applyClick = action;
    }

    SurfaceAction applyClick = Utility.DefaultClickAction;

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void setScaleAction(SurfaceAction action) {
        applyScaling = action;
    }

    SurfaceAction applyScaling = Utility.DefaultScalingAction;

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void setSenseAction(SurfaceAction action) {
        applySense = action;
    }

    SurfaceAction applySense = Utility.DefaultSenseAction;

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void setSwipeAction(SurfaceAction action) {
        applySwipe = action;
    }

    SurfaceAction applySwipe = Utility.DefaultSwipeAction;

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void setDragAction(SurfaceAction action) {
        applyDrag = action;
    }

    SurfaceAction applyDrag = Utility.DefaultDragAction;

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public interface SurfacePainter {
        void actOn(NavigableSurface panel, Graphics2D brush);
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void setPaintAction(SurfacePainter action) {
        _painter = action;
    }

    SurfacePainter _painter;

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //=======================================================================================

    public NavigableSurface() {
        _painter = NavigableSurface.Utility.DefaultPainter;
        _animator = new Animator();
        _listener = new SurfaceListener(this);
        this.addMouseListener(_listener);
        this.addMouseWheelListener(_listener);
        this.addMouseMotionListener(_listener);
        setBackground(Color.black);
        repaint(0, 0, getWidth(), getHeight());
        this.scaleAt(getWidth() / 2, getHeight() / 2, 0.001);
        GUITimer = new Timer(25, this);
        GUITimer.start();
    }

    public void updateAndRedraw()
    {
        _listener.updateOn(this);
        applySwipe.actOn(this);
        applyClick.actOn(this);
        applyLongPress.actOn(this);
        applyDoubleClick.actOn(this);
        applyScaling.actOn(this);
        applySense.actOn(this);
        applyDrag.actOn(this);

        double tlx = realX(0);
        double tly = realY(0);
        double brx = tlx+((getWidth()) / (getScale()*1)) ;
        double bry = tly+((getHeight())/ (getScale()*1));
        _currentFrameSpace = new SurfaceRepaintSpace(tlx, tly, brx, bry);

        if (PanelMap != null) {
            LinkedList<SurfaceObject> killList = new LinkedList<SurfaceObject>();
            LinkedList<SurfaceObject> updateList = new LinkedList<SurfaceObject>();
            PanelMap.applyToAll(
                    (SurfaceObject thing) -> {
                        updateList.add(thing);
                        if ((thing).killable()) killList.add((thing));
                        return true;
                    }
            );
            if (killList.size() > 0) System.out.println("KILLING OCCURRED! :O");
            Surface surface = this;
            updateList.forEach((SurfaceObject thing) -> thing.updateOn(surface));
            for(int ki = 0; ki<killList.size(); ki++) PanelMap = PanelMap.removeAndUpdate(killList.get(ki));
        }

        // REPAINT:
        repaint(0, 0, getWidth(), getHeight());

        frameDelta = (int) (Math.abs((System.nanoTime() - frameStart)));
        fps = 1e9 / (((double) frameDelta));
        if (fps > 60) {
            double time = (fps - 60.0) / 4;
            try {
                if (time < 50) {
                    Thread.sleep((long) time);
                }
            } catch (Exception e) {

            }
        }
        frameDelta = (int) (Math.abs((System.nanoTime() - frameStart)));
        fps = 1e9 / (((double) frameDelta));
        smoothFPS = (fps + 12 * smoothFPS) / 13;
        frameStart = System.nanoTime();

        //if (scaleAnimationCounter > 0) {
        //    double scale = 1 / Math.pow(1 + (2 / ((double) scaleAnimationCounter + 15)), 2);
        //    this.scaleAt(getWidth() / 2, getHeight() / 2, scale);
        //    scaleAnimationCounter -= 1;
        //    repaint(0, 0, getWidth(), getHeight());
        //}

    }

    //================================================================================================================================
    public int lastSenseX() {
        return lastSenseX;
    }

    public int lastSenseY() {
        return lastSenseY;
    }

    public void setLastSenseX(int value) {
        lastSenseX = value;
    }

    public void setLastSenseY(int value) {
        lastSenseY = value;
    }

    public double realLastSenseX() {
        return realX(lastSenseX);
    }

    public double realLastSenseY() {
        return realY(lastSenseY);
    }

    //--------------------------------------------------------------------------------------------------------------------------------
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D brush = (Graphics2D) g;
        if (this.isAntialiasing())
            brush.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        else brush.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        // Surface shift:
        if ((this.getCenterY() != ((double) this.getHeight()) / 2) || (this.getCenterX() != ((double) this.getWidth()) / 2)) {
            double newCenterX = ((double) this.getWidth()) / 2;
            double newCenterY = ((double) this.getHeight()) / 2;
            this.translate((newCenterX - this.getCenterX()), (newCenterY - this.getCenterY()));
            this.setCenterX(newCenterX);
            this.setCenterY(newCenterY);
        }
        // Surface scale:
        brush.transform(this.getScaler());
        brush.transform(this.getTranslator());

        //Map visualization
        if (this.isMaprendering() && getMap() != null) getMap().paintStructure(brush);

        //================================
        _painter.actOn(this, brush);
        //================================

        //RENDERING VISUALIZATION:
        if (this.isClipRendering()) {
            brush.setColor(Color.WHITE);
            for (int lid = 0; lid < layers().length; lid++) {
                layers()[lid].forEach((space) -> space.paint(brush));
                layers()[lid] = new ArrayList<ObjectPainter>();
            }
        }
        //===
    }

    //--------------------------------------------------------------------------------------------------------------------------------
    public void movementAt(int x, int y) {
        int[] newSense = new int[2];
        newSense[0] = x;
        newSense[1] = y;
        Sense = newSense;
    }

    //--------------------------------------------------------------------------------------------------------------------------------
    private SurfaceObject find(double x, double y, boolean topMost, SurfaceObject upToException, AbstractSpaceMap.MapAction action) {
        List<SurfaceObject> List = null;
        if (PanelMap != null) List = PanelMap.findAllAt(x, y, action);
        if (List == null) return null;
        SurfaceObject best = null;
        ListIterator<SurfaceObject> Iterator = List.listIterator();
        while (Iterator.hasNext()) {
            SurfaceObject current = Iterator.next();
            if (best == null) {
                best = current;
                if (upToException != null) {
                    if (topMost) {
                        if (upToException.getLayerID() < current.getLayerID()) {
                            best = null;
                        }
                    } else {
                        if (upToException.getLayerID() > current.getLayerID()) {
                            best = null;
                        }
                    }
                }
            } else {
                if (topMost) {
                    if (upToException != null) {
                        if (current.getLayerID() > best.getLayerID() && upToException.getLayerID() > current.getLayerID()) {
                            best = current;
                        }
                    } else {
                        if ((current).getLayerID() > best.getLayerID()) {
                            best = current;
                        }
                    }
                }  else {
                    if (upToException != null) {
                        if (current.getLayerID() < best.getLayerID() && upToException.getLayerID() < current.getLayerID()) {
                            best = current;
                        }
                    } else {
                        if ((current).getLayerID() >= best.getLayerID()) {
                            best = current;
                        }
                    }
                }
            }
        }
        if (upToException != null && best != null) {
            if (best == upToException) {
                return null;
            }
        }
        return best;
    }

    //============================================================

    //--------------------------------------------------------------------------------------------------------------------------------
    public SurfaceObject findObject(double x, double y, boolean topMost, SurfaceObject upToException) {
        return find(x, y, topMost, upToException,
                (SurfaceObject element) -> {
                    if (element instanceof SurfaceObject) {
                        if ((element).hasGripAt(x, y, this)) {
                            return true;
                        }
                    }
                    return false;
                });
    }

    //--------------------------------------------------------------------------------------------------------------------------------
    public double realX(double x) {
        return ((x) / Scaler.getScaleX()) - Translator.getTranslateX();
    }

    public double realY(double y) {
        return ((y) / Scaler.getScaleY()) - Translator.getTranslateY();
    }

    public double realToOnPanelX(double x) {
        return ((x + Translator.getTranslateX()) * Scaler.getScaleX());
    }

    public double realToOnPanelY(double y) {
        return ((y + Translator.getTranslateY()) * Scaler.getScaleY());
    }

    //--------------------------------------------------------------------------------------------------------------------------------
    public void draggedBy(int[] Vector) {
        Swipe = Vector;
    }

    public void translate(double translateX, double translateY) {
        Translator.translate(translateX * 1 / Scaler.getScaleX(), translateY * 1 / Scaler.getScaleY());
        repaint(0, 0, getWidth(), getHeight());//Repaint spaces?
    }
    //--------------------------------------------------------------------------------------------------------------------------------


    //--------------------------------------------------------------------------------------------------------------------------------
    //GraphSurface interaction:
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    @Override
    public void scaleAt(int x, int y, double scaleFactor) {
        double[] newScaling = new double[3];
        newScaling[0] = x;
        newScaling[1] = y;
        newScaling[2] = scaleFactor;
        Scaling = newScaling;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    @Override
    public void clickedAt(int x, int y) {
        int[] newClick = new int[2];
        newClick[0] = x;
        newClick[1] = y;
        Click = newClick;
    }

    public int[] getClick() {
        return Click;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    @Override
    public void pressedAt(int x, int y) {
        int[] newDrag = new int[2];
        newDrag[0] = x;
        newDrag[1] = y;
        Drag = newDrag;
    }

    @Override
    public void longPressedAt(int x, int y) {
        int[] newLongPress = new int[2];
        newLongPress[0] = x;
        newLongPress[1] = y;
        LongPress = newLongPress;
    }

    @Override
    public void releasedAt(int x, int y) {
    }

    @Override
    public double getScale() {
        return Scaler.getScaleX();
    }

    @Override
    public void doubleClickedAt(int x, int y) {
        int[] newDoubleClick = new int[2];
        newDoubleClick[0] = x;
        newDoubleClick[1] = y;
        DoubleClick = newDoubleClick;
    }

    @Override
    public void draggedAt(int x, int y) {
        int[] newDrag = new int[2];
        newDrag[0] = x;
        newDrag[1] = y;
        Drag = newDrag;
    }

    @Override
    public Animator getAnimator() {
        return _animator;
    }

    @Override
    public int getFrameDelta() {
        return frameDelta;
    }

    @Override
    public AbstractSpaceMap getMap() {
        return PanelMap;
    }

    @Override
    public void setMap(AbstractSpaceMap newMap) {
        PanelMap = newMap;
    }

    public static class Utility
    {
        //-------------------------------------
        static SurfacePainter DefaultPainter =
                (surface, brush) ->
                {
                    if (surface.isMaprendering()) {
                        if (surface.getMap() != null) {
                            surface.getMap().paintStructure(brush);
                        }
                    }
                    brush.setColor(Color.GREEN);
                    brush.fillOval((int) surface.realX(surface.lastSenseX()) - 5, (int) surface.realY(surface.lastSenseY()) - 5, 10, 10);
                };
        //-------------------------------------
        static SurfaceAction DefaultPressAction =
                (surface) ->
                {
                    int[] Press = surface.getPress();
                    if (Press == null) return;
                    int x = Press[0];
                    int y = Press[1];
                    surface.setPress(null);
                };
        //-------------------------------------
        static SurfaceAction DefaultLongPressAction =
                (surface) ->
                {
                    int[] LongPress = surface.getLongPress();
                    if (LongPress == null) return;
                    int x = LongPress[0];
                    int y = LongPress[1];
                    surface.setLongPress(null);
                };
        //-------------------------------------
        static SurfaceAction DefaultClickAction =
                (surface) ->
                {
                    int[] Click = surface.getClick();
                    if (Click == null) return;
                    int x = Click[0];
                    int y = Click[1];
                    surface.getListener().setDragStart(x, y);
                    SurfaceObject found = surface.findObject(surface.realX(x), surface.realY(y), true, null);
                    if (found != null) {
                        if (found.clickedAt(surface.realX(x), surface.realY(y), surface)) {
                            found.clickedAt(x, y, surface);
                        }
                    }
                    surface.setClick(null);
                    surface.repaint(0, 0, surface.getWidth(), surface.getHeight());
                };
        //-------------------------------------
        static SurfaceAction DefaultDoubleClickAction = (surface) ->
        {
            int[] DoubleClick = surface.getDoubleClick();
            if (DoubleClick == null) return;
            int x = DoubleClick[0];
            int y = DoubleClick[1];
            SurfaceObject found = surface.findObject(surface.realX(x), surface.realY(y), true, null);
            if (found != null) {
                found.doubleClickedAt(surface.realX(x), surface.realY(y), surface);
            } else {
                //Double clicked in empty space!
            }
            surface.setDoubleClick(null);
        };
        //-------------------------------------
        static SurfaceAction DefaultScalingAction =
                (surface) ->
                {
                    if (surface.getScaling() == null) return;
                    int x = (int) surface.getScaling()[0];
                    int y = (int) surface.getScaling()[1];
                    double scaleFactor = surface.getScaling()[2];
                    surface.setLastSenseX(x);
                    surface.setLastSenseY(y);
                    surface.translate(-x, -y);
                    surface.getScaler().scale((scaleFactor), (scaleFactor));
                    surface.translate(x, y);
                    surface.setScaling(null);
                    surface.repaint(0, 0, surface.getWidth(), surface.getHeight());
                };
        //-------------------------------------
        static SurfaceAction DefaultSenseAction =
                (surface) ->
                {
                    int[] Sense = surface.getSense();
                    if (Sense == null) return;
                    int x = Sense[0];
                    int y = Sense[1];
                    surface.setMovement(null);
                    double realX = surface.realX(x);
                    double realY = surface.realY(y);
                    if (surface.getFocusObject() != null) surface.getFocusObject().movementAt(realX, realY, surface);
                    surface.setFocusObject(surface.findObject(realX, realY, true, null));
                    surface.setLastSenseX(x);
                    surface.setLastSenseY(y);
                };
        //-------------------------------------
        static SurfaceAction DefaultSwipeAction =
                (surface) ->
                {
                    int[] Swipe = surface.getSwipe();
                    if (Swipe == null) return;
                    if (surface.isInTouchMode()) {
                        surface.setFocusObject(surface.findObject(surface.realX(Swipe[0]), surface.realY(Swipe[1]), true, null));
                        if (surface.getFocusObject() != null) {
                            surface.setLastSenseX(Swipe[2]);
                            surface.setLastSenseY(Swipe[3]);
                            double[] data = {surface.realX(Swipe[0]), surface.realY(Swipe[1]), surface.realX(Swipe[2]), surface.realY(Swipe[3])};
                            surface.getFocusObject().moveDirectional(data, surface);
                            Swipe[0] = Swipe[2];
                            Swipe[1] = Swipe[3];
                            return;
                        }
                        if (surface.getFocusObject() == null) {
                            surface.translate(Swipe[2] - Swipe[0], Swipe[3] - Swipe[1]);
                            Swipe[0] = Swipe[2];
                            Swipe[1] = Swipe[3];
                            surface.setSwipe(null);
                        }
                    } else {
                        if (Swipe.length == 4) surface.translate(Swipe[2] - Swipe[0], Swipe[3] - Swipe[1]);
                        Swipe[0] = Swipe[2];
                        Swipe[1] = Swipe[3];
                        surface.setSwipe(null);
                    }
                    surface.setSwipe(null);
                };
        //-------------------------------------
        static SurfaceAction DefaultDragAction =
                (surface) ->
                {
                    int[] Drag = surface.getSwipe();
                    if (Drag == null) return;
                    if (surface.isInTouchMode()) {

                    } else {

                    }
                    surface.setSwipe(null);
                };
        //-------------------------------------

    }


    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (arg0.getSource() == GUITimer) updateAndRedraw();
    }

}