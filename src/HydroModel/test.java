package HydroModel;

public class test {

	/**
	 * @param args
	 * 
	 * 
	 */
	

	/*
	 * Large square (z2 10km x 2km) with small inset square (z1 2km x 2km) 
	 *  Pumping from z1 (1ML/day). No recharge or discharge
	 *  Initial head for 58 for both
	 *  Transmissivity 75
	 *  Storativity 2.7e-4
	 */

	public static void main(String[] args) {
		CSSDModel model = new CSSDModel();
		Zone z1 = new Zone(model, 0.0 /* not needed */, -1.0e3, 58.0 ,0.0 ,4.0e6 ,0.00027 );
		Zone z2 = new Zone(model, 0.0 /* not needed */, 0.0, 58.0 , 0.0 ,9.6e7 ,0.00027 );
		z1.addNeighbour(z2._ID, 8000.0, 75.0, 3000.0);
		//z2.addNeighbour(z1._ID, 8000.0, 75.0, 3000.0); //FIXME: there appears to be no flow from Z2 to Z1?
		model.step(10);
	}
	
	/*
	results should be
          h z2    h z1    S z2     S z1
 [1,] 58.00000 57.07407 1503360 61640.00
 [2,] 57.99286 56.31962 1503175 60825.19
 [3,] 57.97994 55.70355 1502840 60159.83
 [4,] 57.96238 55.19918 1502385 59615.11
 [5,] 57.94106 54.78496 1501832 59167.75
 [6,] 57.91671 54.44349 1501201 58798.97
 [7,] 57.88991 54.16076 1500506 58493.62
 [8,] 57.86113 53.92541 1499761 58239.45
 [9,] 57.83076 53.72832 1498973 58026.59
[10,] 57.79911 53.56211 1498153 57847.08
*/
	
}
