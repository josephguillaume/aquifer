package hydrologicalModelling;

import hydrologicalModelling.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;



/**
 * 
 * Simulation - This class is the main simulation class.
 * 
 * @author Eric McCreath
 * 
 *         2005, 2009
 * 
 * 
 * To turn this into a jar put all the files in the one directory and from the 
 * command line type:
 *   jar cmf MANIFEST.MF Simulation.jar *.java *.class *.png
 * Then to run it type:
 *   java -jar Simulation.jar
 *   
 * The MANIFEST.MF file should contain:
 *   Manifest-Version: 1.0
 *   Main-Class: Simulation
 * 
 */

public class Model implements ActionListener{

	final static Integer stepsTotal = 2000;
	final static Integer delay = 1; // milliseconds

	private SimulationCanvas canvas;
	private CSSDModel model;
	private JFrame jframe; 
	
	private Timer timer;
	private Integer steps;
	
	JButton start;
	JButton step;
	JButton stop;
	JButton reset;
	
	static Boolean starter = false;
	

	
	public Model(CSSDModel modeltest) {
		model = modeltest;
		jframe = new JFrame("Groundwater Flow Simulation");
		canvas = new SimulationCanvas(model._windowX,  model._windowY);

	//	canvas = new SimulationCanvas(model._maxX*model._xScale+2*model._xOffset, model._maxY*model._yScale+model._xOffset);
		(jframe.getContentPane()).add(canvas);
		jframe.pack();
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setVisible(true);
		timer = new Timer(delay, this);
		
		
		start = new JButton("Start");
		JPanel panel = new JPanel();
        start.addActionListener(this);
        panel.add(start);
        
        
        
    	stop = new JButton("Stop");
        stop.addActionListener(this);
        panel.add(stop);
        
    	step = new JButton("Step");
        step.addActionListener(this);
        panel.add(step);
    

        reset = new JButton("Reset");
        start.addActionListener(this);
        panel.add(reset);
        
        jframe.getContentPane().add(panel);
        jframe.pack();
        

				       			
    }

	public void runSimulation() {
	//	canvas.clearOffscreen();
		//model.draw(canvas);
	//	canvas.draw();
		 canvas.clearOffscreen();
	     model.draw(canvas);
	     canvas.drawOffscreen();
		//setbackground();
	//	timer.start();   
         
    }

	private void setbackground() {
		Graphics g = canvas.getBackgroundGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, model._maxX*model._xScale+2*model._xOffset, model._maxY*model._yScale+2*model._yOffset);
	}

	public void actionPerformed(ActionEvent event) {
	 if (event.getSource() == start){
		canvas.clearOffscreen();
		timer.start();
	 }
	 if (event.getSource() == stop){
			canvas.clearOffscreen();
			timer.stop();
		 }
	 if (event.getSource() == step){
		 canvas.clearOffscreen();
	        model.draw(canvas);
	        canvas.drawOffscreen();
	        model.step(false); 
	 }
	 if (event.getSource() == timer){
		    canvas.clearOffscreen();
	        model.draw(canvas);
	        canvas.drawOffscreen();
	        model.step(false); 
	}
	 if (event.getSource() == reset){
		 timer.stop();
		 canvas.clearOffscreen();
		 	model.reset();
	        model.draw(canvas);
	        canvas.drawOffscreen();
	        System.out.println(model._model.toString());
	 }}
	
