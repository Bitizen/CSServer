package com.bitizen.server;


public class MySQLAccessDemo {
	 public static void main(String args[]){
		 System.out.println("Usage of MySQLAccess Object...");
		 try {
			MySQLAccess dbAccess = new MySQLAccess();
			dbAccess.register();
			dbAccess.connect();
			dbAccess.createStatement();

			//dbAccess.markerIsTaken(1, "kash098");
			//dbAccess.deletePlayer("dice098");
			//dbAccess.deletePlayer("dicer");
			//dbAccess.deletePlayer("dicey");
			//dbAccess.deleteHost("kash098");
			//dbAccess.deletePlayer("kash098");
			//dbAccess.deleteHost("kashr");
			
			
			/*
			System.out.println("Team is full? " +  dbAccess.teamIsFull("a", "kashka"));
			System.out.println("All players in match are ready? " + dbAccess.allPlayersAreReady("kashka"));
			System.out.println(dbAccess.eachTeamHasOnePlayer("kashka"));
			*/
			
			 dbAccess.close();
		}
		 catch( Exception e ) {
				e.printStackTrace();
				e.getMessage();
		}

	 }//end main
}//end class