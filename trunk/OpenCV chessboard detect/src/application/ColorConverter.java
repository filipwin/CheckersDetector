package application;

public class ColorConverter {

	public static double[] RGBToHSV(double red, double grn, double blu){
		double hue, sat, val;
		double x, f, i;
		double result[] = new double[3];
		 
		x = Math.min(Math.min(red, grn), blu);
		val = Math.max(Math.max(red, grn), blu);
		if (x == val){
		hue = 0;
		sat = 0;
		}
		else {
		f = (red == x) ? grn-blu : ((grn == x) ? blu-red : red-grn);
		i = (red == x) ? 3 : ((grn == x) ? 5 : 1);
		hue = ((i-f/(val-x))*60)%360;
		sat = ((val-x)/val);
		}
		result[0] = hue;
		result[1] = sat;
		result[2] = val;
		return result;
	}
}
