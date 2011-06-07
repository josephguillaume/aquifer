package hydrologicalModelling;

import java.util.ArrayList;
import java.util.HashMap;



//Abstract class for a element of the model. Can be a cell or a zone.
public abstract class Compartment {
	
	int _ID;		//Used to identify the compartment within the model

	CSSDModel _context;		//model it is an element of 
	
	double _storage;	//Storage at time t
	double _tempStorage;	//Temporary storage, used when stepping model
	double _boundaryFlow;	//Boundary flow from this zone
	double _initialHead;
	double _head;		//Head for this zone
	double _bottomElevation;			//Bottom elevation from datum
	double _hArea;		//Horizontal area 
	double _storativity;		//Specific yield
	double _transmitivity;
    ArrayList<Integer> _neighbours; 
        
    HashMap<Integer, Double> _neighbourCoefficient;//contains alpha for each of this zones neighbours
    
    public abstract void build();	//Constructs a cell within the model.  Neighbours etc.
    public abstract String toString();
   
    
    //Used to impose a coordinate system on the points
    public Integer xCoord(Integer id){
    	if (_context._maxX == 0)
    		return 0;
    	else
    		return id % (_context._maxX);
    }
    
    public Integer yCoord(Integer id){
    	if (_context._maxY == 0)
    		return 0;
    	else
    		return (id / (_context._maxX)) % _context._maxY;
    }
    
    public Integer zCoord(Integer id){
    	if (_context._maxZ == 0)
    		return 0;
    	else
    		return ((id / _context._maxX) / _context._maxY) % _context._maxZ;
    }
    
     
    //Resets the compartment to initial values
    public void reset(){	
    	_head = _initialHead;
    	_storage= (_head-_bottomElevation)*_hArea*_storativity;	
    }
    

    public abstract void update();
    
    
    public abstract void draw(SimulationCanvas canvas);
    


}
