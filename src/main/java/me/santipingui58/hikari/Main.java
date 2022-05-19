package me.santipingui58.hikari;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	
	   private SQLManager sql;
	   private static Main pl;
	   
	@Override
	public void onEnable() {
		pl = this;
		 initDatabase();
	}
	
	@Override 
	public void onDisable() {
		 sql.onDisable();
	}
	
	
	
	 private void initDatabase() {
	        sql = new SQLManager(this);
	    }
	
	 
	 public SQLManager getSQLManager() {
	        return sql;
	    }
	
		public static Main get() {
		    return pl;
		  }	
}
