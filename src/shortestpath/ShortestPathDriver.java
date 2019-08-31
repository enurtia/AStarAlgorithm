package shortestpath;

import java.util.ArrayList;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

public class ShortestPathDriver extends Application
{
    private Group group;
    private ArrayList<Double[]> grid;
    
    //x and y used in mouse movement deltas
    private double x;
    private double y;
    
    //Settings for camera sensitivities, grid size, and point density for dicretization of surface.
    //Note: point density can be more precise than the actual terrain's mesh itself.
    private final double SCROLL_SENS = 3;
    private final double KEY_SENS = 4;
    private final double DRAG_SENS = 0.01;
    private final int SIZE = 50;
    private final double POINT_DENSITY = 6;    //The amount of points per square area unit
    
    //Variables for the spheres created to show paths
    private ArrayList<Sphere> pathSpheres;
    private DoubleProperty pathSphereRadius = new SimpleDoubleProperty(0.15);
    private double pathSphereDelta = 0.001;
    
    //Variables concerning starting and ending points.
    private int startIndex = -1;
    private int endIndex = -1;
    private DoubleProperty startEndRadius = new SimpleDoubleProperty(0.3);
    private final double startEndDelta = 0.001;
    private boolean removeStart;
    private Sphere start;
    private Sphere end;
    
    //Discrete points
    ArrayList<Sphere> discreteGrid;
    private BooleanProperty showDiscrete = new SimpleBooleanProperty(false);
    
