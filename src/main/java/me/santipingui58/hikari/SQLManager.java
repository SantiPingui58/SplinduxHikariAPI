package me.santipingui58.hikari;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;

import com.google.gson.Gson;

import me.santipingui58.splindux.cosmetics.particles.effect.ParticleEffectType;
import me.santipingui58.splindux.cosmetics.particles.type.ParticleTypeSubType;
import me.santipingui58.splindux.game.PlayerOptions;
import me.santipingui58.splindux.game.parkour.ParkourManager;
import me.santipingui58.splindux.game.parkour.ParkourPlayer;
import me.santipingui58.splindux.game.parkour.PlayerStats;
import me.santipingui58.splindux.game.spleef.SpleefPlayer;
import me.santipingui58.splindux.game.spleef.SpleefType;
import me.santipingui58.splindux.relationships.friends.FriendsManager;
import me.santipingui58.splindux.relationships.friends.Friendship;
import me.santipingui58.splindux.stats.PlayersRankingType;
import me.santipingui58.splindux.stats.RankingEnum;
import me.santipingui58.splindux.vote.Rewarded;
import me.santipingui58.splindux.vote.VoteClaims;
import me.santipingui58.translate.Language;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;


public class SQLManager {
	 
    private final ConnectionPoolManager pool;
    
    public SQLManager(Main plugin) {
        pool = new ConnectionPoolManager(plugin);
        createTables();
    }
    
    
    
    public boolean isDataCreated(UUID uuid) {
    	Connection conn = null;
		PreparedStatement ps = null;
		try {
			try {
				conn = pool.getConnection();

			} catch (SQLException e) {
				System.out.println("Error al intentar conectar a la base de datos!");
				e.printStackTrace();
				return true;
			}

			String selectQuery = "SELECT uuid FROM player_data WHERE uuid = ? LIMIT 1";
			ps = conn.prepareStatement(selectQuery);
			ps.setString(1, uuid.toString());
			ResultSet resultSet = ps.executeQuery();
			pool.close(conn, ps, null);
			return resultSet.next();
			
    } catch(Exception ex) {
    	return true;
    }
    }
    
    private void createTables() {
		createPlayerDataTable();
		createParkourDataTable();
		createPlayerStatsTable();
	}
    
    public void createData(UUID uuid) {
   
    	createParkourData(uuid);
    	createPlayerStats(uuid);
    	createPlayerData(uuid);
    }
	
