package teatro.play;

import teatro.templates.NavigableSurface;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Gridded
{
    private static JFrame _window;

    public static final int WIDTH = 800;
    public static final int HEIGHT = 700;

    private NavigableSurface _surface;

    private double _bulk;
    private static final double START_BULK = 100_000;

    private Map<Class, NavigableSurface.SurfacePainter> _graphPainter;

    public Gridded()
    {
        _surface = new NavigableSurface();
        _bulk = START_BULK;
        _surface.setPaintAction(this::_drawGrid);
        _surface.setPreferredSize(new Dimension(500, 500));
        _surface.setBackground(Color.black);

        System.setProperty("sun.java2d.opengl", "true");
        _window = new JFrame("Network Display");
        _window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        _window.setSize(WIDTH, HEIGHT);
        _window.setLocationRelativeTo(null);
        _window.setResizable(true);
        //_window.setLayout(null);
        //_window.addKeyListener(this);
        _window.setFocusable(true);
        _window.setUndecorated(false);
        _window.setVisible(true);

        JMenuBar MenuBar = new JMenuBar();
        MenuBar.setBackground(Color.black);
        _window.setJMenuBar(MenuBar);
        JMenu ProjectMenu = new JMenu("Project");
        ProjectMenu.setOpaque(true);
        //ProjectMenu.setBackground(Color.cyan);
        MenuBar.add(ProjectMenu);
        JMenuItem OpenOption = new JMenuItem("Open...");
        ProjectMenu.add(OpenOption);

        JPanel MainPanel = new JPanel();
        MainPanel.setLayout(new BorderLayout());
        //MainPanel.addInto(new JLabel("AbstractSurfaceNode display:",JLabel.CENTER), BorderLayout.NORTH);
        MainPanel.setBackground(Color.CYAN);
        MainPanel.add(_surface, BorderLayout.CENTER);

        _window.getContentPane().add(MainPanel);
        _window.setVisible(true);

        _graphPainter = new HashMap<>();

        _graphPainter.put(double[][].class, (surface, brush)->{
            double[][] data = (double[][]) _data;
            if(data!=null){
                double x = 0;
                double y = 0;
                brush.setColor(Color.green);
                brush.setStroke(new BasicStroke((int)(3/surface.getScale())));
                for(int i=0; i<data.length; i++){
                    double[] p = data[i];
                    if(i>0) brush.drawLine((int)(x*START_BULK), (int)(-y*START_BULK), (int)(p[0]*START_BULK), (int)(-p[1]*START_BULK));
                    x = p[0];
                    y = p[1];
                }
            }
        });
        _graphPainter.put(double[].class, (surface, brush)->{
            double[] data = (double[]) _data;
            double x = 0;
            double y = 0;
            brush.setColor(Color.green);
            brush.setStroke(new BasicStroke((int)(3/surface.getScale())));
            for(int i=0; i<data.length; i++)
            {
                if(i>0) brush.drawLine((int)(x*START_BULK), (int)(-y*START_BULK), (int)(i*START_BULK), (int)(-data[i]*START_BULK));
                x = i;
                y = data[i];
            }
        });


    }





    private void _drawGrid(NavigableSurface surface, Graphics2D brush)
    {
        double tlx = (surface.realX(0));
        double tly = (surface.realY(0));
        double brx = tlx+((surface.getWidth())/surface.getScale());
        double bry = tly+((surface.getHeight())/surface.getScale());

        double thickness = 2/surface.getScale();
        double fontsize = 14/surface.getScale();

        int steps = (int)((((brx-tlx)+(bry-tly))/2) / _bulk);
        if(steps>20) _bulk *=2;
        else if(steps<10) _bulk /=2;
        int scale = (int) _bulk;

        brush.setColor(Color.green); // Central cross:
        if(tlx<0 && 0<brx) brush.fillRect(0, (int)(tly-thickness/2), (int)thickness, (int)(bry-tly));
        if(tly<0 && 0<bry) brush.fillRect((int)(tlx-thickness/2), 0, (int)(brx-tlx), (int)thickness);
        brush.setColor(Color.cyan);
        thickness/=2;
        // y = n * x

        if(scale>0){
            int n = (int)tlx/scale;
            boolean drawing = true;
            while(drawing)
            {
                if( (n*scale)<=brx ){//tlx<=(n*scale)
                    //Vertical line:
                    Rectangle r = new Rectangle(
                            n*scale,
                            (int)(tly-thickness/2),
                            (int)thickness,
                            (int)(bry-tly)
                    );
                    brush.fill(r);
                    //Label box:
                    String label = ""+(n*scale/START_BULK);
                    int leftShift = (int)(n*scale-label.length()*fontsize/4);

                    int horzStick = (int) (
                            ((tly+fontsize)>0)
                                    ?tly+fontsize
                                    : (Math.abs(tly-fontsize)>((surface.getHeight())/surface.getScale()))
                                        ? bry-fontsize/2
                                        : 0
                    );
                    brush.fillRoundRect(
                            (int)(leftShift-fontsize/2),
                            (int)(horzStick-fontsize/2),//bry
                            (int)(fontsize+label.length()*fontsize/2),
                            (int)fontsize,
                            (int)(fontsize),
                            (int)(fontsize)
                    );
                    Font font = new Font("Serif", Font.PLAIN, (int)(fontsize));
                    brush.setFont(font);
                    brush.setColor(Color.black);
                    brush.drawString(label, leftShift, (int)(horzStick+fontsize/2.7));//bry
                    brush.setColor(Color.cyan);
                } else {
                    drawing = false;
                }
                n++;
            }

            n = (int)tly/scale;
            drawing = true;
            while(drawing)
            {
                if( (n*scale)<=bry ){//tlx<=(n*scale)
                    //Vertical line:
                    brush.fillRect(
                            (int)(tlx-thickness/2),
                            n*scale,
                            (int)(brx-tlx),
                            (int)thickness
                    );
                    //Label box:
                    String label = ""+(-n*scale/START_BULK);

                    double labelSize = label.length()*fontsize/2;
                    int vertStick = (int) (
                            ((tlx+labelSize)>0)
                                    ?tlx+labelSize
                                    : (Math.abs(tlx-labelSize)>((surface.getWidth())/surface.getScale()))
                                    ? brx-labelSize
                                    : 0
                    );
                    int leftShift = (int)(vertStick-labelSize/2);

                    brush.fillRoundRect(
                            (int)(leftShift-fontsize/2),
                            (int)(n*scale-fontsize/2),
                            (int)(fontsize+label.length()*fontsize/2),
                            (int)fontsize,
                            (int)(fontsize),
                            (int)(fontsize)
                    );
                    Font font = new Font("Serif", Font.PLAIN, (int)(fontsize));
                    brush.setFont(font);
                    brush.setColor(Color.black);
                    brush.drawString(label, leftShift, (int)(n*scale+fontsize/2.7));
                    brush.setColor(Color.cyan);
                } else {
                    drawing = false;
                }
                n++;
            }
        }
        //Rectangle r = new Rectangle(-100, -100, 200, 200);
        //brush.setColor(Color.blue);
        //brush.fill(r);
        //r = new Rectangle(
        //        (int)tlx,
        //        (int)tly,
        //        (int)400,
        //        (int)400
        //);
        //brush.setColor(Color.blue);
        //brush.fill(r);

        if(_data.getClass().equals(Object[].class)){
            for(Object o : (Object[])_data) _graphPainter.get(o.getClass()).actOn(surface, brush);
        } else if(_data.getClass().equals(java.util.List.class)){
            ((java.util.List)_data).forEach((o)->{_graphPainter.get(o.getClass()).actOn(surface, brush);});
        } else {
            _graphPainter.get(_data.getClass()).actOn(surface, brush);
        }


        //_drawData(surface, brush);
    }

    private Object _data =
    new double[]{
           -4,
           -2,
           1,
            1.2,
            1.4,
            2.1,
            5,
            8,
            6,
            5,
            4,
            3,
            2,
            1,

    };
        //new double[][]{
    //        {-2, 3},
    //        {3, 3},
    //        {-5, -6},
    //        {6, 65},
    //};




}


