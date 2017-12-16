package application;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Point;

public class CapturedFrame implements Cloneable {
	Point[][] allEdges;
	List<Point> markerPoints = new ArrayList<Point>();
	List<Point> player1Points = new ArrayList<Point>();
	List<Point> player2Points = new ArrayList<Point>();
	List<Point> borderPoints = new ArrayList<>();
	List<Point> allPoints = new ArrayList<Point>();
	BoardSquare[][] squares;

	public CapturedFrame(CapturedFrame toCopy){
		this.allEdges = toCopy.allEdges.clone();
		this.markerPoints = this.clonePointList(toCopy.markerPoints);
		this.player1Points = this.clonePointList(toCopy.player1Points);
		this.player2Points = this.clonePointList(toCopy.player2Points);
		this.borderPoints = this.clonePointList(toCopy.borderPoints);
		this.allPoints = this.clonePointList(toCopy.allPoints);
		this.squares = toCopy.squares.clone();
	}
	
	public CapturedFrame(){
	}
	
	public int compareTo(CapturedFrame compArg) {
		return ((this.player1Points.size() >= compArg.player1Points.size())
				&& (this.player2Points.size() >= compArg.player2Points.size())) ? 1 : 0;
	}
	
	private List<Point> clonePointList(List<Point> points){
		List<Point> clonedList = new ArrayList<Point>();
		for(Point p : points){
			clonedList.add(p.clone());
		}
		return clonedList;
	}
}
