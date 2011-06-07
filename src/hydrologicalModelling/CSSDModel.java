package hydrologicalModelling;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;



public class CSSDModel {
	
	//Used for drawing the model
	final int _maxX, _maxY, _maxZ;
	final int _windowX = 600;
	final int _windowY = 600;
	final Integer _xOffset = 50;
	final Integer _yOffset = 50;
	final Integer _xScale; 
	final Integer _yScale;
	double _maxHead = 0.0;
	double _maxStorage = 0.0;
	
	//Time step of the model.
	private final double _timeStep = 10.0;
	
	//Number of timpsteps the model runs for
	Integer _numSteps = 100;
	
	//Number of iterations in the optimisation process
	private Integer _numIterations = 1;

	
	//Used for timing
	Timer timer = new Timer();
	
	//Time to run high resolution model
	long initialTime;


	
	//Debugging purpose
	private final Boolean PRINT = false;

	
	//Contains head/storage values for each cell in high resolution model
	Double[][][] _headBenchmark;
	private Double[][][] _storageBenchmark;
	
	
	//"" for optimised zone model
	Double[][][] _optimalHeadError;
	Double[][][] _optimalStorageError;
	
	// "" for initial zone assignation
	Double[][][] _firstHeadError;
	Double[][][] _firstStorageError;
	
	//List of difference between initial and optimised zones
	ArrayList<Double> _headImprovement;
	ArrayList<Double> _storageImprovement;
	
	Integer _numZones; //Number of zones currently.
	Integer _baseSize; //Number of cells
	
	
	//Used in optimisation
	ArrayList<Integer> _searchOrder;
	
	HashSet<Integer> _zoneIDList;
	CSSDModel _context = this;
	Integer _maxId;
	Integer _maxZone;
	ArrayList<Compartment> _model;
	ArrayList<Cell> _baseModel;
	HashMap<Integer, Integer> _zoneIDtoID;
	HashMap<Integer, Cell> _zoneLookUp;
	HashMap<Integer, Compartment> _zIDLookUp;
	
	HashMap<Integer, Integer> _zoneIDLookUp;
	Double[][] _zoneFlows; //Square matrix containing the flows from zone i to zone j.
	final Double[][] _baseZoneFlows;

	
	//Constructs a high resolution model from list of arrays
	CSSDModel(double[][][] head, double[][][] bottomElevation, double[][][] hArea, double[][][] storativity, double[][][] boundaryFlow, double[][][] transmitivity){
	_maxId = 0;
	_model = new ArrayList<Compartment>();
	_baseModel = new ArrayList<Cell>();
	_zoneLookUp = new HashMap<Integer, Cell>();
	_zoneIDtoID = new HashMap<Integer, Integer>();
	_zoneIDLookUp = new HashMap<Integer, Integer>();
	_zIDLookUp = new HashMap<Integer, Compartment>();
	_headImprovement = new ArrayList<Double>();
	_storageImprovement = new ArrayList<Double>();
	

	_maxX = head.length;
	_maxY = head[0].length;
	_maxZ = head[0][0].length;
	_xScale = (_windowX -2*_xOffset) / _maxX;
	_yScale = (_windowY -2*_yOffset) / _maxY;

	_baseSize = _maxX * _maxY * _maxZ;
	_headBenchmark = new Double[_maxX][_maxY][_maxZ];
	_storageBenchmark = new Double[_maxX][_maxY][_maxZ];
	_firstHeadError = new Double[_maxX][_maxY][_maxZ];
	_firstStorageError = new Double[_maxX][_maxY][_maxZ];
	_optimalHeadError = new Double[_maxX][_maxY][_maxZ];
	_optimalStorageError = new Double[_maxX][_maxY][_maxZ];

	for (int z = 0; ((z < _maxZ) || (z ==0)); z++){
		for (int y = 0; y < _maxY; y++){
			for (int x =0; x < _maxX; x ++){
				_maxHead = Math.max(head[x][y][z], _maxHead);
				new Cell(this, boundaryFlow[x][y][z], head[x][y][z], bottomElevation[x][y][z], storativity[x][y][z], transmitivity[x][y][z]);
			}
		}
	}
	//Builds neighbours for each cell
	for (int i = 0; i < _maxId; i ++){
		_zoneLookUp.get(i).build();
	}
	_baseZoneFlows = new Double[_model.size()][_model.size()];
	//Constructs the zone flows matrix
	for (Compartment m : _model){
		for (Integer i : m._neighbours){
				_baseZoneFlows[m._ID][i] = ( _zoneLookUp.get(i)._head - m._head) *m._neighbourCoefficient.get(i);			
		}
	}
	
	//Stores time to run the base model
	timer.ResetTimer();
	run();
	timer.StopTimer();
	initialTime = timer._lElapsedTime;
	
	
	//stores values from running the base model, for comparison 
	for (int z = 0; (z < _maxZ || z == 0) ; z++){
		for (int y = 0; y < _maxY; y++){
			for (int x =0; x < _maxX; x ++){
				_headBenchmark[x][y][z] = _zoneLookUp.get(id(x,y,z))._head;
				_storageBenchmark[x][y][z] = _zoneLookUp.get(id(x,y,z))._storage;	
			}
		}
	}
	reset();
	
	}
		

	

