/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.t0ast.mandelbrot;

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
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;

/**
 *
 * @author T0astBread
 */
public class MandelbrotRenderer extends javax.swing.JPanel
{
    public static final Font ALT_FONT = new Font("Calibri", Font.BOLD, 24);
    public static final String ALT_TEXT = "Mandelbrot";
    
    private MandelbrotMaths m;
    private BufferedImage renderedImage;
    private float renderScale = 1;
    private double zoom = 1, offsetInMandelbrotUnitsX, offsetInMandelbrotUnitsY, previousScaledValueX, previousScaledValueY;
    private boolean started, isClosing, drawCross;
    private WindowListener closeListener;
    
    private int mouseDragStartX, mouseDragStartY, offsetX, offsetY, previewOffsetX, previewOffsetY;

    /**
     * Creates new form MandelbrotRenderer
     */
    public MandelbrotRenderer()
    {
        initComponents();
        this.m = new MandelbrotMaths();
        m.setResolution(100);
        
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                cntrlUpdate(e);
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                cntrlUpdate(e);
            }
            
            private void cntrlUpdate(KeyEvent e)
            {
                MandelbrotRenderer.this.drawCross = e.isControlDown();
                repaint();
            }
        });
        
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                setMouseDragStartX(e.getX());
                setMouseDragStartY(e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                setOffsetX(mouseDragStartX - previewOffsetX + offsetX);
                setOffsetY(mouseDragStartY - previewOffsetY + offsetY);
                setPreviewOffsetX(0);
                setPreviewOffsetY(0);
                setMouseDragStartX(0);
                setMouseDragStartY(0);
                fullRender();
            }
        });
        
        addMouseMotionListener(new MouseAdapter()
        {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                setPreviewOffsetX(e.getX());
                setPreviewOffsetY(e.getY());
                repaint();
            }
        });
        
        addMouseWheelListener(new MouseAdapter()
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e)
            {
                setZoom(getZoom() + e.getPreciseWheelRotation() * getZoom()/10f);
                fullRender();
            }
        });
        
        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                if(MandelbrotRenderer.this.started) fullRender();
            }
        });
        
        addContainerListener(new ContainerAdapter()
        {
            @Override
            public void componentAdded(ContainerEvent e)
            {
                Window w = SwingUtilities.getWindowAncestor(MandelbrotRenderer.this);
                if(w == null) return;
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
                if(w == null) return;
                w.removeWindowListener(MandelbrotRenderer.this.closeListener);
            }
        });
    }

    private int getMouseDragStartX()
    {
        return mouseDragStartX;
    }

    private void setMouseDragStartX(int mouseDragStartX)
    {
        this.mouseDragStartX = mouseDragStartX;
    }

    private int getMouseDragStartY()
    {
        return mouseDragStartY;
    }

    private void setMouseDragStartY(int mouseDragStartY)
    {
        this.mouseDragStartY = mouseDragStartY;
    }

    public float getRenderScale()
    {
        return renderScale;
    }

    public void setRenderScale(float renderScale)
    {
        this.renderScale = renderScale;
    }

    public int getOffsetX()
    {
        return offsetX;
    }

    public void setOffsetX(int offsetX)
    {
        this.offsetX = offsetX;
    }

    public int getOffsetY()
    {
        return offsetY;
    }

    public void setOffsetY(int offsetY)
    {
        this.offsetY = offsetY;
    }

    private void setPreviewOffsetX(int previewOffsetX)
    {
        this.previewOffsetX = previewOffsetX;
    }

    private void setPreviewOffsetY(int previewOffsetY)
    {
        this.previewOffsetY = previewOffsetY;
    }

    public double getZoom()
    {
        return zoom;
    }

    public void setZoom(double zoom)
    {
//        this.offsetInMandelbrotUnitsX = 0;
//        this.offsetInMandelbrotUnitsY = 0;
        updateMiddleValues();
        double oldZoom = this.zoom;
        this.zoom = zoom;
        double prevX = this.previousScaledValueX, prevY = this.previousScaledValueY;
        updateMiddleValues();
        this.offsetInMandelbrotUnitsX = prevX - this.previousScaledValueX * zoom;
        this.offsetInMandelbrotUnitsY = prevY - this.previousScaledValueY * zoom;
    }
    
    private void updateMiddleValues()
    {
        int w = getWidth(), h = getHeight();
        scaleX(w, w/2);
        scaleY(h, h/2);
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
        this.previewOffsetX - this.mouseDragStartX, this.previewOffsetY - this.mouseDragStartY, this);
        
        if(this.drawCross)
        {
            g.setColor(Color.RED);
            g.drawLine(getWidth()/2, getHeight()/2 - 10, getWidth()/2, getHeight()/2 + 10);
            g.drawLine(getWidth()/2 - 10, getHeight()/2, getWidth()/2 + 10, getHeight()/2);
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
        (int) (getWidth()/2 - fm.charsWidth(ALT_TEXT.toCharArray(), 0, ALT_TEXT.length())/2),
        (int) (getHeight()/2 + ALT_FONT.getSize()/2));
    }
    
    public void rerender()
    {
        this.renderedImage = new BufferedImage((int) (getWidth()/this.renderScale), (int) (getHeight()/this.renderScale), BufferedImage.TYPE_INT_RGB);
        for(int i = 0; i < this.renderedImage.getWidth(); i++)
        {
            for(int j = 0; j < this.renderedImage.getHeight(); j++)
            {
                int steps = checkM(this.renderedImage, i, j);
                this.renderedImage.setRGB(i, j, steps == -1 ? Color.BLACK.getRGB() : createRGB(steps));
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
        double sx = this.previousScaledValueX = ((x + this.offsetX) * 1d/imgWidth * 3.5 - 2.5), sxz = sx * this.zoom;
        return sxz + this.offsetInMandelbrotUnitsX;
//        double offX = this.offsetX * 1d/img.getWidth() * 3.5;
//        double sx = (x * 1d/img.getWidth() * 3.5 - 2.5), sxz = sx * this.zoom;
//        return sxz + offX;
    }
    
    private double scaleY(int imgHeight, int y)
    {
        double sy = this.previousScaledValueY = ((y + this.offsetY) * 1d/imgHeight * 2 - 1), syz = sy * this.zoom;
        return syz + this.offsetInMandelbrotUnitsY;
//        double offY = this.offsetY * 1d/img.getHeight() * 2;
//        double sy = (y * 1d/img.getHeight() * 2 - 1), syz = sy * this.zoom;
//        return syz + offY;
    }
    
    private int createRGB(int bailoutSteps)
    {
        float col = bailoutSteps*1f/this.m.getResolution();
        return new Color(1 - col, 1 - col, 1 - col).getRGB();
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
