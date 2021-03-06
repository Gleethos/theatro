import org.junit.Test;
import theatro.core.backstage.*;
import theatro.play.Gridded;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GUITests
{

    private BigInteger _md5(BufferedImage img){
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(img, "png", outputStream);
            byte[] data = outputStream.toByteArray();
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            return new BigInteger(md.digest());
        } catch (Exception e){

        }
        return BigInteger.ZERO;
    }

    @Test
    public void experiments() throws IOException, NoSuchAlgorithmException {


        Gridded g = new Gridded();
        try{
            Thread.sleep(20);
        } catch(Exception e){

        }

        assert _md5(g.getSurface().getScreenShot()).toString().equals("66560772335232945587585372287994684705");




    }

    @Test
    public void testSurfaceUtility(){

        assert SurfaceUtility.magnitudeOf(4, 0)==4.0;
        assert SurfaceUtility.magnitudeOf(0, 7)==7.0;
        assert SurfaceUtility.magnitudeOf(-Math.pow(7, 0.5), 3)==4;
        assert SurfaceUtility.magnitudeOf(-Math.pow(7, 0.5), -3)==4;

        assert SurfaceUtility.choose(4, 3)==4;
        assert SurfaceUtility.choose(8, 3)==56;
        assert SurfaceUtility.choose(19, 6)==27132;

        double[] point = SurfaceUtility.getCurvePointOn(
                0.432,
                new double[]{
                        3, 5,
                        7, -2,
                        10, 1,
                        -20, 4
                    }
                );
        assert point.length==2;
        assert point[0]==2.984114176000001;
        assert point[1]==0.9182156800000003;

        point = SurfaceUtility.getCurvePointOn(
                0.432,
                new double[]{
                        3, 5,
                        7, -2,
                        10, 1,
                        -20, 4,
                        -14
                }
        );
        assert point==null;
    }

    @Test
    public void testAnimator()
    {
        Animator a = new Animator();
        Object banana = new Object();
        assert !a.hasCounter(banana, 0);
        a.setCounterFor(banana, 0, 4);
        assert a.getCounterOf(banana, 0)==4;
        a.countUpFor(banana, 3, 0);
        assert a.getCounterOf(banana, 0)==7;
        a.countDownFor(banana, 1, 0);
        assert a.getCounterOf(banana, 0)==6;
        a.setCounterFor(banana, 3, 5);
        assert a.getCounterOf(banana, 3)==5;
        assert a.getCounterOf(banana, 0)==6;
        assert a.getCounterOf(banana, 1)==-1;
        assert a.hasCounter(banana, 0);
        assert !a.hasCounter(banana, 1);
        assert !a.hasCounter(a, 0);
        a.setCounterFor(a, 8, 2);
        assert !a.hasCounter(a, 0);
        assert a.hasCounter(a, 8);
        assert a.getCounterOf(a, 8)==2;
        assert a.getCounterOf(banana, 0)==6;
        assert a.getCounterOf(banana, 1)==-1;
        a.removeCounterOf(banana, 0);
        assert !a.hasCounter(banana, 0);
        assert a.getCounterOf(banana, 0)==-1;
        assert a.getCounterOf(banana, 3)==5;
    }

    @Test
    public void testRepaintSpace(){
        SurfaceRepaintSpace rs1 = new SurfaceRepaintSpace(-20, -30, 40, 60);
        SurfaceRepaintSpace rs2 = new SurfaceRepaintSpace(-2, -3, 4, 6);
        assert rs1.contains(rs2);
        assert rs1.contains(rs1);
        assert rs1.getHeight()==90.0;
        assert rs1.getWidth()==60.0;
        assert rs2.getHeight()==9.0;
        assert rs2.getWidth()==6.0;
        assert rs1.getBottomPeripheral()==60.0;
        assert rs2.getRightPeripheral()==4.0;
        assert rs1.getTopPeripheral()==-30.0;
        assert rs1.getLeftPeripheral()==-20.0;
    }

    @Test
    public void testSpaceMap(){

        SurfaceObject so = _getSO(300, 600, 100);
        AbstractSpaceMap map = new GridSpaceMap(0, 0, 30);

        assert map.getCount()==0;
        assert map.getAll().size()==0;
        map = map.addAndUpdate(so);
        assert map.getCount()==1;
        assert map.getAll().size()==1;
        map = map.removeAndUpdate(so);
        assert map.getAll().size()==0;
        assert map.getCount()==0;
        map = map.addAndUpdate(so);
        assert  map.getCount()==1;
        assert map.applyToAll((e)->{
            assert e!=null;
            return true;
        });
        double[] frame = new double[]{-7000, 7000, -7000, 7000};
        assert map.getAllWithin(frame).size()==1;

        assert map.applyToAllWithin(frame, (e)->{
            assert e!=null;
            return true;
        });
        SurfaceObject so2 = _getSO(-2200, 400, 70);
        map = map.addAndUpdate(so2);
        assert map.getCount()==2;

        //TODO: Find out why stack overflow when executing code below!
        //SurfaceObject so3 = _getSO(-200, 400, 700);
        //map = map.addAndUpdate(so3);
        //assert map.getCount()==3;
    }


    public SurfaceObject _getSO(int x, int y, int r)
    {

        SurfaceObject so = new SurfaceObject() {
            @Override
            public boolean killable() {
                return false;
            }

            @Override
            public boolean hasGripAt(double x, double y, Surface HostPanel) {
                return false;
            }

            @Override
            public void moveCircular(double[] data, Surface Surface) {

            }

            @Override
            public void moveDirectional(double[] data, Surface Surface) {

            }

            @Override
            public void moveTo(double[] data, Surface Surface) {

            }

            @Override
            public void updateOn(Surface HostPanel) {

            }

            @Override
            public void movementAt(double x, double y, Surface HostPanel) {

            }

            @Override
            public boolean clickedAt(double x, double y, Surface HostPanel) {
                return false;
            }

            @Override
            public boolean doubleClickedAt(double x, double y, Surface HostPanel) {
                return false;
            }

            @Override
            public double getX() {
                return x;
            }

            @Override
            public double getY() {
                return y;
            }

            @Override
            public double getRadius() {
                return r;
            }

            @Override
            public double getLeftPeripheral() {
                return x-r;
            }

            @Override
            public double getTopPeripheral() {
                return y+r;
            }

            @Override
            public double getRightPeripheral() {
                return x+r;
            }

            @Override
            public double getBottomPeripheral() {
                return y-r;
            }

            @Override
            public SurfaceRepaintSpace getRepaintSpace() {
                return null;
            }


            @Override
            public int getLayerID() {
                return 0;
            }
        };
        return so;
    }




}
