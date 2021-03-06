package models.facade.Configuration;

import models.maze.Maze;
import models.maze.MazeBuilder;
import models.maze.MazeObject;
import models.mazeObjects.ObjectsFactory;
import models.wall.Wall;
import models.wall.WallCell;
import views.Drawable;

import java.awt.*;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;


//todo Life Gifts adding after its implementation
//todo hashMap for time complexity instead of linkedList
public class Configuration {
    private final Point startPoint;
    private final Point endPoint;
    private final int cellWidth;
    private int range_of_bomb_actions = 5;
    private int range_of_gift_actions = 5;
    private int mazeLength;
    private int mazeWidth;
    private int bombsNum;
    private int giftsNum;
    private int monstersNum;
    private String gameMode;
    // private LinkedList<String> listOfTakenPositions;
    private LinkedList<Point> listOfTakenPositions;
    private Random rand = new Random();
    private int pickedTemplate;
    private int NUMBER_OF_TEMPLATES=2;
    private String difficulty;

    public Configuration(Properties info) {
        this.mazeLength = Integer.parseInt(info.getProperty("height"));
        this.mazeWidth = Integer.parseInt(info.getProperty("width"));
        this.bombsNum = Integer.parseInt(info.getProperty("number_of_bombs"));
        this.giftsNum = Integer.parseInt(info.getProperty("number_of_gifts"));
        this.monstersNum = Integer.parseInt(info.getProperty("number_of_monsters"));
        this.gameMode = info.getProperty("game_mode");
        startPoint = new Point(Integer.parseInt(info.getProperty("start_X")), Integer.parseInt(info
                .getProperty("start_Y")));
        endPoint = new Point(Integer.parseInt(info.getProperty("end_X")), Integer.parseInt(info
                .getProperty("end_Y")));

        this.pickedTemplate=rand.nextInt(NUMBER_OF_TEMPLATES)+1;
        this.difficulty=info.getProperty("difficulty");

        Properties property = new Properties();
        try {
            property.load(getClass().getResourceAsStream("/Mazes/"+difficulty+ ".Mazes"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        this.listOfTakenPositions = this.makeSetOFWallCellsPositions(property.getProperty("template"+pickedTemplate) );
        
        this.cellWidth = Integer.parseInt(info.getProperty("cell_width"));
    }

    /*initializations for new game*/
    public Maze loadConfiguration() {
        MazeBuilder builder = new MazeBuilder();
        builder.setStartPoint(startPoint);
        builder.setEndPoint(endPoint);

        /*Adding Walls to MazeBuilder in Random valid positions*/
        for (int i = 0; i < listOfTakenPositions.size(); i++) {
            Point pos = listOfTakenPositions.get(i);
            Wall wall = new WallCell();
            wall.setBreakable(false);

            int breakableProbability = rand.nextInt(101);

            if (breakableProbability>=85 && notBorder(pos))
                wall.setBreakable(true);

            setPositionOf((Drawable) wall,pos);
            builder.addMazeObject(wall, pos);
        }

        /*Adding Bombs to MazeBuilder in Random valid positions*/
        for (int i = 0; i < bombsNum; i++) {
            int x = rand.nextInt(mazeWidth);
            int y = rand.nextInt(mazeLength);
            Point bombPosition = new Point(x, y);

            int range = rand.nextInt(range_of_bomb_actions) + 1;// randomize bomb's effect in the interval [1,num_of_bomb_types]

            if (!listOfTakenPositions.contains(bombPosition) && !bombPosition.equals(startPoint)  ) {
                listOfTakenPositions.add(bombPosition);
                MazeObject bomb = ObjectsFactory.produce(ObjectsFactory.BOMB, range, bombPosition);
                setPositionOf((Drawable) bomb,bombPosition);
                builder.addMazeObject(bomb, bombPosition);
            } else i--;
        }

        /*Adding Gifts to MazeBuilder in Random valid positions*/
        for (int i = 0; i < giftsNum; i++) {
            int x = rand.nextInt(mazeWidth);
            int y = rand.nextInt(mazeLength);
            Point giftPosition = new Point(x, y);

            int range = rand.nextInt(range_of_gift_actions) + 1;// randomize gift's effect in the interval [1,num_of_gift_types]

            if (!listOfTakenPositions.contains(giftPosition) && !giftPosition.equals(startPoint)) {
                listOfTakenPositions.add(giftPosition);
                boolean healthGift = rand.nextBoolean();
                boolean liveGift = rand.nextBoolean();
                MazeObject gift = null;

                //TODO add life gifts
                if (healthGift) {
                    gift = ObjectsFactory.produce(ObjectsFactory.HEAL_GIFT, range, giftPosition);
                    builder.addMazeObject(gift, giftPosition);
                }  else {
                    gift = ObjectsFactory.produce(ObjectsFactory.AMMO_GIFT, range, giftPosition);
                    builder.addMazeObject(gift, giftPosition);
                }
                setPositionOf((Drawable) gift,giftPosition);
            } else i--;
        }
        builder.setCellSize(cellWidth);
        builder.setLength(mazeLength);
        builder.setWidth(mazeWidth);
        return builder.buildMaze();
    }

    private boolean notBorder(Point point) {
        int x = point.x;
        int y= point.y;
        int offset =2;
        if (x > (startPoint.x +offset) &&  x< (endPoint.x -offset) && y >(startPoint.y+offset) && y< (endPoint.y-offset) )
        {
                return true;
        }

        return false;
    }

    private void setPositionOf(final Drawable drawable, final Point pos) {
        drawable.setSrcX(0);
        drawable.setSrcY(0);
        drawable.setSrcWidth(40);
        drawable.setSrcHeight(40);
        drawable.setDestinationX(pos.x * cellWidth);
        drawable.setDestinationY(pos.y * cellWidth);
        drawable.setDestinationWidth(cellWidth);
        drawable.setDestinationHeight(cellWidth);
    }


    /*
    * takes a String of points of Wall cells
    * and convert it to a hash set
    */
    private LinkedList makeSetOFWallCellsPositions(String WallPositions) {
        LinkedList<Point> setOfWallCells = new LinkedList();
        String[] pointString = WallPositions.split(" ");
        for (String s : pointString) {
            s = s.replace("(", "");
            s = s.replace(")", "");

            int x = Integer.parseInt(s.split(",")[0]);
            int y = Integer.parseInt(s.split(",")[1]);

            Point wallCellPoint = new Point(x, y);
            setOfWallCells.add(wallCellPoint);
        }
        return setOfWallCells;
    }

    public LinkedList<Point> getListOfTakenPositions() {
        return listOfTakenPositions;
    }
}
