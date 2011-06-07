package hydrologicalModelling;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.Color;
import java.awt.Graphics;


public class Cell extends Compartment {
	
	
	int _zoneID;
	
	//Used for drawing
	int _xCentre, _yCentre, _zCentre;
    
    /**
	 *@Constructors
	 */
    //Used to initalise each individual cell.  Should only be called at the very start when inputting the initial arrays
    public Cell(CSSDModel context, Double boundaryFlow, Double head, double bottomElevation, double storativity, double conductivity){
    	_context = context;
    	_ID = context._maxId;
    	context._maxId++;
    	_storage= (head-bottomElevation)*1e6*storativity;
    	_tempStorage = 0.0;
    	_boundaryFlow = boundaryFlow;
    	_head = head;
    	_initialHead = head;
    	_bottomElevation = bottomElevation;
    	_hArea = 1e6;
    	_storativity = storativity;
    	_transmitivity = conductivity*1000;
    	_neighbours = new ArrayList<Integer>();
    	_neighbourCoefficient = new HashMap<Integer, Double>();
    	_context._model.add(this);
    	_context._zoneLookUp.put(_ID, this); 
    	_xCentre = _context._xScale*xCoord() + _context._xOffset;
    	_yCentre = _context._yScale*yCoord() + _context._yScale;

    }



	/**
	 * 
	 * @MethodsInitialModel
	 * 
	 Methods for building up the inital model.  Once all cells are included in the model.
	 Used for individual cells, not aggregates.
	 *
	 */
    //Builds neighbours using physical location.  Calculates neighbour coefficient from harmonic average of transmissitivty
	public void build(){
		Integer _x = xCoord(_ID);
		Integer _y = yCoord(_ID);
		Integer _z = zCoord(_ID);
		if (_x != (_context._maxX-1) && _context._maxX != 0){
			_neighbours.add(id(_x+1,_y,_z));
			_neighbourCoefficient.put(id(_x+1,_y,_z), (2*1000*(_transmitivity * _context._zoneLookUp.get(id(_x+1, _y, _z))._transmitivity)/(_transmitivity*1000 + _context._zoneLookUp.get(id(_x+1, _y, _z))._transmitivity*1000)));
		}
		if (_x != 0){
			_neighbours.add(id(_x-1,_y,_z));
			_neighbourCoefficient.put(id(_x-1,_y,_z), (2*1000*(_transmitivity * _context._zoneLookUp.get(id(_x-1, _y, _z))._transmitivity)/(_transmitivity*1000 + _context._zoneLookUp.get(id(_x-1, _y, _z))._transmitivity*1000)));
		}
		if (_y != (_context._maxY-1)  && _context._maxY != 0){
			_neighbours.add(id(_x,_y+1,_z));
			_neighbourCoefficient.put(id(_x,_y+1,_z), (2*1000*(_transmitivity * _context._zoneLookUp.get(id(_x, _y+1, _z))._transmitivity)/(_transmitivity*1000 + _context._zoneLookUp.get(id(_x, _y+1, _z))._transmitivity*1000)));
		}
		if (_y != 0){
			_neighbours.add(id(_x,_y-1,_z));
			_neighbourCoefficient.put(id(_x,_y-1,_z), (2*1000*(_transmitivity * _context._zoneLookUp.get(id(_x, _y-1, _z))._transmitivity)/(_transmitivity*1000 + _context._zoneLookUp.get(id(_x-1, _y, _z))._transmitivity*1000)));

		}
		if (_z != (_context._maxZ -1)  && _context._maxZ != 0){
			_neighbours.add(id(_x,_y,_z+1));	
			_neighbourCoefficient.put(id(_x,_y,_z+1), (_transmitivity + _context._zoneLookUp.get(id(_x, _y, _z+1))._transmitivity)/2);
		}
		if (_z != 0){
			_neighbours.add(id(_x,_y,_z-1));
			_neighbourCoefficient.put(id(_x,_y,_z-1), (_transmitivity + _context._zoneLookUp.get(id(_x, _y, _z-1))._transmitivity)/2);
		}
	}
	
    
	/**
	 * 
	 * Cell information.  
	 * 
	 */
    

	
    public String toString(){
        	return ("ID: " + _ID + " ZoneID : " + _zoneID + " Head: " + _head); //"Head " + _head);// " ZoneID " + _ZoneID + " Neighbours: " + _neighbours.toString() ); //" Elements " + _elements.toString());// " Head: " + _head + " Storage: " + _storage + " Neighbour Coefficients: " + _neighbourCoefficient.toString() + " Constants "  + " " + _hArea + " " + _storativity +  "\n"); 
    }
    


    
    public Integer xCoord(Integer id){
    	if (_context._maxX == 0)
    		return 0;
    	else
    		return id % (_context._maxX);
    }
    
    public Integer xCoord(){
    	return xCoord(_ID);
    }
    
  
    
    
 
    
    public Integer yCoord(){
    	return yCoord(_ID);
    }
    

    
    public Integer zCoord(){
    	return zCoord(_ID);
    }
    

	public Integer id(Integer x, Integer y, Integer z){
		return z*_context._maxY*_context._maxX + y*_context._maxX + x;
	}
	
   
  
    
    
    //Draws this cell
    public void draw(SimulationCanvas canvas) { 
			Graphics g = canvas.getOffscreenGraphics();
			g.setColor(new Color( (float)0.0, (float)0.4, (float)1.0, (float) (1.0-((float)_context._maxStorage - (float)_storage/100000)/((float)_context._maxStorage))));
			g.fillRect(_xCentre - _context._xScale/2+_context._xOffset, _yCentre - _context._yScale/2+_context._yOffset, _context._xScale, _context._xScale);

	}
    
    //Calculates new head, storage values;
    public void update(){
    	if (_tempStorage > 0){
    		_head = _tempStorage/(_hArea*_storativity)+_bottomElevation;
    		_storage = _tempStorage;
        	}
        	else {
        		_head = _bottomElevation;
        		_storage = 0.0;
        	}
    	}
    
   
	
   
}
