package com.bitizen.server;

import java.sql.ResultSet;
import java.util.ArrayList;


public class GameLogic {
	private static final int NOTHING			= 0;
	private static final int GET_USERNAME 		= 1;
	private static final int LOGIN_USERNAME 	= 2;
	private static final int VIEW_MATCHES 		= 3;
	private static final int GET_USERMATCH		= 4;
	private static final int VIEW_TEAMS 		= 5;
	private static final int GET_USERTEAM		= 6;
	private static final int VIEW_LOBBY  		= 7;
	private static final int WAITING_READYUSER	= 8;
	private static final int WAITING_READYMATCH	= 9;
	private static final int GAME_START 		= 10;
	
	private static final String KEY_GET_USERNAME	= "username: ";
	private static final String KEY_USERNAME_AVAIL 	= "uname available!";
	private static final String KEY_USERNAME_TAKEN 	= "uname taken";
	private static final String KEY_MATCH_AVAIL 	= "match available";
	private static final String KEY_MATCH_FULL 		= "match full";
	private static final String KEY_TEAM_AVAIL 		= "team available";
	private static final String KEY_TEAM_FULL 		= "team full";
	private static final String KEY_INVALID			= "invalid";
	private static final String KEY_READY_USER 		= "waiting for user ready...";
	private static final String KEY_READY_MATCH 	= "waiting for match ready...";
	private static final String KEY_START_GAME 		= "start game";
	
	private int state = NOTHING;

	private String userName =  null;
	private String userMatch = null;
	private String userTeam = null;
	
	private MySQLAccess dbAccess;
	


	public GameLogic(){
		dbAccess = new MySQLAccess();
		state = GET_USERNAME;
	}
		
	public String processInput(String clientRequest) {
		String reply = null;
		try {
			if(clientRequest != null && clientRequest.equalsIgnoreCase("login")) {
				state = GET_USERNAME;
			}
			if(clientRequest != null && clientRequest.equalsIgnoreCase("exit")) {
				return "exit";
			}
			if(state == GET_USERNAME){
				reply = KEY_GET_USERNAME;
				state = LOGIN_USERNAME;
			}
			else if(state == LOGIN_USERNAME) {
				userName = clientRequest;
				
				dbAccess.createStatement();
				
				if( !dbAccess.usernameIsTaken(userName) ){
					dbAccess.addUsername(userName);
				
					reply = KEY_USERNAME_AVAIL;
					state = VIEW_MATCHES;
				}
				else{
					
					reply = KEY_USERNAME_TAKEN;
					state = GET_USERNAME;
				}
			}
			else if(state == VIEW_MATCHES){
				reply = "Matches: ";
				ArrayList<String> matches = new ArrayList<String>();
				
				ResultSet rs = dbAccess.retrieveMatches();
				while (rs.next()) {              
					//reply = rs.getString("HOSTNAME");
					matches.add(rs.getString("Hostname"));
				}
				
				reply = "Matches: " + matches.toString();
				state = GET_USERMATCH;
			}
			else if(state == GET_USERMATCH){
				userMatch = clientRequest;
				
				if( !dbAccess.matchIsFull(userMatch) ){
					dbAccess.joinMatch(userName, userMatch);
					
					reply = KEY_MATCH_AVAIL;
					state = VIEW_TEAMS;
				}
				else{
					reply = KEY_MATCH_FULL;
					state = VIEW_TEAMS; //edit out
					
				}
			}
			else if(state == VIEW_TEAMS){
				reply = "Teams A or B?";
				state = GET_USERTEAM;
			}
			else if(state == GET_USERTEAM){
				userTeam = clientRequest;
				
				if( !dbAccess.teamIsFull(userTeam) ){
					dbAccess.joinTeam(userName, userTeam);
					reply = KEY_TEAM_AVAIL;
					state = VIEW_LOBBY;
				}
				else{
					reply = KEY_TEAM_FULL;
					state = VIEW_TEAMS;
				}
			}
			else if(state == VIEW_LOBBY){
				
				
				ArrayList<String> players = new ArrayList<String>();
				
				ResultSet rs = dbAccess.retrievePlayersInTeam("A");
				while(rs.next()){
					//reply = dbAccess.retrievePlayersInTeam("A").getString("USERNAME");
					players.add(rs.getString("Username"));
				}
				
				rs = dbAccess.retrievePlayersInTeam("B");
				while(rs.next()){
					//reply = dbAccess.retrievePlayersInTeam("B").getString("USERNAME");
					players.add(rs.getString("Username"));
				}
				
				reply = "Team " + userTeam + " - " + players.toString();
				state = WAITING_READYUSER;
			}		
			else if(state == WAITING_READYUSER){
				reply = KEY_READY_USER;
				
				dbAccess.setStatusToReady(userName);
				
				state = WAITING_READYMATCH;
			}
			else if(state == WAITING_READYMATCH){
				reply = KEY_READY_MATCH;
				if(dbAccess.allPlayersAreReady(userMatch)){
					state = GAME_START;
				}
			}
			else if(state == GAME_START){
				reply = KEY_START_GAME;
			}
			else {
				reply = KEY_INVALID;
			}
		} catch(Exception e) {
			System.out.println("input process falied: " + e.getMessage());
			return "exit";
		}

		return reply;
	}
}
