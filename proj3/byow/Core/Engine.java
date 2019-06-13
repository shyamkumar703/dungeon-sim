package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    boolean colon = true;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        TETile[][] randomTiles = null;
        WorldGen world = new WorldGen();
        world.generateStartScreen(randomTiles);
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, both of these calls:
     * - interactWithInputString("n123sss:q")
     * - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     * - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        TETile[][] randomTiles = null;
        WorldGen world = new WorldGen();
        char[] inputChar = input.toCharArray();
        if (inputChar[0] == 'l') {
            randomTiles = world.readFile(randomTiles);
        } else if (inputChar[0] == 'n') {
            System.out.println(getSeed(1, inputChar));
            randomTiles = world.worldGen4(getSeed(1, inputChar), randomTiles);
        }
        for (char c : inputChar) {
            if (c >= 97 && c <= 122) {
                randomTiles = world.avatarKeyReplay(randomTiles, c);
            }
            if (c == 58) {
                colon = true;
            }
            if (colon && (c == 81 || c == 113)) {
                world.deleteFile();
                world.createNewFile(randomTiles);
            }
        }
        return randomTiles;
    }

    private long getSeed(int indexStart, char[] inputChar) {
        long seed = 0L;
        while (inputChar[indexStart] <= 57) {
            int nextInt = inputChar[indexStart] - '0';
            seed = seed * 10 + nextInt;
            indexStart += 1;
        }
        return seed;
    }

    public static void main(String[] args) {
        Engine e = new Engine();
        e.interactWithKeyboard();
    }
}
