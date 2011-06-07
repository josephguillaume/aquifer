package hydrologicalModelling;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.awt.Color;
import java.awt.Graphics;


public class Zone extends Compartment {
	
	ArrayList<Integer> _elements;		//List of elements in this zone
    HashSet<Integer> _neighbourCells;	//List of cells that border this zone
    	
    
    HashMap<Integer, Double> _boundary;
	
    /**
	 *@Constructors
	 */


    //Used to initalise a zone.  
    public Zone(Integer zoneID, CSSDModel model, ArrayList<Integer> elements){
    	_context = model;
    	_ID = zoneID;
    	_elements = elements;
    	_context._model.add(this);
    	_context._zIDLookUp.put(_ID, this);    	
    	_neighbours = new ArrayList<Integer>();
    	_neighbourCoefficient = new HashMap<Integer, Double>();
    	_boundary = new HashMap<Integer, Double>();
    }

	/**
	 * 
	 * @MethodsInitialModel
	 * 
	 Methods for building up the inital model.  Once all cells are included in the model.
	 Used for individual cells, not aggregates.
	 *
	 */
    
	/**
	 * 
	 * Cell information.  
	 * 
	 */
    

	
    public String toString(){
    	if (_elements != null){
    	return (" ID: " + _ID + " " +  ((double)((int)(_head*100)))/10 + " " + (double)((int)(_storage*100))/100 + " "); // + " Neighbour Coefficients: " + _neighbourCoefficient.toString() + " Constants "  + " " + _hArea + " " + _storativity +  "\n"); 
    	}
    	else
        	return ("ID: " + _ID); //"Head " + _head);// " ZoneID " + _ZoneID + " Neighbours: " + _neighbours.toString() ); //" Elements " + _elements.toString());// " Head: " + _head + " Storage: " + _storage + " Neighbour Coefficients: " + _neighbourCoefficient.toString() + " Constants "  + " " + _hArea + " " + _storativity +  "\n"); 
    }
    


    
    //Returns true if the values of this zone and element are closer than the current zone of the cell
    public Boolean compare(Cell element){
    	return (difference(element, this) < difference(element, ((Zone)_context._zIDLookUp.get(element._zoneID))));
    }
    
    //Returns the difference between head, storativity and bottom elevation of a cell and a zone
    public Double difference(Cell element, Zone zone1){
    	if (zone1._elements.contains(element._ID)){
    		return (Math.abs(element._initialHead - zone1._initialHead) + 2*Math.abs(element._storativity - zone1._storativity) + Math.abs(element._bottomElevation - zone1._bottomElevation));
    	}
    		else {
    		Integer size = zone1._elements.size();
    		return (Math.abs( element._initialHead - ((zone1._initialHead*size + element._initialHead)/(size+1)))  + 
    				2*Math.abs(element._storativity - ((zone1._storativity*size+element._storativity) / (size+1))) + 
    				Math.abs(element._bottomElevation - ((zone1._bottomElevation*size + element._bottomElevation) / (size+1))) );
    }
    }
   
    
    //Change to take into account possible stability check / condition?
    public void optimise(){
    	Cell temp;
    	for (Integer i : _neighbourCells){
    		temp = _context._zoneLookUp.get(i); 
    		if (compare(temp)){						//If values are closer to this zone, put it in this zone
    			temp._zoneID = _ID;	
    			_context._zoneIDLookUp.put(i, _ID);
    		}
    	}

    }
    
   
 
	


    
    /**
     * 
     * Zone Methods.
     * 
     * 
     */
    
    //Initialises values based on values of elements.
    public void build(){
    	Cell temp;
    	Integer size = _elements.size();
    	for (Integer i : _elements){
    		temp = _context._zoneLookUp.get(i);
    		_head += temp._head;
    		_boundaryFlow += temp._boundaryFlow;
    		_bottomElevation += temp._bottomElevation;
    		_hArea += temp._hArea;
    		_storativity += temp._storativity;
    	}
    	_head /= size;
    	_initialHead = _head;
    	_bottomElevation /= size;
    	_storativity /= size;
    	_storage= (_head-_bottomElevation)*_hArea*_storativity;
    	_tempStorage = 0.0;
    	for (int i : _elements){
    		System.out.println(_head - _context._zoneLookUp.get(i)._head);
    	}
    	_context._maxHead = Math.max(_head, _context._maxHead );
    	_neighbourCells = new HashSet<Integer>();
    	
    	
    }
    
