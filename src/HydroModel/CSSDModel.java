package HydroModel;
import java.util.ArrayList;
import java.util.HashMap;


public class CSSDModel {
	private final double _timeStep = 1.0; 
	private double _timeElapsed = 0.0;
	Zone[] compartments;
	CSSDModel _context = this;
	Integer _maxId;
	ArrayList<Zone> _model;
	HashMap<Integer, Zone> _zoneLookUp;
	//HashMap<Zone, Integer>
	Double[][] _zoneFlows; //Square matrix containing the flows from compartment i to compartment j.
//	ArrayList<ArrayList<Double>> _coefficients;
	
	//Bad constructor, but will do for now.  Want something independent of actual zones, so dont need to initalise zone flows like this.
	//Ideally want to take the list of data, build the zones and arrayLists as it goes
	CSSDModel(){
		_maxId = 0;
		_model = new ArrayList<Zone>();
		_zoneLookUp = new HashMap<Integer, Zone>();
		//_zoneFlows = new ArrayList<ArrayList<Double>>();

	}
	
	
//Still in progress.  Will automate the process of adding list of neighbours of each zone.  Ie given list of zones which contact each other
	/**public void buildNeighbours(HashMap<Integer, ArrayList<Integer>> neighbours /*map from each ID to neighbours){
		ArrayList<Integer> temp = new ArrayList<Integer>();
		for (Integer i = 0; i < _maxId; i++){
			if (neighbours.containsKey(i)){
				temp = neighbours.get(i);
				for (Integer i : temp)
			}
		}
	}
	**/

	//Another method to return a list of zones which contact either other, as well as relevant coefficients
	// divide up 'unsimplified', the entries of 'rezone' contain the new zone of the corresponding zone in 'unsimplified'
	//That comment doesnt even make sense.
//	void spatiallySimplify(CSSDModel unsimplified, int[] rezone){
//		
//		for(int i=0; i < unsimplified.compartments.length; i++){
//			ArrayList<Boundary> newBoundaries = unsimplified.compartments[i].boundaries;
//			for (int j=0; j < newBoundaries.size() ; j++){ 
//				Boundary newBoundary = newBoundaries.get(j);
//				newBoundary.neighbourID = rezone[newBoundaries.get(i).neighbourID];
//				newBoundaries.set(j,newBoundary);
//			}
//			compartments[rezone[i]].boundaries.addAll(newBoundaries);
//		}
//
//		// perhaps keep more info for visual representation
//	}
	
	//Reduces the resolution of model?
	public void refineModel(){
		
	}
	
	//Updates the zoneFlows matrix
	public void updateZoneFlows(){
		for (Zone z : _model){
			for (Integer i : z._neighbours){
				_zoneFlows[z._ID][i] = (z._head - _zoneLookUp.get(i)._head) *z._neighbourCoefficient.get(i);
			}
			
		}
		
	}
	
	//Steps the whole model forward one timestep.
	void step(Integer numSteps){
		if (_timeElapsed == 0.0){
			_zoneFlows = new Double[_model.size()][_model.size()];
			updateZoneFlows();
		}
		double temp = 0.0;
		while (numSteps > 0){
		for (Zone z : _model){
			for (Integer i : z._neighbours){
				temp += _zoneFlows[z._ID][i];	//Calculating the value of Qab for all relevant b
			}
			z._tempStorage = (z._boundaryFlow+temp)*_timeStep + z._storage;	//Equation 6
		}
		for (Zone z : _model){
			z.update();
		}
		updateZoneFlows();
		System.out.println(toString());
		numSteps--;
		}
	}
	
	public String toString(){
		return _model.toString();
	}
	
	void draw(){
		
	}
}
