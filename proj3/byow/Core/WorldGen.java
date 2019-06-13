package byow.Core;

import byow.InputDemo.InputSource;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import edu.princeton.cs.introcs.StdDraw;
import java.util.Scanner;

/**
 * Draws a world that contains RANDOM tiles.
 */

public class WorldGen implements InputSource {
    private static final int WIDTH = 100;
    private static final int HEIGHT = 50;
    private TERenderer ter;

    private static long seed;
    private static Random random;
    private Room prevRoom;
    private ArrayList<int[]> floorTiles;
    private ArrayList<Room> roomArray;
    private int avatarX;
    private int avatarY;
    boolean col = false;
    boolean light = false;

    public WorldGen() {
        prevRoom = null;
        floorTiles = new ArrayList<int[]>();
        roomArray = new ArrayList<>();
        ter = new TERenderer();
    }

    private class Room {
        int xTL;
        int yTL;
        int width;
        int height;
        int xTR;
        int yTR;
        int xBR;
        int yBR;
        int xBL;
        int yBL;
        int xMid;
        int yMid;
        Room(int xTL, int yTL, int width, int height) {
            this.xTL = xTL;
            this.yTL = yTL;
            this.width = width;
            this.height = height;
            xTR = xTL + width;
            yTR = yTL;
            xBR = xTR;
            yBR = yTL - height;
            xBL = xTL;
            yBL = yBR;
            xMid = xTL + (width / 2);
            yMid = yTL - (height / 2);
        }
    }

    private void drawRoom(int width, int height, int x, int y, TETile[][] tiles) {
        if (checkRoom(x, y, width, height, tiles) == 0) {
            return;
        }
        if (!checkSidesRoom(new Room(x, y, width, height), tiles)) {
            return;
        }
        //TOP WALL
        for (int i = 0; i < width; i++) {
            if (checkClear(x + i, y, tiles) == 1) {
                tiles[x + i][y] = Tileset.WALL;
            }
        }
        //LEFT WALL
        for (int i = 0; i < height; i++) {
            if (checkClear(x, y - i, tiles) == 1) {
                tiles[x][y - i] = Tileset.WALL;
            }
        }
        //BOTTOM WALL
        for (int i = 0; i < width; i++) {
            if (checkClear(x + i, y - height, tiles) == 1) {
                tiles[x + i][y - height] = Tileset.WALL;
            }
        }
        //RIGHT WALL
        for (int i = 0; i <= height; i++) {
            if (checkClear(x + width, y - height + i, tiles) == 1) {
                tiles[x + width][(y - height) + i] = Tileset.WALL;
            }
        }
        floorify(x, y, width, height, tiles);
        if (prevRoom == null) {
            prevRoom = new Room(x, y, width, height);
        } else {
            Room currRoom = new Room(x, y, width, height);
            prevRoom = currRoom;
        }
        roomArray.add(new Room(x, y, width, height));
    }

    private int checkClear(int x, int y, TETile[][] tiles) {
        if (tiles[x][y].equals(Tileset.FLOOR)) {
            return 1;
        } else {
            return 0;
        }
    }

    private void floorify(int x, int y, int width, int height, TETile[][] tiles) {
        int upLeftX = x + 1;
        int upLeftY = y - 1;
        for (int i = 0; i < width - 1; i++) {
            for (int j = 0; j < height - 1; j++) {
                if (!(upLeftX + i > WIDTH || upLeftY - j < 0)) {
                    tiles[upLeftX + i][upLeftY - j] = Tileset.NOTHING;
                    int[] floorArray = {upLeftX + i, upLeftY - j};
                    floorTiles.add(floorArray);
                }
            }
        }

    }