	/*
	 * Zone methods.  
	 */
	
	
	//Optimises a given assignation of zone id's
	public void optimise(Integer[][][] zoneIDs){
		timer.ResetTimer();
		assignZones(zoneIDs);
		aggregateZones();
		buildZones();
		run();
		//calculates error for initial zones
		for (int z = 0; (z < _maxZ || z == 0) ; z++){
			for (int y = 0; y < _maxY; y++){
				for (int x =0; x < _maxX; x ++){
					_firstHeadError[x][y][z] = Math.abs(_headBenchmark[x][y][z] - _zoneLookUp.get(id(x,y,z))._head);
					_firstStorageError[x][y][z] = Math.abs(_storageBenchmark[x][y][z] - _zoneLookUp.get(id(x,y,z))._storage);	
				}
			}
		}
		reset();

		
		_searchOrder = new ArrayList<Integer>();
		_searchOrder.addAll((Collection<Integer>)_zoneIDList);
		Integer current = _searchOrder.get(0);
		Integer next = _searchOrder.get(0);
		while (_numIterations > 0){
			current = next;
			((Zone)_zIDLookUp.get(current)).optimise();	//optimises the current zone
			_searchOrder.remove((Object) current);
			_searchOrder.add(current);			//puts the optimises zone to the back of the search order
			next = _searchOrder.get(0);				// next zone to be optimised is the one done longest ago
			aggregateZones();		
			buildZones();
			_numIterations--;
		}
		for (Compartment m : _model){
			connectZone(((Zone)m)._elements);		//ensure each zone is contiguous. 
		}
		
		aggregateZones();
		buildZones();
		flushCache();		//Flushes the cache out, removing any intermediary zones
		timer.StopTimer();
		System.out.println("Optimisation Time " + timer._lElapsedTime + " milliseconds");
		timer.ResetTimer();
		run();
		timer.StopTimer();
		long zoneTime = timer._lElapsedTime;
		for (int z = 0; (z < _maxZ || z == 0) ; z++){
			for (int y = 0; y < _maxY; y++){
				for (int x =0; x < _maxX; x ++){
					_optimalHeadError[x][y][z] = Math.abs(_headBenchmark[x][y][z] - _zoneLookUp.get(id(x,y,z))._head);
					_optimalStorageError[x][y][z] = Math.abs(_storageBenchmark[x][y][z] - _zoneLookUp.get(id(x,y,z))._storage);	
				}
			}
		}
		reset();
		
		for (int z = 0; (z < _maxZ || z == 0) ; z++){
			for (int y = 0; y < _maxY; y++){
				for (int x =0; x < _maxX; x ++){
					_headImprovement.add((Math.abs((_headBenchmark[x][y][z] - _firstHeadError[x][y][z])/ _headBenchmark[x][y][z]*100) - Math.abs((_headBenchmark[x][y][z] - _optimalHeadError[x][y][z]))/ _headBenchmark[x][y][z]*100) ); //= Math.abs(_headBenchmark[x][y][z] - _zoneLookUp.get(id(x,y,z))._head);
					_storageImprovement.add((Math.abs((_storageBenchmark[x][y][z] - _firstStorageError[x][y][z])/ _storageBenchmark[x][y][z]*100) - Math.abs((_storageBenchmark[x][y][z] - _optimalStorageError[x][y][z]))/ _storageBenchmark[x][y][z]*100) ); //= Math.abs(_headBenchmark[x][y][z] - _zoneLookUp.get(id(x,y,z))._head);
				}
			}
		}
		
		System.out.println(_headImprovement);
		System.out.println(_storageImprovement);
		double _headimprov = 0.0;
		double _storageimprov = 0.0;
		for (int i = 0; i < _headImprovement.size(); i++){
			if (!_headImprovement.get(i).isNaN())
				_headimprov += _headImprovement.get(i);
			if (!_storageImprovement.get(i).isNaN())
				_storageimprov += _storageImprovement.get(i);
		}
		System.out.println("Head improvement " + _headimprov);
		System.out.println("Storage improvement " + _storageimprov);
		System.out.println("Time Improvement " + (initialTime - zoneTime) );
		

	}
	