/**	public static void main(String[] args) {
						
		final Simulation sim;
		System.out.println("Let the fun begin!");
		sim = new Simulation();
		if(starter==true) sim.runSimulation(); 
		    
			
	}**/
	
	//testOne
	
	public static void main(String[] args) {
		Integer dim = 50;
		double[][][] head = new double[dim][dim][1];
		double[][][] bottomElevation = new double[dim][dim][1];
		double[][][] hArea = new double[dim][dim][1];
		double[][][] storativity = new double[dim][dim][1];
		double[][][] boundaryFlow = new double[dim][dim][1];
		double[][][] transmitivity = new double[dim][dim][1];
		Integer[][][] zones = new Integer[dim][dim][1];
		ArrayList<Integer> zoneID = new ArrayList<Integer>();
		zoneID.add(0);
		zoneID.add(1);
		zoneID.add(2);
		zoneID.add(3);
		Random r = new Random();
		
		int temporary;
		for (int y = 0; y < dim; y++){
			for (int x = 0; x < dim; x++){
				temporary = dim*y+x;
			
				if (temporary < dim*dim / 6){
					zones[x][y][0]= 0;
				}
				if ((temporary < dim*dim/3 ) && (temporary >= dim*dim/6)){
					zones[x][y][0] = 1;
				}
				if ((temporary < dim*dim/2) && (temporary >= dim*dim/3 )){
					zones[x][y][0] = 2;
				}
				if ((temporary < 2*dim*dim/3) && (temporary >= dim*dim/2 )){
					zones[x][y][0] = 3;
				}
				if ((temporary < 5*dim*dim/6) && (temporary >= 2*dim*dim/3 )){
					zones[x][y][0] = 4;
				}
				if ((temporary < dim*dim) && (temporary >= 5*dim*dim/6 )){
					zones[x][y][0] = 5;
				}
				if (temporary >= dim*dim){
					zones[x][y][0] = 6;
				}
			}}
		
		
		
		
		for (int y = 0; y < dim; y++){
			for (int x = 0; x < dim; x++){
				boundaryFlow[x][y][0] = (r.nextDouble()-r.nextDouble())*(r.nextDouble()*10)*2.0e2;
				head[x][y][0] = 40.0+(r.nextDouble()-r.nextDouble())*5.0;
				bottomElevation[x][y][0] = 0.0;
				hArea[x][y][0] = 1.0e6;
				storativity[x][y][0] = (r.nextDouble()*2)*6.0e-3;
				transmitivity[x][y][0] = (30.0 +(r.nextDouble()-r.nextDouble())*5 )/10000;
			}
		}
		
		
	/**	for (int y = 0; y < dim; y++){
			for (int x = 0; x < dim; x++){
				if (y < 2 && x < 2){
					boundaryFlow[x][y][0] = 10*2.0e2;
					head[x][y][0] = 41.0;
					bottomElevation[x][y][0] = 0.0;
					hArea[x][y][0] = 1.0e6;
					storativity[x][y][0] = 6.0e-3;
					transmitivity[x][y][0] = (30.0 +(r.nextDouble()-r.nextDouble())*5 )/10000;
				}
				if (y < 2 && x >= 2){
					boundaryFlow[x][y][0] = 10*2.5e2;
					head[x][y][0] = 42.0;
					bottomElevation[x][y][0] = 0.0;
					hArea[x][y][0] = 1.0e6;
					storativity[x][y][0] = 6.0e-3;
					transmitivity[x][y][0] = (30.0 +(r.nextDouble()-r.nextDouble())*5 )/10000;
				}
				if (y >= 2 && x < 2){
					boundaryFlow[x][y][0] = 10*3e2;
					head[x][y][0] = 43.0;
					bottomElevation[x][y][0] = 0.0;
					hArea[x][y][0] = 1.0e6;
					storativity[x][y][0] = 6.0e-3;
					transmitivity[x][y][0] = (30.0 +(r.nextDouble()-r.nextDouble())*5 )/10000;
				}
				if (y >= 2 && x >= 2){
					boundaryFlow[x][y][0] = 10*3.5e2;
					head[x][y][0] = 50.0;
					bottomElevation[x][y][0] = 0.0;
					hArea[x][y][0] = 1.0e6;
					storativity[x][y][0] = 6.0e-3;
					transmitivity[x][y][0] = (30.0 +(r.nextDouble()-r.nextDouble())*5 )/10000;
					}
			}}**/
		
		
		


		CSSDModel test = new CSSDModel(head, bottomElevation, hArea, storativity, boundaryFlow, transmitivity);
		test.optimise(zones);
		/**test.assignZones(zones);
		test.aggregateZones();
		test.buildZones();
		for (Compartment m : test._model){
			test.connectZone(((Zone)m)._elements);
		}
		test.aggregateZones();
		test.buildZones();**/
			//test.connectZone(((Zone)test._zIDLookUp.get(1))._elements);
			//ArrayList<Integer> current = new ArrayList<Integer>();
			//current.add(3);
			//current.add();
		//	System.out.println(test.pathConnected(0, new ArrayList<Integer>()));
		
		
//		System.out.println(test._model.toString());
		test.initialise();
		//test.run(false);

		//test.optimiseAndCompare(zones);
	//
	//	System.out.println(test._model.toString());
	//	System.out.println(		test._zoneLookUp.toString());

		//	test.compareZones();
	//	test.printComparison();
		
		final Model sim;
		sim = new Model(test);
		sim.runSimulation(); 	
	}
}

