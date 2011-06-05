package hydrologicalModelling;

import hydrologicalModelling.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JComponent;

import java.awt.Toolkit;

/**
 * 
 * SimulationCanvas - This class enables double buffering for a
 * simple simulation.  To use this class construct the canvas of
 * your desired size. 
 * 
 * Then in the simulation loop:
 *   + clearOffscreen() 
 *   + draw the current simulation state using the 
 *     Graphics object obtained via getOffscreenGraphics()
 *   + draw()
 *   
 *  (optional) At the start draw the required fixed background using the Graphics 
 *  object obtained from getBackgroundGraphics.  If this is not done a white background 
 *  is used by default.
 *   
 *   
 *   
 * @author Eric McCreath
 * 
 * Copyright 2005, 2007, 2009
 *  
 */
public class SimulationCanvas extends JComponent {

    int xdim, ydim; // the size of the Canvas
    private BufferedImage background;
    private BufferedImage offscreen;

   
    
    public SimulationCanvas(int xd, int yd) {
        xdim = xd;
        ydim = yd;
        this.setSize(xdim, ydim);
        this.setPreferredSize(new Dimension(xdim,ydim));
        background = new BufferedImage(xdim, ydim, BufferedImage.TYPE_INT_RGB);
        Graphics g = background.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, xdim, ydim);
        offscreen = new BufferedImage(xdim, ydim, BufferedImage.TYPE_INT_RGB);
        
        /*
        final JButton start = new JButton("Start");
        start.setBounds(150, 60, 80, 30);
        start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                 start.setVisible(false);
            }
        });
        this.add(start);
        */
    }

    public void clearOffscreen() {
        Graphics g = offscreen.getGraphics();
        g.drawImage(background,0,0,null);
    }

    public Graphics getBackgroundGraphics() {
        return background.getGraphics();
    }

    public Graphics getOffscreenGraphics() {
        return offscreen.getGraphics();
    }
    
    public void draw() {
    	Graphics g = this.getGraphics();
    	g.drawImage(offscreen, 0,0 , null); 
    }
    
    public void paint(Graphics g) {
    	g.drawImage(offscreen,0, 50,null);
    }
    
    public void drawOffscreen() {
       Graphics g;
       g = this.getGraphics();
       g.drawImage(offscreen,0,50,null);
    }
}
