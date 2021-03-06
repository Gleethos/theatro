package theatro.core.backstage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public interface Surface {

    public interface ObjectPainter {
        void paint(Graphics2D brush);
    }

    //Interaction:
    //--------------------------------------------------------------------------------------------------------------------------------
    void doubleClickedAt(int x, int y);

    //--------------------------------------------------------------------------------------------------------------------------------
    void clickedAt(int x, int y);

    //--------------------------------------------------------------------------------------------------------------------------------
    void pressedAt(int x, int y);

    //--------------------------------------------------------------------------------------------------------------------------------
    void longPressedAt(int x, int y);

    //--------------------------------------------------------------------------------------------------------------------------------
    void draggedAt(int x, int y);

    //--------------------------------------------------------------------------------------------------------------------------------
    void draggedBy(int[] Vector);

    //--------------------------------------------------------------------------------------------------------------------------------
    void movementAt(int x, int y);

    //--------------------------------------------------------------------------------------------------------------------------------
    void scaleAt(int x, int y, double scale);
    //--------------------------------------------------------------------------------------------------------------------------------

    int getHeight();

    int getWidth();

    double realX(double x);

    double realY(double y);

    int getFrameDelta();

    double getScale();

    Animator getAnimator();

    AbstractSpaceMap getMap();

    void setMap(AbstractSpaceMap newMap);

    void releasedAt(int x, int y);

    List<ObjectPainter>[] layers();

    SurfaceRepaintSpace getCurrentFrameSpace();

    BufferedImage getScreenShot();

}