    @Override
    public void start(Stage primaryStage) throws Exception 
    {
        //Set group and scene
        group = new Group(); 
        Scene scene = new Scene(group, 1200, 700, Color.BLACK);
        primaryStage.setScene(scene);
        primaryStage.setTitle("AStar Algorithm Simulation, Z for controls");
        
        //Initialize ArrayList containing spheres that create the paths
        pathSpheres = new ArrayList<>();
        
        //Initialize start and end spheres
        PhongMaterial sMat = new PhongMaterial(Color.GREEN.brighter());
        start = new Sphere();
        start.radiusProperty().bind(startEndRadius);
        start.setMaterial(sMat);
        
        PhongMaterial eMat = new PhongMaterial(Color.RED.brighter());
        end = new Sphere();
        end.radiusProperty().bind(startEndRadius);
        end.setMaterial(eMat);
        
        group.getChildren().add(start);
        group.getChildren().add(end);
        
        start.setVisible(false);
        end.setVisible(false);
        
        //Initialize Camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0);
        camera.setFarClip(1000);
        scene.setCamera(camera);

        //Create terrain
        TerrainGenerator generator = new TerrainGenerator(SIZE);
        grid = generator.getGrid(POINT_DENSITY);
        
        //Create discretized grid
        discreteGrid = new ArrayList<>();
        for(int i = 0; i < grid.size(); i++)
        {
            Double[] coords = grid.get(i);

            Sphere s = new Sphere(0.05);
            s.setTranslateX(coords[0]);
            s.setTranslateY(coords[1]);
            s.setTranslateZ(coords[2]);
            s.visibleProperty().bind(showDiscrete);
            
            group.getChildren().add(s);
        }
        
        
        
        
        MeshView obj = new MeshView(generator.getMesh());
        obj.setCullFace(CullFace.NONE);
        
        PhongMaterial meshMat = new PhongMaterial(Color.ORANGE);
        obj.setMaterial(meshMat);
        
        obj.setDrawMode(DrawMode.LINE);
        obj.setRotationAxis(Rotate.X_AXIS);
        group.getChildren().add(obj);
        
        //Scrolling Controls
        scene.setOnScroll((ScrollEvent se) ->
        {
            Transform t = camera.getLocalToSceneTransform();
            double delta = se.getDeltaY();
            
            camera.setTranslateX(camera.getTranslateX() + ((delta > 0 ? 1 : -1) * SCROLL_SENS * t.getMxz()));
            camera.setTranslateY(camera.getTranslateY() + ((delta > 0 ? 1 : -1) * SCROLL_SENS * t.getMyz()));
            camera.setTranslateZ(camera.getTranslateZ() + ((delta > 0 ? 1 : -1) * SCROLL_SENS * t.getMzz()));
        });
        
        //Key Controls
        scene.setOnKeyPressed((KeyEvent ke) ->
        {
            Transform t = camera.getLocalToSceneTransform();
            switch(ke.getCode())
            {
                case W:
                    camera.setTranslateX(camera.getTranslateX() + (KEY_SENS * t.getMxz()));
                    camera.setTranslateY(camera.getTranslateY() + (KEY_SENS * t.getMyz()));
                    camera.setTranslateZ(camera.getTranslateZ() + (KEY_SENS * t.getMzz()));
                    break;
                case S:
                    camera.setTranslateX(camera.getTranslateX() - (KEY_SENS * t.getMxz()));
                    camera.setTranslateY(camera.getTranslateY() - (KEY_SENS * t.getMyz()));
                    camera.setTranslateZ(camera.getTranslateZ() - (KEY_SENS * t.getMzz()));
                    break;
                case A:
                    camera.setTranslateX(camera.getTranslateX() - (KEY_SENS * t.getMxx()));
                    camera.setTranslateY(camera.getTranslateY() - (KEY_SENS * t.getMyx()));
                    camera.setTranslateZ(camera.getTranslateZ() - (KEY_SENS * t.getMzx()));
                    break;
                case D:
                    camera.setTranslateX(camera.getTranslateX() + (KEY_SENS * t.getMxx()));
                    camera.setTranslateY(camera.getTranslateY() + (KEY_SENS * t.getMyx()));
                    camera.setTranslateZ(camera.getTranslateZ() + (KEY_SENS * t.getMzx()));
                    break;
                case SPACE:
                    camera.setTranslateX(camera.getTranslateX() - (KEY_SENS * t.getMxy()));
                    camera.setTranslateY(camera.getTranslateY() - (KEY_SENS * t.getMyy()));
                    camera.setTranslateZ(camera.getTranslateZ() - (KEY_SENS * t.getMzy()));
                    break;
                case CONTROL:
                    camera.setTranslateX(camera.getTranslateX() + (KEY_SENS * t.getMxy()));
                    camera.setTranslateY(camera.getTranslateY() + (KEY_SENS * t.getMyy()));
                    camera.setTranslateZ(camera.getTranslateZ() + (KEY_SENS * t.getMzy()));
                    break;
                case ENTER:
                    if(startIndex != -1 && endIndex != -1)
                    {
                        findPaths();
                    }
                    break;
                case X:
                    //Show discretized points
                    showDiscrete.set(!showDiscrete.getValue());
                    break;
                case Z:
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Controls");
                    alert.setHeaderText(null);
                    alert.setContentText(
                            ">WASD/Left Click Drag\n\t - Side Movement\n" +
                            ">Space/Control/Scroll\n\t - Zooming\n" + 
                            ">Right Click\n\t - Place starting and ending points\n" +
                            ">Enter\n\t - Find paths. Green is shortest path, Blue is shortest path using heuristics (A*)\n" +
                            ">Right Click Drag\n\t - X controls path sphere size, Y controls starting and ending sphere size\n\n" + 
                            ">X\n\t - Toggle discretized grid.\n" + 
                            "Note: Path found without heuristics will take longer to create.\n" +
                            "Note: Path is found using a discretized grid. It will be as accurate as that grid created, which can be more accurate than the mesh."
                    );
                    alert.show();
                    break;
            }
        });
        
        //Mouse Press Controls
        scene.setOnMousePressed((MouseEvent me) ->
        {
            if(me.isPrimaryButtonDown())
            {
                x = me.getSceneX();
                y = me.getSceneY();
            }
            else if(me.isSecondaryButtonDown())
            {
                x = me.getSceneX();
                y = me.getSceneY();
                
                if(me.getTarget() == obj)
                {
                    //Set start and end points
                    if(startIndex != -1 && endIndex != -1)
                    {
                        if(removeStart)
                        {
                            startIndex = -1;
                            start.setVisible(false);
                        }
                        else
                        {
                            endIndex = -1;
                            end.setVisible(false);
                        }
                        removeStart = !removeStart;
                    }
                    else
                    {
                        //Set start
                        int ind = findClosest(grid, me.getX(), me.getY(), me.getZ());
                        Double[] xyz = grid.get(ind);
                        if(startIndex == -1 && endIndex == -1 || startIndex == -1 && endIndex != -1)
                        {
                            start.setTranslateX(xyz[0]);
                            start.setTranslateY(xyz[1]);
                            start.setTranslateZ(xyz[2]);
                            startIndex = ind;
                            start.setVisible(true);
                        }
                        else
                        {
                            //Set end
                            end.setTranslateX(xyz[0]);
                            end.setTranslateY(xyz[1]);
                            end.setTranslateZ(xyz[2]);
                            endIndex = ind;
                            end.setVisible(true);
                        }
                    }
                }
            }
        });
        
        //Mouse Drag Controls
        scene.setOnMouseDragged((MouseEvent me) ->
        {
            //Left mouse drags camera around
            if(me.isPrimaryButtonDown())
            {
                double dy = y - me.getSceneY();
                double dx = x - me.getSceneX();
                
                camera.setTranslateX(camera.getTranslateX() + (dx * DRAG_SENS));
                camera.setTranslateY(camera.getTranslateY() + (dy * DRAG_SENS));
                
                y = me.getSceneY();
                x = me.getSceneX();
            }//Right mouse dy scales starting and ending spheres, dx scales path spheres
            else if(me.isSecondaryButtonDown())
            {
                double dy = y - me.getSceneY();
                double dx = x - me.getSceneX();
                
                startEndRadius.setValue(startEndRadius.doubleValue() + startEndDelta * dy);
                pathSphereRadius.setValue(pathSphereRadius.doubleValue() + pathSphereDelta * dx);
                
                x = me.getSceneX();
                y = me.getSceneY();
            }
        });
        
        primaryStage.show();
    }
    
