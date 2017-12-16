package application;

public class BoardSquare {
	boolean taken = false;
	int player = 0;
	
	BoardSquare(boolean taken, int player){
		this.taken = taken;
		this.player = player;
	}
}
