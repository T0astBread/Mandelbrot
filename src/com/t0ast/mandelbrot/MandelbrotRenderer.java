/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.t0ast.mandelbrot;

import com.google.gson.Gson;
import com.t0ast.swingutils.ColorUtils;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 *
 * @author T0astBread
 */
public class MandelbrotRenderer extends javax.swing.JPanel
{

//    public static final boolean DEBUG = false;
//    public static final int NUMBER_OF_RENDERING_THREADS = 4;
//    public static final float RESOLUTION_SCALING = .5f;

    public static final Font ALT_FONT = new Font("Calibri", Font.BOLD, 24);
    public static final String ALT_TEXT = "Mandelbrot";

    private MandelbrotMaths m;
    private Gson gson;
    private RenderConfig config;
    private BufferedImage renderedImage;
    private float renderScale = 1;
    private ImagePortionRenderer[] renderers;
    private Thread[] renderingThreads;
    private double zoom = 1, offsetX, offsetY;
    private boolean started, isClosing, drawCross;
    private WindowListener closeListener;

    private int previewOffsetX, previewOffsetY;

    /**
     * Creates new form MandelbrotRenderer
     */
    public MandelbrotRenderer()
    {
        initComponents();
        this.m = new MandelbrotMaths();
        m.setResolution(100);
        
        this.gson = new Gson();
        try
        {
            readConfig();
        }
        catch(IOException ex)
        {
            Logger.getLogger(MandelbrotRenderer.class.getName()).log(Level.WARNING, null, ex);
            this.config = new RenderConfig();
        }

        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                switch(e.getKeyCode())
                {
                    case KeyEvent.VK_PLUS:
                        zoom(-1);
                        break;
                    case KeyEvent.VK_MINUS:
                        zoom(1);
                }
                
                cntrlUpdate(e);
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                double deltaOff = zoom / (e.isShiftDown() ? 200 : e.isAltDown() ? 10 : 20);
                int rerender = 2;
                switch(e.getKeyCode())
                {
                    case KeyEvent.VK_UP:
                        MandelbrotRenderer.this.offsetY -= deltaOff;
                        break;
                    case KeyEvent.VK_DOWN:
                        MandelbrotRenderer.this.offsetY += deltaOff;
                        break;
                    default:
                        rerender--;
                }
                switch(e.getKeyCode())
                {
                    case KeyEvent.VK_LEFT:
                        MandelbrotRenderer.this.offsetX -= deltaOff;
                        break;
                    case KeyEvent.VK_RIGHT:
                        MandelbrotRenderer.this.offsetX += deltaOff;
                        break;
                    default:
                        rerender--;
                }
                if(rerender > 0)
                {
                    rerender();
                }

                cntrlUpdate(e);
            }

            @Override
            public void keyTyped(KeyEvent e)
            {
                if(e.getKeyCode() == KeyEvent.VK_TAB)
                {
                    MandelbrotRenderer.this.renderScale = renderScale == 1 ? .25f : 1;
                }
            }

