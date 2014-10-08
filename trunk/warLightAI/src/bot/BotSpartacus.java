/**
 * Warlight AI Game Bot
 *
 * Last update: April 02, 2014
 *
 * @author Jim van Eeden
 * @version 1.0
 * @License MIT License (http://opensource.org/Licenses/MIT)
 */

package bot;

/**
 * This is a simple bot that does random (but correct) moves.
 * This class implements the Bot interface and overrides its Move methods.
 *  You can implements these methods yourself very easily now,
 * since you can retrieve all information about the match from variable “state”.
 * When the bot decided on the move to make, it returns an ArrayList of Moves. 
 * The bot is started by creating a Parser to which you add
 * a new instance of your bot, and then the parser is started.
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import main.Region;
import main.SuperRegion;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

public class BotSpartacus implements Bot 
{
    
       private Region[] hqRegions = null;
    
       private Region getRegionWithLowestNeighborsAmount(BotState state, ArrayList<Region> except, boolean relocateHQ, SuperRegion continent) {
           Region r = null;
           int max_neighbors = 42;
           
           for (Region reg : state.getPickableStartingRegions()) {
               int neighborsCount = reg.getNeighbors().size();
               if (neighborsCount < max_neighbors &&  // less neibhors
                   !except.contains(r) &&   // not already selected regions
                   (!relocateHQ || reg.ownedByPlayer(state.getMyPlayerName())) &&  // for relocation should be owned by player
                    reg.getSuperRegion() == continent
                   ) {
                   max_neighbors = neighborsCount;
                   r = reg;
               }
           }
           
           return r;
       }
    
	@Override
	/**
	 * A method used at the start of the game to decide which player start with what Regions. 6 Regions are required to be returned.
	 * This example randomly picks 6 regions from the pickable starting Regions given by the engine.
	 * @return : a list of m (m=6) Regions starting with the most preferred Region and ending with the least preferred Region to start with 
	 */
	public ArrayList<Region> getPreferredStartingRegions(BotState state, Long timeOut)
	{
		final int m = 6;
		ArrayList<Region> preferredStartingRegions = new ArrayList<Region>();
                LinkedList<SuperRegion> continents = state.getFullMap().getSuperRegions();
                
		for(int i=0; i<m; i++)
		{
			Region region = getRegionWithLowestNeighborsAmount(state, preferredStartingRegions, false, continents.get(i));
			if(!preferredStartingRegions.contains(region))
				preferredStartingRegions.add(region);
		}
		
		return preferredStartingRegions;
	}

        private ArrayList<SuperRegion> getContinentsUsed(BotState state) {
            String myName = state.getMyPlayerName();
            ArrayList<SuperRegion> continents = new ArrayList<>();
            
            for (Region reg : state.getVisibleMap().getRegions()) {
                if (reg.ownedByPlayer(myName))
                    if (!continents.contains(reg.getSuperRegion()))
                        continents.add(reg.getSuperRegion());
            }
            
            return continents;
        }
        
	private ArrayList<Region> getPreferredRelocatingRegionsForHQ(BotState state, Long timeOut)
	{
		final int m = 3;
		ArrayList<Region> preferredStartingRegions = new ArrayList<Region>();
                ArrayList<SuperRegion> continents = getContinentsUsed(state);
                
		for(int i=0; i<m; i++)
		{
                        SuperRegion continent;
                        try {
                            continent = continents.get(i);
                        } catch (Exception e) {
                            continent = continents.get(0);
                        }
			Region region = getRegionWithLowestNeighborsAmount(state, preferredStartingRegions, true, continent);
			if(!preferredStartingRegions.contains(region))
				preferredStartingRegions.add(region);
		}
		
		return preferredStartingRegions;
	}
        
        private boolean regionHaveTargetInNeibhors(BotState state, Region reg) {
            String myName = state.getMyPlayerName();
            if(!reg.ownedByPlayer(myName))
                return false;
             
            for (Region r : reg.getNeighbors()) {
                if (!r.ownedByPlayer(myName))
                    return true;
            }
            
            return false;
        }
        
        private int numberOfEnemyCountriesInSuperregion(BotState state, SuperRegion continent) {
            String myName = state.getMyPlayerName();
            int ret = 0;
            
            for (Region r : state.getFullMap().getRegions()) {
                if (!r.ownedByPlayer(myName) && r.getSuperRegion() == continent)
                    ret++;
            }
            
            return ret;
        }

        private int numberOfEnemyArmiesInSuperregion(BotState state, SuperRegion continent) {
            String myName = state.getMyPlayerName();
            int ret = 0;
            
            for (Region r : state.getFullMap().getRegions()) {
                if (!r.ownedByPlayer(myName) && r.getSuperRegion() == continent)
                    ret += r.getArmies();
            }
            
            return ret;
        }        

        private SuperRegion getContinentWithSmalestAmountOfEnemyArmies(BotState state) {
            String myName = state.getMyPlayerName();
            ArrayList<SuperRegion> myContinents = getContinentsUsed(state);
            SuperRegion continentFound = null;
            int enemyArmiesCount;
            int enemyArmiesCountMin = Integer.MAX_VALUE;

            for (SuperRegion myContinent : myContinents) {
                enemyArmiesCount = numberOfEnemyArmiesInSuperregion(state, myContinent);
                if (enemyArmiesCount < enemyArmiesCountMin && enemyArmiesCount > 0) {
                    enemyArmiesCountMin = enemyArmiesCount;
                    continentFound = myContinent;
                }
            }
            
            return continentFound;
        }        
        
        private SuperRegion getContinentWithSmalestAmountOfEnemyCountries(BotState state) {
            String myName = state.getMyPlayerName();
            ArrayList<SuperRegion> myContinents = getContinentsUsed(state);
            SuperRegion continentFound = null;
            int enemyCountriesCount;
            int enemyCountriesCountMin = Integer.MAX_VALUE;

            for (SuperRegion myContinent : myContinents) {
                enemyCountriesCount = numberOfEnemyCountriesInSuperregion(state, myContinent);
                if (enemyCountriesCount < enemyCountriesCountMin && enemyCountriesCount > 0) {
                    enemyCountriesCountMin = enemyCountriesCount;
                    continentFound = myContinent;
                }
            }
            
            return continentFound;
        }
        
        private int minimumArmiesInNeighbors(BotState state, Region reg, SuperRegion continent) {
            String myName = state.getMyPlayerName();                      
            int armiesMinimum = Integer.MAX_VALUE;
            
            for (Region r : reg.getNeighbors()) {
                if (!r.ownedByPlayer(myName) && (r.getSuperRegion() == continent || continent == null) && r.getArmies() < armiesMinimum && r.getArmies() > 0) {
                    armiesMinimum = r.getArmies();
                }
            }
            
            return armiesMinimum;
        }
        
        private Region firstRegionWithLowestAmountOfArmiesInNeighbors(BotState state, SuperRegion continent) {
            String myName = state.getMyPlayerName();           
            int enemyArmies;
            int enemyArmiesMin = Integer.MAX_VALUE;
            Region ret = null;
            
            for (Region r : state.getVisibleMap().getRegions()) {
                if(r.ownedByPlayer(myName)) {
                    enemyArmies = minimumArmiesInNeighbors(state, r, continent);
                    if (enemyArmies < enemyArmiesMin && enemyArmies > 0 && (continent == null || continent == r.getSuperRegion())) {
                        enemyArmiesMin = enemyArmies;
                        ret = r;
                    }
                }
            }
            
            return ret;
        }
        
        private Region firstAlliedRegionWithTarget(BotState state) {
            SuperRegion targetContinent = getContinentWithSmalestAmountOfEnemyArmies(state);
            Region placeArmiesRegion = firstRegionWithLowestAmountOfArmiesInNeighbors(state, targetContinent);        
            
            return placeArmiesRegion;
        }
        
	@Override
	/**
	 * This method is called for at first part of each round. This example puts two armies on random regions
	 * until he has no more armies left to place.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) 
	{             
                
                ArrayList<PlaceArmiesMove> placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
                String myName = state.getMyPlayerName();
                int armiesLeft = state.getStartingArmies();
                
                // place armies
                Region toBase = firstAlliedRegionWithTarget(state);
                if (toBase != null)
                    placeArmiesMoves.add(new PlaceArmiesMove(myName, toBase, armiesLeft));                

		return placeArmiesMoves;
	}

        private Region getNextCandidateInDeph(Region startingRegion, ArrayList<Region> searchPath, int maxDeph, int startIx) {
            Region nextRegTry = null;
            
            if (searchPath.size() == 0)
                return null;
            
            if (searchPath.size() >= maxDeph)
                return null;
            
            Region fromReg = searchPath.get(searchPath.size() - 1);
            
            while (true) {
                try {
                    nextRegTry = fromReg.getNeighbors().get(startIx);
                } catch (IndexOutOfBoundsException e) {
                    break;
                }
                if (nextRegTry != null && !searchPath.contains(nextRegTry) && nextRegTry != startingRegion)
                    return nextRegTry;
                else
                    startIx++;
            }
            
            return null;
        }
        
        private Region getNextCandidateInSameLevel(Region startingRegion, ArrayList<Region> searchPath) {
            Region nextRegTry = null;
            
            if (searchPath.size() == 0)
                return null;
            
            Region fromReg = (searchPath.size() == 1)? startingRegion : searchPath.get(searchPath.size() - 2);
            Region toReg = searchPath.get(searchPath.size() - 1);
            int startIx = fromReg.getNeighbors().indexOf(toReg) + 1;
            
            while (true) {
                try {
                    nextRegTry = fromReg.getNeighbors().get(startIx);
                } catch (IndexOutOfBoundsException e) {
                    break;
                }
                if (nextRegTry != null && !searchPath.contains(nextRegTry))
                    return nextRegTry;
                else
                    startIx++;
            }
            
            return null;
        }        
        
        private ArrayList<Region> getDirectionToTarget(BotState state, Region startingRegion, boolean neutralsFirst, boolean onlyInSameContinent) {            
            return getDirectionToTargetWithDeph(state, startingRegion, neutralsFirst, 10, onlyInSameContinent); // searchPath;
        }
        
        private ArrayList<Region> getDirectionToTargetWithDeph(BotState state, Region startingRegion, boolean neutralsFirst, int deph, boolean onlyInSameContinent) {
            String myPlayer = state.getMyPlayerName();
            String opponentPlayer = state.getOpponentPlayerName();
            ArrayList<Region> searchPath = new ArrayList<>();
            ArrayList<Region> bestPath = new ArrayList<>();            
            boolean targetFound = false;
            int dephStartIx = 0;
            
            // direct search for nearest enemy with lowest amount of armies
            Region regNearestEnemy = null;
            int enemyArmies = Integer.MAX_VALUE;

            for (Region r : startingRegion.getNeighbors()) {
                targetFound = !r.ownedByPlayer(myPlayer) && 
                             (!onlyInSameContinent || r.getSuperRegion() == startingRegion.getSuperRegion());                  
                if (targetFound) {
                    if (r.getArmies() < enemyArmies) {
                        enemyArmies = r.getArmies();
                        regNearestEnemy = r;
                    }
                }
            }            
            if (regNearestEnemy != null) {
                searchPath.add(regNearestEnemy);
                return searchPath;
            }
            
            // perform backtracking algorithm for target search
            targetFound = false;
            searchPath.add(startingRegion.getNeighbors().get(0));
            if (searchPath.isEmpty())
                return null;
            
            while (!searchPath.isEmpty()) {
                Region lastRegionInList = searchPath.get(searchPath.size() - 1);
                targetFound = ( 
                              // are we searching for neutrals or enemies ?
                              (neutralsFirst && !lastRegionInList.ownedByPlayer(myPlayer) && !lastRegionInList.ownedByPlayer(opponentPlayer)) || 
                             (!neutralsFirst && lastRegionInList.ownedByPlayer(opponentPlayer))
                              ) &&   
                              // are we searching in same continent ?
                             (!onlyInSameContinent || lastRegionInList.getSuperRegion() == startingRegion.getSuperRegion());  
                
                if (targetFound) {
                    if (searchPath.size() < bestPath.size() || bestPath.isEmpty()) {
                        bestPath = new ArrayList<>(searchPath);
                    }
                }
                
                // search in deph
                Region regNext = getNextCandidateInDeph(startingRegion, searchPath, deph, dephStartIx);
                if (regNext != null) {
                    searchPath.add(regNext);
                }
                // search in same level
                else {
                    regNext = getNextCandidateInSameLevel(startingRegion, searchPath);
                    if (regNext != null) {
                        dephStartIx = 0;
                        searchPath.remove(searchPath.size() - 1);
                        searchPath.add(regNext);
                    }
                }
                if (regNext == null) {
                    Region fromReg = (searchPath.size() == 1)? startingRegion : searchPath.get(searchPath.size() - 2);                    
                    dephStartIx = fromReg.getNeighbors().indexOf(lastRegionInList) + 1;                    
                    searchPath.remove(searchPath.size() - 1);
                }
                
            }
            
            return bestPath;
        }
        
        private ArrayList<Region> getNewAttackPathWithStrategy(BotState state, Region fromRegion) {
            
            ArrayList<Region> regList = new ArrayList<>();
            
            if (!BotState.continentBelongsToPlayer(state, fromRegion.getSuperRegion(), state.getMyPlayerName()))
                regList = getNewAttackPath(state, fromRegion, true); // first we are looking in same continent for targets
            
            if (regList.isEmpty()) 
                regList = getNewAttackPath(state, fromRegion, false); // trying to find targets in any continent            

            return regList;
        }        
        
        private ArrayList<Region> getNewAttackPath(BotState state, Region fromRegion, boolean onlyInSameContinent) {
            
            ArrayList<Region> regList = getDirectionToTarget(state, fromRegion, true, onlyInSameContinent); // neutrals first
            if (regList.isEmpty()) 
                regList = getDirectionToTarget(state, fromRegion, false, onlyInSameContinent); // enemies second            

            return regList;
        }

        private void clearAllAttackPathsForMyRegions(BotState state) {
            String myName = state.getMyPlayerName();
            
            for (Region region : state.getVisibleMap().getRegions()) {
                if (region.ownedByPlayer(myName)) {
                    region.lastAttackPath = null;
                }
            }
        }
        
        private Region getRegionFromExistingAttackPath(BotState state, Region fromRegion) {
            String myName = state.getMyPlayerName();
            
            for (Region reg : state.getVisibleMap().getRegions()) {
                if (reg != fromRegion && reg.ownedByPlayer(myName) && reg.lastAttackPath != null && !reg.lastAttackPath.isEmpty()) {
                    for (int i = 0; i < reg.lastAttackPath.size() - 1; i++) {
                        if (reg.lastAttackPath.get(i) == fromRegion) {
                            return reg.lastAttackPath.get(i+1);
                        }
                    }
                }
            }
            
            return null;
        }
        
        private Region getTransferTarget(BotState state, Region fromRegion) {
            
            Region regTransfer = getRegionFromExistingAttackPath(state, fromRegion);
            if (regTransfer == null) {
               ArrayList<Region> attackPath = getNewAttackPathWithStrategy(state, fromRegion);     
               if (!attackPath.isEmpty()) {
                   regTransfer = attackPath.get(0);
                   fromRegion.lastAttackPath = attackPath;
               }
            }
           
            return regTransfer;
        }
        
        private int getAttackThreshold(BotState state, Region fromRegion) {
            return (BotState.continentBelongsToPlayer(state, fromRegion.getSuperRegion(), state.getMyPlayerName()))? 4 : 2;
        }
        
	@Override
	/**
	 * This method is called for at the second part of each round. This example attacks if a region has
	 * more than 6 armies on it, and transfers if it has less than 6 and a neighboring owned region.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) 
	{
		ArrayList<AttackTransferMove> attackTransferMoves = new ArrayList<AttackTransferMove>();
		String myName = state.getMyPlayerName();
		int armiesToPlace;
		
                clearAllAttackPathsForMyRegions(state);
		for(Region fromRegion : state.getVisibleMap().getRegions())
		{
			if(fromRegion.ownedByPlayer(myName))
			{
                                armiesToPlace = fromRegion.getArmies() - 1;
                                if (armiesToPlace < getAttackThreshold(state, fromRegion))
                                    continue;

                                Region regTransfer = getTransferTarget(state, fromRegion);
                                if (regTransfer != null)
                                    attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, regTransfer, armiesToPlace));
                        }
		}
		
		return attackTransferMoves;
	}

	public static void main(String[] args)
	{
		BotParser parser = new BotParser(new BotSpartacus());
		parser.run();
	}

}
