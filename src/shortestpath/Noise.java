package shortestpath;

public class Noise
{
    private final int max; 
    private double[][] xyVectorDir;
    
    public Noise(int max)
    {
        this.max = max;
        
        xyVectorDir = new double[max][max];
        for(int row = 0; row < max; row++)
        {
            for(int col = 0; col < max; col++)
            {
                xyVectorDir[row][col] = Math.random() * Math.PI * 2;    //Vector direction in radians
            }
        }
    }
    
    public double noise(double x, double y)
    {
        x %= max-1;
        y %= max-1;
        
        int bx0 = (int)x;       //Bottom left bound
        int by0 = (int)y;
        int bx1 = (int)x + 1;   //Bottom right bound
        int by1 = (int)y;
        int bx2 = (int)x;       //Top left bound
        int by2 = (int)y + 1;
        int bx3 = (int)x + 1;   //Top right bound
        int by3 = (int)y + 1;
        
        //Calculate dot products
        double dp0 = (x - bx0) * (Math.cos(xyVectorDir[bx0][by0])) + (y - by0) * (Math.sin(xyVectorDir[bx0][by0]));
        double dp1 = (x - bx1) * (Math.cos(xyVectorDir[bx1][by1])) + (y - by1) * (Math.sin(xyVectorDir[bx1][by1]));
        double dp2 = (x - bx2) * (Math.cos(xyVectorDir[bx2][by2])) + (y - by2) * (Math.sin(xyVectorDir[bx2][by2]));
        double dp3 = (x - bx3) * (Math.cos(xyVectorDir[bx3][by3])) + (y - by3) * (Math.sin(xyVectorDir[bx3][by3]));
        
        //Interpolate 
        double x1 = lerp(dp0, dp1, fade(x - (int)x));    //Bottom
        double x2 = lerp(dp2, dp3, fade(x - (int)x));    //Top
        
        return lerp(x1, x2, fade(y - (int)y));
    }
    
    //Fade function
    private double fade(double in)
    {
        return 6*Math.pow(in,5) - 15*Math.pow(in,4) + 10*Math.pow(in,3);
    }
    
    //Linear interpolation
    private double lerp(double val1, double val2, double percent)
    {
        return val1 + (percent * (val2 - val1));
    }
}