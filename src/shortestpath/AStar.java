package shortestpath;

import java.util.ArrayList;
import javafx.concurrent.Task;

public class AStar extends Task
{
    private ArrayList<Double[]> grid;
    
    private int[] parents;
    
    private int startIndex;
    private int endIndex;
    
    private boolean enableHeuristics;
    
    public AStar(ArrayList<Double[]> grid, int startIndex, int endIndex)
    {
        this.grid = grid;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        
        parents = new int[grid.size()];
        
        enableHeuristics = false;
    }
    
    public AStar(ArrayList<Double[]> grid, int startIndex, int endIndex, boolean enableHeuristics)
    {
        this.grid = grid;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        
        parents = new int[grid.size()];
        
        this.enableHeuristics = enableHeuristics;
    }
    
    private void findPath()
    {
        ArrayList<Integer> closed = new ArrayList<>();
        ArrayList<Double[]> open = new ArrayList<>();
        open.add(new Double[]{(double)startIndex, 0.0, 0.0, 0.0});  //In form of {grid index, F, G, H}
        
        while(!open.isEmpty())
        {
            //Find lowest F in open list
            int minInd = 0;
            for(int i = 0; i < open.size(); i++)
            {
                if(open.get(i)[1] < open.get(minInd)[1])
                {
                    minInd = i;
                }
            }
            int currInd = open.get(minInd)[0].intValue();   //Current index in grid
            
            if(currInd == endIndex)
            {
                break;
            }
            
            closed.add(currInd);
            double currG = open.get(minInd)[2];
            open.remove(minInd);
            
            //Calculate adjacents
            int s = (int)Math.sqrt(grid.size());
            int[] adjacents = {currInd + 1, currInd - 1, 
                               currInd + s, currInd + s + 1, currInd + s - 1,
                               currInd - s, currInd - s + 1, currInd - s - 1};
            //Foreach adjacent
            for(int i = 0; i < adjacents.length; i++)
            {
                int adj = adjacents[i];
                if(!closed.contains(adj) && adj < grid.size() && adj >= 0)  //Last 2 conditions ensures adjacent exists
                {
                    //Check if adjacent is in the open list
                    boolean openContainsAdj = false;
                    int adjOpenInd = 0;
                    for(int x = 0; x < open.size(); x++)
                    {
                        if(open.get(x)[0] == adj)
                        {
                            openContainsAdj = true;
                            adjOpenInd = x;
                            break;
                        }
                    }
                    
                    //Calculate distance between current and adjacent
                    double dx = grid.get(currInd)[0] - grid.get(adj)[0];
                    double dy = grid.get(currInd)[1] - grid.get(adj)[1];
                    double dz = grid.get(currInd)[2] - grid.get(adj)[2];
                    double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
                    
                    //Heuristic (calculated using straight line distance between adjacent and end)
                    //it is only calculated if enabled.
                    double adjH = 0;
                    if(enableHeuristics)
                    {
                        double hDx = grid.get(adj)[0] - grid.get(endIndex)[0];
                        double hDy = grid.get(adj)[1] - grid.get(endIndex)[1];
                        double hDz = grid.get(adj)[2] - grid.get(endIndex)[2];
                        adjH = Math.sqrt(hDx*hDx + hDy*hDy + hDz*hDz);
                    }
                    
                    if(!openContainsAdj)
                    {
                        //Add adjacent to open list and set parent
                        double adjG = currG + dist;
                        
                        open.add(new Double[]{(double)adj, adjG + adjH, adjG, adjH});
                        parents[adj] = currInd;
                    }
                    else
                    {
                        //If the adjacent G + dist is less than the current G
                        if(open.get(adjOpenInd)[2] + dist < currG)
                        {
                            parents[adj] = currInd;
                            double adjNewG = open.get(adjOpenInd)[2] + dist;
                            open.remove(adjOpenInd);
                            open.add(new Double[]{(double)adj, adjNewG + adjH, adjNewG, adjH});
                        }
                    }
                }//End if adjacent not in closed list
            }//End foreach adjacent
        }//End while open contains values
    }
    
    public ArrayList<Double[]> getPath()
    {
        ArrayList<Double[]> path = new ArrayList<>();
        int currentIndex = endIndex;
        while(currentIndex != startIndex)
        {
            path.add(grid.get(parents[currentIndex]));
            currentIndex = parents[currentIndex];
        }
        
        return path;
    }

    @Override
    protected ArrayList<Double[]> call() throws Exception 
    {
        findPath();
        return getPath();
    }
}
