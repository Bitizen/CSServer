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
	
	// Check if the username exists in table `player`
	public Boolean usernameIsTaken(String username){
		try {
			cs = con.prepareCall("{call checkIfUsernameExists(?)}");
			cs.setString(1, username);
			
			if(!cs.executeQuery().next()){
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	// Adds username to table `player` with default values. Called when username not exists
	public void addNewPlayer(String username){
		try {
			cs = con.prepareCall("{call addNewPlayer(?)}");
			cs.setString(1, username);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Retrieves existing match names from table `match`
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
	
	// Returns true if all 6 slots in the match are taken
	public Boolean matchIsFull(String hostName){
		try {
			cs = con.prepareCall("{call returnPlayersInMatch(?)}");
			cs.setString(1, hostName);
			
			ResultSet rs = cs.executeQuery();
			
			if(rs.next()){
				rs.last();
				System.out.println("players in match: " + rs.getRow());
				return rs.getRow() >= MAX_MATCHPLAYERS ? true : false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;	
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
	
	// Returns true if specified team in specified match already has 6 players
	public Boolean teamIsFull(String teamName, String matchName){
		try {
			cs = con.prepareCall("{call returnPlayersInTeam(?,?)}");
			cs.setString(1, teamName);
			cs.setString(2, matchName);
			
			ResultSet rs = cs.executeQuery();
			
			if(rs.next()){
				rs.last();
				System.out.println("players in team: " + rs.getRow());
				return rs.getRow() >= MAX_TEAMPLAYERS ? true : false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
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

	// Returns the players in specified team of specified match
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
	
	// Returns true if all players in the specified match are ready
	public Boolean allPlayersAreReady(String matchName){
		try {
			cs = con.prepareCall("{call returnPlayersInMatch(?)}");
			cs.setString(1, matchName);
			ResultSet rs = cs.executeQuery();
			
			if(rs.next()){
				rs.last();
				
				return (rs.getRow() == getReadyPlayersInMatch(matchName));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;	
	}
	
	// Returns false when a team has 0 players
	public Boolean eachTeamHasOnePlayer(String matchName){
		String[] teamNames = {"A", "B"};
		try {
			
			cs = con.prepareCall("{call returnPlayersInTeam(?,?)}");
			cs.setString(2, matchName);
			
			for(String teamName : teamNames){
				cs.setString(1, teamName);
				ResultSet rs = cs.executeQuery();
				
				//if no rows were retrieved, there are 0 players in that team
				if(!rs.next()){
					return false;
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
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
	 

}//end class