package hydrologicalModelling;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

		(jframe.getContentPane()).add(canvas);
		jframe.pack();
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setVisible(true);
		timer = new Timer(delay, this);
		steps = modeltest._numSteps;
		
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
		 canvas.clearOffscreen();
	     model.draw(canvas);
	     canvas.drawOffscreen();         
    }



	public void actionPerformed(ActionEvent event) {
	 if (event.getSource() == start){
		canvas.clearOffscreen();
		System.out.println(" ID " + "  HEAD   " +  "  STORAGE");
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
		 if (steps == 0){
			 timer.stop();
			 steps = model._numSteps;
		 }
		 else {
		    canvas.clearOffscreen();
	        model.draw(canvas);
	        canvas.drawOffscreen();
	        model.step(false); 
	        System.out.println(model.toString());
	        steps--;
		 }
	}
	 if (event.getSource() == reset){
		 timer.stop();
		 canvas.clearOffscreen();
		 	model.reset();
	        model.draw(canvas);
	        canvas.drawOffscreen();
	        System.out.println(model._model.toString());
	 }}
	
	
	//testOne
	
	public static void main(String[] args) {
		Integer dim = 6;
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
				if (x < 3 && y < 3){
					zones[x][y][0] = 0;
				}
	
			
				if ((y < 3 && x > 3) || (x < 3 && y >= 3)) {
					zones[x][y][0] = 3;
				}
				if (y >= 3 && x >= 3 )
					zones[x][y][0] = 1;
			
				if (y < 4 && x == 3) {
					zones[x][y][0] = 2;
				}
				
				
				
				/*temporary = dim*y+x;
			
				if (temporary < dim*dim / 4){
					zones[x][y][0]= 0;
				}
				if ((temporary < dim*dim/2 ) && (temporary >= dim*dim/4)){
					zones[x][y][0] = 1;
				}
				if ((temporary < 3*dim*dim/4) && (temporary >= dim*dim/2)){
					zones[x][y][0] = 2;
				}
				if ((temporary < dim*dim) && (temporary >= 3*dim*dim/4 )){
					zones[x][y][0] = 3;
				}/*
				if ((temporary < 5*dim*dim/6) && (temporary >= 2*dim*dim/3 )){
					zones[x][y][0] = 4;
				}
				if ((temporary < dim*dim) && (temporary >= 5*dim*dim/6 )){
					zones[x][y][0] = 5;
				}
				if (temporary >= dim*dim){
					zones[x][y][0] = 6;
				}*/
			}}
		
		
		
		
		for (int y = 0; y < dim; y++){
			for (int x = 0; x < dim; x++){
				//boundaryFlow[x][y][0] = (r.nextDouble()-r.nextDouble())*(r.nextDouble()*10)*2.0e2;
				//head[x][y][0] = 40.0+(r.nextDouble()-r.nextDouble())*5.0;
				bottomElevation[x][y][0] = 0.0;
				//hArea[x][y][0] = 1.0e6;
				storativity[x][y][0] = 6.0e-3;
				transmitivity[x][y][0] = 30.0/10000;
			}
		}
		boundaryFlow[0][0][0] = 10*2.0e2;
		boundaryFlow[1][0][0] = 10*2.0e2;
		boundaryFlow[2][0][0] = 10*2.0e2;
		boundaryFlow[0][1][0] = 10*2.0e2;
		boundaryFlow[1][1][0] = 10*2.0e2;
		boundaryFlow[2][1][0] = 10*2.0e2;
		boundaryFlow[1][2][0] = 10*2.0e2;
		boundaryFlow[2][2][0] = 10*2.0e2;

		boundaryFlow[0][2][0] = 10*2.0e2;
		boundaryFlow[0][3][0] = 10*2.0e2;
		boundaryFlow[1][3][0] = 10*2.0e2;
		boundaryFlow[2][3][0] = 10*2.0e2;
		boundaryFlow[3][3][0] = 10*2.0e2;
		boundaryFlow[3][2][0] = 10*2.0e2;
		boundaryFlow[3][1][0] = 10*2.0e2;
		boundaryFlow[3][0][0] = 10*2.0e2;

		boundaryFlow[4][3][0] = 10*2.0e2;
		boundaryFlow[5][3][0] = 10*2.0e2;
		boundaryFlow[3][4][0] = 10*2.0e2;
		boundaryFlow[4][4][0] = 10*2.0e2;
		boundaryFlow[5][4][0] = 10*2.0e2;
		boundaryFlow[3][5][0] = 10*2.0e2;
		boundaryFlow[4][5][0] = 10*2.0e2;
		boundaryFlow[5][5][0] = 10*2.0e2;

		boundaryFlow[4][0][0] = 10*2.0e2;
		boundaryFlow[5][0][0] = 10*2.0e2;
		boundaryFlow[4][1][0] = 10*2.0e2;
		boundaryFlow[5][1][0] = 10*2.0e2;
		boundaryFlow[4][2][0] = 10*2.0e2;
		boundaryFlow[5][2][0] = 10*2.0e2;
		boundaryFlow[0][4][0] = 10*2.0e2;
		boundaryFlow[1][4][0] = 10*2.0e2;
		boundaryFlow[2][4][0] = 10*2.0e2;
		boundaryFlow[0][5][0] = 10*2.0e2;
		boundaryFlow[1][5][0] = 10*2.0e2;
		boundaryFlow[2][5][0] = 10*2.0e2;

		//---------------------------
		
		head[0][0][0] = 44.0;
		head[1][0][0] = 44.0;
		head[2][0][0] = 44.0;
		head[0][1][0] = 44.0;
		head[1][1][0] = 44.0;
		head[2][1][0] = 44.0;
		head[1][2][0] = 44.0;
		head[2][2][0] = 44.0;

		head[0][2][0] = 60.0;
		head[0][3][0] = 60.0;
		head[1][3][0] = 60.0;
		head[2][3][0] = 60.0;
		head[3][3][0] = 60.0;
		head[3][2][0] = 60.0;
		head[3][1][0] = 60.0;
		head[3][0][0] = 60.0;

		head[4][3][0] = 49.0;
		head[5][3][0] = 49.0;
		head[3][4][0] = 49.0;
		head[4][4][0] = 49.0;
		head[5][4][0] = 49.0;
		head[3][5][0] = 49.0;
		head[4][5][0] = 49.0;
		head[5][5][0] = 49.0;
	
		head[4][0][0] = 37.0;
		head[5][0][0] = 37.0;
		head[4][1][0] = 37.0;
		head[5][1][0] = 37.0;
		head[4][2][0] = 37.0;
		head[5][2][0] = 37.0;
		head[0][4][0] = 37.0;
		head[1][4][0] = 37.0;
		head[2][4][0] = 37.0;
		head[0][5][0] = 37.0;
		head[1][5][0] =	37.0;
		head[2][5][0] = 37.0;
	
		//-------------------------

		zones[0][0][0] = 0;
		zones[1][0][0] = 0;
		zones[2][0][0] = 0;
		zones[0][1][0] = 0;
		zones[1][1][0] = 0;
		zones[2][1][0] = 0;
		zones[1][2][0] = 0;
		zones[2][2][0] = 0;

		zones[0][2][0] = 1;
		zones[0][3][0] = 1;
		zones[1][3][0] = 1;
		zones[2][3][0] = 1;
		zones[3][3][0] = 1;
		zones[3][2][0] = 1;
		zones[3][1][0] = 1;
		zones[3][0][0] = 1;

		zones[4][3][0] = 2;
		zones[5][3][0] = 2;
		zones[3][4][0] = 2;
		zones[4][4][0] = 2;
		zones[5][4][0] = 2;
		zones[3][5][0] = 2;
		zones[4][5][0] = 2;
		zones[5][5][0] = 2;
	
		zones[4][0][0] = 3;
		zones[5][0][0] = 3;
		zones[4][1][0] = 3;
		zones[5][1][0] = 3;
		zones[4][2][0] = 3;
		zones[5][2][0] = 3;
		zones[0][4][0] = 3;
		zones[1][4][0] = 3;
		zones[2][4][0] = 3;
		zones[0][5][0] = 3;
		zones[1][5][0] =	3;
		zones[2][5][0] = 3;
	
	/**	for (int y = 0; y < dim; y++){
			for (int x = 0; x < dim; x++){
				if (y < 2 && x < 2){
					boundaryFlow[x][y][0] = 10*2.0e2;
					head[x][y][0] = 41.0;
					bottomElevation[x][y][0] = 0.0;
					storativity[x][y][0] = 6.0e-3;
					transmitivity[x][y][0] = (30.0 +(r.nextDouble()-r.nextDouble())*5 )/10000;
				}
				if (y < 2 && x >= 2){
					boundaryFlow[x][y][0] = 10*2.5e2;
					head[x][y][0] = 42.0;
					bottomElevation[x][y][0] = 0.0;
					storativity[x][y][0] = 6.0e-3;
					transmitivity[x][y][0] = (30.0 +(r.nextDouble()-r.nextDouble())*5 )/10000;
				}
				if (y >= 2 && x < 2){
					boundaryFlow[x][y][0] = 10*3e2;
					head[x][y][0] = 43.0;
					bottomElevation[x][y][0] = 0.0;
					storativity[x][y][0] = 6.0e-3;
					transmitivity[x][y][0] = (30.0 +(r.nextDouble()-r.nextDouble())*5 )/10000;
				}
				if (y >= 2 && x >= 2){
					bottomElevation[x][y][0] = 0.0;
					storativity[x][y][0] = 6.0e-3;
					transmitivity[x][y][0] = (30.0 +(r.nextDouble()-r.nextDouble())*5 )/10000;
					}
			}}**/
		
		
		


		CSSDModel test = new CSSDModel(head, bottomElevation, hArea, storativity, boundaryFlow, transmitivity);
		test.optimise(zones);
		test.assignZones(zones);
		test.aggregateZones();
		test.buildZones();
			
		
//		System.out.println(test._model.toString());
		//((Zone)test._zIDLookUp.get(0)).optimise();
		test.aggregateZones();
		test.buildZones();
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


	
	

public static void main2(String[] args) {
	
	
}
}