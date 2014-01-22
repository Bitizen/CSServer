package com.bitizen.server;


public class GameLogic {
	private static final int LoginUserName = 0;
	private static final int LoginPassword = 1;
	private static final int AuthenticateUser = 2;
	private static final int AuthSuccess   = 3;

	private int state = LoginUserName;

	private String userName =  null;

	public String processInput(String clientRequest) {
		String reply = null;
		try {
			if(clientRequest != null && clientRequest.equalsIgnoreCase("login")) {
				state = LoginPassword;
			}if(clientRequest != null && clientRequest.equalsIgnoreCase("exit")) {
				return "exit";
			}

			if(state == LoginUserName) {
				reply = "username: ";
				state = AuthenticateUser;
			} else if(state == AuthenticateUser) {
				userName = clientRequest;
				
				if(userName.equalsIgnoreCase("kashka")) { 
					reply = "success";
					state = AuthSuccess;
				} else {
					reply = "taken. username:";
					state = LoginUserName;
				}
			} else {
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
			System.out.println("input process falied: " + e.getMessage());
			return "exit";
		}

		return reply;
	}
	}
	*/