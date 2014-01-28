package com.bitizen.server;

import java.sql.*;

public class MySQLAccessDemo {
	 public static void main(String args[]){
		 System.out.println("Usage of MySQLAccess Object...");
		 try {
			MySQLAccess dbAccess = new MySQLAccess();

					
			 // Query the database, storing the result
			 // in an object of type ResultSet			
			String query = "SELECT * " + "from player ORDER BY Id";
			
			ResultSet rs = dbAccess.retrieve(query);
			// Loop to display data in the ResultSet
			System.out.println("Display all results in Player table:");
			while(rs.next()){
				int id= rs.getInt("Id");
				String username = rs.getString("Username");
				System.out.println("\tID= " + id
				+ "\tusername = " + username);
			}

			 // Display the data in a specific row using the rs.absolute method
			 System.out.println("Display row 4:");
			 if(rs.absolute(4)){
				int id= rs.getInt("Id");
				String username = rs.getString("Username");
				System.out.println("\tID= " + id 
									+ "\tusername = " + username);
			 }
			
			 
			if(dbAccess.usernameIsTaken("Kashkaddd")){
				System.out.println("taken");
			}
			else{
				System.out.println("available");
				dbAccess.addUsername("Kashkaddd");
			}
			
			rs = dbAccess.retrieveMatches();
			System.out.println("Display all results in Matches table:");
			while(rs.next()){
				String hostName = rs.getString("HostName");
				System.out.println("\thostName = " + hostName);
			}
			
			rs = dbAccess.retrieveTeams();
			System.out.println("Display all results in Matches table:");
			while(rs.next()){
				String teamName = rs.getString("TeamName");
				System.out.println("\tusername = " + teamName);
			}
			
			/* stored proc
			cs = this.con.prepareCall("{call RAISE_PRICE(?,?,?)}");
			cs.setString(1, coffeeNameArg);
			cs.setFloat(2, maximumPercentageArg);
			cs.registerOutParameter(3, Types.NUMERIC);
			cs.setFloat(3, newPriceArg);

			cs.execute();
			*/
			


			 dbAccess.close();
		}catch( Exception e ) {
				e.printStackTrace();
		}

	 }//end main
}//end class