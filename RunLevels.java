import agents.MarioAgent;
import engine.core.MarioGame;
import engine.core.MarioResult;
import engine.helper.MarioStats;
import levelGenerators.MarioLevelGenerator;

import static engine.helper.RunUtils.*;

public class RunLevels {

    private static void printHelp() {
        System.out.println("RunLevels.java usage: 4+ args expected\n" +
                "\t[index = 0] number of levels\n" +
                "\t[index = 1] repetitions per level\n" +
                "\t[index = 2] using level generator (boolean; using preset levels if false)" +
                "\t[index = 3] AI agent response limit (20 default)" +
                "\t[index = 4...] preset levels (if not using generator). there should be exactly the number specified" +
                "before (index 0) of filepaths, separated by space.");
    }

    public static void main(String[] args) {
        // Run default settings:
        int noLevels = 2;
        int repsPerLevel = 50;
        boolean usingGenerator = true;
        int agentTimer = 20;
        String[] levels = new String[]{"levels/original/lvl-1.txt", "levels/original/lvl-2.txt"};  // TODO: Set levels here.If using generator,set null; 

        // Check arguments for overwrite:
        if (args.length >= 4) {
            noLevels = Integer.parseInt(args[0]);
            repsPerLevel = Integer.parseInt(args[1]);
            usingGenerator = Boolean.parseBoolean(args[2]);
            agentTimer = Integer.parseInt(args[3]);

            // Optional arguments, to be set only if not using level generator:
            levels = new String[noLevels];
            if (!usingGenerator && args.length == 4 + noLevels) {
                System.arraycopy(args, 4, levels, 0, noLevels);
            } else if (!usingGenerator) {
                printHelp();
                return;
            }
        }

        // Create a MarioGame instance, AI to play the game and level generator (not necessary if `levels' used)
        MarioGame game = new MarioGame();
        MarioAgent agent = new agents.robinBaumgarten.Agent();  // TODO: agent to play the game
        MarioAgent agent2 = new agents.trondEllingsen.Agent();  // TODO: agent2 to play the game
        MarioAgent[] allAgent = {agent,agent2};        // TODO: Set agents here.(New some examples and add them into the array.)
        MarioLevelGenerator generator = new levelGenerators.notch.LevelGenerator();  // TODO: Set level generator

        if (!usingGenerator) {  // Make sure the value is correct if not using level generator.
            noLevels = levels.length;
        }

        MarioStats average = new MarioStats();  // Keep average of statistics over all the runs
        MarioStats bestResult = new MarioStats();
        for (int i = 0; i < noLevels; i++) {
            // Find level
            String level;
            if (usingGenerator) {
                level = generateLevel(generator);
            } else {
                level = retrieveLevel(levels[i]);
            }
            
            int players = allAgent.length;
            MarioStats[] playerStats = new MarioStats[players];
            float[] timeOut = new float[players];
            for (int p = 0; p < players; p++) {
            	// Run the level several times
            	average = new MarioStats();
            	int timeOutTime = 0;
                for (int j = 0; j < repsPerLevel; j++) {
                    MarioResult result = game.runGame(allAgent[p], level, agentTimer, 0, false);
                    
                    String gameStatus = result.getGameStatus().toString();
                    System.out.println((p+1) + "p:" + (i+1) + "/" + noLevels + ";" + (j+1) + "/" + repsPerLevel + ": "
                            + gameStatus);
                    MarioStats stats = resultToStats(result);
                    average = average.merge(stats);
                    if (gameStatus == "TIME_OUT") {
                    	timeOutTime += 1;
                    }
                }
                addResult(average,bestResult);
                playerStats[p] = average;
                timeOut[p] = timeOutTime / repsPerLevel;
                System.out.println("------" + (p+1) + "p------");
                System.out.println(average.toString());
            }
            double bestFitness = 0;
            double winRateSum = 0;
            double timeOutSum = 0;
            for (int p = 0; p < players; p++) {
            	double fitness = calFitness(playerStats[p],bestResult);
            	if (fitness > bestFitness) {
            		bestFitness = fitness;
            	}
            	winRateSum += playerStats[p].winRate;
            	timeOutSum += timeOut[p];
            }
            double win = winRateSum/players;            // TODO: Calculate win param;
            double timeout = Math.exp(-timeOutSum/players);  // TODO: Calculate timeout param;
            double evaluation = (1/bestFitness) * win * timeout * 100; // TODO: Calculate evaluation of the array;
            System.out.println("level:" + (i+1) + "  evaluation:" + evaluation
            		+ "  bestFitness:"+ 1/bestFitness +"  winRate:" + win
            		+ "  timeout:"+ timeout);
        }

    }
    
