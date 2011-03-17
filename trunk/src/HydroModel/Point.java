package HydroModel;

public class Point {
	
	int _id;
	int _x,_y,_z;
	
	
	public Boolean coLinear(Point p1, Point p2){
		return ((_x == p1._x && _x == p2._x) || (_y == p1._y && _y == p2._y) || (_y == p1._y && _y == p2._y));
	}

}