    private int checkRoom(int x, int y, int width, int height, TETile[][] tiles) {
        if (x - 2 < 6 || x + 2 > WIDTH - 6) {
            return 0;
        }
        if (y - 2 < 6 || y + 2 > HEIGHT - 6) {
            return 0;
        }
        if (x + width + 2 > WIDTH - 6) {
            return 0;
        }
        if (y - height - 2 < 6) {
            return 0;
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (tiles[x + i][y - j].equals(Tileset.WALL)) {
                    return 0;
                }
            }
        }
        return 1;
    }

    public TETile[][] worldGen4(long seed1, TETile[][] tiles) {
        this.seed = seed1;
        random = new Random(seed);
        tiles = new TETile[WIDTH][HEIGHT];
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                tiles[i][j] = Tileset.NOTHING;
            }
        }
        traceOutline(tiles);
        addFloor(tiles);
        int numRooms = random.nextInt(100) + 450;
        for (int i = 0; i < numRooms; i++) {
            int leftX = random.nextInt(100);
            int leftY = random.nextInt(50);
            int width = random.nextInt(7) + 4;
            int height = random.nextInt(7) + 4;
            drawRoom(width, height, leftX, leftY, tiles);
        }
        int hallways = random.nextInt(10) + 15;
        for (int i = 0; i < hallways; i++) {
            addHallway(tiles);
        }
        this.avatarX = random.nextInt(WIDTH);
        this.avatarY = random.nextInt(HEIGHT);
        while (!(tiles[avatarX][avatarY].equals(Tileset.FLOOR))) {
            this.avatarX = random.nextInt(WIDTH);
            this.avatarY = random.nextInt(HEIGHT);
        }
        tiles[avatarX][avatarY] = Tileset.AVATAR;
        int numEncounters = 1;
        for (int i = 0; i < numEncounters; i++) {
            int encounterX = random.nextInt(WIDTH);
            int encounterY = random.nextInt(HEIGHT);
            while (!(tiles[encounterX][encounterY].equals(Tileset.FLOOR))) {
                encounterX = random.nextInt(WIDTH);
                encounterY = random.nextInt(HEIGHT);
            }
            tiles[encounterX][encounterY] = Tileset.LOCKED_DOOR;
        }
        return tiles;
    }

    TETile[][] avatarKeyReplay(TETile[][] tiles, char key) {
        switch (key) {
            case 'w' :
                if (checkCoordinates(avatarX, avatarY + 1)
                        && tiles[avatarX][avatarY + 1].equals(Tileset.FLOOR)) {
                    tiles[avatarX][avatarY] = Tileset.FLOOR;
                    tiles[avatarX][avatarY + 1] = Tileset.AVATAR;
                    avatarY = avatarY + 1;
                    return tiles;
                } else if (checkCoordinates(avatarX, avatarY + 1)
                        && tiles[avatarX][avatarY + 1].equals(Tileset.LOCKED_DOOR)) {
                    ter.renderTile(avatarX, avatarY, Tileset.FLOOR, tiles);
                    tiles[avatarX][avatarY] = Tileset.FLOOR;
                    tiles[avatarX][avatarY + 1] = Tileset.AVATAR;
                    avatarY = avatarY + 1;
                    return tiles;
                }
                break;
            case 'a' :
                if (checkCoordinates(avatarX - 1, avatarY)
                        && tiles[avatarX - 1][avatarY].equals(Tileset.FLOOR)) {
                    tiles[avatarX][avatarY] = Tileset.FLOOR;
                    tiles[avatarX - 1][avatarY] = Tileset.AVATAR;
                    avatarX = avatarX - 1;
                    return tiles;
                }
                break;
            case 's' :
                if (checkCoordinates(avatarX, avatarY - 1)
                        && tiles[avatarX][avatarY - 1].equals(Tileset.FLOOR)) {
                    tiles[avatarX][avatarY] = Tileset.FLOOR;
                    tiles[avatarX][avatarY - 1] = Tileset.AVATAR;
                    avatarY = avatarY - 1;
                    return tiles;
                }
                break;
            case 'd' :
                if (checkCoordinates(avatarX + 1, avatarY)
                        && tiles[avatarX + 1][avatarY].equals(Tileset.FLOOR)) {
                    tiles[avatarX][avatarY] = Tileset.FLOOR;
                    tiles[avatarX + 1][avatarY] = Tileset.AVATAR;
                    avatarX = avatarX + 1;
                    return tiles;
                }
                break;
            default: return tiles;
        }
        return tiles;
    }

    private void avatarKey(TETile[][] tiles) {
        while (true) {
            switch (getNextKey()) {
                case 'w' :
                    if (checkCoordinates(avatarX, avatarY + 1)
                            && tiles[avatarX][avatarY + 1].equals(Tileset.FLOOR)) {
                        TERenderer ter1 = new TERenderer();
                        ter1.renderTile(avatarX, avatarY, Tileset.FLOOR, tiles);
                        tiles[avatarX][avatarY] = Tileset.FLOOR;
                        ter1.renderTile(avatarX, avatarY + 1, Tileset.AVATAR, tiles);
                        tiles[avatarX][avatarY + 1] = Tileset.AVATAR;
                        avatarY = avatarY + 1;
                    } else if (checkForDoor(avatarX, avatarY + 1, tiles)) {
                        TERenderer ter1 = new TERenderer();
                        ter1.renderTile(avatarX, avatarY, Tileset.FLOOR, tiles);
                        tiles[avatarX][avatarY] = Tileset.FLOOR;
                        avatarY = avatarY + 1;
                    }
                    break;
                case 'a' :
                    if (checkCoordinates(avatarX - 1, avatarY)
                            && tiles[avatarX - 1][avatarY].equals(Tileset.FLOOR)) {
                        TERenderer ter1 = new TERenderer();
                        ter1.renderTile(avatarX, avatarY, Tileset.FLOOR, tiles);
                        tiles[avatarX][avatarY] = Tileset.FLOOR;
                        ter1.renderTile(avatarX - 1, avatarY, Tileset.AVATAR, tiles);
                        tiles[avatarX - 1][avatarY] = Tileset.AVATAR;
                        avatarX = avatarX - 1;
                    } else if (checkForDoor(avatarX - 1, avatarY, tiles)) {
                        TERenderer ter1 = new TERenderer();
                        ter1.renderTile(avatarX, avatarY, Tileset.FLOOR, tiles);
                        tiles[avatarX][avatarY] = Tileset.FLOOR;
                        avatarX = avatarX - 1;
                    }
                    break;
                case 's' :
                    if (checkCoordinates(avatarX, avatarY - 1)
                            && tiles[avatarX][avatarY - 1].equals(Tileset.FLOOR)) {
                        TERenderer ter1 = new TERenderer();
                        ter1.renderTile(avatarX, avatarY, Tileset.FLOOR, tiles);
                        tiles[avatarX][avatarY] = Tileset.FLOOR;
                        ter1.renderTile(avatarX, avatarY - 1, Tileset.AVATAR, tiles);
                        tiles[avatarX][avatarY - 1] = Tileset.AVATAR;
                        avatarY = avatarY - 1;
                    } else if (checkForDoor(avatarX, avatarY - 1, tiles)) {
                        TERenderer ter1 = new TERenderer();
                        ter1.renderTile(avatarX, avatarY, Tileset.FLOOR, tiles);
                        tiles[avatarX][avatarY] = Tileset.FLOOR;
                        avatarY = avatarY - 1;
                    }
                    break;
                case 'd' :
                    if (checkCoordinates(avatarX + 1, avatarY)
                            && tiles[avatarX + 1][avatarY].equals(Tileset.FLOOR)) {
                        TERenderer ter1 = new TERenderer();
                        ter1.renderTile(avatarX, avatarY, Tileset.FLOOR, tiles);
                        tiles[avatarX][avatarY] = Tileset.FLOOR;
                        ter1.renderTile(avatarX + 1, avatarY, Tileset.AVATAR, tiles);
                        tiles[avatarX + 1][avatarY] = Tileset.AVATAR;
                        avatarX = avatarX + 1;
                    } else if (checkForDoor(avatarX + 1, avatarY, tiles)) {
                        TERenderer ter1 = new TERenderer();
                        ter1.renderTile(avatarX, avatarY, Tileset.FLOOR, tiles);
                        tiles[avatarX][avatarY] = Tileset.FLOOR;
                        avatarX = avatarX + 1;
                    }
                    break;
                case ':' :
                    col = true;
                    break;
                case 'Q' :
                    if (col) {
                        deleteFile();
                        createNewFile(tiles);
                        System.exit(0);
                    }
                    break;
                case 'q' :
                    if (col) {
                        deleteFile();
                        createNewFile(tiles);
                        System.exit(0);
                    }
                    break;
                default: System.out.println("Error");
            }
        }
    }

    private boolean checkForDoor(int x, int y, TETile[][] tiles) {
        if (checkCoordinates(x, y) && (tiles[x][y].equals(Tileset.LOCKED_DOOR)
                || tiles[x][y].equals(Tileset.UNLOCKED_DOOR))) {
            light = !light;
            TERenderer ter1 = new TERenderer();
            ter1.renderTile(x, y, Tileset.AVATAR, tiles);
            tiles[x][y] = Tileset.AVATAR;
            StdDraw.enableDoubleBuffering();
            changeLightSource(tiles, light);
            StdDraw.show();
            return true;
        }
        return false;
    }

    private void changeLightSource(TETile[][] tiles, boolean light1) {
        TERenderer ter1 = new TERenderer();
        if (light1) {
            for (int i = 0; i < WIDTH; i++) {
                for (int j = 0; j < HEIGHT; j++) {
                    if (tiles[i][j].equals(Tileset.NOTHING)) {
                        ter1.renderTile(i, j, Tileset.WATER, tiles);
                    }
                    if (tiles[i][j].equals(Tileset.WALL)) {
                        ter1.renderTile(i, j, Tileset.GRASS, tiles);
                    }
                }
            }
        } else {
            for (int i = 0; i < WIDTH; i++) {
                for (int j = 0; j < HEIGHT; j++) {
                    if (tiles[i][j].equals(Tileset.WATER)) {
                        ter1.renderTile(i, j, Tileset.NOTHING, tiles);
                    }
                    if (tiles[i][j].equals(Tileset.GRASS)) {
                        ter1.renderTile(i, j, Tileset.WALL, tiles);
                    }
                }
            }
        }
    }

    private void traceOutline(TETile[][] tiles) {
        //LEFT COLUMN
        for (int i = 0; i < HEIGHT; i++) {
            tiles[0][i] = Tileset.WALL;
        }
        //TOP ROW
        for (int i = 0; i < WIDTH; i++) {
            tiles[i][HEIGHT - 1] = Tileset.WALL;
        }
        //RIGHT COLUMN
        for (int i = 0; i < HEIGHT; i++) {
            tiles[WIDTH - 1][HEIGHT - i - 1] = Tileset.WALL;
        }
        for (int i = 0; i < WIDTH; i++) {
            tiles[WIDTH - i - 1][0] = Tileset.WALL;
        }
    }

    private void addFloor(TETile[][] tiles) {
        for (int i = 1; i < WIDTH - 1; i++) {
            for (int j = HEIGHT - 2; j > 0; j--) {
                tiles[i][j] = Tileset.FLOOR;
            }
        }
    }

    private void addHallway(TETile[][] tiles) {
        int x = -1;
        int y = -1;
        int length = -1;
        boolean or = false;
        boolean hallWayCheck = false;
        while (!hallWayCheck) {
            int orientation = random.nextInt(2);
            switch (orientation) {
                case 0: or = true;
                        break;
                case 1: or = false;
                        break;
                default: or = true;
            }
            length = random.nextInt(10) + 10;
            int index = random.nextInt(roomArray.size());
            Room r1 = roomArray.get(index);
            int side = random.nextInt((4));
            switch (side) {
                //ON TOP OF ROOM
                case 0: {
                    x = r1.xTL;
                    y = r1.yTL + 2;
                    hallWayCheck = checkHallway(x, y, length, or, tiles);
                    break;
                }
                //ON RIGHT OF ROOM
                case 1: {
                    x = r1.xTR + 2;
                    y = r1.yTR;
                    hallWayCheck = checkHallway(x, y, length, or, tiles);
                    break;
                }
                //ON BOTTOM OF ROOM
                case 2: {
                    x = r1.xBL;
                    y = r1.yBL - 2;
                    hallWayCheck = checkHallway(x, y, length, or, tiles);
                    break;
                }
                //ON LEFT OF ROOM
                case 3: {
                    x = r1.xTL - 2;
                    y = r1.yTL;
                    hallWayCheck = checkHallway(x, y, length, or, tiles);
                    break;
                }
                default: break;
            }
        }
        drawHallway(x, y, length, or, tiles);
    }

    private void drawHallway(int x, int y, int length, boolean or, TETile[][] tiles) {
        if (or) {
            for (int i = 0; i < length; i++) {
                tiles[x][y - i] = Tileset.WALL;
            }
        } else {
            for (int i = 0; i < length; i++) {
                tiles[x + i][y] = Tileset.WALL;
            }
        }
    }

    private boolean checkHallway(int x, int y, int length, boolean orientation, TETile[][] tiles) {
        //VERTICAL HALLWAY
        if (orientation) {
            if (y - length - 2 < 6) {
                return false;
            }
            if (y >= HEIGHT - 6 || y < 6 || x >= WIDTH - 6 || x < 6) {
                return false;
            }
            if (tiles[x][y].equals(Tileset.WALL) && tiles[x][y - length].equals(Tileset.WALL)) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                if (tiles[x][y - i].equals(Tileset.WALL)
                        || tiles[x][y - i].equals(Tileset.NOTHING)) {
                    return false;
                }
            }
            return true;
        } else {
            if (x + length + 2 > WIDTH) {
                return false;
            }
            if (x >= WIDTH || x < 0 || y >= HEIGHT || y < 0) {
                return false;
            }
            if (tiles[x][y].equals(Tileset.WALL) && tiles[x + length][y].equals(Tileset.WALL)) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                if (tiles[x + i][y].equals(Tileset.WALL)
                        || tiles[x + i][y].equals(Tileset.NOTHING)) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean checkCoordinates(int x, int y) {
        if (x < 0 || x > WIDTH - 1) {
            return false;
        }
        if (y < 0 || y > HEIGHT - 1) {
            return false;
        }
        return true;
    }

    private boolean checkSidesRoom(Room r, TETile[][] tiles) {
        for (int i = 0; i < r.width; i++) {
            if (checkCoordinates(r.xTL + i, r.yTL - 1)) {
                if (tiles[r.xTL + i][r.yTL - 1].equals(Tileset.WALL)) {
                    return false;
                }
            }
        }
        for (int i = 0; i < r.height; i++) {
            if (checkCoordinates(r.xTL - 1, r.yTL - i)) {
                if (tiles[r.xTL - 1][r.yTL - i].equals(Tileset.WALL)) {
                    return false;
                }
            }
        }
        for (int i = 0; i < r.width; i++) {
            if (checkCoordinates(r.xTL + i, r.yBL - 1)) {
                if (tiles[r.xTL + i][r.yBL - 1].equals(Tileset.WALL)) {
                    return false;
                }
            }
        }
        for (int i = 0; i < r.height; i++) {
            if (checkCoordinates(r.xBR + 1, r.yBR + i)) {
                if (tiles[r.xBR + 1][r.yBR + i].equals(Tileset.WALL)) {
                    return false;
                }
            }
        }
        return true;
    }

    void generateStartScreen(TETile[][] tiles) {
        StdDraw.setCanvasSize(600, 600);
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 22));
        StdDraw.text(0.5, 0.75, "CS61B: THE GAME");
        StdDraw.setFont();
        StdDraw.text(0.5, 0.65, "NEW GAME (N)");
        StdDraw.text(0.5, 0.55, "LOAD GAME (L)");
        StdDraw.text(0.5, 0.45, "QUIT (Q)");
        switch (getNextKey()) {
            case 'n':
                enterSeedScreen();
                TERenderer ter1 = new TERenderer();
                ter1.initialize(WIDTH, HEIGHT + 5);
                tiles = worldGen4(seed, tiles);
                ter1.renderFrame(tiles);
                StdDraw.show();
                mainLoop(tiles);
                break;
            case 'l':
                TETile[][] tilesPrev = null;
                tilesPrev = readFile(tilesPrev);
                TERenderer ter2 = new TERenderer();
                ter2.initialize(WIDTH, HEIGHT + 5);
                ter2.renderFrame(tilesPrev);
                StdDraw.show();
                mainLoop(tilesPrev);
                break;
            case 'N':
                enterSeedScreen();
                TERenderer ter3 = new TERenderer();
                ter3.initialize(WIDTH, HEIGHT + 5);
                tiles = worldGen4(seed, tiles);
                ter3.renderFrame(tiles);
                StdDraw.show();
                mainLoop(tiles);
                break;
            case 'L':
                TETile[][] tilesPrev2 = null;
                tilesPrev = readFile(tilesPrev2);
                TERenderer ter4 = new TERenderer();
                ter4.initialize(WIDTH, HEIGHT + 5);
                ter4.renderFrame(tilesPrev2);
                StdDraw.show();
                mainLoop(tilesPrev2);
                break;
            default: System.out.println("error");
        }
    }

    private long enterSeedScreen() {
        StdDraw.setCanvasSize(600, 600);
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 22));
        StdDraw.text(0.5, 0.75, "ENTER SEED (TYPE S WHEN DONE)");
        StdDraw.setFont();
        seed = 0;
        char nextKey = getNextKey();
        while (nextKey != 's') {
            int nextInt = nextKey - '0';
            seed = seed * 10 + nextInt;
            StdDraw.setPenColor(Color.BLACK);
            StdDraw.filledRectangle(0.5, 0.65, 0.15, 0.03);
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.text(0.5, 0.65, Long.toString(seed));
            StdDraw.show();
            nextKey = getNextKey();
        }
        return seed;
    }

    private void mainLoop(TETile[][] tiles) {
        while (true) {
            int mouseX = (int) StdDraw.mouseX();
            int mouseY = (int) StdDraw.mouseY();
            if (checkCoordinates(mouseX, mouseY)) {
                ter.displayElement(mouseX, mouseY, tiles);
                StdDraw.show();
            }
            ter.displayTime(tiles);
            StdDraw.show();
            if (StdDraw.hasNextKeyTyped()) {
                avatarKey(tiles);
            }

        }
    }

    public char getNextKey() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                return StdDraw.nextKeyTyped();
            }
        }
    }

    public boolean possibleNextInput() {
        return true;
    }

    void createNewFile(TETile[][] tiles) {
        try {
            PrintWriter writer = new PrintWriter("file.txt");
            for (int i = 0; i < WIDTH; i++) {
                for (int j = 0; j < HEIGHT; j++) {
                    writer.println(tiles[i][j].description());
                }
            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    TETile[][] readFile(TETile[][] tiles) {
        tiles = new TETile[WIDTH][HEIGHT];
        File file = new File("file.txt");
        try {
            Scanner sc = new Scanner(file);
            for (int i = 0; i < WIDTH; i++) {
                for (int j = 0; j < HEIGHT; j++) {
                    if (sc.hasNextLine()) {
                        String s = sc.nextLine();
                        tiles[i][j] = restore(s, i, j);
                    }
                }
            }
            return tiles;
        } catch (FileNotFoundException fe) {
            System.out.println(fe);
        }
        return null;
    }

    private TETile restore(String s, int i, int j) {
        switch (s) {
            case "wall" : return Tileset.WALL;
            case "you": avatarX = i;
                        avatarY = j;
                        return Tileset.AVATAR;
            case "nothing": return Tileset.NOTHING;
            case "locked door": return Tileset.LOCKED_DOOR;
            case "floor": return Tileset.FLOOR;
            default: return null;
        }
    }

    boolean deleteFile() {
        File f = new File("file.txt");
        boolean success = f.delete();
        return success;
    }
}
