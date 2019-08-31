package shortestpath;

import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableFloatArray;
import javafx.collections.ObservableIntegerArray;
import javafx.scene.shape.TriangleMesh;

public class TerrainGenerator 
{
    private int width;
    private TriangleMesh mesh;
    private float[][] heightMap;
    
    private final int noiseSize = 100;
    private final int MULT = 20;
    private final float coordDivisor = 15f;
    
    private Noise n;
    
    public TerrainGenerator(int width)
    {
        this.width = width;
        generateTerrain();
    }
    
    private void generateTerrain()
    {
        //Create height map
        heightMap = new float[width+1][width+1];
        n = new Noise(noiseSize);
        
        for(int x = 0; x < heightMap.length; x++)
        {
            for(int y = 0; y < heightMap.length; y++)
            {
                heightMap[x][y] = MULT * (float)n.noise(x / coordDivisor, y / coordDivisor);
            }
        }
        
        mesh = new TriangleMesh();
        
        ObservableIntegerArray faces = FXCollections.observableIntegerArray();
        ObservableFloatArray pts = FXCollections.observableFloatArray();
        
        int vertNum = 0;
        //Create triangle mesh
        for(int y = 0; y < width; y++)
        {
            for(int x = 0; x < width; x++)
            {
                pts.addAll(x, y, heightMap[x][y]);          
                faces.addAll(vertNum++, 0);
                
                pts.addAll(x, y+1, heightMap[x][y+1]);      
                faces.addAll(vertNum++, 0);
                
                pts.addAll(x+1, y, heightMap[x+1][y]);      
                faces.addAll(vertNum++, 0);
                
                pts.addAll(x+1, y, heightMap[x+1][y]);      
                faces.addAll(vertNum++, 0);
                
                pts.addAll(x, y+1, heightMap[x][y+1]);      
                faces.addAll(vertNum++, 0);
                
                pts.addAll(x+1, y+1, heightMap[x+1][y+1]); 
                faces.addAll(vertNum++, 0);
            }
        }
        
        mesh.getPoints().addAll(pts);
        mesh.getFaces().addAll(faces);
        mesh.getTexCoords().addAll(0, 0);
    }
    
    public ArrayList<Double[]> getGrid(double density)
    {
        ArrayList<Double[]> grid = new ArrayList<>();
        for(double x = 0; x <= width; x += (1.0 / density))
        {
            for(double y = 0; y <= width; y += (1.0 / density))
            {
                Double[] coords = new Double[3];
                coords[0] = x;
                coords[1] = y;
                coords[2] = MULT * n.noise(coords[0] / coordDivisor, coords[1] / coordDivisor);
                
                grid.add(coords);
            }
        }
        
        return grid;
    }
    
    public TriangleMesh getMesh()
    {
        return mesh;
    }
    
    public float[][] getHeightMap()
    {
        return heightMap;
    }
}
