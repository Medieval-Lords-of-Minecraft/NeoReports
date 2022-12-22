package me.neoblade298.neoreports;

import java.sql.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class NeoReports extends JavaPlugin implements org.bukkit.event.Listener {
	
	static int nextReport;
	static int numBugs;
	static int numUrgent;
	static int numResolved;
	static String sqlUser = "neoblade298";
	static String sqlPass = "7H56480g09&Z01pz";
	static String connection = "jdbc:mysql://66.70.180.136:3306/MLMC?useSSL=false";
	private static NeoReports inst;
	
	public void onEnable() {
		Bukkit.getServer().getLogger().info("NeoReports Enabled");
		getServer().getPluginManager().registerEvents(this, this);

		// Initialize static vars
		try{  
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection(connection, sqlUser, sqlPass);
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("select MAX(id) from neoreports_bugs");
			while (rs.next()) {
				nextReport = rs.getInt(1) + 1;
			}
			rs = stmt.executeQuery("select COUNT(*) from neoreports_bugs WHERE is_urgent = 0 AND is_resolved = 0");
			while (rs.next()) {
				numBugs = rs.getInt(1);
			}
			rs = stmt.executeQuery("select COUNT(*) from neoreports_bugs WHERE is_urgent = 1 AND is_resolved = 0");
			while (rs.next()) {
				numUrgent = rs.getInt(1);
			}
			numResolved = 0;
			con.close();
		}
		catch(Exception e) {
			getServer().getPluginManager().disablePlugin(this);
			e.printStackTrace();
		}  
	    
	    // Get command listener
	    this.getCommand("neoreport").setExecutor(new ReportCommand(this));
	    this.getCommand("neoreports").setExecutor(new ReportsCommand(this));
	    inst = this;
	}
	
	public void onDisable() {
	    org.bukkit.Bukkit.getServer().getLogger().info("NeoReports Disabled");
	    super.onDisable();
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (p.hasPermission("neoreports.admin")) {
	    	BukkitRunnable joinTask = new BukkitRunnable() {
				public void run() {
					try{  
						Class.forName("com.mysql.jdbc.Driver");
						Connection con = DriverManager.getConnection(NeoReports.connection, NeoReports.sqlUser, NeoReports.sqlPass);
						Statement stmt = con.createStatement();
						ResultSet rs;
						rs = stmt.executeQuery("SELECT COUNT(*) FROM neoreports_bugs WHERE is_resolved = 0 AND is_urgent = 0;");
						rs.next();
						int numBugs = rs.getInt(1);
						rs = stmt.executeQuery("SELECT COUNT(*) FROM neoreports_bugs WHERE is_resolved = 0 AND is_urgent = 1;");
						rs.next();
						int numUrgent = rs.getInt(1);
						p.sendMessage("§4[§c§lMLMC§4] §7# Bugs: §e" + numBugs + "§7, # Urgent: §e" + numUrgent + "§7, # Resolved today: §e" + NeoReports.numResolved);
						con.close();
						if (numUrgent > 0) {
							// Double check that the number is urgent
							p.sendMessage("§4[§c§lMLMC§4] §c§lThere are unfixed urgent bugs!");
						}
					}
					catch(Exception e) {
						e.printStackTrace();
					}

				}
			};
			joinTask.runTaskLater(this, 100L);
		}
	}
	
	public static NeoReports inst() {
		return inst;
	}
	
}
