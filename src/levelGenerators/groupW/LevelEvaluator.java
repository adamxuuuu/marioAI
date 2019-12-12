package levelGenerators.groupW;

import agents.MarioAgent;
import engine.core.MarioGame;
import engine.core.MarioResult;
import engine.helper.MarioStats;

import static engine.helper.RunUtils.resultToStats;

public class LevelEvaluator {

    public static void evaluate(MarioAgent[] agents, String[] level, int repsPerLevel, int agentTimer) {
        int noLevels = level.length;
        MarioStats average;
        MarioStats bestResult = new MarioStats();
        MarioGame game = new MarioGame();
        for (int i = 0; i < noLevels; i++) {
            int players = agents.length;
            MarioStats[] playerStats = new MarioStats[players];
            float[] timeOut = new float[players];
            for (int p = 0; p < players; p++) {
                // Run the level several times
                average = new MarioStats();
                int timeOutTime = 0;
                for (int j = 0; j < repsPerLevel; j++) {
                    MarioResult result = game.runGame(agents[p], level[i], agentTimer, 0, false);

                    String gameStatus = result.getGameStatus().toString();
                    System.out.println((p + 1) + "p:" + (i + 1) + "/" + noLevels + ";" + (j + 1) + "/" + repsPerLevel + ": "
                            + gameStatus);
                    MarioStats stats = resultToStats(result);
                    average = average.merge(stats);
                    if (gameStatus.equals("TIME_OUT")) {
                        timeOutTime += 1;
                    }
                }
                if (average.percentageComplete > bestResult.percentageComplete)
                    bestResult.percentageComplete = average.percentageComplete;
                if (average.coins > bestResult.coins)
                    bestResult.coins = average.coins;
                if (average.marioState > bestResult.marioState)
                    bestResult.marioState = average.marioState;
                if (average.mushroomsCollected > bestResult.mushroomsCollected)
                    bestResult.mushroomsCollected = average.mushroomsCollected;
                if (average.killsTotal > bestResult.killsTotal)
                    bestResult.killsTotal = average.killsTotal;
                if (average.bricksDestroyed > bestResult.bricksDestroyed)
                    bestResult.bricksDestroyed = average.bricksDestroyed;
                if (average.numJumps > bestResult.numJumps)
                    bestResult.numJumps = average.numJumps;
                if (average.maxXjump > bestResult.maxXjump)
                    bestResult.maxXjump = average.maxXjump;
                if (average.maxJumpAirTime > bestResult.maxJumpAirTime)
                    bestResult.maxJumpAirTime = average.maxJumpAirTime;
                if (average.numBumpBrick > bestResult.numBumpBrick)
                    bestResult.numBumpBrick = average.numBumpBrick;
                if (average.numBumpQuestionBlock > bestResult.numBumpQuestionBlock)
                    bestResult.numBumpQuestionBlock = average.numBumpQuestionBlock;
                if (average.numHurts > bestResult.numHurts)
                    bestResult.numHurts = average.numHurts;
                playerStats[p] = average;
                timeOut[p] = timeOutTime / repsPerLevel;
                System.out.println("------" + (p + 1) + "p------");
                System.out.println(average.toString());
            }
            double bestFitness = 0;
            double winRateSum = 0;
            double timeOutSum = 0;
            for (int p = 0; p < players; p++) {
                double fitness = 0;
                int n = 0;
                if (bestResult.percentageComplete > 0) {
                    fitness += playerStats[p].percentageComplete / bestResult.percentageComplete;
                    n += 1;
                }
                if (bestResult.coins > 0) {
                    fitness += playerStats[p].coins / bestResult.coins;
                    n += 1;
                }
                if (bestResult.marioState > 0) {
                    fitness += playerStats[p].marioState / bestResult.marioState;
                    n += 1;
                }
                if (bestResult.mushroomsCollected > 0) {
                    fitness += playerStats[p].mushroomsCollected / bestResult.mushroomsCollected;
                    n += 1;
                }
                if (bestResult.killsTotal > 0) {
                    fitness += playerStats[p].killsTotal / bestResult.killsTotal;
                    n += 1;
                }
                if (bestResult.bricksDestroyed > 0) {
                    fitness += playerStats[p].bricksDestroyed / bestResult.bricksDestroyed;
                    n += 1;
                }
                if (bestResult.numJumps > 0) {
                    fitness += playerStats[p].numJumps / bestResult.numJumps;
                    n += 1;
                }
                if (bestResult.maxXjump > 0) {
                    fitness += playerStats[p].maxXjump / bestResult.maxXjump;
                    n += 1;
                }
                if (bestResult.maxJumpAirTime > 0) {
                    fitness += playerStats[p].maxJumpAirTime / bestResult.maxJumpAirTime;
                    n += 1;
                }
                if (bestResult.numBumpBrick > 0) {
                    fitness += playerStats[p].numBumpBrick / bestResult.numBumpBrick;
                    n += 1;
                }
                if (bestResult.numBumpQuestionBlock > 0) {
                    fitness += playerStats[p].numBumpQuestionBlock / bestResult.numBumpQuestionBlock;
                    n += 1;
                }
                if (bestResult.numHurts > 0) {
                    fitness += playerStats[p].numHurts / bestResult.numHurts;
                    n += 1;
                }
                fitness /= n;
                if (fitness > bestFitness) {
                    bestFitness = fitness;
                }
                winRateSum += playerStats[p].winRate;
                timeOutSum += timeOut[p];
            }

            double win = winRateSum / players;            // TODO: Calculate win param;
            double timeout = Math.exp(-timeOutSum / players);  // TODO: Calculate timeout param;
            double evaluation = (1 / bestFitness) * win * timeout * 100; // TODO: Calculate evaluation of the array;
            System.out.println("level:" + (i + 1) + "  evaluation:" + evaluation
                    + "  bestFitness:" + 1 / bestFitness + "  winRate:" + win
                    + "  timeout:" + timeout);
        }
    }

}
