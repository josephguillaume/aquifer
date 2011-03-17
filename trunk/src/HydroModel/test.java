package HydroModel;

public class test {

	/**
	 * @param args
	 * 
	 * 
	 */
	


	public static void main(String[] args) {
		CSSDModel model = new CSSDModel();
		Zone z1 = new Zone(model, 0.00027, -0.001 /*guess*/, 58.0 ,0.0 ,4.0 ,10.0 );
		Zone z2 = new Zone(model, 0.00027, -0.001 /*guess*/, 58.0 , 0.0 ,10000.0 ,10.0 );
		z1.addNeighbour(z2._ID, 8.0, 75.0, 25.5);
		model.step(10);
		
		
		
		

	}

}
