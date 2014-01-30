package com.bitizen.server;

import java.sql.*;

public class MySQLAccessDemo {
	 public static void main(String args[]){
		 System.out.println("Usage of MySQLAccess Object...");
		 try {
			MySQLAccess dbAccess = new MySQLAccess();
			dbAccess.register();
			dbAccess.connect();
			dbAccess.createStatement();
			
			ResultSet rs = dbAccess.retrieveMatches();
			while (rs.next()) {    
				
					System.out.println("*" + rs.getString(1));
	
			}
	
		
			

			 dbAccess.close();
		}catch( Exception e ) {
				e.printStackTrace();
				e.getMessage();
		}

	 }//end main
}//end class