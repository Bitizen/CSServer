package com.bitizen.server;


public class MySQLAccessDemo {
	 public static void main(String args[]){
		 System.out.println("Usage of MySQLAccess Object...");
		 try {
			MySQLAccess dbAccess = new MySQLAccess();
			dbAccess.register();
			dbAccess.connect();
			dbAccess.createStatement();
			
			System.out.println("teamisfull: " +  dbAccess.teamIsFull("a", "kashka"));
			System.out.println("all players are ready: " + dbAccess.allPlayersAreReady("kashka"));
			
			 dbAccess.close();
		}catch( Exception e ) {
				e.printStackTrace();
				e.getMessage();
		}

	 }//end main
}//end class