	//Checks that a given list of elements is "connected".  If not, breaks it up into separate zones.
	 public void connectZone(ArrayList<Integer> elements){
			ArrayList<Integer> current = new ArrayList<Integer>();
			if (elements.size() == 0){
				return;
			}
			if (elements.size() >1 ){
				current.add(elements.get(0));
				current = pathConnected(elements.get(0), new ArrayList<Integer>());
				if (current.size() == elements.size()){
					System.out.println(current);
					return;
				}
				else {
					for (Integer i : current){
						elements.remove((Object) i);
						_context._zoneLookUp.get(i)._zoneID = _context._maxZone;	
		    			_context._zoneIDLookUp.put(i, _context._maxZone);
						_context._zoneIDList.add(_context._maxZone);
					}
					_context._maxZone++;
					connectZone(elements);
				}
			}
				
			
	}

	//Returns the sub-list of temporary of elements that are connected to i 
	public ArrayList<Integer> pathConnected(Integer i, ArrayList<Integer> temporary){
		ArrayList<Integer> result = new ArrayList<Integer>();
		result.add(i);
		temporary.add(i);
		
		Cell temp = _zoneLookUp.get(i);
		timer.ResetTimer();
		for (Integer j : temp._neighbours){
			if (temp._zoneID != _zoneIDLookUp.get(j) || temporary.contains(j)){
				continue;
			}
			else if ((temp._zoneID == _zoneIDLookUp.get(j))){
				result.addAll(pathConnected(j, temporary));

			}
			
		}
		timer.StopTimer();
		return result;
	}

	//Flushes out all the hashmaps.  Saves memory.
	public void flushCache(){
		ArrayList<Compartment> keep = new ArrayList<Compartment>();
		for (Compartment m : _model){
			if (!((Zone) m)._elements.isEmpty()){
				keep.add(m);
			}
		}
	
		_maxZone = 0;
		_model = keep;
		_zIDLookUp.clear();
		_zoneIDLookUp.clear();
		_zoneIDList.clear();
		for (Compartment m : _model){
			_zoneIDList.add(_maxZone);
			((Zone) m)._ID = _maxZone;
			for (Integer i :((Zone)m)._elements){
				_zoneLookUp.get(i)._zoneID = _maxZone;
				_zoneIDLookUp.put(i, _maxZone);
			}
			_zIDLookUp.put(_maxZone, m);
			_maxZone++;
		}
		aggregateZones();
		buildZones();

		
	}

	
	//Assigns each cell in high resolution model the corresponding zoneID
	public void assignZones(Integer[][][] zoneID){
		_zoneIDList = new HashSet<Integer>();
		_maxZone=0;
		Cell temp;
		for (int k = 0; k < _baseSize; k++){
			if (_zoneLookUp.get(k) != null){
				temp = _zoneLookUp.get(k);
				temp._zoneID = zoneID[temp.xCoord()][temp.yCoord()][temp.zCoord()];
				_zoneIDLookUp.put(k, zoneID[temp.xCoord()][temp.yCoord()][temp.zCoord()]);
				_zoneIDList.add(zoneID[temp.xCoord()][temp.yCoord()][temp.zCoord()]);
				_maxZone = Math.max(_maxZone, temp._zoneID);
		}
	}
		_maxZone++;
	}
	
	
	