    private void drawPoints(boolean bool)
    {
        //Draw the discretized grid
        for(int i = 0; i < grid.size(); i++)
        {
            Double[] coords = grid.get(i);

            Sphere s = new Sphere(0.05);
            s.setTranslateX(coords[0]);
            s.setTranslateY(coords[1]);
            s.setTranslateZ(coords[2]);
            
            group.getChildren().add(s);
        }
    }
    
    //Find paths using threads to avoid program from stopping.
    private void findPaths()
    {
        group.getChildren().removeAll(pathSpheres);
        
        //Draw path with heuristics
        AStar aStarHeuristics = new AStar(grid, startIndex, endIndex, true);
        aStarHeuristics.setOnSucceeded((ev) -> 
        {
            ArrayList<Double[]> pathHeur = aStarHeuristics.getPath();
            for(int i = 0; i < pathHeur.size(); i++)
            {
                Double[] coords = pathHeur.get(i);

                Sphere s = new Sphere();
                s.radiusProperty().bind(pathSphereRadius);
                s.setTranslateX(coords[0]);
                s.setTranslateY(coords[1]);
                s.setTranslateZ(coords[2]);

                pathSpheres.add(s);

                PhongMaterial mat2 = new PhongMaterial();
                mat2.setDiffuseColor(Color.BLUE);
                s.setMaterial(mat2);

                group.getChildren().add(s);
            }
        });
        new Thread(aStarHeuristics).start();
        
        //Draw path without heuristics (Exact shortest path, in terms of the discretized grid)
        AStar aStarWOHeuristics = new AStar(grid, startIndex, endIndex, false);
        aStarWOHeuristics.setOnSucceeded((ev) ->
        {
            ArrayList<Double[]> pathWOHeur = aStarWOHeuristics.getPath();
            for(int i = 0; i < pathWOHeur.size(); i++)
            {
                Double[] coords = pathWOHeur.get(i);

                Sphere s = new Sphere();
                s.radiusProperty().bind(pathSphereRadius);
                s.setTranslateX(coords[0]);
                s.setTranslateY(coords[1]);
                s.setTranslateZ(coords[2]);

                pathSpheres.add(s);

                PhongMaterial mat2 = new PhongMaterial();
                mat2.setDiffuseColor(Color.GREEN);
                s.setMaterial(mat2);

                group.getChildren().add(s);
            }
        });
        new Thread(aStarWOHeuristics).start();
    }
    
    //Return index of closest point in ArrayList to (x1,y1,z1)
    public int findClosest(ArrayList<Double[]> g, double x1, double y1, double z1)
    {
        double min = Integer.MAX_VALUE;
        int minIndex = 0;
        for(int i = 0; i < g.size(); i++)
        {
            Double[] coord = g.get(i);
            double dx = coord[0] - x1;
            double dy = coord[1] - y1;
            double dz = coord[2] - z1;
            double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
            
            if(dist < min)
            {
                min = dist;
                minIndex = i;
            }
        }
        
        return minIndex;
    }
    
    public static void main(String[] args)
    {
       launch(args);
    }
}