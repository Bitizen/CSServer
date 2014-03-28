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
	private static final int WAITING_READYMATCH		= 8;
	private static final int GAME_START 			= 9;
	private static final int RESULTS 				= 10;

	private static final String KEY_IAMREADY 		= "IAMREADY";
	private static final String KEY_IAMIDLE 		= "IAMIDLE";
	private static final String KEY_HOST_LOGIN 		= "HOST";
	private static final String KEY_REG_LOGIN 		= "REG";
	private static final String KEY_CHANGEMYMARKER 	= "CHANGEMYMARKER";
	private static final String KEY_HIT				= "HIT";
	private static final String KEY_GET_HIT			= "GETHIT";
	private static final String KEY_GAMEOVER		= "GAMEOVER";
	private static final String KEY_SHOWMATCHES		= "SHOWMATCHES";
	private static final String KEY_PICKMATCH		= "PICKMATCH";
	private static final String KEY_MARKERTAKEN		= "MARKERTAKEN";
	private static final String KEY_MARKERCHANGED	= "MARKERCHANGED";
	private static final String KEY_MATCHISOVER		= "MATCHISOVER";
	private static final String KEY_VICTOR			= "MATCHWON";
	private static final String KEY_LOSER			= "MATCHLOST";
	private static final String KEY_MATCHONGOING	= "MATCHNOTOVER";
	private static final String KEY_QUITHOSTING		= "QUITHOSTING";
	private static final String KEY_LEAVEMATCH  	= "leavematch";
	private static final String KEY_REPLAYMATCH		= "replaymatch";
	private static final String KEY_HOSTHASLEFT		= "hosthasleft";
	private static final String KEY_LOGOUT			= "LOGOUT";
	private static final String KEY_LEAVETEAM		= "LEAVETEAM";
	private static final String KEY_GAMEWON			= "GAMEWON";
	
	
	private static final String KEY_GET_USERNAME	= "username: ";
	private static final String KEY_USERNAME_AVAIL 	= "uname available!";
	private static final String KEY_USERNAME_TAKEN 	= "uname taken";
	private static final String KEY_MATCH_AVAIL 	= "match available";
	private static final String KEY_MATCH_FULL 		= "match full";
	private static final String KEY_TEAM_AVAIL 		= "team available";
	private static final String KEY_TEAM_FULL 		= "team full";
	private static final String KEY_READY_MATCH 	= "waiting for match ready...";
	private static final String KEY_START_GAME 		= "start game";
	private static final String KEY_HOST_AVAIL 		= "host ok";
	
	private int state = NOTHING;

	private String userName =  null;
	private String userMatch = null;
	private String userTeam = null;
	private String otherTeam = null;
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
		    
			if(state == GET_USERNAME) {
				reply = KEY_GET_USERNAME;
				state = LOGIN_USERNAME;
			}
			else if(state == LOGIN_USERNAME) {
				String str = clientRequest;
			    String[] s = str.split("[\\-]+");
			    userName = s[1];
			    
			    dbAccess.createStatement();
				
			    // If Player wants to Host, host a new match
			    if (s[0].equalsIgnoreCase(KEY_HOST_LOGIN)
			    		&& !dbAccess.userNameExists(userName)) {
			    	dbAccess.hostMatch(userName);
			    	userMatch = userName;
			    	userTeam = "A";
					reply = KEY_HOST_AVAIL;
			    	state = VIEW_LOBBY;
			    }
			    
			    // If Player wants to join an existing match, create new Player
			    else if (s[0].equalsIgnoreCase(KEY_REG_LOGIN)
			    		&& !dbAccess.userNameExists(userName)) {
			    	dbAccess.addNewPlayer(userName);
					reply = KEY_USERNAME_AVAIL;
					state = VIEW_MATCHES;
			    }
			    
			    // If Player's desired username is taken, ask for another username
			    else if (dbAccess.userNameExists(userName)) {
					reply = KEY_USERNAME_TAKEN;
					state = GET_USERNAME;
			    }
			} else if(state == VIEW_MATCHES) {
				
				// Show Player Available Matches
				if(clientRequest.equalsIgnoreCase(KEY_SHOWMATCHES)) {
					ArrayList<String> matches = new ArrayList<String>();
					
					ResultSet rs = dbAccess.retrieveMatches();
					while (rs.next()) {              
						matches.add(rs.getString("matchName"));
					}
					
					reply = "Matches: " + matches.toString();
				}
				
				// If Player chooses to Logout
			    if(clientRequest != null && clientRequest.equalsIgnoreCase(KEY_LOGOUT)) {
		    		dbAccess.deletePlayer(userName);
			    	state = LOGIN_USERNAME; 
			    }
			    
				String str = clientRequest;
			    String[] s = str.split("[\\-]+");
			    
			    // If Player picks a Match
			    if(s[0].equalsIgnoreCase(KEY_PICKMATCH)) {
					state = GET_USERMATCH;
			    	userMatch = s[1];
			    }
			}
			else if(state == GET_USERMATCH) {
				
				// Checks if Player's desired Match is full
				if( !dbAccess.matchIsFull(userMatch) ) {
					dbAccess.joinMatch(userName, userMatch);
					
					reply = KEY_MATCH_AVAIL;
					state = VIEW_TEAMS;
				}
				else{
					reply = KEY_MATCH_FULL;
					state = VIEW_MATCHES;
				}
			}
			else if(state == VIEW_TEAMS) {
				reply = "Teams A or B?";
				state = GET_USERTEAM;
			}
			else if(state == GET_USERTEAM) {
				userTeam = clientRequest;
				
			    // If Player chooses to Leave Match
			    if(clientRequest != null && clientRequest.equalsIgnoreCase(KEY_LEAVEMATCH)) {
			    	dbAccess.leaveMatch(userName);
			    	state = VIEW_MATCHES; 
			    }
			    
				// Checks if Player's desired Team is full
			    else if( dbAccess.teamIDExists(userTeam, userMatch)
						&& !dbAccess.teamIsFull(userTeam, userMatch) ) {
					dbAccess.joinTeam(userName, userTeam, userMatch);
					
					// If available, join team
					if(userTeam.equalsIgnoreCase("A")) otherTeam = "B";
					else if(userTeam.equalsIgnoreCase("B")) otherTeam = "A";
					
					reply = KEY_TEAM_AVAIL;
					state = VIEW_LOBBY;
				}
				else if(!dbAccess.teamIDExists(userTeam, userMatch)){
					reply = KEY_TEAM_FULL;
					state = VIEW_TEAMS;
				}
			}
			else if(state == VIEW_LOBBY) {
				
				// If Host is still live (did not quit)
				if(dbAccess.matchNameExists(userMatch)) {
			
					String str = clientRequest;
				    String[] s = str.split("[\\-]+");
	
				    // If Player wants to change Marker, check its availability
				    if(s[0] != null && s[0].equalsIgnoreCase(KEY_CHANGEMYMARKER)) {
				    	if(dbAccess.markerIsTaken(Integer.parseInt(s[1]), userMatch)) {
					    	reply = KEY_MARKERTAKEN;
					    } else {
					    	userMarker = Integer.parseInt(s[1]);
					    	dbAccess.changePlayerMarker(userName, userMarker);
					    	reply = KEY_MARKERCHANGED;
					    }
				    } 
				    
				    // If Player is Ready
				    else if(clientRequest != null && clientRequest.equalsIgnoreCase(KEY_IAMREADY)) {
						dbAccess.setStatusToReady(userName);
						state = WAITING_READYMATCH;
					}
				    
				    // If Host chooses to Quit Hosting
				    else if(clientRequest != null && clientRequest.equalsIgnoreCase(KEY_QUITHOSTING)) {
						dbAccess.stopHosting(userName);
						state = VIEW_MATCHES;
					}

				    // If Player chooses to Leave Match
				    else if(clientRequest != null && clientRequest.equalsIgnoreCase(KEY_LEAVEMATCH)) {
				    	dbAccess.leaveMatch(userName);
				    	state = VIEW_MATCHES; 
				    }

				    // If Player chooses to Leave Team
				    else if(clientRequest != null && clientRequest.equalsIgnoreCase(KEY_LEAVETEAM)) {
			    		dbAccess.leaveTeam(userName);
				    	state = VIEW_TEAMS; 
				    }
				    
				    // If Player chooses to Logout
				    else if(clientRequest != null && clientRequest.equalsIgnoreCase(KEY_LOGOUT)) {
				    	// If Player is Host, quit Hosting first
				    	if(userMatch.equalsIgnoreCase(userName)) {
				    		dbAccess.stopHosting(userName);
				    	}
				    	
				    	// Logout
			    		dbAccess.deletePlayer(userName);
				    	state = LOGIN_USERNAME; 
				    }
				    
				    // If no request, output Lobby
				    else {
				    
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
				
				// TODO
				// If Host Quit, trigger/redirect Player to view Available Matches
				else {
					dbAccess.leaveMatch(userName);
					
					reply = KEY_HOSTHASLEFT;
					state = VIEW_MATCHES;
				}
			} 
			
			if(state == WAITING_READYMATCH) {
				// If Host chooses to Quit Hosting
			    if(clientRequest != null && clientRequest.equalsIgnoreCase(KEY_QUITHOSTING)) {
					dbAccess.stopHosting(userName);
					state = VIEW_MATCHES;
				}

			    // If Player chooses to Logout
			    else if(clientRequest != null && clientRequest.equalsIgnoreCase(KEY_LOGOUT)) {
			    	// If Player is Host, quit Hosting first
			    	if(userMatch.equalsIgnoreCase(userName)) {
			    		dbAccess.stopHosting(userName);
			    	}
			    	
			    	// Logout
		    		dbAccess.deletePlayer(userName);
			    	state = LOGIN_USERNAME; 
			    }
			    
				// If Player is suddenly not Ready and wants to be Idle
				if(clientRequest != null && clientRequest.equalsIgnoreCase(KEY_IAMIDLE)) {
					dbAccess.setStatusToIdle(userName);
					state = VIEW_LOBBY;
				} else {
				
					// If all Players in the match are ready, Begin Game
					if(dbAccess.eachTeamHasOnePlayer(userMatch) 
							&& dbAccess.allPlayersHaveMarkers(userMatch)
							&& dbAccess.allPlayersAreReady(userMatch)) {
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
			    
			    // If Player hits a target, ensure validity of target (Same Match, Different Teams)
			    if(s[0] != null && s[0].equalsIgnoreCase(KEY_HIT)) {
			    	dbAccess.hit(userName, Integer.parseInt(s[2]), userMatch, userTeam);
			    }
			    
			    // If Opposing Team are all gameover, show results
			    if(dbAccess.getWinningTeam(userMatch) != null) {
			    	reply = KEY_GAMEWON;
			    	state = RESULTS;
			    }
			    
			    // If Player runs out of HP, gameover for Player
			    if(dbAccess.getHealth(userName) == 0) {
			    	reply = KEY_GAMEOVER;
			    	state = RESULTS;
			    }
			    
			    // If Player's HP is deducted, confirm hit
			    else if(dbAccess.getHealth(userName) < currentHp) {
			    	--currentHp;
			    	reply = KEY_GET_HIT;
			    }
			}
			
			else if(state == RESULTS) {
				
				// If Player wants a rematch, redirect to Lobby/HostLobby of Match
				if (clientRequest.equalsIgnoreCase(KEY_REPLAYMATCH)) {
					dbAccess.setStatusToIdle(userName);
					dbAccess.deletePlayerFromMarker(userName);
					dbAccess.restoreHealth(userName);
					state = VIEW_LOBBY;
				}
				
				// If Player wants to leave the Match, redirect to Available Matches
				// If Player is a Host, quit Hosting and redirect to Available Matches
				else if (clientRequest.equalsIgnoreCase(KEY_LEAVEMATCH)) {
					if (userMatch.equalsIgnoreCase(userName)) {
						dbAccess.stopHosting(userName);
					} else {
						dbAccess.leaveMatch(userName);
					}
					
					dbAccess.restoreHealth(userName);
					state = VIEW_MATCHES;
				}
				
				// Output victor if game is over
				else {
				    String winner = dbAccess.getWinningTeam(userMatch);
		
			    	if (winner == null) {
			    		reply = KEY_MATCHONGOING;
			    	} else if (winner.equalsIgnoreCase(userTeam)) {
			    		reply = userTeam;
			    	} else if (winner.equalsIgnoreCase(otherTeam)) {
			    		reply = otherTeam;
			    	}
				}
			}
		
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("input process falied: " + e.getMessage());
			return "exit";
		}

		return reply;
	}
}