	private void createPlayerDataTable() {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS  player_data  ( uuid  VARCHAR(36),  name  VARCHAR(36),  ip  VARCHAR(36),  register_date  VARCHAR(36),  onlinetime  INT,  last_login  VARCHAR(36),"
            		+ "  coins  INT,  level  INT,  translate  INT,  nightvision  INT,  language  VARCHAR(36),  ads  INT,  color  VARCHAR(36),  join_message  INT, "
            		+ " ranked_arena  VARCHAR(36),  country  VARCHAR(36),  mutation_tokens  INT, rankeds  INT,  particles_type  VARCHAR(36), particles_effect  VARCHAR(36),  namemc  INT,  twitch  INT, "
            		+ " youtube  INT,  twitter  INT, "
            		+ " discord  INT, helmet  VARCHAR(36), permission_level INT);");
            ps.executeUpdate();   
            ps.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
        	pool.close(conn, ps, null);
        }
    }
	
	private void createParkourDataTable() {
		Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn =pool.getConnection();
            ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS  parkour  ( uuid  VARCHAR(36),  current_level  INT,  level1  INT,  level2  INT,  level3  INT,"
            		+ "  level4  INT,  level5  INT,  level6  INT,  level7  INT,  level8  INT,  level9  INT,  level10  INT,  level11  INT,  level12  INT,  level13  INT,  level14  INT, "
            		+ " level15  INT,  level16  INT,  level17  INT,  level18  INT,  level19  INT,  level20  INT,  level21  INT, level22  INT,  level23  INT,  level24  INT, level25  INT, points  INT);");
            ps.executeUpdate();        
            ps.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
        	pool.close(conn, ps, null);
        }
	}
	
	private void createPlayerStatsTable() {
		createStatsTable("SPLEEF");
		createStatsTable("SPLEGG");
		createStatsTable("TNTRUN");
	}
	
	
	private void createStatsTable(String type) {
		
		String table = type.toLowerCase()+"_stats";
		
		Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS  "+table +" ( uuid  VARCHAR(36), elo  INT,  duel_wins  INT,  duel_games  INT,  global_ffa_games  INT,"
            		+ " global_ffa_wins  INT,  global_ffa_kills  INT,  monthly_ffa_games  INT,  monthly_ffa_wins  INT,  monthly_ffa_kills  INT,"
            		+ " weekly_ffa_games  INT,  weekly_ffa_wins  INT,  weekly_ffa_kills  INT)");
            ps.executeUpdate();        
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
        	pool.close(conn, ps, null);
        }
        
	}
	
	public void createPlayerData(UUID uuid) {
		OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
		
		String ip = null;
		try {
			ip = p.getPlayer().getAddress().getAddress().toString();
			ip = ip.replace("/", "");
		} catch(Exception ex) {}
		
		
		 Date now = new Date();
		 SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		String country = getCountry(ip);
		String language = languageFromCountry(country);
		
		Connection conn = null;
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		try {
			try {
				conn = pool.getConnection();

			} catch (SQLException e) {
				System.out.println("Error al intentar conectar a la base de datos!");
				e.printStackTrace();
				return;
			}

			String selectQuery = "SELECT uuid FROM player_data WHERE uuid = ? LIMIT 1";
			ps = conn.prepareStatement(selectQuery);

			ps.setString(1, uuid.toString());
			ResultSet resultSet = ps.executeQuery();
			if (!resultSet.next()) {
				String insertQuery = "INSERT INTO  player_data  ( uuid , name , ip ,  register_date ,  onlinetime ,  last_login ,  coins , level , "
						+ " translate ,  nightvision ,  language ,  ads , color ,  join_message ,  ranked_arena ,  country ,  mutation_tokens ,  rankeds ,"
						+ " particles_type ,  particles_effect ,  namemc ,  twitch ,  youtube ,  twitter , discord , helmet,permission_level,duel_notif, duel_permission, msg_permission) "
						+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
				
				ps2 = conn.prepareStatement(insertQuery);
				ps2.setString(1, uuid.toString()); //uuid
				ps2.setString(2, p.getName());  //name
				ps2.setString(3, ip); //ip
				ps2.setString(4, format.format(now)); //register date
				ps2.setInt(5, 0); // onlinetime
				ps2.setString(6, format.format(now)); //last login
				ps2.setInt(7, 0);  // coins
				ps2.setInt(8, 0); //level
				
				ps2.setInt(9, 1);  //translate
				ps2.setInt(10, 0); //nightvision
				ps2.setString(11, language); //language
				ps2.setInt(12, 1); //ads
				ps2.setString(13, "AQUA"); //color
				ps2.setInt(14, 1); //join msg
				ps2.setString(15, null); //ranked arena
				ps2.setString(16, country); // country
				ps2.setInt(17, 0); //mutation tokens
				ps2.setInt(18, 10); //rankeds
				
				ps2.setString(19, null); //partyicly t
				ps2.setString(20, null); // particle
				ps2.setInt(21, 0); //namemc
				ps2.setInt(22, 0); //twitch
				ps2.setInt(23, 0); //youtube
				ps2.setInt(24, 0); //twitter
				ps2.setInt(25, 0); //discord
				ps2.setString(26, null); //helmet
				ps2.setInt(27, 0); // permission_level
				ps2.setInt(28, 0); // duel  notif
				ps2.setInt(29, 0); // duel perm
				ps2.setInt(30, 0);	 //msg perm

				if (ps2.executeUpdate() <= 0) {
				}
				ps2.close();
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.close(conn, ps, null);
			pool.close(conn, ps2, null);
			loadData(uuid);
		}

	}
	
	
	
	public void createPlayerStats (final UUID uuid) {
		
		new BukkitRunnable() {
			public void run() {
		createPlayerStats(uuid, SpleefType.SPLEEF);
		}
		}.runTaskLaterAsynchronously(Main.get(), 2L);
		
		new BukkitRunnable() {
			public void run() {
				createPlayerStats(uuid, SpleefType.TNTRUN);
		}
		}.runTaskLaterAsynchronously(Main.get(), 2L);
		
		new BukkitRunnable() {
			public void run() {
				createPlayerStats(uuid, SpleefType.SPLEGG);
		}
		}.runTaskLaterAsynchronously(Main.get(), 2L);	
	}
	
	
	
	public void createPlayerStats(UUID uuid,SpleefType type) {		
		
		
		String table = type.equals(SpleefType.SPLEEF) ?  "spleef_stats" : type.equals(SpleefType.SPLEGG) ?  "splegg_stats" : "tntrun_stats";
		
		Connection conn = null;
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		try {
			try {
				conn = pool.getConnection();

			} catch (SQLException e) {
				System.out.println("Error al intentar conectar a la base de datos!");
				e.printStackTrace();
				return;
			}

			String selectQuery = "SELECT uuid FROM "+table+" WHERE uuid = ? LIMIT 1";
			ps = conn.prepareStatement(selectQuery);

			ps.setString(1, uuid.toString());
			ResultSet resultSet = ps.executeQuery();
			if (!resultSet.next()) {
				String insertQuery = "INSERT INTO "+table+  " ( uuid , elo , duel_wins ,  duel_games ,  global_ffa_games ,  global_ffa_wins ,  global_ffa_kills , monthly_ffa_games , "
						+ " monthly_ffa_wins ,  monthly_ffa_kills ,  weekly_ffa_games ,  weekly_ffa_wins , weekly_ffa_kills ) "
						+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?);";
				
				ps2 = conn.prepareStatement(insertQuery);
				ps2.setString(1, uuid.toString());
				ps2.setInt(2, 1000);
				ps2.setInt(3, 0);
				ps2.setInt(4, 0);
				ps2.setInt(5, 0);
				ps2.setInt(6, 0);
				ps2.setInt(7, 0);
				ps2.setInt(8, 0);
				ps2.setInt(9, 0);
				ps2.setInt(10, 0);
				ps2.setInt(11, 0);
				ps2.setInt(12, 0);
				ps2.setInt(13, 0);


				if (ps2.executeUpdate() <= 0) {
				}
				ps2.close();
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.close(conn, ps, null);
			pool.close(conn, ps2, null);
		}

	}
	
	
	
	public void createParkourData(UUID uuid) {		
		Connection conn = null;
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		try {
			try {
				conn = pool.getConnection();

			} catch (SQLException e) {
				System.out.println("Error al intentar conectar a la base de datos!");
				e.printStackTrace();
				return;
			}

			String selectQuery = "SELECT uuid FROM parkour WHERE uuid = ? LIMIT 1";
			ps = conn.prepareStatement(selectQuery);

			ps.setString(1, uuid.toString());
			ResultSet resultSet = ps.executeQuery();
			if (!resultSet.next()) {
				String insertQuery = "INSERT INTO  parkour  ( uuid , current_level , level1 ,  level2 ,  level3 ,  level4 ,  level5 , level6 , "
						+ " level7 ,  level8 ,  level9 ,  level10 , level11 ,  level12 ,  level13 ,  level14 ,  level15 ,  level16 ,"
						+ " level17 ,  level18 ,  level19 ,  level20 ,  level21 ,  level22 , level23 , level24 , level25, points ) "
						+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
				
				ps2 = conn.prepareStatement(insertQuery);
				ps2.setString(1, uuid.toString());
				ps2.setInt(2, 0);
				ps2.setInt(3, 0);
				ps2.setInt(4, 0);
				ps2.setInt(5, 0);
				ps2.setInt(6, 0);
				ps2.setInt(7, 0);
				ps2.setInt(8, 0);
				ps2.setInt(9, 0);
				ps2.setInt(10, 0);
				ps2.setInt(11, 0);
				ps2.setInt(12, 0);
				ps2.setInt(13, 0);
				ps2.setInt(14, 0);
				ps2.setInt(15, 0);
				ps2.setInt(16, 0);
				ps2.setInt(17, 0);
				ps2.setInt(18, 0);
				ps2.setInt(19, 0);
				ps2.setInt(20, 0);
				ps2.setInt(21, 0);
				ps2.setInt(22, 0);
				ps2.setInt(23, 0);
				ps2.setInt(24, 0);
				ps2.setInt(25, 0);
				ps2.setInt(26, 0);
				ps2.setInt(27, 0);
				ps2.setInt(28, 0);
				if (ps2.executeUpdate() <= 0) {
				}
				ps2.close();
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.close(conn, ps, null);
			pool.close(conn, ps2, null);
		}

	}
	

	
	
	public void loadData(UUID uuid) {
		Connection conn = null;
		PreparedStatement ps = null;

		
		//name last login ip
		
		String ip = null;
		Date register_date = null;
		Date lastlogin = null;
		int onlinetime = 0;
		int level = 0;
		boolean translate = false;
		boolean nightvision = false;
		Language language =null;
		boolean ads = false;
		ChatColor color = null;
		boolean join_message = false;
		String ranked_arena = null;
		String country = null;
		int mutation_tokens = 0;
		int rankeds = 0;
		ParticleTypeSubType type = null;
		ParticleEffectType effect = null;
		boolean namemc = false;
		boolean twitch = false;
		boolean youtube = false;
		boolean twitter = false;
		boolean discord = false;
		String helmet = null;
		int coins = 0;
		//int votes = 0;
		int duel_permission = 0;
		int duel_notif = 0;
		int msg_permission = 0;
		
		int current_level =0;
		int level1 = 0;
		int level2=0;
		int level3=0;
		int level4=0;
		int level5=0;
		int level6=0;
		int level7=0;
		int level8=0;
		int level9=0;
		int level10=0;
		int level11 = 0;
		int level12=0;
		int level13=0;
		int level14=0;
		int level15=0;
		int level16=0;
		int level17=0;
		int level18=0;
		int level19=0;
		int level20=0;
		int level21=0;
		int level22=0;
		int level23=0;
		int level24=0;
		int level25=0;
		int votes = 0;
		int points = 0;
		
		try {
			conn = pool.getConnection();
		} catch (SQLException e) {
			System.out.println("Error al intentar conectar a la base de datos!");
			e.printStackTrace();
		}

		ResultSet resultSet = null;

		try {
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder = new StringBuilder();
			queryBuilder.append("SELECT   uuid ,  current_level ,  level1 ,  level2 ,  level3 ,  level4 ,  level5 ,  level6 ,  level7 ,  level8 ,"
					+ "  level9 ,  level10 ,  level11 , level12 , level13 , level14 , level15 , level16 , level17 , level18 , level19 , level20 , level21 , level22 , level23 , level24 , level25, points ");
			queryBuilder.append("FROM `parkour` ");
			queryBuilder.append("WHERE `uuid` = ? ");
			queryBuilder.append("LIMIT 1;");

			ps = conn.prepareStatement(queryBuilder.toString());
			ps.setString(1,uuid.toString());

			resultSet = ps.executeQuery();

			if (resultSet != null && resultSet.next()) {
				
				current_level = resultSet.getInt("current_level");
				level1 = resultSet.getInt("level1");
				level2 = resultSet.getInt("level2");
				level3 = resultSet.getInt("level3");
				level4 = resultSet.getInt("level4");
				level5 = resultSet.getInt("level5");
				level6 = resultSet.getInt("level6");
				level7 = resultSet.getInt("level7");
				level8 = resultSet.getInt("level8");
				level9 = resultSet.getInt("level9");
				level10 = resultSet.getInt("level10");
				level11 = resultSet.getInt("level11");
				level12 = resultSet.getInt("level12");
				level13 = resultSet.getInt("level13");
				level14 = resultSet.getInt("level14");
				level15 = resultSet.getInt("level15");
				level16 = resultSet.getInt("level16");
				level17 = resultSet.getInt("level17");
				level18 = resultSet.getInt("level18");
				level19 = resultSet.getInt("level19");
				level20 = resultSet.getInt("level20");
				level21 = resultSet.getInt("level21");
				level22 = resultSet.getInt("level22");
				level23 = resultSet.getInt("level23");
				level24 = resultSet.getInt("level24");
				level25 = resultSet.getInt("level25");
				points = resultSet.getInt("points");
				
			}

		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} 
	
		
		try {
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder = new StringBuilder();
			queryBuilder.append("SELECT ip, register_date, onlinetime,  coins ,  level ,  translate ,  nightvision ,  language ,  ads ,  color ,  join_message ,  ranked_arena ,"
					+ "  country ,  mutation_tokens ,  rankeds ,  particles_type , particles_effect ,  namemc ,  twitch ,  youtube ,  twitter ,  discord ,  helmet, last_login, votes, duel_notif, duel_permission, msg_permission ");
			queryBuilder.append("FROM `player_data` ");
			queryBuilder.append("WHERE `uuid` = ? ");
			queryBuilder.append("LIMIT 1;");

			ps = conn.prepareStatement(queryBuilder.toString());
			ps.setString(1,uuid.toString());

			resultSet = ps.executeQuery();

			if (resultSet != null && resultSet.next()) {
				try {
					register_date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(resultSet.getString("register_date"));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				
				try {
					lastlogin = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(resultSet.getString("last_login"));
				} catch (Exception  e) {
					lastlogin = new Date();
				}
				
				ip = resultSet.getString("ip");
				coins = resultSet.getInt("coins");
				onlinetime = resultSet.getInt("onlinetime");
				level = resultSet.getInt("level");
				translate = resultSet.getInt("translate")==1 ? true : false;
				nightvision = resultSet.getInt("nightvision")==1 ? true : false;
				language = Language.valueOf(resultSet.getString("language"));
				ads = resultSet.getInt("ads")==1 ? true : false;
				color = ChatColor.valueOf(resultSet.getString("color"));
				join_message = resultSet.getInt("join_message")==1 ? true : false;
				ranked_arena = resultSet.getString("ranked_arena");
				country = resultSet.getString("country");
				votes = resultSet.getInt("votes");
				mutation_tokens = resultSet.getInt("mutation_tokens");
				rankeds = resultSet.getInt("rankeds");
				helmet = resultSet.getString("helmet");
				type = resultSet.getString("particles_type")==null ? null :ParticleTypeSubType.valueOf(resultSet.getString("particles_type"));
				effect = resultSet.getString("particles_effect")==null ? null :ParticleEffectType.valueOf(resultSet.getString("particles_effect"));
				namemc = resultSet.getInt("namemc")==1 ? true : false;
				twitch = resultSet.getInt("twitch")==1 ? true : false;
				twitter = resultSet.getInt("twitter")==1 ? true : false;
				discord = resultSet.getInt("discord")==1 ? true : false;
				youtube = resultSet.getInt("youtube")==1 ? true : false;
				duel_notif = resultSet.getInt("duel_notif");
				duel_permission = resultSet.getInt("duel_permission");
				msg_permission = resultSet.getInt("msg_permission");
			}

		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			pool.close(conn, ps, null);
		}
		
		final SpleefPlayer sp = SpleefPlayer.getSpleefPlayer(uuid);
		sp.setTotalOnlineTIme(onlinetime);
		sp.setCoins(coins);
		sp.setLevel(level);
		sp.setIP(ip);
		sp.setRegisterDate(register_date);
		sp.setMutationTokens(mutation_tokens);
		sp.setHelmet(helmet);
		sp.setTotalVotes(votes);
		if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
		 Date now = new Date();
		sp.setLastLogin(now);
		} else {
			sp.setLastLogin(lastlogin);
		}
		sp.setCountry(country);
		sp.setVoteClaims( new VoteClaims(namemc, discord, twitch, youtube, twitter));
		sp.loadParkourPlayer(current_level, new PlayerStats(level1, level2, level3, level4, level5, level6, level7, level8, level9, level10, level11, level12, level13, level14, level15, 
				level16, level17, level18, level19, level20, level21, level22, level23, level24, level25,points));
		sp.setRankeds(rankeds);
		sp.setOptions(new PlayerOptions(nightvision,translate,language,ads,join_message,color,ranked_arena,duel_permission,duel_notif,msg_permission));
		sp.setParticleEffect(effect);
		sp.setParticleTypeSubType(type);		
		

		
		
		new BukkitRunnable() {
			public void run() {
				loadStats(sp,SpleefType.SPLEEF);
		}
		}.runTaskLaterAsynchronously(Main.get(), 2L);
		
		new BukkitRunnable() {
			public void run() {
				loadStats(sp,SpleefType.SPLEGG);
		}
		}.runTaskLaterAsynchronously(Main.get(), 2L);
		
		new BukkitRunnable() {
			public void run() {
				loadStats(sp,SpleefType.TNTRUN);
		}
		}.runTaskLaterAsynchronously(Main.get(), 2L);	
		
	}
	
	private void loadStats(SpleefPlayer sp,SpleefType type) {
		Connection conn = null;
		PreparedStatement ps = null;
		String table = type.equals(SpleefType.SPLEEF) ?  "spleef_stats" : type.equals(SpleefType.SPLEGG) ?  "splegg_stats" : "tntrun_stats";
		try {
			conn = pool.getConnection();
		} catch (SQLException e) {
			System.out.println("Error al intentar conectar a la base de datos!");
			e.printStackTrace();
		}

		ResultSet resultSet = null;
		
		int elo = 1000;
		int duel_wins = 0;
		int duel_games = 0;
		int global_ffa_games = 0;
		int global_ffa_wins = 0;
		int global_ffa_kills = 0;
		int monthly_ffa_games =0;
		int monthly_ffa_wins = 0;
		int monthly_ffa_kills = 0;
		int weekly_ffa_games =0;
		int weekly_ffa_wins = 0;
		int weekly_ffa_kills =0;
		
		
		try {
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder = new StringBuilder();
			queryBuilder.append("SELECT uuid, elo, duel_wins,  duel_games ,  global_ffa_games ,  global_ffa_wins ,  global_ffa_kills ,  monthly_ffa_games ,  monthly_ffa_wins ,  monthly_ffa_kills ,"
					+ "  weekly_ffa_kills ,  weekly_ffa_wins , weekly_ffa_games ");
			queryBuilder.append("FROM `"+table+"` ");
			queryBuilder.append("WHERE `uuid` = ? ");
			queryBuilder.append("LIMIT 1;");

			ps = conn.prepareStatement(queryBuilder.toString());
			ps.setString(1,sp.getUUID().toString());

			resultSet = ps.executeQuery();

			if (resultSet != null && resultSet.next()) {
				
				elo = resultSet.getInt("elo");
				duel_wins = resultSet.getInt("duel_wins");
				duel_games = resultSet.getInt("duel_games");
				
				global_ffa_games = resultSet.getInt("global_ffa_games");
				global_ffa_wins = resultSet.getInt("global_ffa_wins");
				global_ffa_kills = resultSet.getInt("global_ffa_kills");
				
				monthly_ffa_games = resultSet.getInt("monthly_ffa_games");
				monthly_ffa_wins = resultSet.getInt("monthly_ffa_wins");
				monthly_ffa_kills = resultSet.getInt("monthly_ffa_kills");
				
				weekly_ffa_games = resultSet.getInt("weekly_ffa_games");
				weekly_ffa_wins = resultSet.getInt("weekly_ffa_wins");
				weekly_ffa_kills = resultSet.getInt("weekly_ffa_kills");
				
				sp.getPlayerStats().setELO(type,elo);
				sp.getPlayerStats().setDuelWins(type,duel_wins);
				sp.getPlayerStats().setDuelGames(type,duel_games);
				sp.getPlayerStats().setFFAGames(type,global_ffa_games);
				sp.getPlayerStats().setFFAWins(type,global_ffa_wins);
				sp.getPlayerStats().setFFAKills(type,global_ffa_kills);
				sp.getPlayerStats().setMonthlyFFAGames(type,monthly_ffa_games);
				sp.getPlayerStats().setMonthlyFFAWins(type,monthly_ffa_wins);
				sp.getPlayerStats().setMonthlyFFAKills(type,monthly_ffa_kills);
				sp.getPlayerStats().setWeeklyFFAGames(type,weekly_ffa_games);
				sp.getPlayerStats().setWeeklyFFAWins(type,weekly_ffa_wins);
				sp.getPlayerStats().setWeeklyFFAKills(type,weekly_ffa_kills);
			}

		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			pool.close(conn, ps, resultSet);
		}
		
		
		
	}
	
	public void saveData(SpleefPlayer sp) {
		savePlayerData(sp);
		savePlayerStats(sp);
		savePlayerParkourData(sp);
	}
	
	
	private void savePlayerParkourData(SpleefPlayer sp) {
		Connection conn = null;
		PreparedStatement ps = null;
		StringBuilder queryBuilder = new StringBuilder();
		
		queryBuilder.append("UPDATE  parkour  SET ");
		queryBuilder.append(" current_level  = ?,  level1  = ?,  level2  = ?, ");
		queryBuilder.append(" level3  = ?,  level4  = ?,  level5  = ?,  level6  = ?,  level7  = ?,  level8  = ?,  level9  = ?,  level10  = ?,  level11  = ?"
				+ ",  level12  = ?,  level13  = ?,  level14  = ?,  level15  = ?,  level16  = ?,  level17  = ?,  level18  = ?,  level19  = ?,  level20  = ?,  level21  = ?"
				+ ",  level22  = ?,  level23  = ?,  level24  = ?,  level25  = ?, points = ?");
		queryBuilder.append(" WHERE `uuid` = ?;");

		try {
			conn = pool.getConnection();
		} catch (SQLException e) {
			System.out.println("Error al intentar conectar a la base de datos!");
			e.printStackTrace();
		}
		
		
		ParkourPlayer pp = sp.getParkourPlayer();		
		ParkourManager pm = ParkourManager.getManager();
		try {	
		ps = conn.prepareStatement(queryBuilder.toString());
		ps.setInt(1, pp.getCurrentLevel());
		ps.setInt(2, pp.getRecord(pm.getLevel(1)));
		ps.setInt(3, pp.getRecord(pm.getLevel(2)));
		ps.setInt(4, pp.getRecord(pm.getLevel(3)));
		ps.setInt(5, pp.getRecord(pm.getLevel(4)));
		ps.setInt(6, pp.getRecord(pm.getLevel(5)));
		ps.setInt(7, pp.getRecord(pm.getLevel(6)));
		ps.setInt(8, pp.getRecord(pm.getLevel(7)));
		ps.setInt(9, pp.getRecord(pm.getLevel(8)));
		ps.setInt(10, pp.getRecord(pm.getLevel(9)));
		ps.setInt(11, pp.getRecord(pm.getLevel(10)));
		ps.setInt(12, pp.getRecord(pm.getLevel(11)));
		ps.setInt(13, pp.getRecord(pm.getLevel(12)));
		ps.setInt(14, pp.getRecord(pm.getLevel(13)));
		ps.setInt(15, pp.getRecord(pm.getLevel(14)));
		ps.setInt(16, pp.getRecord(pm.getLevel(15)));
		ps.setInt(17, pp.getRecord(pm.getLevel(16)));
		ps.setInt(18, pp.getRecord(pm.getLevel(17)));
		ps.setInt(19, pp.getRecord(pm.getLevel(18)));
		ps.setInt(20, pp.getRecord(pm.getLevel(19)));
		ps.setInt(21, pp.getRecord(pm.getLevel(20)));
		ps.setInt(22, pp.getRecord(pm.getLevel(21)));
		ps.setInt(23, pp.getRecord(pm.getLevel(22)));
		ps.setInt(24, pp.getRecord(pm.getLevel(23)));
		ps.setInt(25, pp.getRecord(pm.getLevel(24)));
		ps.setInt(26, pp.getRecord(pm.getLevel(25)));
		ps.setInt(27, pp.getStats().getPoints());
		ps.setString(28, sp.getOfflinePlayer().getUniqueId().toString());
	
		ps.executeUpdate();
		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			pool.close(conn, ps, null);
		}
	}
	
	private void savePlayerData(SpleefPlayer sp) {
		
		Connection conn = null;
		PreparedStatement ps = null;
		 SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("UPDATE `player_data` SET ");
		queryBuilder.append(" name  = ?,  ip  = ?, ");
		queryBuilder.append(" onlinetime  = ?,  last_login  = ?,  coins  = ?,  level  = ?,  translate  = ?,  nightvision  = ?,  language  = ?,  ads  = ?,  color  = ?"
				+ ",  join_message  = ?,  ranked_arena  = ?,  country  = ?,  mutation_tokens  = ?,  rankeds  = ?,  particles_type  = ?,  particles_effect  = ?,  namemc  = ?,  twitch  = ?,  youtube  = ?"
				+ ",  twitter  = ?,  discord  = ?, permission_level = ?, helmet = ?, votes = ?, duel_permission = ?, duel_notif = ?, msg_permission = ?");
		queryBuilder.append(" WHERE uuid = ?;");

		try {
			conn = pool.getConnection();
		} catch (SQLException e) {
			System.out.println("Error al intentar conectar a la base de datos!");
			e.printStackTrace();
		}
		
		try {
			ps = conn.prepareStatement(queryBuilder.toString());
		ps.setString(1, sp.getOfflinePlayer().getName());
		ps.setString(2, sp.getIP());
		ps.setInt(3, sp.getTotalOnlineTime());
		String time = null;
		try {
			time = format.format(sp.getLastLogin());
		} catch(Exception e) {}
		
		ps.setString(4, time);
		ps.setInt(5, sp.getCoins());
		ps.setInt(6, sp.getLevel());
		ps.setInt(7, sp.getOptions().hasTranslate() ? 1 : 0);
		ps.setInt(8, sp.getOptions().hasNightVision() ? 1 :0);
		ps.setString(9, sp.getOptions().getLanguage().toString());
		ps.setInt(10, sp.getOptions().hasAds() ? 1 :0);
		ps.setString(11, sp.getOptions().getDefaultColorChat().name());
		ps.setInt(12, sp.getOptions().joinMessageEnabled() ? 1 :0);
		ps.setString(13, sp.getOptions().getRankedArena());
		ps.setString(14, sp.getCountry());
		int mutations = sp.getMutationTokens()>=200 ? 200 : sp.getMutationTokens();
		ps.setInt(15, mutations);
		ps.setInt(16, sp.getRankeds());
		ps.setString(17, sp.getParticleType() == null ? null : sp.getParticleType().toString());
		ps.setString(18, sp.getParticleEffect()==null ? null : sp.getParticleEffect().toString());
		ps.setInt(19, sp.getVoteClaims().hasClaimed(sp, Rewarded.NAMEMC) ? 1 :0);
		ps.setInt(20, sp.getVoteClaims().hasClaimed(sp, Rewarded.TWITCH) ? 1 :0);
		ps.setInt(21, sp.getVoteClaims().hasClaimed(sp, Rewarded.YOUTUBE) ? 1 :0);
		ps.setInt(22, sp.getVoteClaims().hasClaimed(sp, Rewarded.TWITTER) ? 1 :0);
		ps.setInt(23, sp.getVoteClaims().hasClaimed(sp, Rewarded.DISCORD) ? 1 :0);
		PermissionUser s = null; 
		try {
			PermissionsEx.getUser(sp.getOfflinePlayer().getName());
		} catch(Exception ex) {}
		
		int permission_level = s!=null ? s.has("splindux.extreme") ? 3 : s.has("splindux.epic") ? 2 : s.has("splindux.vip") ? 1 : 0 : 0;
		ps.setInt(24, permission_level);
		String helmet = sp.getHelmet()==null ? "" : sp.getHelmet().getId();
		ps.setString(25, helmet);
		ps.setInt(26, sp.getTotalVotes());
		ps.setInt(27, sp.getOptions().getDuelPermission());
		ps.setInt(28, sp.getOptions().getDuelNotification());
		ps.setInt(29, sp.getOptions().getMsgPermission());
		ps.setString(30, sp.getOfflinePlayer().getUniqueId().toString());
		ps.executeUpdate();
		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			pool.close(conn, ps, null);
		}
	}
	
	
	
	@SuppressWarnings("deprecation")
	public HashMap<UUID,List<String>> getAllPlayers() {
		Connection conn = null;
		PreparedStatement ps = null;
		HashMap<UUID,List<String>> list = new HashMap<UUID,List<String>>();
		try {
			conn = pool.getConnection();
		} catch (SQLException e) {
			System.out.println("Error al intentar conectar a la base de datos!");
			e.printStackTrace();
		}

		ResultSet resultSet = null;
		
		try {
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder = new StringBuilder();			
			queryBuilder.append("select uuid,level,last_login from player_data;");
			ps = conn.prepareStatement(queryBuilder.toString());
			resultSet = ps.executeQuery();
			while (resultSet.next()) {
				if (Bukkit.getOfflinePlayer("SantiPingui58").getUniqueId().toString().equalsIgnoreCase(resultSet.getString("uuid"))) {
					Bukkit.getLogger().info("SANTI ESTA ACA");
				}
				
				List<String> lista = new ArrayList<String>();
				lista.add(String.valueOf(resultSet.getInt("level")));
				lista.add(String.valueOf(resultSet.getString("last_login")));
				list.put(UUID.fromString(resultSet.getString("uuid")),lista);
			}

		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			pool.close(conn, ps, null);
		}
		
		
		Bukkit.getLogger().info("ALL PLAYERS LIST" + list.size());
		return list;
	}
	
	
	

	
	private void savePlayerStats(final SpleefPlayer sp) {
		new BukkitRunnable() {
			public void run() {
				saveStats(sp,SpleefType.SPLEEF);
		}
		}.runTaskLaterAsynchronously(Main.get(), 2L);
		
		new BukkitRunnable() {
			public void run() {
				saveStats(sp,SpleefType.SPLEGG);
		}
		}.runTaskLaterAsynchronously(Main.get(), 2L);
		
		new BukkitRunnable() {
			public void run() {
				saveStats(sp,SpleefType.TNTRUN);
		}
		}.runTaskLaterAsynchronously(Main.get(), 2L);
	}
	
	
	private void saveStats(SpleefPlayer sp, SpleefType type) {
		Connection conn = null;
		PreparedStatement ps = null;
		
		String table = type.equals(SpleefType.SPLEEF) ?  "spleef_stats" : type.equals(SpleefType.SPLEGG) ?  "splegg_stats" : "tntrun_stats";
		
		try {
			conn = pool.getConnection();
		} catch (SQLException e) {
			System.out.println("Error al intentar conectar a la base de datos!");
			e.printStackTrace();
		}

		try {
			
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder.append("UPDATE  "+table+" SET ");
			queryBuilder.append(" elo = ?,  duel_wins = ?,  duel_games = ?, ");
			queryBuilder.append(" global_ffa_games = ?,  global_ffa_wins = ?,  global_ffa_kills = ?,  monthly_ffa_games = ?,  monthly_ffa_wins = ?,  monthly_ffa_kills = ?,  weekly_ffa_games = ?,"
					+ "  weekly_ffa_wins = ?,  weekly_ffa_kills = ?");
			queryBuilder.append(" WHERE `uuid` = ?;");

			ps = conn.prepareStatement(queryBuilder.toString());
			ps.setInt(1, sp.getPlayerStats().getELO(type));
			ps.setInt(2, sp.getPlayerStats().getDuelWins(type));
			ps.setInt(3, sp.getPlayerStats().getDuelGames(type));
			ps.setInt(4, sp.getPlayerStats().getFFAGames(type));
			ps.setInt(5, sp.getPlayerStats().getFFAWins(type));
			ps.setInt(6, sp.getPlayerStats().getFFAKills(type));
			ps.setInt(7, sp.getPlayerStats().getMonthlyFFAGames(type));
			ps.setInt(8, sp.getPlayerStats().getMonthlyFFAWins(type));
			ps.setInt(9, sp.getPlayerStats().getMonthlyFFAKills(type));
			ps.setInt(10, sp.getPlayerStats().getWeeklyFFAGames(type));
			ps.setInt(11, sp.getPlayerStats().getWeeklyFFAWins(type));
			ps.setInt(12, sp.getPlayerStats().getWeeklyFFAKills(type));
			ps.setString(13, sp.getOfflinePlayer().getUniqueId().toString());
			ps.executeUpdate();
		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			pool.close(conn, ps, null);
		}
	}


	
	
	public LinkedHashMap<UUID,Integer> getRanking(final RankingEnum rankingType) {
		Connection conn = null;
		PreparedStatement ps = null;
		LinkedHashMap<UUID,Integer> hashmap = new LinkedHashMap<UUID,Integer>();
		try {
			conn = pool.getConnection();
		} catch (SQLException e) {
			System.out.println("Error al intentar conectar a la base de datos!");
			e.printStackTrace();
		}

		ResultSet resultSet = null;
		
		try {
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder = new StringBuilder();
			final String row = rankingType.getRowName();
			final String table =rankingType.getTableName();	
			String where = "";
			if (rankingType.equals(RankingEnum.SPLEEF1VS1_ELO) || rankingType.equals(RankingEnum.SPLEGG1VS1_ELO) || rankingType.equals(RankingEnum.TNTRUN1VS1_ELO)) 
				where = " where duel_games > 0";
			
			queryBuilder.append("select uuid, "+row +" from "+table+ where +" order by "+row+" DESC;");
			
			ps = conn.prepareStatement(queryBuilder.toString());
			resultSet = ps.executeQuery();

			while (resultSet.next()) {
				
			    UUID uuid = UUID.fromString(resultSet.getString("uuid"));
			    try {
			    int values = resultSet.getInt(row);
			    hashmap.put(uuid, values);
			    } catch(Exception ex) {
			    	Bukkit.broadcastMessage(row);
			    }
			}

		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			pool.close(conn, ps, null);
		}
		
		return hashmap;
	}
	
	
	public LinkedHashMap<UUID,Integer> getRanking(final PlayersRankingType rankingType) {
		Connection conn = null;
		PreparedStatement ps = null;
		LinkedHashMap<UUID,Integer> hashmap = new LinkedHashMap<UUID,Integer>();
		try {
			conn = pool.getConnection();
		} catch (SQLException e) {
			System.out.println("Error al intentar conectar a la base de datos!");
			e.printStackTrace();
		}

		ResultSet resultSet = null;
		
		try {
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder = new StringBuilder();
			 String row = null;
			switch (rankingType) {
			case COINS:
				row = "coins"; break;
			case EXP:
				row = "level"; break;
			case VALUE:
				break;
			case VOTES:
				row = "votes";break;
			default:
				break;			
			}
			

			queryBuilder.append("select uuid, "+row +" from player_data order by "+row+" DESC;");
			
			ps = conn.prepareStatement(queryBuilder.toString());
			resultSet = ps.executeQuery();

			while (resultSet.next()) {
				
			    UUID uuid = UUID.fromString(resultSet.getString("uuid"));
			    try {
			    int values = resultSet.getInt(row);
			    hashmap.put(uuid, values);
			    } catch(Exception ex) {
			    	Bukkit.broadcastMessage(row);
			    }
			}

		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			pool.close(conn, ps, null);
		}
		
		return hashmap;
	}
	
	
	

	
	public LinkedHashMap<UUID,Integer> getParkourTop(int level) {
		Connection conn = null;
		PreparedStatement ps = null;
		LinkedHashMap<UUID,Integer> hashmap = new LinkedHashMap<UUID,Integer>();
		try {
			conn = pool.getConnection();
		} catch (SQLException e) {
			System.out.println("Error al intentar conectar a la base de datos!");
			e.printStackTrace();
		}

		ResultSet resultSet = null;
		
		try {
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder = new StringBuilder();
			String row = "level"+level;
			String table = "parkour";
			
			queryBuilder.append("select uuid, "+row +" from "+table+" order by "+row+" DESC;");
			ps = conn.prepareStatement(queryBuilder.toString());
			resultSet = ps.executeQuery();

			while (resultSet.next()) {
				
			    UUID uuid = UUID.fromString(resultSet.getString("uuid"));
			    try {
			    int values = resultSet.getInt(row);
			    hashmap.put(uuid, values);
			    } catch(Exception ex) {
			    	Bukkit.broadcastMessage(row);
			    }
			}

		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			pool.close(conn, ps, null);
		}
		
		return hashmap;
	}
	
	
	
	public void resetELO() {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = pool.getConnection();
		} catch (SQLException e) {
			System.out.println("Error al intentar conectar a la base de datos!");
			e.printStackTrace();
		}

		try {		
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder.append("UPDATE  spleef_stats SET elo = 1000;");
			ps = conn.prepareStatement(queryBuilder.toString());
			ps.executeUpdate();
			
		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			pool.close(conn, ps, null);
		}
		
	}
	
	
	
	public void resetWeekly() {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = pool.getConnection();
		} catch (SQLException e) {
			System.out.println("Error al intentar conectar a la base de datos!");
			e.printStackTrace();
		}

		try {		
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder.append("UPDATE spleef_stats SET weekly_ffa_wins = 0, weekly_ffa_games = 0, weekly_ffa_kills =0;");
			ps = conn.prepareStatement(queryBuilder.toString());
			ps.executeUpdate();
			
		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		}
		
		try {		
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder.append("UPDATE splegg_stats SET weekly_ffa_wins = 0, weekly_ffa_games = 0, weekly_ffa_kills =0;");
			ps = conn.prepareStatement(queryBuilder.toString());
			ps.executeUpdate();
			
		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} 
		
		try {		
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder.append("UPDATE tntrun_stats SET weekly_ffa_wins = 0, weekly_ffa_games = 0, weekly_ffa_kills =0;");
			ps = conn.prepareStatement(queryBuilder.toString());
			ps.executeUpdate();
			
		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		}	finally {
			pool.close(conn, ps, null);
		}
		
	}
	
	public void resetMonthly() {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = pool.getConnection();
		} catch (SQLException e) {
			System.out.println("Error al intentar conectar a la base de datos!");
			e.printStackTrace();
		}

		try {		
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder.append("UPDATE  spleef_stats SET monthly_ffa_wins = 0, monthly_ffa_games = 0, monthly_ffa_kills =0;");
			ps = conn.prepareStatement(queryBuilder.toString());
			ps.executeUpdate();
			
		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		}
		
		try {		
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder.append("UPDATE  splegg_stats SET monthly_ffa_wins = 0, monthly_ffa_games = 0, monthly_ffa_kills =0;");
			ps = conn.prepareStatement(queryBuilder.toString());
			ps.executeUpdate();
			
		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} 
		
		try {		
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder.append("UPDATE  tntrun_stats SET monthly_ffa_wins = 0, monthly_ffa_games = 0, monthly_ffa_kills =0;");
			ps = conn.prepareStatement(queryBuilder.toString());
			ps.executeUpdate();
			
		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		}	finally {
			pool.close(conn, ps, null);
		}
		
	}
	
	public void giveMutations() {
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = pool.getConnection();
		} catch (SQLException e) {
			System.out.println("Error al intentar conectar a la base de datos!");
			e.printStackTrace();
		}
		
		try {		
			ps = conn.prepareStatement("UPDATE player_data SET mutation_tokens = mutation_tokens + 5 WHERE permission_level = 3;");
			ps.executeUpdate();
			
		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} 
		
		try {		
			ps = conn.prepareStatement("UPDATE player_data SET mutation_tokens = mutation_tokens + 3 WHERE permission_level = 2;");
			ps.executeUpdate();
			
		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} 
		
		try {		
			ps = conn.prepareStatement("UPDATE player_data SET mutation_tokens = mutation_tokens + 1 WHERE permission_level = 1;");
			ps.executeUpdate();
			
		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			pool.close(conn, ps, null);
		}
	}
	
	
	public void giveRankeds() {
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = pool.getConnection();
		} catch (SQLException e) {
			System.out.println("Error al intentar conectar a la base de datos!");
			e.printStackTrace();
		}
		
		try {		
			ps = conn.prepareStatement("UPDATE player_data SET rankeds = 25 WHERE permission_level = 3;");
			ps.executeUpdate();
			
		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} 
		
		try {		
			ps = conn.prepareStatement("UPDATE player_data SET rankeds = 20 WHERE permission_level = 2;");
			ps.executeUpdate();
			
		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} 
		
		try {		
			ps = conn.prepareStatement("UPDATE player_data SET rankeds = 10 WHERE permission_level = 0;");
			ps.executeUpdate();
			
		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} 
		
		try {		
			ps = conn.prepareStatement("UPDATE player_data  SET rankeds = 15 WHERE permission_level = 1;");
			//ps = conn.prepareStatement("UPDATE player_stats INNER JOIN player_dataON player_stats.uuid = player_data.uuid SET rankeds = 15 WHERE permission_level = 1");
			ps.executeUpdate();
			
		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			pool.close(conn, ps, null);
		}
	}
	
	
	
	
	public String languageFromCountry(String s) {
		if (s==null) return "ENGLISH";
		if (s.equalsIgnoreCase("AR") 
			|| s.equalsIgnoreCase("ES")
			|| s.equalsIgnoreCase("BO")
			|| s.equalsIgnoreCase("BR")
			|| s.equalsIgnoreCase("CL")
			|| s.equalsIgnoreCase("UY")
			|| s.equalsIgnoreCase("PY")
			|| s.equalsIgnoreCase("CO")
			|| s.equalsIgnoreCase("PE")
			|| s.equalsIgnoreCase("VE")
			|| s.equalsIgnoreCase("MX")
			|| s.equalsIgnoreCase("CU")
			|| s.equalsIgnoreCase("EC")
			|| s.equalsIgnoreCase("GT")
			|| s.equalsIgnoreCase("HN")) {
			return "SPANISH";
		} else if (s.equalsIgnoreCase("RU")
				|| s.equalsIgnoreCase("BY")
				|| s.equalsIgnoreCase("KZ")
				|| s.equalsIgnoreCase("KG")
				|| s.equalsIgnoreCase("UZ")){
			return "RUSSIAN";
		} else {
			return "ENGLISH";
		}
	}
	
	public String getCountry (String ip) {
		if (ip==null) return null;
		 String jsonS = "";
	        URL url = null;
			try {
				 ip = ip.replace("/", "");
				if (ip.equalsIgnoreCase("127.0.0.1") || ip.contains("192.168.0")) {
					return "AR";
				}
				url = new URL("http://ip-api.com/json/" + ip+ "?fields=message,countryCode");
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
				return null;
			}
	        URLConnection conn = null;
			try {
				conn = url.openConnection();
			} catch (IOException e1) {
				e1.printStackTrace();
				return null;
			}
	        try {
				conn.connect();
			} catch (IOException e1) {
				e1.printStackTrace();
				return null;
			}
	        BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			} catch (IOException e1) {
				e1.printStackTrace();
				return null;
			}
	        String inputLine;

	        try {
				while((inputLine = in.readLine()) != null) {
				    jsonS+=inputLine;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
	        
			JSONObject json = new Gson().fromJson(jsonS, JSONObject.class);		
			return json.get("countryCode").toString();

	}
    
    public void onDisable() {
        pool.closePool();
    }



	public void loadFriends(UUID uuid) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = pool.getConnection();
		} catch (SQLException e) {
			System.out.println("Error al intentar conectar a la base de datos!");
			e.printStackTrace();
		}

		ResultSet resultSet = null;
		
		try {
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder = new StringBuilder();
			queryBuilder.append("SELECT friend1, friend2 ");
			queryBuilder.append("FROM `friends` ");
			queryBuilder.append("WHERE `friend1` = ? OR `friend2` = ? ");

			ps = conn.prepareStatement(queryBuilder.toString());
			ps.setString(1,uuid.toString());
			ps.setString(2,uuid.toString());
			
			resultSet = ps.executeQuery();
			while (resultSet.next()) {
				UUID friend1 = UUID.fromString(resultSet.getString("friend1"));
				UUID friend2 = UUID.fromString(resultSet.getString("friend2"));
				Friendship friendship=	FriendsManager.getManager().getFriendship(friend1, friend2);
				if (friendship==null) new Friendship(friend1,friend2);
			}
		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			pool.close(conn, ps, null);
		}
		
		
	}



	 public void saveFriend(Friendship fr) {
			
			Connection conn = null;
			PreparedStatement ps = null;
			try {
				try {
					conn = pool.getConnection();

				} catch (SQLException e) {
					System.out.println("Error al intentar conectar a la base de datos!");
					e.printStackTrace();
					return;
				}

					String insertQuery = "INSERT INTO  friends  (friend1, friend2) VALUES(?, ?);";
					
					ps = conn.prepareStatement(insertQuery);
					ps.setString(1, fr.getPlayer1().toString());
					ps.setString(2, fr.getPlayer2().toString());

					if (ps.executeUpdate() <= 0) {
					}
					ps.close();	
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				pool.close(conn, ps, null);
			}

	}
	  
	 public void deleteFriend(Friendship fr) {
			Connection conn = null;
			PreparedStatement ps = null;
			try {
				try {
					conn = pool.getConnection();

				} catch (SQLException e) {
					System.out.println("Error al intentar conectar a la base de datos!");
					e.printStackTrace();
					return;
				}

					String deleteQuerey = "DELETE from friends where friend1 ='" +fr.getPlayer1().toString() +"' AND friend2='" + fr.getPlayer2().toString()+"';";  
					
					ps = conn.prepareStatement(deleteQuerey);

					if (ps.executeUpdate() <= 0) {
					}
					ps.close();	
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				pool.close(conn, ps, null);
			}
	 }
	 
	 
	 public void executeSQL(String string) {
		 Connection conn = null;
			PreparedStatement ps = null;
			try {
				try {
					conn = pool.getConnection();

				} catch (SQLException e) {
					System.out.println("Error al intentar conectar a la base de datos!");
					e.printStackTrace();
					return;
				}

					ps = conn.prepareStatement(string);

					if (ps.executeUpdate() <= 0) {
					}
					ps.close();	
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				pool.close(conn, ps, null);
			}
	 }
	 
	 public void eloDecay(SpleefType spleefType) {
			Connection conn = null;
			PreparedStatement ps = null;
			try {
				conn = pool.getConnection();
			} catch (SQLException e) {
				System.out.println("Error al intentar conectar a la base de datos!");
				e.printStackTrace();
			}

			String table = "";
			
			switch (spleefType) {
			case SPLEEF:
				table = "spleef_stats";
				break;
			case SPLEGG:
				table = "splegg_stats";
				break;
			case TNTRUN:
				table = "tntrun_stats";
				break;
			default:
				break;
			}
			
			
			try {		
				StringBuilder queryBuilder = new StringBuilder();
				queryBuilder.append("UPDATE  "+table+" SET elo = elo-(elo/100) where elo>1000; update "+table+" SET elo = elo+(elo/100) where elo<1000;");
				ps = conn.prepareStatement(queryBuilder.toString());
				ps.executeUpdate();
				
			} catch (final SQLException sqlException) {
				sqlException.printStackTrace();
			} finally {
				pool.close(conn, ps, null);
			}
			
	 }



		public void updateSWWSRanking(String code,LinkedHashMap<UUID,Integer> hashmap) {
			Connection conn = null;
			PreparedStatement ps = null;
			
			try {
				conn = pool.getConnection();
			} catch (SQLException e) {
				System.out.println("Error al intentar conectar a la base de datos!");
				e.printStackTrace();
			}

			try {
				  StringBuilder queryBuilder = new StringBuilder();
				for (Entry<UUID, Integer> entry : hashmap.entrySet()) {
				    UUID key = entry.getKey();
				    int value = entry.getValue();
				  
					queryBuilder.append("INSERT INTO sws (uuid,points,country) VALUES ('"+key.toString()+"','"+value+"', '"+code+"') "
							+ "ON DUPLICATE KEY UPDATE uuid='"+key.toString()+"', points='"+entry.getValue()+"', country ='"+code+"'; ");
				}
				String s = queryBuilder.toString();
				if (!s.isEmpty()) {
				ps = conn.prepareStatement(s);
				ps.executeUpdate();
				}
			} catch (final SQLException sqlException) {
				sqlException.printStackTrace();
			} finally {
				pool.close(conn, ps, null);
			}
		}
		

		
		public LinkedHashMap<UUID,Integer> getSWSData(String code) {
			Connection conn = null;
			PreparedStatement ps = null;
			LinkedHashMap<UUID,Integer> hashmap = new LinkedHashMap<UUID,Integer>();
			try {
				conn = pool.getConnection();
			} catch (SQLException e) {
				System.out.println("Error al intentar conectar a la base de datos!");
				e.printStackTrace();
			}

			ResultSet resultSet = null;
			
			try {
				StringBuilder queryBuilder = new StringBuilder();
				queryBuilder = new StringBuilder();				
				queryBuilder.append("select uuid, points from sws where country='"+code+"' order by points DESC;");
				ps = conn.prepareStatement(queryBuilder.toString());
				resultSet = ps.executeQuery();

				while (resultSet.next()) {
				    UUID uuid = UUID.fromString(resultSet.getString("uuid"));
				    int values = resultSet.getInt("points");
				    hashmap.put(uuid, values);
				    }
			

			} catch (final SQLException sqlException) {
				sqlException.printStackTrace();
			} finally {
				pool.close(conn, ps, null);
			}
			
			return hashmap;
		}
	  
}




 