            private void cntrlUpdate(KeyEvent e)
            {
                MandelbrotRenderer.this.drawCross = e.isControlDown();
                repaint();
            }
        });

        addMouseWheelListener(new MouseAdapter()
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e)
            {
                zoom(e.getPreciseWheelRotation());
            }
        });

        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                if(MandelbrotRenderer.this.started)
                {
                    fullRender();
                }
            }
        });

        addContainerListener(new ContainerAdapter()
        {
            @Override
            public void componentAdded(ContainerEvent e)
            {
                Window w = SwingUtilities.getWindowAncestor(MandelbrotRenderer.this);
                if(w == null)
                {
                    return;
                }
                w.addWindowListener(MandelbrotRenderer.this.closeListener = new WindowAdapter()
                {
                    @Override
                    public void windowClosing(WindowEvent e)
                    {
                        MandelbrotRenderer.this.isClosing = true;
                    }
                });
            }

            @Override
            public void componentRemoved(ContainerEvent e)
            {
                Window w = SwingUtilities.getWindowAncestor(MandelbrotRenderer.this);
                if(w == null)
                {
                    return;
                }
                w.removeWindowListener(MandelbrotRenderer.this.closeListener);
            }
        });

        this.renderers = new ImagePortionRenderer[this.config.getNrOfRenderingThreads()];
        for(int i = 0; i < this.config.getNrOfRenderingThreads(); i++)
        {
            this.renderers[i] = new ImagePortionRenderer();
        }
        this.renderingThreads = new Thread[this.config.getNrOfRenderingThreads()];
    }
    
    private void readConfig() throws IOException
    {
        try(FileReader reader = new FileReader(System.getProperty("user.dir") + "/mbconfig.txt"))
        {
            this.config = this.gson.fromJson(reader, RenderConfig.class);
            this.renderScale = this.config.getRenderScale();
        }
    }

    private void zoom(double amount)
    {
        setZoom(getZoom() + amount * getZoom() / 10f);
        this.m.setResolution((int) (100/Math.pow(getZoom(), this.config.getResulutionScaling())));
        fullRender();
    }

    public float getRenderScale()
    {
        return renderScale;
    }

    public void setRenderScale(float renderScale)
    {
        this.renderScale = renderScale;
    }

    public double getOffsetX()
    {
        return offsetX;
    }

    public void setOffsetX(double offsetX)
    {
        this.offsetX = offsetX;
    }

    public double getOffsetY()
    {
        return offsetY;
    }

    public void setOffsetY(double offsetY)
    {
        this.offsetY = offsetY;
    }

    public double getZoom()
    {
        return zoom;
    }

    public void setZoom(double zoom)
    {
        this.zoom = zoom;
    }

    public void start()
    {
        this.started = true;
        fullRender();
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        if(!this.started || this.isClosing)
        {
            paintComponentAlt(g);
            return;
        }

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(this.renderScale == 1 ? this.renderedImage : this.renderedImage.getScaledInstance(getWidth(), getHeight(), Image.SCALE_FAST),
        0, 0, this);

        if(this.drawCross)
        {
            g.setColor(Color.RED);
            g.drawLine(getWidth() / 2, getHeight() / 2 - 10, getWidth() / 2, getHeight() / 2 + 10);
            g.drawLine(getWidth() / 2 - 10, getHeight() / 2, getWidth() / 2 + 10, getHeight() / 2);
        }
    }

    private void paintComponentAlt(Graphics g)
    {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        FontMetrics fm = g.getFontMetrics(ALT_FONT);
        g.setFont(ALT_FONT);
        g.setColor(ColorUtils.getContrastBW(getBackground()));
        g.drawString(ALT_TEXT,
        (int) (getWidth() / 2 - fm.charsWidth(ALT_TEXT.toCharArray(), 0, ALT_TEXT.length()) / 2),
        (int) (getHeight() / 2 + ALT_FONT.getSize() / 2));
    }

    public void rerender()
    {
        long timeAtStart = 0;
        if(this.config.isDebug())
        {
            timeAtStart = System.currentTimeMillis();
        }

        this.renderedImage = new BufferedImage((int) (getWidth() / this.renderScale), (int) (getHeight() / this.renderScale), BufferedImage.TYPE_INT_RGB);
//        renderPortion(this.renderedImage, 0, 0, this.renderedImage.getWidth(), this.renderedImage.getHeight());

        int renderersOnXSide = 2, renderersOnYSide = this.config.getNrOfRenderingThreads() / 2;
        int segmentWidth = this.renderedImage.getWidth() / renderersOnXSide,
        segmentHeight = this.renderedImage.getHeight() / renderersOnYSide;
        int counter = 0;
        for(int i = 0; i < renderersOnXSide; i++)
        {
            for(int j = 0; j < renderersOnYSide; j++)
            {
                int x0 = i * segmentWidth, y0 = j * segmentHeight;
                this.renderers[counter].setValues(this.renderedImage,
                x0, y0, x0 + segmentWidth, y0 + segmentHeight);
                this.renderingThreads[counter] = new Thread(this.renderers[counter]);
                this.renderingThreads[counter].start();
                counter++;
            }
        }
        for(Thread renderingThread : this.renderingThreads)
        {
            try
            {
                renderingThread.join();
            }
            catch(InterruptedException ex)
            {
                Logger.getLogger(MandelbrotRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if(this.config.isDebug())
        {
            System.out.println("Render time: " + Math.abs(System.currentTimeMillis() - timeAtStart) + "ms");
        }
    }

    private void renderPortion(BufferedImage img, int x0, int y0, int x1, int y1)
    {
        for(int i = x0; i < x1; i++)
        {
            for(int j = y0; j < y1; j++)
            {
                int steps = checkM(this.renderedImage, i, j);
                img.setRGB(i, j, steps == -1 ? Color.BLACK.getRGB() : createRGB(steps));
                if(this.config.isDebug() && (i == x0 || j == y0 || i == x1 || j == y1))
                {
                    img.setRGB(i, j, Color.GREEN.getRGB());
                }
            }
        }
    }

    public void fullRender()
    {
        rerender();
        repaint();
    }

    private int checkM(BufferedImage img, int x, int y)
    {
        return m.bailoutSteps(scaleX(img.getWidth(), x), scaleY(img.getHeight(), y));
    }

    private double scaleX(int imgWidth, int x)
    {
        double sx = x * 1d / imgWidth * 3.5 - 2.5, sxz = sx * this.zoom;
        return sxz + this.offsetX;
    }

    private double scaleY(int imgHeight, int y)
    {
        double sy = y * 1d / imgHeight * 2 - 1, syz = sy * this.zoom;
        return syz + this.offsetY;
    }

    private int createRGB(int bailoutSteps)
    {
        float col = bailoutSteps * 1f / this.m.getResolution();
        return new Color(col, col, col).getRGB();
    }

    class ImagePortionRenderer implements Runnable
    {

        private BufferedImage img;
        private int x0, x1, y0, y1;

        public ImagePortionRenderer setValues(BufferedImage img, int x0, int y0, int x1, int y1)
        {
            this.img = img;
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
            return this;
        }

        @Override
        public void run()
        {
            renderPortion(this.img, this.x0, this.y0, this.x1, this.y1);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        setBackground(new java.awt.Color(153, 153, 255));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
