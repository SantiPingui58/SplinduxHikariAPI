package me.santipingui58.hikari;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import me.santipingui58.splindux.game.spleef.SpleefPlayer;
import me.santipingui58.splindux.game.spleef.SpleefType;
import me.santipingui58.splindux.relationships.friends.Friendship;
import me.santipingui58.splindux.stats.PlayersRankingType;
import me.santipingui58.splindux.stats.RankingEnum;

public class HikariAPI {

	private static HikariAPI manager;	
	 public static HikariAPI getManager() {
	        if (manager == null)
	        	manager = new HikariAPI();
	        return manager;
	    }
	 
	 public void createData(UUID uuid) {
		  Main.get().getSQLManager().createData(uuid);
	 }
	 
	 public void loadData(UUID uuid) {
		  Main.get().getSQLManager().loadData(uuid);
	 }
	 
	 public void loadFriends(UUID uuid) {
		  Main.get().getSQLManager().loadFriends(uuid);
	 }
	 
	 public void saveFriend(Friendship fr) {
		  Main.get().getSQLManager().saveFriend(fr);
	 }
	 public void deleteFriend(Friendship fr) {
		  Main.get().getSQLManager().deleteFriend(fr);
	 }
	 
	 
	 public void saveData(SpleefPlayer sp) {
		 Main.get().getSQLManager().saveData(sp);
	 }
	 
	 
	 public void eloDecay() {
		 Main.get().getSQLManager().eloDecay(SpleefType.SPLEEF);
		 Main.get().getSQLManager().eloDecay(SpleefType.SPLEGG);
		 Main.get().getSQLManager().eloDecay(SpleefType.TNTRUN);
	 }
	 
	 public boolean isDataCreated(UUID uuid) {
		 return Main.get().getSQLManager().isDataCreated(uuid);
	 }
	 
	 
		public HashMap<UUID, Integer> getRanking(RankingEnum rankingType) {
			return Main.get().getSQLManager().getRanking(rankingType);
		}
		
		public HashMap<UUID, Integer> getRanking(PlayersRankingType rankingType) {
			return Main.get().getSQLManager().getRanking(rankingType);
		}
		
		public HashMap<UUID, Integer> getParkourRanking(int i) {
			return Main.get().getSQLManager().getParkourTop(i);
		}
		
		public void resetMonthly() {
			Main.get().getSQLManager().resetMonthly();
		}
		
		public void resetWeekly() {
			Main.get().getSQLManager().resetWeekly();
		}
		
		public void giveMutations() {
			Main.get().getSQLManager().giveMutations();
		}
		
		public void giveRankeds() {
			Main.get().getSQLManager().giveRankeds();
		}
		
		public void resetELO() {
			Main.get().getSQLManager().resetELO();
		}
		
		public HashMap<UUID,List<String>> getAllPlayers() {
			return Main.get().getSQLManager().getAllPlayers();
		}
		
		
		public void executeSQL(String string) {
			Main.get().getSQLManager().executeSQL(string);
		}
			
		
		public void updateSWWSData(String country,LinkedHashMap<UUID,Integer> ranking) {
			Main.get().getSQLManager().updateSWWSRanking(country,ranking);
		}
		
		public LinkedHashMap<UUID,Integer> getSWSData(String code) {
			return Main.get().getSQLManager().getSWSData(code);
		}
		
		
}
