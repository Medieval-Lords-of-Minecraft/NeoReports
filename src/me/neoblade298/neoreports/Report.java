package me.neoblade298.neoreports;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Report {
	private int id;
	private String date, user, description, comment, resolver, fixdate;
	private boolean is_resolved, is_urgent;
	private static DateFormat dateformat = new SimpleDateFormat("MM-dd HH:mm:ss");
	
	public Report(int id, String user, String description, boolean is_urgent) {
		this.id = id;
		this.date = dateformat.format(new Date());
		this.user = user;
		this.description = description;
		this.comment = "none";
		this.resolver = "none";
		this.fixdate = "none";
		this.is_resolved = false;
		this.is_urgent = is_urgent;
	}
	
	public Report(int id, String date, String user, String description, String resolver, String comment, String fixdate, boolean is_resolved, boolean is_urgent) {
		this.id = id;
		this.date = date;
		this.user = user;
		this.description = description;
		this.comment = comment;
		this.resolver = resolver;
		this.fixdate = fixdate;
		this.is_resolved = is_resolved;
		this.is_urgent = is_urgent;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	public void setDescription(String description) {
		this.description = description.replaceAll("'", "\\\\'");
	}
	
	public void setComment(String comment) {
		this.comment = comment.replaceAll("'", "\\\\'");
	}
	
	public void setResolver(String resolver) {
		this.resolver = resolver;
	}
	
	public void setResolved(boolean resolved) {
		this.is_resolved = resolved;
	}
	
	public void setUrgency(boolean urgency) {
		this.is_urgent = urgency;
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getDate() {
		return this.date;
	}
	
	public String getUser() {
		return this.user;
	}
	
	public String getDescription() {
		return this.description.replaceAll("\\\\'", "'");
	}
	
	public String getComment() {
		return this.comment.replaceAll("\\\\'", "'");
	}
	
	public String getResolver() {
		return this.resolver;
	}
	
	public boolean isResolved() {
		return this.is_resolved;
	}
	
	public boolean isUrgent() {
		return this.is_urgent;
	}
	
	public void list(CommandSender p) {
		String prefix = "§4";
		if (is_urgent) {
			prefix = "§4§l";
		}
		if (is_resolved) {
			p.sendMessage(prefix + "#" + id + ": §c" + user + " " + date + "§7 - " + getDescription());
			p.sendMessage("§c" + resolver + " " + fixdate + "§7 - " + getComment());
		}
		else {
			p.sendMessage(prefix + "#" + id + ": §c" + user + " " + date + "§7 - " + getDescription());
		}
	}
	
	public void post(CommandSender p) {
		int resolved = 0, urgent = 0;
		if (is_resolved) {
			resolved = 1;
		}
		if (is_urgent) {
			urgent = 1;
		}
		try{  
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
			Statement stmt = con.createStatement();
			int post = stmt.executeUpdate("INSERT INTO neoreports_bugs VALUES (" + id + ",'" + date + "','" + user + "','" + description +
					"','" + comment + "','" + resolver + "','" + fixdate + "','" +  resolved + "','" + urgent +"')");
			if (post > 0) {
				p.sendMessage("§4[§c§lMLMC§4] §7Successfully posted report!");
				Main.nextReport++;
				if(is_urgent) {
					Main.numUrgent++;
					for (Player staff : Bukkit.getOnlinePlayers()) {
						if (staff.hasPermission("neoreports.admin")) {
							staff.sendMessage("§4[§c§lMLMC§4] §c§lAn urgent bug report was just posted!");
						}
					}
				}
				else {
					Main.numBugs++;
				}
			}
			else {
				p.sendMessage("§4[§c§lMLMC§4] §7Failed to post report!");
			}
			con.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			p.sendMessage("§4[§c§lMLMC§4] §cSomething went wrong! Report to neo and don't use the plugin anymore!");
		}
	}
}
