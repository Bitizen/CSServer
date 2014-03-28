package com.bitizen.server;

import java.sql.*;


public class MySQLAccess {
	 
	private Statement stmt = null;
	private CallableStatement cs = null;
	private ResultSet rs = null;
	private Connection con;
	private final static String DB_URL = "jdbc:mysql://localhost:3306/COUNTERSWIPE";
	private final static int MAX_TEAMPLAYERS = 3;
	private final static int MAX_MATCHPLAYERS = 6;
	private final static String[] TEAM_NAMES = {"A", "B"};

	
	// Constructor
	public MySQLAccess(){
		register();
		connect();
	}
	
	// Register the driver
	public void register(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	// Establish connection with database
	public void connect(){
		try {
			con = DriverManager.getConnection(DB_URL,"root", "");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Create a Statement object for sending SQL statements to database
	public void createStatement(){
		try {
			stmt = con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Releases JDBC resources that were opened
	public void close() {
		    try {
			      if (rs != null) {
			        rs.close();
			      }
			      if (stmt != null) {
			        stmt.close();
			      }
			      if (con != null) {
			        con.close();
			      }
		    }catch (Exception e) {
		    	e.printStackTrace();
		    }
	}
	
	///////////////////////////////////////////////////
	//////////////------------- RESULTSET -------------
	///////////////////////////////////////////////////
	
	// Retrieves existing matchNames from table `match`
	public ResultSet retrieveMatches(){
		try {
			cs = con.prepareCall("{call returnHostNames()}");
			rs = cs.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
			e.getMessage();
		}
		
		return rs;
	}
	
	// Returns userNames in specified team of specified match
	public ResultSet returnPlayersInTeam(String teamName, String matchName){
		try {
			cs = con.prepareCall("{call returnPlayersInTeam(?,?)}");
			cs.setString(1, teamName);
			cs.setString(2, matchName);
			rs = cs.executeQuery();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return rs;		
	}	
	
	// Returns null if the marker was not selected by anyone
	public ResultSet returnOwnerOfMarker(int markerID, String hostName){
		
		try {
			cs = con.prepareCall("{call returnMarkerOwner(?,?)}");
			cs.setInt(1, markerID);
			cs.setString(2, hostName);
			ResultSet rs = cs.executeQuery();

			if(rs.next()){
				System.out.println("owner of marker: " +  rs.getString("userName"));
				return rs;
			}
			

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return rs;			
	}
	
	///////////////////////////////////////////////////
	////////////// ------------- STRING ---------------
	///////////////////////////////////////////////////	
	
	// Either 	returns null if no winner yet (both teams have at least 1 living member)
	// Or 		returns winner String "A" or "B"
	public String getWinningTeam(String matchName){
		for(String teamName: TEAM_NAMES){
			if(teamIsWipedOut(matchName, teamName)){
				return	 teamName.equalsIgnoreCase(TEAM_NAMES[0]) ? TEAM_NAMES[1] : TEAM_NAMES[0];
			}
		}
		return null;
	}
	
	///////////////////////////////////////////////////
	////////////// ------------- INT ------------------
	///////////////////////////////////////////////////	
	
	// Returns number of players in match
	public int getNumberOfMatchPlayers(String matchName){
		try {
			cs = con.prepareCall("{ call GET_MATCH_PLAYERCOUNT(?,?) }");
			cs.setString(1, matchName);
			cs.registerOutParameter(2, Types.INTEGER);
			cs.executeQuery();
			//System.out.println("number of players in match: " + cs.getInt(2));
			return cs.getInt(2);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}	
	
	// Returns number of players in team
	public int getNumberOfTeamPlayers(String teamName, String matchName){
		try {
			cs = con.prepareCall("{ call GET_TEAM_PLAYERCOUNT(?,?,?) }");
			cs.setString(1, teamName);
			cs.setString(2, matchName);
			cs.registerOutParameter(3, Types.INTEGER);
			cs.executeQuery();
			//System.out.println("number of players in team: " + cs.getInt(3));
			return cs.getInt(3);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}	

	//Returns int health
	public int getHealth(String userName){
		try {
			cs = con.prepareCall("{call returnPlayerHealth(?)}");
			cs.setString(1, userName);
			ResultSet rs = cs.executeQuery();
			
			if(rs.next()){

				return rs.getInt(1);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	// Returns a count of ready players in the specified match
	public int getReadyPlayersInMatch(String matchName){
		
		try {
			cs = con.prepareCall("{call returnReadyPlayersInMatch(?)}");
			cs.setString(1, matchName);
			ResultSet rs = cs.executeQuery();
			
			if(rs.next()){
				rs.last();
				
				return rs.getRow();
			}
			rs = cs.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
				
		return 0;
	}
	
	// Returns count of match players who have selected markers
	public int getNumberOfMarkerOwners(String matchName){
		try {
			cs = con.prepareCall("{ call GET_MARKEROWNERCOUNT(?,?) }");
			cs.setString(1, matchName);
			cs.registerOutParameter(2, Types.INTEGER);
			cs.executeQuery();
			//System.out.println("number of players in team: " + cs.getInt(3));
			return cs.getInt(2);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	///////////////////////////////////////////////////
	////////////// ------------- BOOLEAN --------------
	///////////////////////////////////////////////////

	// Returns true if all 6 slots in the match are taken
	public Boolean matchIsFull(String hostName){
		return getNumberOfMatchPlayers(hostName) >= MAX_MATCHPLAYERS;
	}
	
	// Returns true if specified team in specified match already has 6 players
	public Boolean teamIsFull(String teamName, String matchName){
		return getNumberOfTeamPlayers(teamName, matchName) >= MAX_TEAMPLAYERS;
	}	
	
	// Returns true if all players in the specified match are ready
	public Boolean allPlayersAreReady(String matchName){
		return (getNumberOfMatchPlayers(matchName) == getReadyPlayersInMatch(matchName));
	}
	

	// Returns true if teamID exists in table `team`
	public Boolean teamIDExists(String userTeam, String userMatch){
		try {
			cs = con.prepareCall("{call GET_TEAMID(?,?)}");
			cs.setString(1, userTeam);
			cs.setString(2, userMatch);
			
			if(!cs.executeQuery().next()){
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	// Returns true if marker is taken
	public Boolean markerIsTaken(int markerID, String hostName){
		
		try {
			cs = con.prepareCall("{call returnMarkerOwner(?,?)}");
			cs.setInt(1, markerID);
			cs.setString(2, hostName);
			ResultSet rs = cs.executeQuery();

			if(rs.next()){
				return true;
			}
			

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;			
	}
	
	// Returns false when a team has 0 players
	public Boolean eachTeamHasOnePlayer(String matchName){
		
		
		for(String teamName: TEAM_NAMES){
			if(getNumberOfTeamPlayers(teamName, matchName) < 1){
				return false;
			}
		}
		
		return true;
	}
	
	// Returns true if username exists in table `player`
	public Boolean userNameExists(String username){
		try {
			cs = con.prepareCall("{call GET_USERNAME(?)}");
			cs.setString(1, username);
			
			if(!cs.executeQuery().next()){
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public Boolean matchNameExists(String hostName){
		try {
			cs = con.prepareCall("{call GET_MATCHNAME(?)}");
			cs.setString(1, hostName);
			
			if(!cs.executeQuery().next()){
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;		
	}
	
	// Returns true if team has 0 living members
	public Boolean teamIsWipedOut(String matchName, String teamName){
		try {
			cs = con.prepareCall("{call GET_TEAMNAME_AND_ALIVECOUNT(?,?)}");
			cs.setString(1, matchName);
			cs.setString(2, teamName);
			ResultSet rs = cs.executeQuery();
			
			if(!rs.next()){
				return true;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		return false;
	}
		
	// Returns true if all players in the match have markers
	public Boolean allPlayersHaveMarkers(String matchName){
		return (getNumberOfMatchPlayers(matchName) == getNumberOfMarkerOwners(matchName));
	}
		
	//////////////////////////////////////////////////////////////
	////////////// ------ VOID / INSERT / UPDATE / DELETE --------
	/////////////////////////////////////////////////////////////

	
	// Adds username to table `player` with default values. 
	// Should be called when username does not exist
	public void addNewPlayer(String username){
		try {
			cs = con.prepareCall("{call addNewPlayer(?)}");
			cs.setString(1, username);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Updates isReady field of user in table `player` to 1
	public void setStatusToReady(String username){
		try {
			cs = con.prepareCall("{call setStatusToReady(?)}");
			cs.setString(1, username);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Updates isReady field of user in table `player` to 0
	public void setStatusToIdle(String username){
		try {
			cs = con.prepareCall("{call setStatusToIdle(?)}");
			cs.setString(1, username);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Updates the matchID column of a user in table `player` 
	public void joinMatch(String userName, String hostName){
		try {
			cs = con.prepareCall("{call joinMatch(?,?)}");
			cs.setString(1, userName);
			cs.setString(2, hostName);
			cs.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
		
	// Joins the player to a specific team in specific match.
	public void joinTeam(String userName, String teamName, String matchName){
		try {
			cs = con.prepareCall("{call joinTeam(?,?,?)}");
			cs.setString(1, userName);
			cs.setString(2, teamName);
			cs.setString(3, matchName);
			cs.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
	
	// Creates a new match
	// Automatically creates two teams, then joins these 2 teams to the new match
	public void hostMatch(String hostName){
		try {
			cs = con.prepareCall("{call hostMatch(?)}");
			cs.setString(1, hostName);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	// Changes player marker
	public void changePlayerMarker(String username, int markerID){
		try {
			cs = con.prepareCall("{call changePlayerMarker(?,?)}");
			cs.setString(1, username);
			cs.setInt(2, markerID);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Reduce player health by 1
	public void reducePlayerHealth(String userName){
		try {
			cs = con.prepareCall("{call reducePlayerHealth(?)}");
			cs.setString(1, userName);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	 // Note that health check is also done within stored procedure 'reducePlayerHealth'
	public void hit(String shooterName, int markerID, String userMatch, String userTeam){
		try{
			ResultSet markerOwner = returnOwnerOfMarker(markerID, userMatch);
			
			// Compares the credentials of the shooter with credentials of markerOwner, to check if:
			if(markerOwner != null){
				if(markerOwner.getString("hostName").equalsIgnoreCase(userMatch) 			//same match
						&& !markerOwner.getString("teamName").equalsIgnoreCase(userTeam)){  //different team
					
					if(markerOwner.getInt("health") > 0){									//not yet dead
						System.out.println("DO DAMAGE.");
						reducePlayerHealth(markerOwner.getString("userName"));
					}
					else{
						System.out.println("TARGET IS ALREADY DEAD.");
					}
					
				}
				else{
					System.out.println("DON'T HIT FRIENDS.");
				}
			}
			else{
				System.out.println("MARKER IS NOT ASSOCIATED WITH ANY PLAYER IN THIS MATCH.");
			}
			
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	// Deletes player from all affected tables
	public void deletePlayer(String userName){
		if(userNameExists(userName)){
			deletePlayerFromMarker(userName);
			deletePlayerFromTeam(userName);
			deletePlayerFromPlayers(userName);
		}
		else{
			System.out.println("DELETE FAILED. PLAYER DOES NOT EXIST IN TABLE PLAYER.");
		}
	}
	
	// Deletes userName from table PLAYER
	public void deletePlayerFromPlayers(String userName){
		try {
			cs = con.prepareCall("{call DELETE_PLAYER_FROM_PLAYER(?)}");
			cs.setString(1, userName);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}			
	}
	
	// Deletes userName from table TEAM_X_PLAYER
	public void deletePlayerFromTeam(String userName){
		try {
			cs = con.prepareCall("{call DELETE_PLAYER_FROM_TEAM(?)}");
			cs.setString(1, userName);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
	
	// Deletes userName from table PLAYER_X_MARKER
	public void deletePlayerFromMarker(String userName){
		try {
			cs = con.prepareCall("{call DELETE_PLAYER_FROM_MARKER(?)}");
			cs.setString(1, userName);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
	
	
	// Removes Host from everything that makes him a host
	// and sets his isHost to 0
	public void stopHosting(String hostName){
		if(matchNameExists(hostName)){
			setStatusToIdle(hostName);
			deletePlayerFromMarker(hostName);
			setIsHostToZero(hostName);
			resetMatchIDOfPlayers(hostName);
			deletePlayerFromTeam(hostName);
			deleteTeamsOfHost(hostName);
			deleteHostFromTeams(hostName);
			deleteHostFromMatch(hostName);
		}
		else{
			System.out.println("DELETE FAILED. MATCH DOES NOT EXIST IN MATCH TABLE.");
		}
	}

	// Sets isHost to 0
	public void setIsHostToZero(String hostName){
		try {
			cs = con.prepareCall("{call SET_ISHOST_TO_ZERO(?)}");
			cs.setString(1, hostName);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	// Deletes host from table MATCH
	public void deleteHostFromMatch(String hostName){
		try {
			cs = con.prepareCall("{call DELETE_HOST_FROM_MATCH(?)}");
			cs.setString(1, hostName);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}

	// Deletes host from table MATCH_X_TEAM
	public void deleteHostFromTeams(String hostName){
		try {
			cs = con.prepareCall("{call DELETE_HOST_FROM_TEAMS(?)}");
			cs.setString(1, hostName);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	// When deleting host,
	// deletes associated teams from table TEAM
	public void deleteTeamsOfHost(String hostName){
		try {
			cs = con.prepareCall("{call DELETE_TEAMS_OF_HOST(?)}");
			cs.setString(1, hostName);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
	
	// Sets matchID = 0 for all players in the match
	public void resetMatchIDOfPlayers(String hostName){
		try {
			cs = con.prepareCall("{call RESET_MATCHID_OF_PLAYERS(?)}");
			cs.setString(1, hostName);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Removes Player completely
	public void logout(String userName){
		deletePlayerFromMarker(userName); //remove player from playerXmarker
		deletePlayerFromTeam(userName);   //remove player from team
		leaveMatch(userName);             //remove player from match
		deletePlayerFromPlayers(userName);//remove player from player

	}
	
	// Removes the player from match
	// by setting the player's match id to 0
	public void leaveMatch(String userName){
		setStatusToIdle(userName);
		deletePlayerFromMarker(userName);
		deletePlayerFromTeam(userName);
		try {
			cs = con.prepareCall("{call SET_MATCHID_TO_ZERO(?)}");
			cs.setString(1, userName);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}

	// Removes the player from team
	public void leaveTeam(String userName){
		setStatusToIdle(userName);
		deletePlayerFromMarker(userName);
		deletePlayerFromTeam(userName);
	}
	
	// Reduce player health by 1
	public void restoreHealth(String userName){
		try {
			cs = con.prepareCall("{call SET_HEALTH_TO_MAX(?)}");
			cs.setString(1, userName);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}//end class