package levelGenerators.groupW;

import engine.core.MarioLevelModel;
import engine.helper.MarioTimer;
import levelGenerators.MarioLevelGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/***
 * GroupW generator
 * Sampling with parameter search space
 */
@SuppressWarnings("FieldCanBeLocal")
public class LevelGenerator implements MarioLevelGenerator {
    private static int sampleWidth = 5;
    private static String folderName = "levels/original/";

    private Random rnd;

    // divide the level horizontally
    // ground, middle, sky
    private int GROUND_Y_LOCATION = 13;
    private int SKY_Y_LOCATION = 5;

    // tile mutate rate
    private float OBJECT_MUTATE_PROB = 0.2f;
    // enemy mutate rate
    private float ENEMY_MUTATE_PROB = 0.5f;

    // usually don't change this
    private int FLOOR_PADDING = 3;

    private boolean mutate = false;

    @Override
    public String getGeneratedLevel(MarioLevelModel model, MarioTimer timer) {
        rnd = new Random();
        model.clearMap();

        // initialize connection map
        Map<Character, ArrayList<Character>> connections = new HashMap<>(MarioLevelModel.getAllTiles().length);

        // sampling from the origin levels
        // building entire map
        for (int i = 0; i < model.getWidth() / sampleWidth; i++) {
            try {
                int sourceX;
                // first and last segment will be
                // the same from other level
                if (i == 0) {
                    sourceX = 0;
                } else if (i == model.getWidth() / sampleWidth - 1) {
                    sourceX = model.getWidth() - sampleWidth;
                } else {
                    sourceX = rnd.nextInt(model.getWidth());
                }
                model.copyFromString(i * sampleWidth, 0, sourceX, 0, sampleWidth, model.getHeight(), this.getRandomLevel());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mutate) {
            // loop through neighbours and add the valid ones to the list
            for (int x = 0; x < model.getWidth(); x++) {
                for (int y = 0; y < model.getHeight(); y++) {

                    char block = model.getBlock(x, y);
                    // remove start and exit
                    if (block == MarioLevelModel.MARIO_START || block == MarioLevelModel.MARIO_EXIT) {
                        model.setBlock(x, y, MarioLevelModel.EMPTY);
                    }

                    connections.putIfAbsent(block, new ArrayList<>());
                    // build connectivity map
                    for (int i = x - 1; i <= x + 1; i++) {
                        for (int j = y - 1; j < y + 1; j++) {
                            // not the center
                            char neigh = model.getBlock(i, j);
                            if (i == x && j == y || neigh == MarioLevelModel.EMPTY) {
                                continue;
                            }
                            connections.get(block).add(neigh);
                        }
                    }
                }
            }

//        random initialization
//        int nInitCoords = 3;
//        for (int i = 0; i < nInitCoords; i++) {
//            int rndX = rnd.nextInt(model.getWidth());
//            int rndY = rnd.nextInt(model.getHeight());
//
//            for (int m = rndX - 1; m <= rndX + 1; m++) {
//                for (int n = rndY - 1; n < rndY + 1; n++) {
//                    Character block = model.getBlockNull(m, n);
//                    char block = MarioLevelModel.getCollectablesTiles()[rnd.nextInt(MarioLevelModel.getCollectablesTiles().length)];
//                    model.setBlock(m, n, block);
//                }
//            }
//        }

            for (int x = 0; x < model.getWidth(); x++) {
                for (int y = 0; y < model.getHeight(); y++) {
                    char block = model.getBlock(x, y);

                    // mutate enemy chars
                    if (belongTo(block, MarioLevelModel.getEnemyCharacters())) {
                        if (rnd.nextDouble() > ENEMY_MUTATE_PROB) {
                            char[] allEnemies = MarioLevelModel.getEnemyCharacters();
                            model.setBlock(x, y, allEnemies[rnd.nextInt(allEnemies.length)]);
                        }
                        continue;
                    }

                    // mutate blocks and its neighbours
                    // preserve ground and sky
                    if (y < GROUND_Y_LOCATION && y > SKY_Y_LOCATION
                            && block != MarioLevelModel.EMPTY && block != MarioLevelModel.PIPE_FLOWER) {
                        for (int i = x - 1; i <= x + 1; i++) {
                            for (int j = y - 1; j < y + 1; j++) {
//                         not the center
                                if (i == x && j == y) {
                                    continue;
                                }
                                char neigh = model.getBlock(i, j);
                                if (rnd.nextDouble() < OBJECT_MUTATE_PROB && neigh == MarioLevelModel.EMPTY) {
                                    ArrayList<Character> temp = connections.get(block);
                                    if (temp != null && temp.size() > 0) {
                                        model.setBlock(i, j, temp.get(rnd.nextInt(temp.size())));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        model.setRectangle(0, 14, FLOOR_PADDING, 2, MarioLevelModel.GROUND);
        model.setRectangle(model.getWidth() - 1 - FLOOR_PADDING, 14, FLOOR_PADDING, 2, MarioLevelModel.GROUND);
        model.setBlock(FLOOR_PADDING / 2, 13, MarioLevelModel.MARIO_START);
        model.setBlock(model.getWidth() - 1 - FLOOR_PADDING / 2, 13, MarioLevelModel.MARIO_EXIT);

        System.out.println(model.getMap());
        return model.getMap();
    }

    /**
     * select a random original level
     * from the directory
     *
     * @return level string
     */
    private String getRandomLevel() throws IOException {
        File[] listOfFiles = new File(folderName).listFiles();
        File file = Objects.requireNonNull(listOfFiles)[rnd.nextInt(listOfFiles.length)];
        System.out.println(file.toString());

        List<String> lines = Files.readAllLines(file.toPath());
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            result.append(line).append("\n");
        }
        return result.toString();
    }

    /**
     * check if a block belongs to a certain
     * type of tile
     *
     * @param block    char
     * @param category char[]
     * @return boolean
     */
    private boolean belongTo(char block, char[] category) {
        for (char c : category) {
            if (block == c) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getGeneratorName() {
        return "GroupWGenerator";
    }
}
