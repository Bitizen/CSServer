package com.bitizen.server;

import java.sql.ResultSet;
import java.util.ArrayList;


public class GameLogic {
	private static final int NOTHING				= 0;
	private static final int GET_USERNAME 			= 1;
	private static final int LOGIN_USERNAME 		= 2;
	private static final int VIEW_MATCHES 			= 3;
	private static final int GET_USERMATCH			= 4;
	private static final int VIEW_TEAMS 			= 5;
	private static final int GET_USERTEAM			= 6;
	private static final int VIEW_LOBBY  			= 7;
	private static final int VIEW_HOSTLOBBY			= 10;
	//private static final int WAITING_READYUSER		= 8;
	private static final int WAITING_READYMATCH		= 8;
	//private static final int WAITING_HOSTREADYUSER	= 12;
	//private static final int WAITING_HOSTREADYMATCH	= 13;
	private static final int GAME_START 			= 9;

	private static final String KEY_IAMREADY 		= "IAMREADY";
	private static final String KEY_IAMIDLE 		= "IAMIDLE";
	private static final String KEY_HOST_LOGIN 		= "HOST";
	private static final String KEY_REG_LOGIN 		= "REG";
	private static final String KEY_CHANGEMYMARKER 	= "CHANGEMYMARKER";
	private static final String KEY_HIT				= "HIT";
	private static final String KEY_GET_HIT			= "GETHIT";
	private static final String KEY_GAMEOVER		= "GAMEOVER";
	
	private static final String KEY_GET_USERNAME	= "username: ";
	private static final String KEY_USERNAME_AVAIL 	= "uname available!";
	private static final String KEY_USERNAME_TAKEN 	= "uname taken";
	private static final String KEY_MATCH_AVAIL 	= "match available";
	private static final String KEY_MATCH_FULL 		= "match full";
	private static final String KEY_TEAM_AVAIL 		= "team available";
	private static final String KEY_TEAM_FULL 		= "team full";
	private static final String KEY_INVALID			= "invalid";
	//private static final String KEY_READY_USER 		= "waiting for user ready...";
	private static final String KEY_READY_MATCH 	= "waiting for match ready...";
	private static final String KEY_START_GAME 		= "start game";
	private static final String KEY_HOST_AVAIL 		= "host ok";
	
	private int state = NOTHING;

	private String userName =  null;
	private String userMatch = null;
	private String userTeam = null;
	private int userMarker = 0;
	private int currentHp = 3;
	
	private MySQLAccess dbAccess;

	public GameLogic() {
		dbAccess = new MySQLAccess();
		state = GET_USERNAME;
	}
		