	//Builds the new zones by aggregating cells with same zoneID.  Note zone here is only a list of elements
	public void aggregateZones(){
		_model.clear();
		ArrayList<Integer> unassigned = new ArrayList<Integer>();
		ArrayList<Integer> temp = new ArrayList<Integer>();
		Integer k;
		Zone z;
		for (int i = 0; i < _baseSize; i++)
			unassigned.add(i);
		for (Integer i : _zoneIDList){
			Iterator<Integer> itr = unassigned.iterator();
			temp = new ArrayList<Integer>();
			while (itr.hasNext()){
				k = (Integer)itr.next();
				if (_zoneIDLookUp.get(k) == i){
					temp.add(k);
					itr.remove();
				}
			}
			z = new Zone(i, this, temp);
			_zoneIDtoID.put(i, z._ID);
		}
	}
	
	//Builds up each zone from the list of elements.  
	public void buildZones(){
		_maxHead = 0.0;
		for (int i : _zoneIDList){
			_zIDLookUp.get(i).build();
		}
		for (int i : _zoneIDList){
			((Zone) _zIDLookUp.get(i)).buildNeighbours();
			((Zone)_zIDLookUp.get(i)).buildZoneNeighbourCoefficients();
		}

		
	}
	

	
/*
 * Methods for running the model 
 */
	//Updates the zoneFlows matrix
	public void updateZoneFlows(){
		for (Compartment m : _model){
			if (m instanceof Zone){
					for (Integer i : m._neighbours){	
						_zoneFlows[m._ID][i] = (_zIDLookUp.get(i)._head - m._head ) *m._neighbourCoefficient.get(i);
					}}
			if (m instanceof Cell)
					for (Integer i : m._neighbours){
					_zoneFlows[m._ID][i] = (_zoneLookUp.get(i)._head - m._head ) *m._neighbourCoefficient.get(i);
				}}
			}
	
	
	public void resetZoneFlows(){
		_zoneFlows = new Double[_model.size()][_model.size()];
		for (Compartment m : _model){
			if (m instanceof Zone){
				for (Integer i : m._neighbours){
					_zoneFlows[m._ID][i] = (_zoneLookUp.get(i)._initialHead - m._initialHead) *m._neighbourCoefficient.get(i);
				}}
			if (m instanceof Cell){
				for (Integer i : m._neighbours){
						_zoneFlows[m._ID][i] = (_zoneLookUp.get(i)._initialHead - m._initialHead) *m._neighbourCoefficient.get(i);
				}}
		}	
	}
	
	//Steps the whole model forward one timestep.
	
	public void initialise(){
		_zoneFlows = new Double[_model.size()][_model.size()];
		reset();
		Double sum = 0.0;
		Double _maxStorage =0.0;
		for (Compartment m : _model){
			_maxStorage += Math.abs((m._storage/100000));
			sum = 0.0;
			for (Integer k : m._neighbours){
				sum += m._neighbourCoefficient.get(k);
			}
			sum *= _timeStep;
			if (sum > m._storativity*m._hArea){
				System.out.println( " Warning: Stability check not met");
			}
		}
		}

	
	public void run(){
		initialise();
		timer.ResetTimer();
		step(_numSteps, PRINT);
		timer.StopTimer();
		System.out.println("Time to run base model" + timer._lElapsedTime + "milliseconds");
	}
	
	public void step(Integer numSteps, Boolean print){
		while (numSteps > 0){
			step(print);
			numSteps--;
		}
	}
	
