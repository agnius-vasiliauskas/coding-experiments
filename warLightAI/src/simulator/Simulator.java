/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

import bot.*;
import main.*;

/**
 *
 * @author agnius
 */
public class Simulator {
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
                                "setup_map neighbors 1 2,4,30 2 4,3,5 3 5,6,14 4 5,7 5 6,7,8 6 8 7 8,9 8 9 9 10 10 11,12 11 12,13 12 13,21 14 15,16 15 16,18,19 16 17 17 19,20,27,32,36 18 19,20,21 19 20 20 21,22,36 21 22,23,24 22 23,36 23 24,25,26,36 24 25 25 26 27 28,32,33 28 29,31,33,34 29 30,31 30 31,34,35 31 34 32 33,36,37 33 34,37,38 34 35 36 37 37 38 38 39 39 40,41 40 41,42 41 42";
    
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
    
    public String getSimulatedGameFailMessage() {
        String error = "";
        
        if ((error = checkMapSetupForTest()).length() != 0 )
            return error;
        
        return error;
    }
    
}
