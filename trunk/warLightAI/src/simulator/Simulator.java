/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

import bot.*;
import java.util.*;
import main.*;

/**
 *
 * @author agnius
 */
public class Simulator {
    final long TIMEOUT = 2000;
    private BotParser parser;
    private BotState state;
    public Bot myBot;
    public Bot opponentBot;
    final String initSettings = "settings your_bot player1\n" +
                                "\n" +
                                "settings opponent_bot player2\n" +
                                "\n" +
                                "setup_map super_regions 1 5 2 2 3 5 4 3 5 7 6 2\n" +
                                "\n" +
                                "setup_map regions 1 1 2 1 3 1 4 1 5 1 6 1 7 1 8 1 9 1 10 2 11 2 12 2 13 2 14 3 15 3 16 3 17 3 18 3 19 3 20 3 21 4 22 4 23 4 24 4 25 4 26 4 27 5 28 5 29 5 30 5 31 5 32 5 33 5 34 5 35 5 36 5 37 5 38 5 39 6 40 6 41 6 42 6\n" +
                                "\n" +
                                "setup_map neighbors 1 2,4,30 2 4,3,5 3 5,6,14 4 5,7 5 6,7,8 6 8 7 8,9 8 9 9 10 10 11,12 11 12,13 12 13,21 14 15,16 15 16,18,19 16 17 17 19,20,27,32,36 18 19,20,21 19 20 20 21,22,36 21 22,23,24 22 23,36 23 24,25,26,36 24 25 25 26 27 28,32,33 28 29,31,33,34 29 30,31 30 31,34,35 31 34 32 33,36,37 33 34,37,38 34 35 36 37 37 38 38 39 39 40,41 40 41,42 41 42\n" +
                                "\n" +
                                "pick_starting_regions 10000 6 4 12 13 19 16 22 23 34 32 41 39"
                                ;
    
    public Simulator() {
        myBot = new BotSpartacus();
        opponentBot = new BotSpartacus();
        parser = new BotParser(myBot, initSettings);
        parser.run();
        state = parser.currentState;
    }
    
    private boolean continentRegionsHaveNeighbors(SuperRegion continent) {
        for (Region r : state.getFullMap().getRegions()) {
            if (r.getSuperRegion() == continent && r.getNeighbors().size() > 0)
                return true;
        }
        return false;
    }

    private String checkMapSetupForTest() {
        if (state.getFullMap().superRegions.size() != 6)
            return "Super region count is not 6 !";
        
        if (state.getFullMap().regions.size() != 42)
            return "Region count is not 42 !";
        
        for (SuperRegion continent : state.getFullMap().getSuperRegions()) {
            if (!continentRegionsHaveNeighbors(continent))
                return String.format("Continent %d regions doesn't have neighbors !", continent.getId());
        }
        
        return "";
    }
    
    private boolean listHasDuplicates(List<?> col) {
        for (Object o : col) {
            if (col.indexOf(o) != col.lastIndexOf(o))
                return true;
        }
        return false;
    }
    
    private ArrayList<Region> chooseThreeStartingRegions(ArrayList<Region> regions, boolean isFirstBot ) {
        ArrayList<Region> selectedReg = new ArrayList<>();
        int startIx = (isFirstBot)? 0 : 1;
                
        for (int i = startIx ; selectedReg.size() != 3 ; i+=2) 
            selectedReg.add(regions.get(i));
        
        return selectedReg;
    } 
    
    private String setInitialRegionsForPlayers() {
        ArrayList<Region> regions1 = myBot.getPreferredStartingRegions(state, TIMEOUT);
        ArrayList<Region> regions2 = opponentBot.getPreferredStartingRegions(state, TIMEOUT);        
        ArrayList<Region> regionsSelected1 = chooseThreeStartingRegions(regions1, true);
        ArrayList<Region> regionsSelected2 = chooseThreeStartingRegions(regions2, false);
        
        if (regions1.size() != 6 || regions2.size() != 6)
            return "Bots have chosen less then 6 regions";
        
        if (listHasDuplicates(regions1) || listHasDuplicates(regions2))
            return "Bots have duplicates in their chosen starting regions";
        
        if (regionsSelected1.size() != 3 || regionsSelected2.size() != 3)
            return "Not 3 distinct regions for bot is chosen !";
        
        // assign 3 regions to each player
        for (int i = 0; i < 3; i++) {
            regionsSelected1.get(i).setPlayerName(state.getMyPlayerName());
            regionsSelected2.get(i).setPlayerName(state.getOpponentPlayerName());
            
            regionsSelected1.get(i).setArmies(2);
            regionsSelected2.get(i).setArmies(2);
        }
        
        // set rest regions to neutral
        for (Region reg : state.getFullMap().getRegions()) {
            if (reg.ownedByPlayer("unknown")) {
                reg.setPlayerName("neutral");
                reg.setArmies(2);
            }
        }
        
        return "";
    }
    
    private int getArmiesPerTurnAmount(String playerName) {
        int total = 5;
        
        for (SuperRegion continent : state.getFullMap().getSuperRegions()) {
            if (BotState.continentBelongsToPlayer(state, continent, playerName))
                total += continent.getArmiesReward();
        }
        
        return total;
    }
    
    private String simulateTestGame() {
        final int TOTAL_ROUNDS = 100;
        final int NUMBER_OF_PLAYERS = 2;
        final String[] playerNames = {"player1", "player2"};
        int armiesPerRound;
        
        for (int i = 0; i < TOTAL_ROUNDS; i++) {
            
            for (int j = 0; j < NUMBER_OF_PLAYERS; j++) {

                // update fog of war
                state.updateVisibleMapForUnitTest(playerNames[j]);
                
                // calculate and set armies per turn amount
                armiesPerRound = getArmiesPerTurnAmount(playerNames[j]);
                if (armiesPerRound < 5)
                    return String.format("Player %s has less armies than 5 in round %d", playerNames[j], i+1);
                else
                    state.setStartingArmies(armiesPerRound);

            }
            
        }
        
        return "";
    }
    
    public String getSimulatedGameFailMessage() {
        String error = "";
        
        if ((error = checkMapSetupForTest()).length() != 0 )
            return error;
        
        if ((error = setInitialRegionsForPlayers()).length() != 0)
            return error;

        if ((error = simulateTestGame()).length() != 0)
            return error;        
        
        return error;
    }
    
}
