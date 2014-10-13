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
       public final int botVersion = 39;
    
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
        public int getBotVersion() {
            return botVersion;
        }
       
	@Override
	/**
	 * A method used at the start of the game to decide which player start with what Regions. 6 Regions are required to be returned.
	 * This example randomly picks 6 regions from the pickable starting Regions given by the engine.
	 * @return : a list of m (m=6) Regions starting with the most preferred Region and ending with the least preferred Region to start with 
	 */
	public ArrayList<Region> getPreferredStartingRegions(BotState state, Long timeOut)
	{
		final int REGION_AMOUNT = 6;
		ArrayList<Region> preferredStartingRegions = new ArrayList<Region>();
                SuperRegion continent = getContinentWithSmalestAmountOfEnemyCountries(state, REGION_AMOUNT, false);

                Region region = getRegionWithLowestNeighborsAmount(state, preferredStartingRegions, false, continent);
                preferredStartingRegions.add(region);
                
                int sIx = 0;
                while (preferredStartingRegions.size() != 6) {
                    for (Region reg : preferredStartingRegions.get(sIx).getNeighbors()) {
                        if (reg.getSuperRegion() == continent && !preferredStartingRegions.contains(reg))
                            preferredStartingRegions.add(reg);
                    }
                    sIx++;
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
        
        private SuperRegion getContinentWithSmalestAmountOfEnemyCountries(BotState state, int lowerBound, boolean onlyContinentsWithMyRegionsInside) {
            String myName = state.getMyPlayerName();
            ArrayList<SuperRegion> myContinents = (onlyContinentsWithMyRegionsInside)? getContinentsUsed(state) : new ArrayList<>(state.getFullMap().getSuperRegions());
            SuperRegion continentFound = null;
            int enemyCountriesCount;
            int enemyCountriesCountMin = Integer.MAX_VALUE;

            for (SuperRegion myContinent : myContinents) {
                enemyCountriesCount = numberOfEnemyCountriesInSuperregion(state, myContinent);
                if (enemyCountriesCount < enemyCountriesCountMin && enemyCountriesCount >= lowerBound) {
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
            SuperRegion targetContinent = getContinentWithSmalestAmountOfEnemyCountries(state, 1, true);
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
        
        private ArrayList<Region> getDirectionToTarget(BotState state, Region startingRegion, boolean neutralsFirst, boolean onlyInSameContinent, boolean firstFrontLine) {            
            return getDirectionToTargetWithDeph(state, startingRegion, neutralsFirst, 10, onlyInSameContinent, firstFrontLine); // searchPath;
        }
        
        private ArrayList<Region> getDirectionToTargetWithDeph(BotState state, Region startingRegion, boolean neutralsFirst, int deph, boolean onlyInSameContinent, boolean firstFrontLine) {
            String myPlayer = state.getMyPlayerName();
            String opponentPlayer = state.getOpponentPlayerName();
            ArrayList<Region> searchPath = new ArrayList<>();
            ArrayList<Region> bestPath = new ArrayList<>();            
            boolean targetFound = false;
            int dephStartIx = 0;
            Region targetFromFirstFrontLine = null;
            
            if (!firstFrontLine) {
                if (startingRegion.lastAttackPath1 == null || startingRegion.lastAttackPath1.isEmpty())
                    return bestPath;
                else
                    targetFromFirstFrontLine = startingRegion.lastAttackPath1.get(startingRegion.lastAttackPath1.size() - 1);
            }
            
            // direct search for nearest enemy with lowest amount of armies
            Region regNearestEnemy = null;
            int enemyArmies = Integer.MAX_VALUE;

            for (Region r : startingRegion.getNeighbors()) {
                targetFound = !r.ownedByPlayer(myPlayer) && 
                             (!onlyInSameContinent || r.getSuperRegion() == startingRegion.getSuperRegion()) &&
                             (firstFrontLine || r != targetFromFirstFrontLine);   
                
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
                             (!onlyInSameContinent || lastRegionInList.getSuperRegion() == startingRegion.getSuperRegion()) &&
                              // are we starting second front line or using first one
                             (firstFrontLine || lastRegionInList != targetFromFirstFrontLine);  
                
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
        
        private ArrayList<Region> getNewAttackPathWithStrategy(BotState state, Region fromRegion, boolean firstFrontLine) {
            
            ArrayList<Region> regList = new ArrayList<>();
            
            if (!BotState.continentBelongsToPlayer(state, fromRegion.getSuperRegion(), state.getMyPlayerName()))
                regList = getNewAttackPath(state, fromRegion, true, firstFrontLine); // first we are looking in same continent for targets
            
            if (regList.isEmpty()) 
                regList = getNewAttackPath(state, fromRegion, false, firstFrontLine); // trying to find targets in any continent            

            return regList;
        }        
        
        private ArrayList<Region> getNewAttackPath(BotState state, Region fromRegion, boolean onlyInSameContinent, boolean firstFrontLine) {
            
            ArrayList<Region> regList = getDirectionToTarget(state, fromRegion, true, onlyInSameContinent, firstFrontLine); // neutrals first
            if (regList.isEmpty()) 
                regList = getDirectionToTarget(state, fromRegion, false, onlyInSameContinent, firstFrontLine); // enemies second            

            return regList;
        }

        private void clearAllAttackPathsForMyRegions(BotState state) {
            String myName = state.getMyPlayerName();
            
            for (Region region : state.getVisibleMap().getRegions()) {
                if (region.ownedByPlayer(myName)) {
                    region.lastAttackPath1 = null;
                    region.lastAttackPath2 = null;                    
                }
            }
        }
        
        private Region getRegionFromExistingAttackPath(BotState state, Region fromRegion, boolean firstFrontLine) {
            String myName = state.getMyPlayerName();
            
            for (Region reg : state.getVisibleMap().getRegions()) {
//                ArrayList<Region> attackPath = (firstFrontLine)? reg.lastAttackPath1 : reg.lastAttackPath2;
                if (reg != fromRegion && reg.ownedByPlayer(myName) 
//                        && attackPath != null && !attackPath.isEmpty()
                    ) {
//                    for (int i = 0; i < attackPath.size() - 1; i++) {
//                        if (attackPath.get(i) == fromRegion) {
//                            return attackPath.get(i+1);
//                        }
//                    }
                    for (int i = 0; reg.lastAttackPath1 != null && i < reg.lastAttackPath1.size() - 1; i++) {
                        if (reg.lastAttackPath1.get(i) == fromRegion) {
                            return reg.lastAttackPath1.get(i+1);
                        }
                    }
                    for (int i = 0; reg.lastAttackPath2 != null && i < reg.lastAttackPath2.size() - 1; i++) {
                        if (reg.lastAttackPath2.get(i) == fromRegion) {
                            return reg.lastAttackPath2.get(i+1);
                        }
                    }
                }
            }
            
            return null;
        }
        
        private Region getTransferTarget(BotState state, Region fromRegion, boolean firstFrontLine) {
            
            Region regTransfer = getRegionFromExistingAttackPath(state, fromRegion, firstFrontLine);
            if (regTransfer == null) {
               ArrayList<Region> attackPath = getNewAttackPathWithStrategy(state, fromRegion, firstFrontLine);     
               if (!attackPath.isEmpty()) {
                   regTransfer = attackPath.get(0);
                   if (firstFrontLine)
                       fromRegion.lastAttackPath1 = attackPath;
                   else
                       fromRegion.lastAttackPath2 = attackPath;
               }
            }
           
            return regTransfer;
        }
        
        private int getAttackThreshold(BotState state, Region fromRegion, boolean forOneFrontline) {
//            if (forOneFrontline)
//            return (BotState.continentBelongsToPlayer(state, fromRegion.getSuperRegion(), state.getMyPlayerName()))? 4 : 2;
//            else
            return 6;
        }

        private void openFrontLine(BotState state , Region fromRegion, ArrayList<AttackTransferMove> attackTransferMoves, int armiesToPlace, boolean firstFrontLine) {
            String myName = state.getMyPlayerName();
            Region regTransfer = getTransferTarget(state, fromRegion, firstFrontLine);
            if (regTransfer != null)
                attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, regTransfer, armiesToPlace));
        }

        private Region getMyRegionWithBiggestArmy(BotState state, ArrayList<Region> exceptions) {
            String myName = state.getMyPlayerName();   
            int armies = 0;
            Region reg = null;
            
            for (Region r : state.getVisibleMap().getRegions()) {
                if (r.ownedByPlayer(myName) && r.getArmies() > armies && !exceptions.contains(r)) {
                    armies = r.getArmies();
                    reg = r;
                }
            }
            
            return reg;
        }
        
        private ArrayList<Region> getMyRegionsWithGreatestArmies(BotState state, int maxNumOfRegions) {
            ArrayList<Region> regionsFound = new ArrayList<>();
            
            for (int i = 0; i < maxNumOfRegions; i++) {
                Region reg = getMyRegionWithBiggestArmy(state, regionsFound);
                if (reg == null)
                    break;
                else
                    regionsFound.add(reg);
            }
            
            return regionsFound;
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
		int armiesToPlaceTotal;
		int armiesToPlaceFrontLine;                
                boolean isContinentMine;
                long time = System.currentTimeMillis();
                final int TIME_FACTOR = 3;
                final int MAX_FIGHTING_COUNTRIES = 10;
                
                clearAllAttackPathsForMyRegions(state);
                ArrayList<Region> myBestRegions = getMyRegionsWithGreatestArmies(state, MAX_FIGHTING_COUNTRIES);

                for(Region fromRegion : myBestRegions)
		{
                    if (System.currentTimeMillis() - time > timeOut/TIME_FACTOR)
                        break;
                    //isContinentMine = BotState.continentBelongsToPlayer(state, fromRegion.getSuperRegion(), myName);
                    armiesToPlaceTotal = fromRegion.getArmies() - 1;
                    armiesToPlaceFrontLine = armiesToPlaceTotal / 2;

                    if (armiesToPlaceTotal >= getAttackThreshold(state, fromRegion, true)) {
                        // open two front lines
                        if (armiesToPlaceFrontLine >= getAttackThreshold(state, fromRegion, false)) {
                            openFrontLine(state, fromRegion, attackTransferMoves, armiesToPlaceFrontLine, true); 
                            if (System.currentTimeMillis() - time > timeOut/TIME_FACTOR)
                                break;
                            openFrontLine(state, fromRegion, attackTransferMoves, armiesToPlaceFrontLine, false);
                        }
                        // open just one front line
                        else {
                            openFrontLine(state, fromRegion, attackTransferMoves, armiesToPlaceTotal, true);                                        
                        }
                    }
                                
		}
                
//                System.out.printf("Paths search time %d ms \n", System.currentTimeMillis() - time);
		
		return attackTransferMoves;
	}

	public static void main(String[] args)
	{
		BotParser parser = new BotParser(new BotSpartacus());
		parser.run();
	}

}
