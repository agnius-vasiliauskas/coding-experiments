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

import java.util.ArrayList;
import java.util.LinkedList;

import main.Map;
import main.Region;
import main.SuperRegion;

import move.AttackTransferMove;
import move.PlaceArmiesMove;
import move.Move;

public class BotState {
	
	private String myName = "";
	private String opponentName = "";
	
	private final Map fullMap = new Map(); //This map is known from the start, contains all the regions and how they are connected, doesn't change after initialization
	private Map visibleMap; //This map represents everything the player can see, updated at the end of each round.
	
	private ArrayList<Region> pickableStartingRegions; //2 randomly chosen regions from each superregion are given, which the bot can chose to start with
	
	private ArrayList<Move> opponentMoves; //list of all the opponent's moves, reset at the end of each round

	private int startingArmies; //number of armies the player can place on map
	
	private int roundNumber;
        
        public int timesCountryCaptureForMyBot;
        public int timesCountryCaptureForOpponentBot;
        
	public BotState()
	{
		pickableStartingRegions = new ArrayList<Region>();
		opponentMoves = new ArrayList<Move>();
		roundNumber = 0;
                timesCountryCaptureForMyBot = 0;
                timesCountryCaptureForOpponentBot = 0;
	}

        public static void checkTimings(LinkedList<Long> times, Long timeOut) {
            long total = 0;
            String durations = "";

            for (int i = 1; i < times.size(); i++) {
                total += (times.get(i) - times.get(i-1))/1000000; // duration in ms
            }
            
            durations += "Timings of: " + Thread.currentThread().getStackTrace()[2].getMethodName() + ": ";
            for (int i = 1; i < times.size(); i++) {
                durations += "," + String.valueOf((times.get(i) - times.get(i-1))/1000000); 
            }                
            durations += " total - " + String.valueOf(total) + " ms.";
            System.out.println(durations);

        }        
        
	public void updateSettings(String key, String value)
	{
		if(key.equals("your_bot")) //bot's own name
			myName = value;
		else if(key.equals("opponent_bot")) //opponent's name
			opponentName = value;
		else if(key.equals("starting_armies")) 
		{
			startingArmies = Integer.parseInt(value);
			roundNumber++; //next round
		}
	}
	
	//initial map is given to the bot with all the information except for player and armies info
	public void setupMap(String[] mapInput)
	{
		int i, regionId, superRegionId, reward;
		
		if(mapInput[1].equals("super_regions"))
		{
			for(i=2; i<mapInput.length; i++)
			{
				try {
					superRegionId = Integer.parseInt(mapInput[i]);
					i++;
					reward = Integer.parseInt(mapInput[i]);
					fullMap.add(new SuperRegion(superRegionId, reward));
				}
				catch(Exception e) {
					System.err.println("Unable to parse SuperRegions");
				}
			}
		}
		else if(mapInput[1].equals("regions"))
		{
			for(i=2; i<mapInput.length; i++)
			{
				try {
					regionId = Integer.parseInt(mapInput[i]);
					i++;
					superRegionId = Integer.parseInt(mapInput[i]);
					SuperRegion superRegion = fullMap.getSuperRegion(superRegionId);
					fullMap.add(new Region(regionId, superRegion));
				}
				catch(Exception e) {
					System.err.println("Unable to parse Regions " + e.getMessage());
				}
			}
		}
		else if(mapInput[1].equals("neighbors"))
		{
			for(i=2; i<mapInput.length; i++)
			{
				try {
					Region region = fullMap.getRegion(Integer.parseInt(mapInput[i]));
					i++;
					String[] neighborIds = mapInput[i].split(",");
					for(int j=0; j<neighborIds.length; j++)
					{
						Region neighbor = fullMap.getRegion(Integer.parseInt(neighborIds[j]));
						region.addNeighbor(neighbor);
					}
				}
				catch(Exception e) {
					System.err.println("Unable to parse Neighbors " + e.getMessage());
				}
			}
		}
	}
	
	//regions from wich a player is able to pick his preferred starting regions
	public void setPickableStartingRegions(String[] mapInput)
	{
		for(int i=2; i<mapInput.length; i++)
		{
			int regionId;
			try {
				regionId = Integer.parseInt(mapInput[i]);
				Region pickableRegion = fullMap.getRegion(regionId);
				pickableStartingRegions.add(pickableRegion);
			}
			catch(Exception e) {
				System.err.println("Unable to parse pickable regions " + e.getMessage());
			}
		}
	}
	
