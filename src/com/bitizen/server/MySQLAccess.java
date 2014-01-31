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
	
	public ResultSet retrieveTeamNames(){
		try {
			cs = con.prepareCall("{call returnTeamNames()}");
			rs = cs.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return rs;		
	}	
	
	public ResultSet retrievePlayersInTeam(String teamName){
		try {
			cs = con.prepareCall("{call returnPlayersInATeam(?)}");
			cs.setString(1, teamName);
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
			cs.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Boolean allPlayersAreReady(String matchName){
		try {
			cs = con.prepareCall("{call allPlayersAreReady(?)}");
			cs.setString(1, matchName);
			
			if(cs.executeQuery().absolute(1)){
				return cs.getBoolean("aBooleanResult");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
		
	}
	
	public Boolean usernameIsTaken(String username){
		try {
			cs = con.prepareCall("{call checkIfUsernameAvailable(?)}");
			cs.setString(1, username);
			if(cs.executeQuery().absolute(1)){
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public Boolean matchIsFull(String hostName){
		try {
			cs = con.prepareCall("{call countPlayersInMatch(?)}");
			cs.setString(1, hostName);
			if(cs.executeQuery().absolute(1)){
				if(cs.getInt("PLAYER_COUNT") >= MAX_MATCHPLAYERS){
					return true;
				}
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;	
	}
	
	public Boolean teamIsFull(String teamName){
		try {
			cs = con.prepareCall("{call checkIfTeamAvailable(?)}");
			cs.setString(1, teamName);
			
			if(cs.executeQuery().absolute(1)){
				if(cs.getInt("Result") >= MAX_TEAMPLAYERS){
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	
	public void addUsername(String username){
		try {
			cs = con.prepareCall("{call addUsernameToCSDB(?)}");
			cs.setString(1, username);
			cs.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void hostMatch(String matchName){
		try {
			cs = con.prepareCall("{call addHostToCSDB(?)}");
			cs.setString(1, matchName);
			cs.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void joinMatch(String userName, String hostName){
		try {
			cs = con.prepareCall("{call acceptMatchChosen(?,?)}");
			cs.setString(1, userName);
			cs.setString(2, hostName);
			cs.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	public void joinTeam(String userName, String teamName){
		try {
			cs = con.prepareCall("{call acceptTeamChosen(?,?)}");
			cs.setString(1, userName);
			cs.setString(2, teamName);
			cs.execute();
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