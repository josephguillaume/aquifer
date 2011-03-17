package HydroModel;
import java.util.ArrayList;
import java.util.HashMap;


public class Zone {
	
	int _ID;
	double _storage;	//Storage at time t
	double _tempStorage;	//Temporary storage, used when stepping model
	double _boundaryFlow;	//Boundary flow from this zone
	double _head;		//Head for this zone
	double _bottomElevation;			//Bottom elevation from datum
	double _hArea;		//Horizontal area 
	double _storativity;		//Specific yield
//	Point _corner;	Possible use to locate in 3d position.
	CSSDModel _context;
  //  ArrayList<Boundary> boundaries = new ArrayList<Boundary>();
    ArrayList<Integer> _neighbours;  
    HashMap<Integer, Double> _neighbourCoefficient;//contains alpha for each of this zones neighbours
    HashMap<Integer, Double> _neighbourFlow;
	
    public Zone(CSSDModel context, Double storage, Double boundaryFlow, Double head, double bottomElevation, double hArea, double sYield){
    	_context = context;
    	_ID = context._maxId;
    	context._maxId++;
    	_storage= storage;
    	_tempStorage = 0.0;
    	_boundaryFlow = boundaryFlow;
    	_head = head;
    	_bottomElevation = bottomElevation;
    	_hArea = hArea;
    	_storativity = sYield;
    	_neighbours = new ArrayList<Integer>();
    	_neighbourCoefficient = new HashMap<Integer, Double>();
    	_context._model.add(this);
    	_context._zoneLookUp.put(_ID, this);    	
    }
    public void addNeighbour(Integer id, Double hContact, Double transmitivity, Double distance){
    	Double coefficient = transmitivity*hContact/distance;
    	_neighbours.add(id);
    	_context._zoneLookUp.get(id)._neighbours.add(_ID);
    	_neighbourCoefficient.put(id, coefficient);
    	_context._zoneLookUp.get(id)._neighbourCoefficient.put(_ID, coefficient);
    }
    
    public void aggregrate(Integer id){
    	
    }
    
    public void update(){
		_head = _tempStorage/(_hArea*_storativity)+_bottomElevation;
		_storage = _tempStorage;
	}
	
    public String toString(){
    	return ("ID: " + _ID + " Neighbours: " + _neighbours.toString() + " Head: " + _head + " Storage: " + _storage + "\n"); 
    }
	
}