    public static void addResult(MarioStats result, MarioStats bestResult)
    {
    	if(result.percentageComplete > bestResult.percentageComplete)
    		bestResult.percentageComplete = result.percentageComplete;
    	if(result.coins > bestResult.coins)
    		bestResult.coins = result.coins;
    	if(result.marioState > bestResult.marioState)
    		bestResult.marioState = result.marioState;
    	if(result.mushroomsCollected > bestResult.mushroomsCollected)
    		bestResult.mushroomsCollected = result.mushroomsCollected;
    	if(result.killsTotal > bestResult.killsTotal)
    		bestResult.killsTotal = result.killsTotal;
    	if(result.bricksDestroyed > bestResult.bricksDestroyed)
    		bestResult.bricksDestroyed = result.bricksDestroyed;
    	if(result.numJumps > bestResult.numJumps)
    		bestResult.numJumps = result.numJumps;
    	if(result.maxXjump > bestResult.maxXjump)
    		bestResult.maxXjump = result.maxXjump;
    	if(result.maxJumpAirTime > bestResult.maxJumpAirTime)
    		bestResult.maxJumpAirTime = result.maxJumpAirTime;
    	if(result.numBumpBrick > bestResult.numBumpBrick)
    		bestResult.numBumpBrick = result.numBumpBrick;
    	if(result.numBumpQuestionBlock > bestResult.numBumpQuestionBlock)
    		bestResult.numBumpQuestionBlock = result.numBumpQuestionBlock;
    	if(result.numHurts > bestResult.numHurts)
    		bestResult.numHurts = result.numHurts;
    }
    
    public static double calFitness(MarioStats player, MarioStats bestResult)
    {
    	double fitness = 0;
    	int n = 0;
    	if(bestResult.percentageComplete > 0) {
    		fitness += player.percentageComplete/bestResult.percentageComplete;
    		n+=1;
    	}
    	if(bestResult.coins > 0) {
    		fitness += player.coins / bestResult.coins;
    		n+=1;
    	}
    	if(bestResult.marioState > 0) {
    		fitness += player.marioState / bestResult.marioState;
    		n+=1;
    	}
    	if(bestResult.mushroomsCollected > 0) {
    		fitness += player.mushroomsCollected / bestResult.mushroomsCollected;
    		n+=1;
    	}
    	if(bestResult.killsTotal > 0) {
    		fitness += player.killsTotal / bestResult.killsTotal;
    		n+=1;
    	}
    	if(bestResult.bricksDestroyed > 0) {
    		fitness += player.bricksDestroyed / bestResult.bricksDestroyed;
    		n+=1;
    	}
    	if(bestResult.numJumps > 0) {
    		fitness += player.numJumps / bestResult.numJumps;
    		n+=1;
    	}
    	if(bestResult.maxXjump > 0) {
    		fitness += player.maxXjump / bestResult.maxXjump;
    		n+=1;
    	}
    	if(bestResult.maxJumpAirTime > 0) {
    		fitness += player.maxJumpAirTime / bestResult.maxJumpAirTime;
    		n+=1;
    	}
    	if(bestResult.numBumpBrick > 0) {
    		fitness += player.numBumpBrick / bestResult.numBumpBrick;
    		n+=1;
    	}
    	if(bestResult.numBumpQuestionBlock > 0) {
    		fitness += player.numBumpQuestionBlock / bestResult.numBumpQuestionBlock;
    		n+=1;
    	}
    	if(bestResult.numHurts > 0) {
    		fitness += player.numHurts / bestResult.numHurts;
    		n+=1;
    	}
    	return fitness / n;
    }
}
