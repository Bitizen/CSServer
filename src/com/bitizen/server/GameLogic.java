package com.bitizen.server;


public class GameLogic {
	private static final int LOGIN_USERNAME 	= 0;
	private static final int VIEW_MATCHES 		= 1;
	private static final int VIEW_TEAMS 		= 2;
	private static final int VIEW_LOBBY  		= 3;
	//private static final int READY_ALL 			= 4;
	//private static final int ONGOING_MATCH		= 5;
	
	private static final String KEY_USERNAME_AVAIL 	= "uname available";
	private static final String KEY_USERNAME_TAKEN 	= "uname taken";
	private static final String KEY_MATCH_AVAIL 	= "match available";
	private static final String KEY_MATCH_FULL 		= "match full";
	private static final String KEY_TEAM_AVAIL 		= "team available";
	private static final String KEY_TEAM_FULL 		= "team full";

	private int state = LOGIN_USERNAME;

	private String userName =  null;
	private String userMatch = null;
	private String userTeam = null;
	
	private MySQLAccess dbAccess;


	public GameLogic(){
		dbAccess = new MySQLAccess();
	}
		
	public String processInput(String clientRequest) {
		String reply = null;
		try {
			if(clientRequest != null && clientRequest.equalsIgnoreCase("login")) {
				state = LOGIN_USERNAME;
			}
			if(clientRequest != null && clientRequest.equalsIgnoreCase("exit")) {
				return "exit";
			}

			if(state == LOGIN_USERNAME) {
				userName = clientRequest;
				
				if( !dbAccess.usernameIsTaken(userName) ){
					dbAccess.addUsername(userName);
					reply = KEY_USERNAME_AVAIL;
					state = VIEW_MATCHES;
				}
				else{
					reply = KEY_USERNAME_TAKEN;
				}
			}
			else if(state == VIEW_MATCHES){
				userMatch = clientRequest;
				
				if( !dbAccess.matchIsFull(userMatch) ){
					dbAccess.joinMatch(userName, userMatch);
					reply = KEY_MATCH_AVAIL;
					state = VIEW_TEAMS;
				}
				else{
					reply = KEY_MATCH_FULL;
				}
			}
			else if(state == VIEW_TEAMS){
				userTeam = clientRequest;
				
				if( !dbAccess.teamIsFull(userTeam) ){
					dbAccess.joinTeam(userName, userTeam);
					reply = KEY_TEAM_AVAIL;
					state = VIEW_LOBBY;
				}
				else{
					reply = KEY_TEAM_FULL;
				}
			}
			
			else {
				reply = "invalid";
			}
		} catch(Exception e) {
			System.out.println("input process falied: " + e.getMessage());
			return "exit";
		}

		return reply;
	}
}
	/*
	private static final int LoginUserName = 0;
	private static final int LoginPassword = 1;
	private static final int AuthenticateUser = 2;
	private static final int AuthSuccess   = 3;
	 
	
	private static final int LoginUserName = 0;			//Login and Verify available username
	private static final int ViewMatches = 1;			//View Matches, Select Match, Verify if not full
	private static final int ViewTeams = 2;				//View Teams, Select Team, Verify if not full
	private static final int ViewLobby = 3;				//View Lobby (idle and ready)
	
	private int state = LoginUserName;

	private String userName =  null;
	private String userMatch =  null;
	private String userTeam =  null;

	
	
	public String processInput(String clientRequest) {
		String reply = null;
		try {
			if(clientRequest != null && clientRequest.equalsIgnoreCase("login")) {
				state = ViewMatches;
			}
			if(clientRequest != null && clientRequest.equalsIgnoreCase("exit")) {
				return "exit";
			}

			if(state == LoginUserName) {
				userName = clientRequest;
				if(userName.equalsIgnoreCase("Kashka")) { 
				// ask webserver if username is taken (via php, JSONParser)
					reply = "Login success.";
					state = ViewMatches;
				} else {
					reply = "Login fail.";
				}
			} else if(state == ViewMatches) {
				// Output matches
				// ask webserver for list of hosts
				// Choose match, CHECK IF FULL
				userMatch = clientRequest;
				if(userMatch.equalsIgnoreCase("Dice")) { 
					reply = "Match chosen success.";
					state = ViewTeams;
				} else {
					reply = "Match full.";
				}
			} else if(state == ViewTeams) {
				// Choose team, CHECK IF FULL
				userTeam = clientRequest;
				if(userTeam.equalsIgnoreCase("A")) { 
					reply = "Team chosen success.";
					state = ViewLobby;
				} else {
					reply = "Team full.";
				}
			}
		} catch(Exception e) {
			System.out.println("input process failed: " + e.getMessage());
			return "exit";
		}

		return reply;
	}
	}
	*/