	public void step(Boolean print){
		_maxHead = 0.0;
		_maxStorage = 0.0;
			for (Compartment m : _model){
				double temp = 0.0;
				for (Integer i : m._neighbours){
						temp += _zoneFlows[m._ID][i];	//Calculating the value of Qab for all relevant b

				}
				m._tempStorage = (m._boundaryFlow+temp)*_timeStep + m._storage;	//Equation 6
				m.update();
				_maxStorage += Math.abs(m._storage/100000);
			}
			updateZoneFlows();
			if (print)
				print();
		}
	
	
	//Comparison methods, that arent currently used.
	public void compareZones(){
		for (Compartment m : _model){
			if (m instanceof Cell){
				break;
			}
			else{
				
			}
		}
	}
	
	public ArrayList<Double> compareHead(){
		ArrayList<Double> comparison = new ArrayList<Double>();
		for (int z = 0; z < _maxZ; z++){
			for (int y = 0; y < _maxY; y++){
				for (int x = 0; x < _maxX; x++){
					comparison.add(Math.abs((_headBenchmark[x][y][z] - (_zoneLookUp.get(id(x,y,z))._head))/ _headBenchmark[x][y][z]*100));
				}
			}
		}
		return comparison;
	}
	
	public Double compareHeadCumulative(){
		Double comparison = 0.0;
		for (int z = 0; z < _maxZ; z++){
			for (int y = 0; y < _maxY; y++){
				for (int x = 0; x < _maxX; x++){
					System.out.println((_zoneLookUp.get(id(x,y,z))._head));
				//	comparison += (Math.abs((_headBenchmark[x][y][z] - (_zoneLookUp.get(id(x,y,z))._head))/ _headBenchmark[x][y][z]*100));
				}
			}
		}
		return comparison;
		
	}
	
	
	public ArrayList<Double> compareStorage(){
		ArrayList<Double> comparison  = new ArrayList<Double>();
		for (int z = 0; z < _maxZ; z++){
			for (int y = 0; y < _maxY; y++){
				for (int x = 0; x < _maxX; x++){
					comparison.add(Math.abs((_storageBenchmark[x][y][z] - (_zoneLookUp.get(id(x,y,z))._storage))/ _storageBenchmark[x][y][z]*100));
				}
			}
		}
		return comparison;
	}
	
	public Double compareStorageCumulative(){
		Double comparison = 0.0;
		for (int z = 0; z < _maxZ; z++){
			for (int y = 0; y < _maxY; y++){
				for (int x = 0; x < _maxX; x++){
					comparison += (Math.abs((_storageBenchmark[x][y][z] - (_zoneLookUp.get(id(x,y,z))._storage))/ _storageBenchmark[x][y][z]*100));
				}
			}
		}
		return comparison;
		
	}
			
	public void print(){
		for (Compartment m : _model){
			System.out.println(m._ID + " ");
			System.out.print("Head: " + m._head);
			System.out.print(" Storage: " + m._storage);
			System.out.print("\n");
			System.out.print(" ");
		}

		}
	
	public void printComparison(){
		ArrayList<Double> heads = compareHead();
		ArrayList<Double> storages = compareStorage();
		double headerror = 0.0, storageerror =0.0;
		for (int i = 0; i < _baseSize; i ++){
			headerror += heads.get(i);
			storageerror += storages.get(i);
			System.out.print(heads.get(i) + " " );
			System.out.print(storages.get(i));
			System.out.print("\n");
		}
		headerror /= _baseSize;
		storageerror /= _baseSize;
		System.out.println(" AVERAGE HEAD ERROR: " + headerror + "  AVERAGE STORAGE ERROR " + storageerror);
	}
	
	public String toString(){
		return _model.toString();
	}

	
	public void reset(){
		for (Compartment m : _model){
			m.reset();
		}
		resetZoneFlows();
	}
	
	public Integer id(Integer x, Integer y, Integer z){
		return z*_maxY*_maxX + y*_maxX + x;
	}
	
	
	
	/*@TODO
	 * -flush out caches.  Dont want to store all zones again.  Need to be able to reset.  Probably store results from last two runs
	 * Need a method to store results.
	 */
	
	 public void draw(SimulationCanvas canvas) { 
			for (Compartment m : _model){
				m.draw(canvas);
			}

	}
	
	

}
