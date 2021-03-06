package theatro.play.graph;

import theatro.core.backstage.*;

import java.awt.*;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class SurfaceNode implements SurfaceObject
{
    private static int convectionTime = 500000000;

    private static final int buttonLimit = 250000000;
    private static final int deckAnimationLimit = 700000000;

    private Color deckButtonColor = new Color(0, 0, 20, 255);
    private double[] position = new double[2];

    private static final int defaultDiameter = 1680;
    private int diameter = defaultDiameter;

    private boolean deckIsRemoved = false;
    private boolean deckAnimationRunning = false;

    private boolean hoveringOverDeckButton = false;
    private boolean wasOnDeckButton = false;


    private boolean isOnDeck = false;
    private boolean wasOnDeck = false;
    private boolean hasRecentlyBeenMoved = false;
    private boolean hasJustBeenMoved = false;

    private double velX;
    private double velY;

    private ArrayList<SurfaceNode> _parents = new ArrayList<>();
    private ArrayList<SurfaceNode> _children = new ArrayList<>();

    private String _node;

    public String getString() {
        return _node;
    }

    public void setString(String node) {
        _node = node;
    }

    public ArrayList<SurfaceNode> getConnection() {
        return _parents;
    }

    public void connect(SurfaceNode n) {
        if (!_parents.contains(n)) {
            _parents.add(n);
            n._registerChild(this);
        }
    }

    private void _registerChild(SurfaceNode child) {
        _children.add(child);
    }

    public void addToVel(double vx, double vy) {
        velX += vx;
        velY += vy;
    }

    interface Painter { void paint(Graphics2D painterBrush, double x, double y);}

    private SurfaceNodeInput InputNode = null;

    //===========================================================================
    SurfaceNode(List<Object> node, double x, double y, NodeWindow surface) {
        velX = 0.0;
        velY = 0.0;
        position[0] = x;
        position[1] = y;
        setString("Branch");
        if(node instanceof List) InputNode = (new SurfaceNodeInput(getX(), getY(), diameter / 2.0));
        _construct(surface, node);
    }

    private void _construct(NodeWindow surface, List<Object> node) {
        //String node = getString();
        if (node!=null) {
            for (Object p : node) {
                if(p instanceof List){
                    SurfaceNode newNode = new SurfaceNode(
                            (List<Object>)p,
                            getX() + new Random().nextInt() % 5000,
                            getY() + new Random().nextInt() % 5000,
                            surface
                    );
                    surface.addSurfaceObject(newNode);
                    this.connect(newNode);
                } else {
                    SurfaceNode newNode = new SurfaceNode(
                            null,
                            getX() + new Random().nextInt() % 5000,
                            getY() + new Random().nextInt() % 5000,
                            surface
                    );
                    surface.addSurfaceObject(newNode);
                    this.connect(newNode);
                }
                
            }
        }
    }

    //===========================================================================
    public boolean justMoved() {
        return hasJustBeenMoved;
    }

    public boolean recentlyMoved() {
        return hasRecentlyBeenMoved;
    }

    public void setHasMoved(boolean moved) {
        hasJustBeenMoved = moved;
    }

    //------------------------------------------
    public SurfaceNodeInput getInputNode() {
        return InputNode;
    }

    @Override
    public void moveCircular(double[] data, Surface Surface) //double centerX, double centerY, double x, double y
    {
        if (data.length == 3){ moveCircularBy(data, Surface); return; }
        double centerX = data[0];
        double centerY = data[1];
        double x = data[2];
        double y = data[3];
        double vectorX = x - centerX;
        double vectorY = y - centerY;
        double distance = (((Math.pow(Math.pow(vectorX, 2) + Math.pow(vectorY, 2), 0.5))));
        vectorX /= distance;
        vectorY /= distance;
        double oldX = position[0];
        double oldY = position[1];
        double newVecX = oldX - centerX;
        double newVecY = oldY - centerY;
        distance = (((Math.pow(Math.pow(newVecX, 2) + Math.pow(newVecY, 2), 0.5))));
        newVecX /= distance;
        newVecY /= distance;
        double alpha;
        alpha = -(Math.atan2(newVecY, newVecX) - Math.atan2(vectorY, vectorX));//Math.PI;
        double[] newData = {alpha, centerX, centerY};
        moveCircularBy(newData, Surface);
    }

    private void moveCircularBy(double[] data, Surface Surface) {
        Surface.setMap(Surface.getMap().removeAndUpdate(this));
        if (Surface.getMap() == null) Surface.setMap(new GridSpaceMap(getX(), getY(), 50000));
        double alpha = data[0];
        double centerX = data[1];
        double centerY = data[2];
        //ArrayList<SurfaceRepaintSpace> queue = new ArrayList<>();
        double vectorX = position[0] - centerX;
        double vectorY = position[1] - centerY;
        if (InputNode != null) {
            //queue.addAll(
                    InputNode.updateOn(getX(), getY(), diameter / 2.0, Surface);
                    //);
            //queue.addAll(
                    InputNode.moveCircular(data, Surface);
                    //);//alpha, centerX, centerY
        }
        //queue.add(getRepaintSpace());
        // TODO: IF CHANGE OCCURRED!
        //Surface.layers()[7].add(getRepaintSpace(Surface));

        position[0] = (centerX + (Math.cos(alpha) * (vectorX) - Math.sin(alpha) * (vectorY)));
        position[1] = (centerY + (Math.cos(alpha) * (vectorY) + Math.sin(alpha) * (vectorX)));
        //queue.add(getRepaintSpace());
        //TODO: CHANGE OCCURRED
        //Surface.layers()[7].add(getRepaintSpace(Surface));
        this.hasJustBeenMoved = true;
        Surface.setMap(Surface.getMap().addAndUpdate(this));
    }

    @Override
    public void moveDirectional(double[] data, Surface Surface)
    {
        double startX = data[0];
        double startY = data[1];
        double targX = data[2];
        double targY = data[3];

        //ArrayList<SurfaceRepaintSpace> queue = new ArrayList<SurfaceRepaintSpace>();
        hasJustBeenMoved = true;
        if (testForBody(startX, startY)) {
            double shiftX = startX - position[0];
            double shiftY = startY - position[1];
            double[] newData = {(targX - shiftX), (targY - shiftY)};
            //queue.addAll(
                    moveTo(newData, Surface);
            //);
        }
        //return queue;
    }

    //---------------------
    @Override
    public void moveTo(double[] data, Surface Surface) //
    {
        Surface.setMap(Surface.getMap().removeAndUpdate(this));
        if (Surface.getMap() == null) Surface.setMap(new GridSpaceMap(getX(), getY(), 50000));
        double x = data[0];
        double y = data[1];
        if (InputNode != null) {
            InputNode .updateOn(getX(), getY(), diameter / 2.0, Surface);
            data = new double[4];
            data[0] = position[0];
            data[1] = position[1];
            data[2] = x;
            data[3] = y;
             InputNode.moveDirectional(data, Surface);
        }
        position[0] = x;
        position[1] = y;
        hasJustBeenMoved = true;
        Surface.setMap(Surface.getMap().addAndUpdate(this));
    }


    @Override
    public SurfaceRepaintSpace getRepaintSpace() {
        return new SurfaceRepaintSpace(position[0]-getRadius(), position[1]-getRadius(), position[0]+getRadius(), position[1]+getRadius());
    }
    // --------------------------------------------------------------------------------------------

    public void updateOn(Surface hostSurface) {

        if (hostSurface.getScale() < 0.13 && deckIsRemoved) deckAnimationRunning = true;
        double LP = getX() - diameter * 2;
        double RP = getX() + diameter * 2;
        double TP = getY() - diameter * 2;
        double BP = getY() + diameter * 2;
        double[] frame = {LP, RP, TP, BP};
        List<SurfaceObject> objects = hostSurface.getMap().getAllWithin(frame);
        if (objects != null) {
            double[] data = {0.0, 0.0};
            objects.forEach((o) -> {
                if (o != this) {
                    double vx = o.getX() - this.getX();
                    double vy = o.getY() - this.getY();
                    double d = SurfaceUtility.magnitudeOf(vx, vy);
                    double s = d / this.getRadius();
                    s = 1 / s;
                    s = Math.pow(s, 4);
                    data[0] += vx * s;
                    data[1] += vy * s;
                }
            });
            velX += data[0];
            velY += data[1];
        }
        ArrayList parents = this.getConnection();
        if (parents != null) {
            double maxDistance = 3 * diameter;
            double[] data = {0.0, 0.0};
            parents.forEach((p) -> {
                SurfaceNode n = (SurfaceNode) p;
                double vx = n.getX() - this.getX();
                double vy = n.getY() - this.getY();
                double d = SurfaceUtility.magnitudeOf(vx, vy);
                double s = d;///this.getRadius();
                if (d > maxDistance) {
                    s = Math.pow(d / maxDistance, 4) / d;
                    n.addToVel(vx * s, vy * s);
                } else {
                    s = 0;
                }
                data[0] -= vx * s;
                data[1] -= vy * s;
            });
            velX += data[0];
            velY += data[1];
        }
        double mag = Math.abs(SurfaceUtility.magnitudeOf(velX, velY))/ (hostSurface.getFrameDelta()/1e6);
        if (!isOnDeck && mag > 10) {
            double s = (mag > diameter) ? diameter : mag;
             this.moveTo(
                  new double[]{
                      this.getX() - s * velX / mag,
                      this.getY() - s * velY / mag
                  },
                  hostSurface
             );
        }
        velX *= 0.7;
        velY *= 0.7;
        if (position.length != 2) {
            double[] old = position;
            position = new double[2];
            position[0] = old[0];
            position[1] = old[1];
        }
        updateAnimations(hostSurface);
        if (InputNode!=null)  InputNode.updateOn(getX(), getY(), diameter / 2.0, hostSurface);
        if (hasJustBeenMoved && !hasRecentlyBeenMoved) hasRecentlyBeenMoved = true;
        else if (hasJustBeenMoved && hasRecentlyBeenMoved) hasJustBeenMoved = false;
        else if (hasJustBeenMoved && hasRecentlyBeenMoved) hasRecentlyBeenMoved = false;
        if(hostSurface.getCurrentFrameSpace().contains(getRepaintSpace())){
            hostSurface.layers()[6].add((brush)->paintNeuron(brush, hostSurface));
            hostSurface.layers()[2].add((brush)->paintNodeConnections(brush, hostSurface.getAnimator()));
        }

    }
    //--------------------------

    private void updateAnimations(Surface hostSurface) {
        Animator Animator = hostSurface.getAnimator();
        int frameDelta = hostSurface.getFrameDelta();
        boolean scaleCheck = true;
        if (hostSurface.getScale() < 0.2) scaleCheck = false;
        if (hostSurface.getScale() < 0.13 && deckIsRemoved) deckAnimationRunning = true;
        boolean UnitNeedsToBeRepainted = false;
        //ArrayList<SurfaceRepaintSpace> queue = new ArrayList<SurfaceRepaintSpace>();
        int animationID = 0;

        // Animation CACHE! -> counter counts only animated cases.
        //=================================================================================================================================================
        //Animation 0 -> Deck Button
        //=================================================================================================================================================
        if (wasOnDeck) {
            //System.out.println("was on deck: "+Animator.getCounterOf(this, animationIDCounter));
            if (Animator.hasCounter(this, animationID)) {
                if (Animator.getCounterOf(this, animationID) < buttonLimit && Animator.getCounterOf(this, animationID) > 0) {
                    double mod = ((double) Animator.getCounterOf(this, 0) / buttonLimit);
                    //hostSurface.layers()[7].add(
                    //        //new SurfaceRepaintSpace(//Moving repaint frame on deck:
                    //        //    (position[0]),
                    //        //    (position[1] - 0.0825 * diameter * mod),
                    //        //    (diameter / 4.0),
                    //        //    (diameter / 8.0) + 0.065 * diameter * mod,
                    //        (brush)->_paintMe(brush, 6, hostSurface)
                    //        //)
                    //);
                }
            }
            if (Animator.hasCounter(this, animationID)) {//System.out.println("Deck counter existing");
                if (isOnDeck && scaleCheck) {
                    //System.out.println("update->wasondeck->Is on deck");
                    Animator.countUpFor(this, frameDelta, animationID);
                    if (Animator.getCounterOf(this, animationID) > buttonLimit) {//100
                        Animator.countDownFor(this, frameDelta, animationID);
                    }
                } else {
                    if (Animator.getCounterOf(this, animationID) >= 0) {
                        Animator.countDownFor(this, frameDelta, animationID);
                    }
                    if (Animator.getCounterOf(this, animationID) <= 0 && !deckAnimationRunning && !wasOnDeckButton) {//
                        Animator.removeCounterOf(this, animationID);
                        wasOnDeck = false;
                    }
                }
            } else {
                if (isOnDeck) {
                    Animator.setCounterFor(this, animationID, 1); // System.out.println("Deck counter setInto!");
                    wasOnDeck = true;
                }
            }
            if (deckAnimationRunning) {
                if (Animator.hasCounter(this, animationID)) {
                    Animator.countDownFor(this, 6 * frameDelta, 0);
                }
            }
            if (wasOnDeck && Animator.getCounterOf(this, animationID) <= 0) {
                Animator.countUpFor(this, 1 - Animator.getCounterOf(this, animationID), animationID);
            }
        }
        //=================================================================================================================================================
        // Animation 1 -> Deck button sense
        //=================================================================================================================================================
        if (wasOnDeckButton && wasOnDeck) {
            animationID = 1;
            //System.out.println("Button Shining: "+Animator.getCounterOf(this, animationIDCounter));
            if (Animator.hasCounter(this, animationID)) {
                if (hoveringOverDeckButton && scaleCheck) {
                    Animator.countUpFor(this, (frameDelta / 6), animationID);
                    if (Animator.getCounterOf(this, animationID) >= 2 * buttonLimit) {//255
                        Animator.countDownFor(this, (frameDelta / 6), animationID);
                    }
                    double mod = ((double) Animator.getCounterOf(this, animationID) / (double) (2 * buttonLimit));
                    if (mod > 1) mod = 1;
                    deckButtonColor = new Color(0, 255, 255, (int) (255 * mod));
                    //if (Animator.getCounterOf(this, animationIDCounter) > 0) {queue.addInto(new SurfaceRepaintSpace((position[0]), (position[1]), (110), (55)));}
                } else {
                    if (Animator.getCounterOf(this, animationID) >= 0) {
                        double mod = ((double) Animator.getCounterOf(this, animationID) / (double) (2 * buttonLimit));
                        if (mod > 1)  mod = 1;
                        else if (mod < 0) mod = 0;
                        deckButtonColor = new Color(0, 255, 255, (int) (255 * mod));
                        Animator.countDownFor(this, (frameDelta / 4), animationID);
                    }
                    if (Animator.getCounterOf(this, animationID) <= 0 && !deckAnimationRunning) {
                        Animator.removeCounterOf(this, animationID);
                        deckButtonColor = new Color(0, 255, 255, (0));
                        wasOnDeckButton = false;
                    }
                }

            } else if (hoveringOverDeckButton){
                Animator.setCounterFor(this, animationID, 1);
            }
            if (deckAnimationRunning) {
                if (Animator.hasCounter(this, animationID)) {
                    Animator.countDownFor(this, 6 * frameDelta, animationID);
                    if (Animator.getCounterOf(this, animationID) <= 0) {
                        Animator.countUpFor(this, 1 - Animator.getCounterOf(this, animationID), animationID);
                    }
                }
            }

        }
        //=================================================================================================================================================
        //animation 2 -> deck opening
        //=================================================================================================================================================
        if (deckAnimationRunning) {
            //System.out.println("deck Animation: "+Animator.getCounterOf(this, animationIDCounter));
            animationID = 2;
            if (Animator.hasCounter(this, animationID)) {
                Animator.countUpFor(this, frameDelta, animationID);
                UnitNeedsToBeRepainted = true;
                if (Animator.getCounterOf(this, animationID) > deckAnimationLimit) //5000000
                {
                    Animator.removeCounterOf(this, animationID);
                    if (deckIsRemoved) {
                        deckIsRemoved = false;
                    } else {
                        deckIsRemoved = true;
                    }
                    deckAnimationRunning = false;
                }
            } else {
                Animator.setCounterFor(this, animationID, 1);
                //Somehow the animation ID is 2 sometimes although so many animations are not even setInto!
            }
        }
        //=================================================================================================================================================
        //Animation 3 -> Weight convolve
        //=================================================================================================================================================
        boolean activityCheck = false;
        if (InputNode!=null && InputNode .isActive())  {
            activityCheck = true;
        }
        updateConnectionVectorAndGetRepaintSpace(activityCheck, hostSurface);
        if (activityCheck) {
            animationID = 3;
            //Error was here => IDCounter was -1!!! Note: This has something to do with the deck animation!
            // Animation 1 -> Deck button sense
            if (Animator.getCounterOf(this, animationID) >= 0) {
                Animator.countUpFor(this, frameDelta, animationID);
                if (Animator.getCounterOf(this, animationID) > convectionTime) {
                    Animator.removeCounterOf(this, animationID);
                    InputNode .setIsActive(false);
                }
            } else {
                Animator.setCounterFor(this, animationID, 1);
            }
        }

        if (this.hasJustBeenMoved || UnitNeedsToBeRepainted) {
            //queue.add(this.getRepaintSpace());
            //hostSurface.layers()[7].add(getRepaintSpace(hostSurface));
            if (InputNode!=null)  {
                //queue.add(InputNode .getRepaintSpace());
                //hostSurface.layers()[7].add(InputNode .getRepaintSpace(hostSurface));
            }
        } else {
            if (InputNode!=null && InputNode .changeOccured())  {
                //queue.add(InputNode .getRepaintSpace());
                //hostSurface.layers()[7].add(InputNode .getRepaintSpace(hostSurface));
            }
        }
    }

    // --------------------------------------------------------------------------------------------
    // ============================================================================================
    // INTERACTION:
    // --------------------------------------------------------------------------------------------
    public void movementAt(double trueMouseX, double trueMouseY, Surface HostSurface) {// System.out.println("Is on deck:
        double panelScale = HostSurface.getScale();                                                                // "+isOnDeck);
        if (testForBody(trueMouseX, trueMouseY)) {
            isOnDeck = true;
            wasOnDeck = true;
        } else {
            isOnDeck = false;
        }
        if (testForButton_Open(trueMouseX, trueMouseY, panelScale)) {
            hoveringOverDeckButton = true;
        }

        if(InputNode!=null && InputNode.testFor(trueMouseX, trueMouseY)) {
            if (isOnDeck) wasOnDeck = true;
            isOnDeck = true;
        }
    }

    // --------------------------------------------------------------------------------------------
    @Override
    public boolean clickedAt(double x, double y, Surface hostSurface) {
        if (deckAnimationRunning) return false;
        if (testForButton_Open((x), (y), hostSurface.getScale())) {//this.getNeuron().getPropertyHub().addProperty(new NNeuronControlFrame("TEST", this.getNeuron()));
            this.deckAnimationRunning = true;
        }
        return true;
    }

    @Override
    public boolean doubleClickedAt(double x, double y, Surface hostSurface) {
        boolean check = false;
        if(InputNode!=null){
            if (InputNode.testFor(x, y)) check = true;
            if (check == true) {
                this.InputNode = (new SurfaceNodeInput(getX(), getY(), diameter / 2.0));
            }
        }
        return false;
    }

    // SENSING:
    // ============================================================================================
    // --------------------------------------------------------------------------------------------
    public boolean isAttachable(double trueX, double trueY, double panelScale) {

        if (!deckAnimationRunning) {
            if (testForButton_Open(trueX, trueY, panelScale)) {
                return false;
            }
        }
        if (InputNode!=null) {
            if (InputNode .testFor(trueX, trueY)) return false;
        }
        double d = Math.pow(Math.pow((trueX - position[2]), 2) + Math.pow((trueY - position[3]), 2), 0.5);
        if (d <= diameter * 2.5) {
            return true;
        }
        return true;
    }

    // TESTFOR FUNCTIONS:
    // --------------------------------------------------------------------------------------------
    public boolean testForBody(double x, double y) {
        double d = Math.pow(Math.pow((x - position[0]), 2) + Math.pow((y - position[1]), 2), 0.5);
        if(InputNode!=null && InputNode .testFor(x, y)) return true;
        if (d <= diameter / 2) return true;
        return false;
    }

    // --------------------------------------------------------------------------------------------
    public boolean testForBody_Root(double x, double y) {
        return false;
    }

    // --------------------------------------------------------------------------------------------
    public boolean testForButton_Open(double trueMouseX, double trueMouseY, double panelScale) {
        if (deckAnimationRunning) {
            hoveringOverDeckButton = false;
            isOnDeck = false;
            return false;
        }
        double modifier = panelScaleModifier(panelScale);
        int x = (int) position[0];
        int y = (int) position[1];
        boolean check = true;
        if (trueMouseX < x - (int) (diameter / 4 * Math.pow(modifier, 100 / modifier)) / 2) check = false;
        if (trueMouseY < y - (int) (diameter / 8 * Math.pow(modifier, 100 / modifier)) / 2) check = false;
        if (trueMouseX > x + (int) (diameter / 4 * Math.pow(modifier, 100 / modifier)) / 2) check = false;
        if (trueMouseY > y + (int) (diameter / 8 * Math.pow(modifier, 100 / modifier)) / 2) check = false;
        wasOnDeckButton = (hoveringOverDeckButton)?true:wasOnDeckButton;
        hoveringOverDeckButton = check;
        if (check) wasOnDeckButton = true;
        return check;
    }

    //CONNECTION REPAINT SPACE
    //=============================================================================================
    // --------------------------------------------------------------------------------------------
    private void updateConnectionVectorAndGetRepaintSpace(boolean convection, Surface surface) // OPTIMIZATION?
    {//-> checking if connection points are within frame!
        double vX;
        double vY;
        ArrayList<SurfaceNode> connection = this.getConnection();
        if (connection != null && connection.size() > 0) {
            for (SurfaceNode surfaceNode : connection) {
                boolean connectionMoved = this.justMoved();
                if (surfaceNode != null && !connectionMoved && surfaceNode.justMoved())  connectionMoved = true;
                if (connectionMoved || InputNode.changeOccured() || convection) {
                    if (surfaceNode != null) {
                        vX = surfaceNode.getX() - getX();
                        vY = surfaceNode.getY() - getY();
                        if (vX == 0 && vY == 0) {
                            Random dice = new Random();
                            vX = dice.nextDouble() % 1;
                            vY = dice.nextDouble() % 1;
                        }
                        InputNode.addToVel(2 * vX / getRadius(), 2 * vY / getRadius());
                        double pX = surfaceNode.getX();
                        double pY = surfaceNode.getY();
                        double pvX = (pX - getX());
                        double pvY = (pY - getY());// v from self center to current parent
                        //---
                        double ivX = InputNode.getX() - getX();
                        double ivY = InputNode.getY() - getY();// v from self center to input node
                        double d = SurfaceUtility.magnitudeOf(ivX, ivY);
                        ivX /= d;
                        ivY /= d;
                        d = SurfaceUtility.magnitudeOf(pvX, pvY);
                        ivX *= d;
                        ivY *= d;
                        ivX = ivX - pX;
                        ivY = ivY - pY;
                        //d = SurfaceUtility.magnitudeOf(ivX, ivY)/(getRadius());
                        //System.out.println(d+" -> "+((2/(1+22/(d*d*d*d*d*d)))-1));
                        //d = ((10/(1+22/(d*d*d*d*d*d)))-9);//((2/(2+1/d))-1);
                        ivX = -2 * ((getX() + ivX) / getRadius());//*Math.pow(d/(2*diameter), 2);
                        ivY = -2 * ((getY() + ivY) / getRadius());//*Math.pow(d/(2*diameter), 2);
                        surfaceNode.addToVel(ivX, ivY);
                        //---
                        if (InputNode.changeOccured() || convection) {
                            double repaintCenterX = getX() + (pvX) / 2;
                            double repaintCenterY = getY() + (pvY) / 2;
                            double distanceX = Math.abs((pX - getX()) / 2);
                            double distanceY = Math.abs((pY - getY()) / 2);
                            if (convection) {
                                d = Math.pow(Math.pow(pvX, 2) + Math.pow(pvY, 2), 0.5);
                                double v0X = pvX / d;
                                double v0Y = pvY / d;
                                double normalX = (-v0Y) * diameter / 2;
                                double normalY = (v0X) * diameter / 2;
                                double sideVecOneX = (pX + normalX) - repaintCenterX;
                                double sideVecOneY = (pY + normalY) - repaintCenterY;
                                double sideVecTwoX = (pX - normalX) - repaintCenterX;
                                double sideVecTwoY = (pY - normalY) - repaintCenterY;
                                if (sideVecOneX > distanceX) distanceX = sideVecOneX;
                                if (sideVecTwoX > distanceX) distanceX = sideVecTwoX;
                                if (sideVecOneY > distanceY) distanceY = sideVecOneY;
                                if (sideVecTwoY > distanceY) distanceY = sideVecTwoY;
                            }
                            InputNode.forgetChange();
                        }
                    }//=================================================================
                }
            }
        }
    }

    //PAINTING:
    //--------------------------------------------------------------------------------------------
    //=============================================================================================
    private void paintNodeConnections(Graphics2D brush, Animator Animator) {
        Color connectionColor = Color.CYAN;
        int animationID = 0;
        int convectionCounter = 0;
        boolean activityCheck = false;
        if (InputNode!=null)  {
            if ( InputNode.isActive()) {
                activityCheck = true;
                //Ii = InputNode.size();
            }
        }
        animationID = 3;
        if (activityCheck) {
            convectionCounter = Animator.getCounterOf(this, animationID);
        }
        //=======================================================================================================================
        ArrayList<SurfaceNode> connection = getConnection();
        if (connection != null) {
            for (int Ii = 0; Ii < connection.size(); Ii++) {
                if (connection.get(Ii) != null) {
                    double selfCenterX = InputNode .getX();
                    double selfCenterY = InputNode .getY();
                    double inputVecX = selfCenterX - getX();
                    double inputVecY = selfCenterY - getY();

                    double connectionX = connection.get(Ii).getX();
                    double connectionY = connection.get(Ii).getY();

                    GeneralPath bezier = new GeneralPath();

                    double vectorX = (connectionX - selfCenterX);
                    double vectorY = (connectionY - selfCenterY);

                    double otherX = selfCenterX + vectorX;
                    double otherY = selfCenterY + vectorY;

                    SurfaceUtility math = new SurfaceUtility();

                    double deg = math.unitaryVectorProduct(vectorX, vectorY, inputVecX, inputVecY);
                    //System.out.println(deg);
                    double outerX = selfCenterX + inputVecX * (1 + Math.abs(deg));
                    double outerY = selfCenterY + inputVecY * (1 + Math.abs(deg));
                    deg = 1.5 * (1 - deg);

                    double orthoX, orthoY;
                    if (math.unitaryVectorProduct(vectorX, vectorY, -inputVecY, inputVecX) < 0) {
                        orthoX = outerX + deg * inputVecY;
                        orthoY = outerY - deg * inputVecX;
                    } else {
                        orthoX = outerX - deg * inputVecY;
                        orthoY = outerY + deg * inputVecX;
                    }
                    double[] points = {
                            selfCenterX, selfCenterY,
                            outerX, outerY,
                            orthoX, orthoY,
                            otherX, otherY
                    };

                    bezier.moveTo(points[0], points[1]);
                    bezier.curveTo(points[2], points[3], points[4], points[5], points[6], points[7]);

                    brush.setColor(connectionColor);
                    brush.setStroke(new BasicStroke(22));
                    brush.draw(bezier);

                    double[] curvePoint = null;
                    double ratio = 1 - ((double) (convectionCounter)) / convectionTime;

                    if (activityCheck) {
                        SurfaceNode Other = (connection.get(Ii));
                        double d = Math.pow(Math.pow(vectorX, 2) + Math.pow(vectorY, 2), 0.5);
                        //System.out.println(diameter);
                        double normalX = (-vectorY / d) * Other.diameter / 2 * ratio;
                        double normalY = (vectorX / d) * Other.diameter / 2 * ratio;
                        Polygon convector = new Polygon();//System.out.println("painting convolve!"+ratio);
                        double invratio = 1 - ratio;
                        curvePoint = math.getCurvePointOn(invratio * invratio * invratio, points);
                        convector.addPoint((int) (curvePoint[0]), (int) (curvePoint[1]));
                        curvePoint = math.getCurvePointOn(invratio, points);//=>Convector center
                        convector.addPoint((int) (curvePoint[0] + normalX), (int) (curvePoint[1] + normalY));
                        convector.addPoint((int) (selfCenterX), (int) (selfCenterY));
                        convector.addPoint((int) (curvePoint[0] - normalX), (int) (curvePoint[1] - normalY));
                        brush.fillPolygon(convector);
                    }
                    curvePoint = math.getCurvePointOn(1 / 3.25, points);

                    String value = "";//+ this.get_tensor();
                    brush.fillOval((int) (curvePoint[0]) - 70, (int) (curvePoint[1]) - 25, 140, 50);
                    brush.setColor(Color.BLACK);

                    brush.drawString(value, (int) (curvePoint[0]) - value.length() * diameter / 250, (int) (curvePoint[1]) + 6);
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------------

    private void paintNeuron(Graphics2D brush, Surface surface) {//, double panelScale, Animator Animator) {
        int x = (int) position[0];
        int y = (int) position[1];
        brush.setColor(Color.cyan);

        //COLORING FOR TYPES:
        //=================================
        //PaletteUtils.NEURAL_BLUE = new Color(0, 60, 120);
        //=================================
        //NODE SIZE:
        diameter = defaultDiameter;

        //GHOST EFFECT:
        //--------------
        Point2D center = new Point2D.Float(x, y);
        float gradientRadius = (float) ((diameter / 2) + 80);
        float[] dist = {0.0f, 0.5f, 1f};
        Color[] colors = {Color.cyan, Color.cyan, new Color(0, 0, 0, 0)};
        Paint oldPaint = brush.getPaint();
        RadialGradientPaint rgp = new RadialGradientPaint(center, gradientRadius, dist, colors, CycleMethod.NO_CYCLE);
        brush.setPaint(rgp);
        brush.fillOval(
                x - 100 - (int) ((diameter / 2)), y - 100 - (int) ((diameter / 2)),
                (diameter + 200), (diameter + 200)
        );
        brush.setPaint(oldPaint);
        //--------------

        //CYAN BASE:
        //--------------
        brush.fillOval(x - (diameter + 40) / 2, y - (diameter + 40) / 2, diameter + 40, diameter + 40);

        //--------------
        if(InputNode!=null){
            brush.setColor(Color.CYAN);
            InputNode.paint(brush);
        }
        //--------------

        //NEURON DECK:
        //--------------
        if (deckIsRemoved == true && deckAnimationRunning == false) {
            paintNodeInterior(brush, (int) position[0], (int) position[1], surface);
        } else if (deckIsRemoved == false && deckAnimationRunning == false) {
            paintNodeDeck(brush, (int) position[0], (int) position[1], surface);
        } else if (deckAnimationRunning == true) {
            paintNodeInterior(brush, (int) position[0], (int) position[1], surface);
            paintNodeDeck(brush, (int) position[0], (int) position[1], surface);
        }

        if (InputNode != null) {// && panelScale > 0.1
            double mod = (surface.getScale() / 0.0275);
            //System.out.println("s: "+panelScale);
            String s = "";
            if (getString() != null) {//.getFunction()
                s += "";//Function.TYPES.REGISTER[getString().getFunction().id()];
            }
            InputNode.paintTop(brush, s, (Math.pow(mod, 10)));
        }

    }

    //NODE DECK:
    // --------------------------------------------------------------------------------------------
    private void paintNodeInterior(Graphics2D brush, int centerX, int centerY, Surface surface) {
        double modifier = panelScaleModifier(surface.getScale());
        double panelScale = (deckAnimationRunning) ? (diameter / 2) : surface.getScale();
        Animator animator = surface.getAnimator();
        double radius = (double) diameter / 2;
        int animationID = 0;
        double buttonAnimationModifier = 0;

        animationID = 0;
        if (animator.hasCounter(this, animationID)) {
            buttonAnimationModifier = (double) animator.getCounterOf(this, animationID) / buttonLimit;
        }
        Ellipse2D.Double ellipse =
                new Ellipse2D.Double(
                        centerX - (radius * modifier), centerY - (radius * modifier),
                        (2 * radius * modifier), (2 * radius * modifier)
                );
        brush.setColor(Color.BLACK);
        brush.fill(ellipse);

        brush.setColor(deckButtonColor);
        brush.fillRoundRect(centerX - (int) (buttonAnimationModifier * radius / 2 * Math.pow(modifier, 200 / modifier)) / 2,
                centerY - (int) (buttonAnimationModifier * radius / 4 * Math.pow(modifier, 300 / modifier)) / 2,
                (int) (buttonAnimationModifier * radius / 2 * Math.pow(modifier, 200 / modifier)),
                (int) (buttonAnimationModifier * radius / 4 * Math.pow(modifier, 300 / modifier)), (int) (radius / 6), (int) (radius / 6));

        _paintButton(brush, centerX, centerY, radius, modifier, buttonAnimationModifier, true);

        _paintDisplayBox(
                brush,
                centerX,
                centerY,
                surface.getScale(),
                buttonAnimationModifier,
                true
        );
        //---

        Font ValueFont = new Font("Tahoma", Font.PLAIN, (int) (diameter * (0.033333)));
        brush.setFont(ValueFont);

        brush.setColor(Color.CYAN);
        //brush.drawString("Paralyzed: "+this.get_tensor().rqsGradient()+";",
        //		       (int) (centerX - radius*0.2), (int) (centerY -radius*0.775));
        brush.setColor(Color.DARK_GRAY);
        brush.fillRect((int) (centerX - radius * 0.4), (int) (centerY - radius * 0.77), (int) (2 * radius * 0.4), (int) (radius * 0.01));

        brush.setColor(Color.CYAN);
        //brush.drawString("First: "+this.get_tensor().rqsGradient()+"; Hidden: "+this.get_tensor().rqsGradient()+"; Last: "+this.get_tensor().rqsGradient()+";",
        //			   (int) (centerX - radius*0.5),  (int) (centerY -radius*0.65));
        brush.setColor(Color.DARK_GRAY);
        brush.fillRect((int) (centerX - radius * 0.625), (int) (centerY - radius * 0.645), (int) (2 * radius * 0.625), (int) (radius * 0.01));

        brush.setColor(Color.CYAN);
        //brush.drawString("Is root component: "+this.get_tensor().is(Tsr.Root)+rootType+";",
        //		       (int) (centerX - radius*0.65), (int) (centerY -radius*0.520));
        brush.setColor(Color.DARK_GRAY);
        brush.fillRect((int) (centerX - radius * 0.675), (int) (centerY - radius * 0.515), (int) (2 * radius * 0.675), (int) (radius * 0.01));

        brush.setColor(Color.CYAN);
        brush.drawString("Gradient behavior", (int) (centerX - radius * 0.85), (int) (centerY - radius * 0.1));
        //brush.drawString("AutoApply: "+this.get_tensor().isInstantGradientApply()+";", (int) (centerX - radius*0.825), (int) (centerY -radius*0.0));
        //brush.drawString("Summing: "+this.get_tensor().isGradientSumming()+";",        (int) (centerX - radius*0.825), (int) (centerY +radius*0.1));
        brush.setColor(Color.DARK_GRAY);
        brush.fillRect((int) (centerX - radius * 0.9), (int) (centerY + radius * 0.15), (int) (2 * radius * 0.9), (int) (radius * 0.01));

        brush.setColor(Color.CYAN);
        brush.drawString("Trainable", (int) (centerX + radius * 0.425), (int) (centerY - radius * 0.1));
        //brush.drawString("Bias: "+this.get_tensor().biasIsTrainable()+"; ",    (int) (centerX + radius*0.425), (int) (centerY -radius*0.0));
        //brush.drawString("Weight: "+this.get_tensor().asCore().weightIsTrainable()+";",  (int) (centerX + radius*0.4), (int) (centerY +radius*0.1));

        brush.setColor(Color.CYAN);
        brush.drawString("Neuron ID", (int) (centerX - radius * 0.14), (int) (centerY + radius * 0.225));
        ValueFont = new Font("Tahoma", Font.PLAIN, (int) (diameter * (0.065)));
        brush.setFont(ValueFont);
        brush.setColor(Color.DARK_GRAY);
        //brush.fillRect((int) (centerX - radius*0.1*Long.toString(this.get_tensor().getID()).length()), (int) (centerY +radius*0.385), (int)(2*radius*0.1*Long.toString(this.get_tensor().getID()).length()), (int)(radius*0.02));

        brush.setColor(Color.CYAN);
        //brush.drawString(Long.toString(this.get_tensor().getID()),(int) (centerX - radius*0.0297*Long.toString(this.get_tensor().getID()).length()), (int) (centerY +radius*0.385));
        //brush.drawString(Long.toString(this.get_tensor().getID()),(int) (centerX - radius*0.02927*Long.toString(this.get_tensor().getID()).length()), (int) (centerY +radius*0.385));

        ValueFont = new Font("Tahoma", Font.PLAIN, (int) (diameter * (0.033333)));
        brush.setFont(ValueFont);

        brush.setColor(Color.CYAN);
        brush.drawString("Property Compartments", (int) (centerX - radius * 0.325), (int) (centerY + radius * 0.475));

        brush.setColor(Color.DARK_GRAY);
        brush.fillRect((int) (centerX - radius * 0.5), (int) (centerY + radius * 0.49), (int) (2 * radius * 0.5), (int) (radius * 0.01));
        ValueFont = new Font("Tahoma", Font.PLAIN, (int) (diameter * (0.025)));

        brush.setFont(ValueFont);
        brush.setColor(Color.CYAN);
        String listedProperties = "";
        double YMod = -radius * 0.05;

        brush.drawString(listedProperties, (int) (centerX - radius * 0.013 * listedProperties.length()), (int) (centerY + radius * 0.6 + YMod));
        YMod += radius * 0.075;
        listedProperties = "";

        double[][] nodeResult = null;

        listedProperties += "Public Values ";
        if (nodeResult == null) {
            listedProperties += " (null)";
        } else {
            if (nodeResult.length == 1) {
                listedProperties += "(Activation:" + nodeResult[0] + ")";
            }
            if (nodeResult.length == 2) {
                listedProperties += "(Activation:" + nodeResult[0] + ", Error:" + nodeResult[1] + ")";
            }
            if (nodeResult.length == 3) {
                listedProperties += "(Activation:" + nodeResult[0] + ", Error:" + nodeResult[1] + ", Optimum:" + nodeResult[2] + ")";
            }
        }
        brush.drawString(listedProperties, (int) (centerX - radius * 0.013 * listedProperties.length()), (int) (centerY + radius * 0.6 + YMod));
        //YMod += radius * 0.075;
        //listedProperties = "";
        //III
        brush.setColor(Color.CYAN);

        ArrayList<Painter> PainterArray = new ArrayList<Painter>();

        PainterArray.add
                ((Graphics2D painterBrush, double x, double y) -> {
                    painterBrush.setColor(Color.DARK_GRAY);
                    painterBrush.fillOval((int) (x - radius * 0.1), (int) (y - radius * 0.1), (int) ((radius * 0.1) * 2), (int) ((radius * 0.1) * 2));
                });


    }

    private void paintNodeDeck(Graphics2D brush, int centerX, int centerY, Surface surface) {
        double modifier = panelScaleModifier(surface.getScale());
        double panelScale = (deckAnimationRunning) ? (diameter / 2) : surface.getScale();
        Animator animator = surface.getAnimator();
        double radius = (double) diameter / 2;
        int animationID = 0;
        double buttonAnimationModifier = 0;
        int deckAnimationCounter = 0;

        animationID = 0;
        if (animator.hasCounter(this, animationID)) {
            buttonAnimationModifier = (double) animator.getCounterOf(this, animationID) / buttonLimit;
        }
        animationID = 2;
        if (animator.hasCounter(this, animationID)) {
            deckAnimationCounter = animator.getCounterOf(this, animationID);
        }

        double decimal = 0;
        if (deckAnimationCounter != 0) {
            decimal = ((double) (deckAnimationCounter / 4) - deckAnimationLimit / 8);

            decimal /= (deckAnimationLimit / 8);

            if (decimal > 1) {
                decimal = 1;
            } else if (decimal < -1) {
                decimal = -1;
            }
            if (deckIsRemoved) {
                decimal *= -1;
            }
            //Transition clip:
            Area clipArea = new Area(new Rectangle.Double(centerX - radius, centerY - radius, radius * 2, radius * 2));
            clipArea.subtract(new Area(new Ellipse2D.Double(centerX - radius * decimal, centerY - radius * decimal, radius * 2 * decimal, radius * 2 * decimal)));
            brush.setClip(clipArea);
        }

        //GRADIENT EFFECT:
        Point2D center = new Point2D.Float(centerX, centerY);
        float gradientRadius = (float) (radius + 50);
        float[] dist = {0.0f, 0.6f, 1f};
        if (decimal > 0) {
            dist[1] += (float) (0.399f * decimal);
        }
        Color[] colors = {PaletteUtils.NEURAL_BLUE, PaletteUtils.DARK_NEURAL_BLUE, Color.black};
        RadialGradientPaint rgp = new RadialGradientPaint(center, gradientRadius, dist, colors, CycleMethod.NO_CYCLE);
        brush.setPaint(rgp);

        Ellipse2D.Double ellipse = new Ellipse2D.Double(centerX - (radius * modifier), centerY - (radius * modifier),
                (2 * radius * modifier), (2 * radius * modifier));
        brush.fill(ellipse);

        Font NFont = new Font("Tahoma", Font.BOLD, (int) (diameter * (0.05)));
        brush.setFont(NFont);

        brush.setColor(PaletteUtils.DARK_NEURAL_BLUE);
        ellipse = new Ellipse2D.Double(
                centerX - (buttonAnimationModifier * radius * Math.pow(modifier, 200 / modifier)) / 2,
                centerY - (buttonAnimationModifier * radius / 1.9 * Math.pow(modifier, 300 / modifier)) / 2,
                (buttonAnimationModifier * radius * Math.pow(modifier, 200 / modifier)),
                (buttonAnimationModifier * radius / 1.9 * Math.pow(modifier, 300 / modifier))
        );
        brush.fill(ellipse);

        brush.setColor(deckButtonColor);
        brush.fillRoundRect(centerX - (int) (buttonAnimationModifier * radius / 2 * Math.pow(modifier, 200 / modifier)) / 2,
                centerY - (int) (buttonAnimationModifier * radius / 4 * Math.pow(modifier, 300 / modifier)) / 2,
                (int) (buttonAnimationModifier * radius / 2 * Math.pow(modifier, 200 / modifier)),
                (int) (buttonAnimationModifier * radius / 4 * Math.pow(modifier, 300 / modifier)), (int) (radius / 6), (int) (radius / 6));

        double expMod;
        expMod = ((double) diameter / defaultDiameter) * panelScale / 0.15;
        if (expMod > 0.7) {
            if (expMod > 1) {
                expMod = 1;
            }
            int expTrans = (int) (Math.pow(expMod, 10) * 255);

            brush.setFont(new Font("Tahoma", Font.BOLD, (int) (diameter * (0.04))));
            brush.setColor(new Color(PaletteUtils.SYSTEM_OCEAN.getRed(), PaletteUtils.SYSTEM_OCEAN.getGreen(), PaletteUtils.SYSTEM_OCEAN.getBlue(), expTrans));
            brush.setColor(new Color(0, 0, 0, expTrans));
        }

        _paintButton(brush, centerX, centerY, radius, modifier, buttonAnimationModifier, false);

        Font ValueFont = new Font("Tahoma", Font.BOLD, (int) (diameter * (0.075)));
        brush.setFont(ValueFont);

        _paintDisplayBox(
                brush,
                centerX,
                centerY,
                surface.getScale(),
                buttonAnimationModifier,
                false
        );

        brush.setClip(null);
    }

    private void _paintButton(Graphics2D brush, int centerX, int centerY, double radius, double modifier, double buttonAnimationModifier, boolean interior) {
        Font NFont = new Font("Tahoma", Font.PLAIN, (int) (radius / 9 * buttonAnimationModifier * Math.pow(modifier, 100 / modifier)));
        brush.setFont(NFont);
        double colorMod = buttonAnimationModifier * (Math.pow(modifier, 100 / modifier));
        if (colorMod > 1) colorMod = 1;
        if (colorMod < 0) colorMod = 0;
        brush.setColor(
                new Color(
                        (int) (PaletteUtils.NEURAL_BLUE.getRed() + (255 - PaletteUtils.NEURAL_BLUE.getRed()) * colorMod),
                        (int) (PaletteUtils.NEURAL_BLUE.getGreen() + (255 - PaletteUtils.NEURAL_BLUE.getGreen()) * colorMod),
                        (int) (PaletteUtils.NEURAL_BLUE.getBlue() + (255 - PaletteUtils.NEURAL_BLUE.getBlue()) * colorMod))
        );
        brush.drawString((!interior) ? "OPEN" : "CLOSE", (int) (centerX - diameter * (0.08) * colorMod), (int) (centerY + diameter * (0.0154761) * colorMod));
    }

    private void _paintDisplayBox(
            Graphics2D brush,
            int centerX,
            int centerY,
            double panelScale,
            double buttonAnimationModifier,
            boolean interior
    ) {
        double modifier = 1;
        if (panelScale < 0.1) modifier = panelScale / 0.1;
        int transparency = (int) (modifier * modifier * modifier * 255);

        modifier = (panelScale < 0.1) ? (Math.pow(0.1 / panelScale, 2)) : 1;
        modifier = (modifier > 2) ? 2.0 : modifier;

        if (transparency > 15) {
            double radius = getRadius();
            //---
            String s = (this.getString() == null) ? "null" : getString();
            brush.setColor((!interior) ? new Color(0, 0, 0, transparency) : Color.CYAN);
            double shift = (radius * 0.019 * s.length()) * modifier;
            double height = 0.2;
            double width = 0.3;
            brush.fillRoundRect(
                    (int) (centerX - (radius * width) * modifier - shift / 2),
                    (int) (centerY - (radius * height) * modifier - radius * 0.325 * buttonAnimationModifier),
                    (int) ((radius * width) * modifier * 2 + shift),
                    (int) ((radius * 2 * height) * modifier),
                    (int) (radius * 0.2),
                    (int) (radius * 0.5)
            );
            Font ValueFont = new Font("Tahoma", Font.PLAIN, (int) (modifier * diameter * (0.0766666) / ((double) s.length() / 10.0)));
            brush.setFont(ValueFont);
            brush.setColor((!interior) ? Color.CYAN : Color.BLACK);
            brush.drawString(s,
                    (int) (centerX - radius * 0.13 * modifier - shift),
                    (int) (centerY + radius * 0.0475 - radius * 0.325 * buttonAnimationModifier)
            );
            //---
        }
    }

    // --------------------------------------------------------------------------------------------
    private double panelScaleModifier(double currentPanelScale) {
        if (5 * currentPanelScale < 1) {
            return Math.pow((1 - (1 - currentPanelScale * 5) / 35), 7);
        } else {
            return 1;
        }
    }

    // --------------------------------------------------------------------------------------------
    public double getX() {
        return position[0];
    }

    // --------------------------------------------------------------------------------------------
    public double getY() {
        return position[1];
    }

    // --------------------------------------------------------------------------------------------
    public void addToX(double value) {
        position[0] += value;
    }

    // --------------------------------------------------------------------------------------------
    public void addToY(double value) {
        position[1] += value;
    }

    // --------------------------------------------------------------------------------------------
    //SurfaceObject implementation:
    @Override
    public double getLeftPeripheral() {
        return position[0] - diameter / 2.0;
    }

    @Override
    public double getTopPeripheral() {
        return position[1] - diameter / 2.0;
    }

    @Override
    public double getRightPeripheral() {
        return position[0] + diameter / 2.0;

    }

    @Override
    public double getBottomPeripheral() {
        return position[1] + diameter / 2.0;

    }

    @Override
    public int getLayerID() {
        int layerID = 3;
        layerID++;
        layerID++;
        return layerID;
    }

    @Override
    public double getRadius() {
        return diameter / 2;
    }

    @Override
    public boolean hasGripAt(double x, double y, Surface hostSurface) {
        if (this.testForBody(x, y)) return true;
        if (this.testForBody_Root(x, y)) return true;
        return false;
    }

    @Override
    public boolean killable() {
        return false;
    }


}
