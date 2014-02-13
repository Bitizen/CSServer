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
	
	
	public MySQLAccess(){
		register();
		connect();
	}
	
	public void register(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void connect(){
		try {
			con = DriverManager.getConnection(DB_URL,"root", "");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void createStatement(){
		try {
			stmt = con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
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
	
	public void addNewPlayer(String username){
		try {
			cs = con.prepareCall("{call addNewPlayer(?)}");
			cs.setString(1, username);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
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
	
	public Boolean matchIsFull(String hostName){
		try {
			cs = con.prepareCall("{call countPlayersInMatch(?)}");
			cs.setString(1, hostName);
			if(cs.executeQuery().next() && cs.getInt("PLAYER_COUNT") >= MAX_MATCHPLAYERS){
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;	
	}
	
	
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
	
	
	public Boolean teamIsFull(String teamName, String matchName){
		try {
			cs = con.prepareCall("{call returnNumberOfPlayersInTeam(?,?)}");
			cs.setString(1, teamName);
			cs.setString(2, matchName);
			
			ResultSet rs = cs.executeQuery();
			
			if(rs.next()){
				rs.last();

				return (rs.getRow() < MAX_TEAMPLAYERS);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	
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

	public ResultSet returnPlayersInTeam(String teamName, String matchName){
		try {
			cs = con.prepareCall("{call returnPlayersInATeam(?,?)}");
			cs.setString(1, teamName);
			cs.setString(2, matchName);
			rs = cs.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return rs;		
	}	
	

	public void setStatusToReady(String username){
		try {
			cs = con.prepareCall("{call setStatusToReady(?)}");
			cs.setString(1, username);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void setStatusToIdle(String username){
		try {
			cs = con.prepareCall("{call setStatusToIdle(?)}");
			cs.setString(1, username);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Boolean allPlayersAreReady(String matchName){
		try {
			cs = con.prepareCall("{call returnNumberOfPlayersInMatch(?)}");
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
	
	
	public Boolean sampleProc(String matchName){
		try {
			cs = con.prepareCall("{call returnNumberOfPlayersInMatch(?)}");
			cs.setString(1, matchName);
			

				if(cs.getInt("PLAYER_COUNT") >= MAX_TEAMPLAYERS){
					return true;
				}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
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