	//visible regions are given to the bot with player and armies info
	public void updateMap(String[] mapInput)
	{
		visibleMap = fullMap.getMapCopy();
		for(int i=1; i<mapInput.length; i++)
		{
			try {
				Region region = visibleMap.getRegion(Integer.parseInt(mapInput[i]));
				String playerName = mapInput[i+1];
				int armies = Integer.parseInt(mapInput[i+2]);
				
				region.setPlayerName(playerName);
				region.setArmies(armies);
				i += 2;
			}
			catch(Exception e) {
				System.err.println("Unable to parse Map Update " + e.getMessage());
			}
		}
		ArrayList<Region> unknownRegions = new ArrayList<Region>();
		
		//remove regions which are unknown.
		for(Region region : visibleMap.regions)
			if(region.getPlayerName().equals("unknown"))
				unknownRegions.add(region);
		for(Region unknownRegion : unknownRegions)
			visibleMap.getRegions().remove(unknownRegion);				
	}

        private boolean someNeighborsAreMine(Region reg, String playerName) {
            for (Region r : reg.getNeighbors()) {
                if (r.ownedByPlayer(playerName))
                    return true;
            }
            return false;
        }

        public void initializeArmiesPerTurnAmount(String playerName) {
            int total = 5;

            for (SuperRegion continent : this.getFullMap().getSuperRegions()) {
                if (BotState.continentBelongsToPlayer(this, continent, playerName))
                    total += continent.getArmiesReward();
            }

            if (total >= 5)
                this.setStartingArmies(total);
            
        }        
        
	public void updateFogOfWar(String playerName)
	{
                visibleMap = fullMap.getMapCopy();
		
		// remove regions which not belongs to player or player is not in neighbors.
                ArrayList<Region> removableRegions = new ArrayList<>();
		for(Region reg : visibleMap.regions) {
                    if (!reg.ownedByPlayer(playerName) && !someNeighborsAreMine(reg, playerName))
                        removableRegions.add(reg);
                }
                
                for (Region r : removableRegions)
                    visibleMap.regions.remove(r);
				
	}
        
	//Parses a list of the opponent's moves every round. 
	//Clears it at the start, so only the moves of this round are stored.
	public void readOpponentMoves(String[] moveInput)
	{
		opponentMoves.clear();
		for(int i=1; i<moveInput.length; i++)
		{
			try {
				Move move;
				if(moveInput[i+1].equals("place_armies")) {
					Region region = visibleMap.getRegion(Integer.parseInt(moveInput[i+2]));
					String playerName = moveInput[i];
					int armies = Integer.parseInt(moveInput[i+3]);
					move = new PlaceArmiesMove(playerName, region, armies);
					i += 3;
				}
				else if(moveInput[i+1].equals("attack/transfer")) {
					Region fromRegion = visibleMap.getRegion(Integer.parseInt(moveInput[i+2]));
					if(fromRegion == null) //might happen if the region isn't visible
						fromRegion = fullMap.getRegion(Integer.parseInt(moveInput[i+2]));

					Region toRegion = visibleMap.getRegion(Integer.parseInt(moveInput[i+3]));
					if(toRegion == null) //might happen if the region isn't visible
						toRegion = fullMap.getRegion(Integer.parseInt(moveInput[i+3]));

					String playerName = moveInput[i];
					int armies = Integer.parseInt(moveInput[i+4]);
					move = new AttackTransferMove(playerName, fromRegion, toRegion, armies);
					i += 4;
				}
				else { //never happens
					continue;
				}
				opponentMoves.add(move);
			}
			catch(Exception e) {
				System.err.println("Unable to parse Opponent moves " + e.getMessage());
			}
		}
	}
	
	public String getMyPlayerName(){
		return myName;
	}
	
	public String getOpponentPlayerName(){
		return opponentName;
	}

	public void setMyPlayerName(String player){
		myName = player;
	}
	
	public void setOpponentPlayerName(String player){
		opponentName = player;
	}        
        
	public int getStartingArmies(){
		return startingArmies;
	}

	public void setStartingArmies(int armies){
		startingArmies = armies;
	}        
        
	public int getRoundNumber(){
		return roundNumber;
	}
	
	public Map getVisibleMap(){
		return visibleMap;
	}
	
	public Map getFullMap(){
		return fullMap;
	}

	public ArrayList<Move> getOpponentMoves(){
		return opponentMoves;
	}
	
	public ArrayList<Region> getPickableStartingRegions(){
		return pickableStartingRegions;
	}

        public static boolean continentBelongsToPlayer(BotState state, SuperRegion continent, String playerName) {

            for (Region r : state.getFullMap().getRegions()) {
                if (r.getSuperRegion() == continent && !r.ownedByPlayer(playerName))
                    return false;
            }
            
            return true;
        }        
        
}