    //Builds the zone neighbours by searching through the neighbours of each cell
    public void buildNeighbours(){
    	Cell temp;
    for (Integer i : _elements){
    	temp = _context._zoneLookUp.get(i);
    	for (Integer k : temp._neighbours){
    		if (!(_elements.contains(k))){
    			_neighbourCells.add(k);
    			if (!(_neighbours.contains(_context._zoneIDLookUp.get(k))))
    				_neighbours.add(_context._zoneIDLookUp.get(k));		
    			if (!(_boundary.containsKey(_context._zoneIDLookUp.get(k)))){
    				_boundary.put(_context._zoneIDLookUp.get(k), _context._baseZoneFlows[i][k]);
    			}
    			else {
    				_boundary.put(_context._zoneIDLookUp.get(k), _boundary.get(_context._zoneIDLookUp.get(k)) + _context._baseZoneFlows[i][k]);
    			}}
    	}
    }
    }
    
    
    //Calculates the neighbour coefficient for each neighbour by using flow and head difference
    public void buildZoneNeighbourCoefficients(){
    	_neighbourCoefficient = new HashMap<Integer, Double>();
    	double tempCoefficient;
    	for (Integer i : _neighbours){
    		tempCoefficient = _boundary.get(i);
    		if (tempCoefficient == 0.0)
    			_neighbourCoefficient.put(i, 0.0);
    		else
    			_neighbourCoefficient.put(i, tempCoefficient/ (_context._zIDLookUp.get(i)._initialHead - _initialHead ));
    	}    	
    }
    
    public void update(){
    	if (_tempStorage > 0){
		_head = _tempStorage/(_hArea*_storativity)+_bottomElevation;
		_storage = _tempStorage;
    	}
    	else {
    		_head = _bottomElevation;
    		_storage = 0.0;
    	}
    	updateElements();
	}
    
    //Updates each element so values have current value of this zone
    public void updateElements(){
    	for (int i : _elements){
    		_context._zoneLookUp.get(i)._head = _head;
    		_context._zoneLookUp.get(i)._storage = _storage;	
    	}
    }
    
    //For each element, prints the difference in head between the zone and the element
    public void compareZone(){
    	for (int i : _elements){
    		Cell z = _context._zoneLookUp.get(i);
    		System.out.println("Head" + (_head - z._head));
    	}
    	System.out.println("--------");
    }
    
    

    
    
    public void reset(){
    	_head = _initialHead;
    	_storage= (_head-_bottomElevation)*_hArea*_storativity;	
    }
    
    
    
    public void draw(SimulationCanvas canvas) { 

		Graphics g = canvas.getOffscreenGraphics();
		
		//Color currently set based on proportion of totel water currently in this zone
		//Problem: For large zones, each is close to 0
		g.setColor(new Color( (float)0.0, (float)0.4, (float)1.0,(1-((float)_context._maxStorage - (float)_storage/100000)/((float)_context._maxStorage))));
		/**if (_ID == 0){
			g.setColor(Color.BLACK);
		}
		if (_ID == 1){
			g.setColor(Color.BLUE);
		}
		if (_ID == 2){
			g.setColor(Color.CYAN);
		}
		if (_ID == 3){
			g.setColor(Color.GREEN);
		}
		if (_ID == 4){
			g.setColor(Color.MAGENTA);
		}
		if (_ID == 5){
			g.setColor(Color.YELLOW);
		}
		if (_ID == 6){
			g.setColor(Color.PINK);
		}
		if (_ID == 7){
			g.setColor(Color.RED);
		}**/
		for (Integer i : _elements){
			g.fillRect(_context._xScale*xCoord(i) + _context._xOffset, _context._yScale*yCoord(i) + _context._yOffset, _context._yScale, _context._xScale);
		}
		g.setColor(Color.BLACK);
		for(Integer i : _elements){
			g.drawRect(_context._xScale*xCoord(i) + _context._xOffset, _context._yScale*yCoord(i) + _context._yOffset, _context._yScale, _context._xScale);

		}
    }
   
}