	public String processInput(String clientRequest) {
		String reply = null;
		
		try {
			if(clientRequest != null && clientRequest.equalsIgnoreCase("login"))
				state = GET_USERNAME;

			if(clientRequest != null && clientRequest.equalsIgnoreCase("exit"))
				return "exit";
			
			/*if(clientRequest != null && clientRequest.equalsIgnoreCase(KEY_IAMREADY)) {
				dbAccess.setStatusToReady(userName);
				state = WAITING_READYMATCH;
			}

			if(clientRequest != null && clientRequest.equalsIgnoreCase(KEY_IAMIDLE)) {
				dbAccess.setStatusToIdle(userName);
				state = VIEW_LOBBY;
			}*/
		    
		    
			if(state == GET_USERNAME) {
				reply = KEY_GET_USERNAME;
				state = LOGIN_USERNAME;
			}
			else if(state == LOGIN_USERNAME) {
				String str = clientRequest;
			    String[] s = str.split("[\\-]+");
			    userName = s[1];
			    
			    dbAccess.createStatement();
				
			    if (s[0].equalsIgnoreCase(KEY_HOST_LOGIN)
			    		&& !dbAccess.usernameIsTaken(userName)) {
			    	dbAccess.hostMatch(userName);
					reply = KEY_HOST_AVAIL;
			    	state = VIEW_HOSTLOBBY;
			    } else if (s[0].equalsIgnoreCase(KEY_REG_LOGIN)
			    		&& !dbAccess.usernameIsTaken(userName)) {
			    	
					if( !dbAccess.usernameIsTaken(userName) ) {
						dbAccess.addNewPlayer(userName);
						reply = KEY_USERNAME_AVAIL;
						state = VIEW_MATCHES;
					} else{
						reply = KEY_USERNAME_TAKEN;
						state = GET_USERNAME;
					}
			    }
			} else if(state == VIEW_MATCHES) {
				ArrayList<String> matches = new ArrayList<String>();
				
				ResultSet rs = dbAccess.retrieveMatches();
				while (rs.next()) {              
					matches.add(rs.getString("matchName"));
				}
				
				reply = "Matches: " + matches.toString();
				state = GET_USERMATCH;
			}
			else if(state == GET_USERMATCH) {
				userMatch = clientRequest;
				
				if( !dbAccess.matchIsFull(userMatch) ) {
					dbAccess.joinMatch(userName, userMatch);
					
					reply = KEY_MATCH_AVAIL;
					state = VIEW_TEAMS;
				}
				else{
					reply = KEY_MATCH_FULL;
					state = VIEW_TEAMS;
					
				}
			}
			else if(state == VIEW_TEAMS) {
				reply = "Teams A or B?";
				state = GET_USERTEAM;
			}
			else if(state == GET_USERTEAM) {
				userTeam = clientRequest;
				
				if( !dbAccess.teamIsFull(userTeam, userMatch) ) {
					dbAccess.joinTeam(userName, userTeam, userMatch);
					reply = KEY_TEAM_AVAIL;
					state = VIEW_LOBBY;
				}
				else{
					reply = KEY_TEAM_FULL;
					state = VIEW_TEAMS;
				}
			}
			else if(state == VIEW_LOBBY) {

				String str = clientRequest;
			    String[] s = str.split("[\\-]+");

			    if(s[0] != null && s[0].equalsIgnoreCase(KEY_CHANGEMYMARKER)) {
			    	userMarker = Integer.parseInt(s[1]);
			    	dbAccess.changePlayerMarker(userName, userMarker);
			    }
			    
			    if(clientRequest != null && clientRequest.equalsIgnoreCase(KEY_IAMREADY)) {
					dbAccess.setStatusToReady(userName);
					state = WAITING_READYMATCH;
				} else {
			    
					ArrayList<String> teamA_players = new ArrayList<String>();
					ArrayList<String> teamB_players = new ArrayList<String>();
					
					ResultSet rs = dbAccess.returnPlayersInTeam("A", userMatch);
					while(rs.next()) {
						teamA_players.add(rs.getString("PLAYER_NAME"));
					}
					
					rs = dbAccess.returnPlayersInTeam("B", userMatch);
					while(rs.next()) {
						teamB_players.add(rs.getString("PLAYER_NAME"));
					}
	
					reply = "LOBBY-" + teamA_players.toString() 
							+ "-" + teamB_players.toString();
				}
			} 
			
			if(state == WAITING_READYMATCH) {
				if(clientRequest != null && clientRequest.equalsIgnoreCase(KEY_IAMIDLE)) {
					dbAccess.setStatusToIdle(userName);
					state = VIEW_LOBBY;
				} else {
				
					if(dbAccess.eachTeamHasOnePlayer(userMatch) && dbAccess.allPlayersAreReady(userMatch)) {
						reply = KEY_START_GAME;
						state = GAME_START; 
					}
					else {
						reply = KEY_READY_MATCH;
					}
				}
			}
			else if(state == GAME_START) {
				String str = clientRequest;
			    String[] s = str.split("[\\-]+");
			    
			    if(s[0] != null && s[0].equalsIgnoreCase(KEY_HIT)) {
			    	dbAccess.hit(userName, Integer.parseInt(s[2]), userMatch, userTeam);
			    }
			    
			    if(dbAccess.getHealth(userName) == 0) {
			    	reply = KEY_GAMEOVER;
			    } else if(dbAccess.getHealth(userName) < currentHp) {
			    	//dbAccess.reducePlayerHealth(userName);
			    	--currentHp;
			    	reply = KEY_GET_HIT;
			    }
			}
			
			// IF HOST
			else if(state == VIEW_HOSTLOBBY) {
				ArrayList<String> playersA = new ArrayList<String>();
				ArrayList<String> playersB = new ArrayList<String>();
				
				ResultSet rs = dbAccess.returnPlayersInTeam("A", userMatch);
				while(rs.next()) {
					playersA.add(rs.getString("Username"));
				}
				
				rs = dbAccess.returnPlayersInTeam("B", userMatch);
				while(rs.next()) {
					playersB.add(rs.getString("Username"));
				}

				reply = "HOSTLOBBY-" + playersA.toString() + "-" + playersB.toString();
			}
		
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("input process falied: " + e.getMessage());
			return "exit";
		}

		return reply;
	}
}
