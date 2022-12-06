package me.neoblade298.neoreports;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.neoblade298.neoreports.Main;

public class ReportsCommand implements CommandExecutor {
	Main main;
	static int NUM_REPORTS_PER_PAGE = 10;
	private static DateFormat dateformat = new SimpleDateFormat("MM-dd");
	
	public ReportsCommand(Main main) {
		this.main = main;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if (sender.hasPermission("neoreports.user")) {
			String author = sender.getName();
			if (args.length == 0) {
				sender.sendMessage("§7--- §cNeoReports §7---");
				sender.sendMessage("§c/report bug [description] §7- Reports a bug to the staff");
				sender.sendMessage("§c/report urgent [description] §7- Reports an urgent bug to the staff, use for time-sensitive issues!");
				sender.sendMessage("§c/reports list §7- Lists all reports made by you");
				sender.sendMessage("§4/reports check §7- Summary of existing bugs");
				if (sender.hasPermission("neoreports.admin")) {
					sender.sendMessage("§4/reports list [bug/urgent/resolved] <pg #> §7- Lists all bugs of a certain type");
					sender.sendMessage("§4/reports resolve [bug id] [comment] <pg #> §7- Resolves a bug, marking it with the comment");
					sender.sendMessage("§4/reports edit [bug id] [comment] §7- Edits an existing comment (only for resolved bugs)");
				}
				return true;
			}
			else if (args.length == 1 && args[0].equalsIgnoreCase("check")) {
				try {  
					Class.forName("com.mysql.jdbc.Driver");
					Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
					Statement stmt = con.createStatement();
					ResultSet rs;
					rs = stmt.executeQuery("SELECT COUNT(*) FROM neoreports_bugs WHERE is_resolved = 0 AND is_urgent = 0;");
					rs.next();
					int numBugs = rs.getInt(1);
					rs = stmt.executeQuery("SELECT COUNT(*) FROM neoreports_bugs WHERE is_resolved = 0 AND is_urgent = 1;");
					rs.next();
					int numUrgent = rs.getInt(1);
					sender.sendMessage("§4[§c§lMLMC§4] §7# Bugs: §e" + numBugs + "§7, # Urgent: §e" + numUrgent + "§7, # Resolved today: §e" + Main.numResolved);
					con.close();
				}
				catch(Exception e) {
					e.printStackTrace();
					sender.sendMessage("§4[§c§lMLMC§4] §cSomething went wrong! Maybe use fewer special characters?");
				}
				return true;
			}
			else if ((args.length == 1 || (args.length == 2 && (!args[1].equalsIgnoreCase("bug") && !args[1].equalsIgnoreCase("urgent"))))
					&& args[0].equalsIgnoreCase("list")) {
				if (args.length == 1) {
					try {  
						Class.forName("com.mysql.jdbc.Driver");
						Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
						Statement stmt = con.createStatement();
						ResultSet rs;
						rs = stmt.executeQuery("SELECT * FROM neoreports_bugs WHERE user = '" + author + "' ORDER BY id DESC;");
						int count = 1;
						Stack<Report> reports = new Stack<Report>();
						while(rs.next()) {
							Report temp = new Report(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
									rs.getString(7), rs.getInt(8) == 1, rs.getInt(9) == 1);
							reports.push(temp);
							count++;
							if (count > NUM_REPORTS_PER_PAGE) {
								break;
							}
						}
						while (!reports.isEmpty()) {
							reports.pop().list(sender);
						}
						sender.sendMessage("§7=====");
						con.close();
					}
					catch(Exception e) {
						e.printStackTrace();
						sender.sendMessage("§4[§c§lMLMC§4] §cSomething went wrong! Maybe use fewer special characters?");
					}
				}
				else if (args.length == 2) {
					int page = Integer.parseInt(args[1]);
					try {  
						Class.forName("com.mysql.jdbc.Driver");
						Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
						Statement stmt = con.createStatement();
						ResultSet rs;
						rs = stmt.executeQuery("SELECT * FROM neoreports_bugs WHERE user = '" + author + "' ORDER BY id DESC;");
						int count = 1;
						Stack<Report> reports = new Stack<Report>();
						while(rs.next()) {
							if (NUM_REPORTS_PER_PAGE * (page - 1) < count) {
								Report temp = new Report(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
										rs.getString(7), rs.getInt(8) == 1, rs.getInt(9) == 1);
								reports.push(temp);
							}
							count++;
							if (count > NUM_REPORTS_PER_PAGE * page) {
								break;
							}
						}
						while (!reports.isEmpty()) {
							reports.pop().list(sender);
						}
						sender.sendMessage("§7=====");
						con.close();
					}
					catch(Exception e) {
						e.printStackTrace();
						sender.sendMessage("§4[§c§lMLMC§4] §cSomething went wrong!");
					}
				}
				return true;
			}
			else if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("list") && args[1].equalsIgnoreCase("urgent")) {
				// Show first page only
				if (args.length == 2) {
					try{  
						Class.forName("com.mysql.jdbc.Driver");
						Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
						Statement stmt = con.createStatement();
						ResultSet rs;
						rs = stmt.executeQuery("SELECT * FROM neoreports_bugs WHERE is_urgent = 1 AND is_resolved = 0 ORDER BY id DESC;");
						int count = 1;
						Stack<Report> reports = new Stack<Report>();
						while(rs.next()) {
							Report temp = new Report(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
									rs.getString(7), rs.getInt(8) == 1, rs.getInt(9) == 1);
							reports.push(temp);
							count++;
							if (count >= NUM_REPORTS_PER_PAGE) {
								break;
							}
						}
						while (!reports.isEmpty()) {
							reports.pop().list(sender);
						}
						sender.sendMessage("§7=====");
						con.close();
					}
					catch(Exception e) {
						e.printStackTrace();
						sender.sendMessage("§4[§c§lMLMC§4] §cSomething went wrong!");
					}
					return true;
				}
				else if (args.length == 3 && StringUtils.isNumeric(args[2])) {
					int page = Integer.parseInt(args[2]);
					try{  
						Class.forName("com.mysql.jdbc.Driver");
						Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
						Statement stmt = con.createStatement();
						ResultSet rs;
						rs = stmt.executeQuery("SELECT * FROM neoreports_bugs WHERE is_urgent = 1 AND is_resolved = 0 ORDER BY id DESC;");
						int count = 1;
						Stack<Report> reports = new Stack<Report>();
						while(rs.next()) {
							if (NUM_REPORTS_PER_PAGE * (page - 1) < count) {
								Report temp = new Report(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
										rs.getString(7), rs.getInt(8) == 1, rs.getInt(9) == 1);
								reports.push(temp);
							}
							count++;
							if (count >= NUM_REPORTS_PER_PAGE * page) {
								break;
							}
						}
						while (!reports.isEmpty()) {
							reports.pop().list(sender);
						}
						sender.sendMessage("§7=====");
						con.close();
					}
					catch(Exception e) {
						e.printStackTrace();
						sender.sendMessage("§4[§c§lMLMC§4] §cSomething went wrong!");
					}
					return true;
				}
			}
			else if (sender.hasPermission("neoreports.admin")) {
				if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("list") && args[1].equalsIgnoreCase("bug")) {
					// Show first page only
					if (args.length == 2) {
						try{  
							Class.forName("com.mysql.jdbc.Driver");
							Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
							Statement stmt = con.createStatement();
							ResultSet rs;
							rs = stmt.executeQuery("SELECT * FROM neoreports_bugs WHERE is_urgent = 0 AND is_resolved = 0 ORDER BY id DESC;");
							int count = 1;
							Stack<Report> reports = new Stack<Report>();
							while(rs.next()) {
								Report temp = new Report(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
										rs.getString(7), rs.getInt(8) == 1, rs.getInt(9) == 1);
								reports.push(temp);
								count++;
								if (count > NUM_REPORTS_PER_PAGE) {
									break;
								}
							}
							while (!reports.isEmpty()) {
								reports.pop().list(sender);
							}
							sender.sendMessage("§7=====");
							con.close();
						}
						catch(Exception e) {
							e.printStackTrace();
							sender.sendMessage("§4[§c§lMLMC§4] §cSomething went wrong!");
						}
						return true;
					}
					else if (args.length == 3 && StringUtils.isNumeric(args[2])) {
						int page = Integer.parseInt(args[2]);
						try{  
							Class.forName("com.mysql.jdbc.Driver");
							Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
							Statement stmt = con.createStatement();
							ResultSet rs;
							rs = stmt.executeQuery("SELECT * FROM neoreports_bugs WHERE is_urgent = 0 AND is_resolved = 0 ORDER BY id DESC;");
							int count = 1;
							Stack<Report> reports = new Stack<Report>();
							while(rs.next()) {
								if (NUM_REPORTS_PER_PAGE * (page - 1) < count) {
									Report temp = new Report(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
											rs.getString(7), rs.getInt(8) == 1, rs.getInt(9) == 1);
									reports.push(temp);
								}
								count++;
								if (count > NUM_REPORTS_PER_PAGE * page) {
									break;
								}
							}
							while (!reports.isEmpty()) {
								reports.pop().list(sender);
							}
							sender.sendMessage("§7=====");
							con.close();
						}
						catch(Exception e) {
							e.printStackTrace();
							sender.sendMessage("§4[§c§lMLMC§4] §cSomething went wrong!");
						}
						return true;
					}
				}
				else if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("list") && args[1].equalsIgnoreCase("resolved")) {
					// Show first page only
					if (args.length == 2) {
						try{  
							Class.forName("com.mysql.jdbc.Driver");
							Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
							Statement stmt = con.createStatement();
							ResultSet rs;
							rs = stmt.executeQuery("SELECT * FROM neoreports_bugs WHERE is_resolved = 1 ORDER BY id DESC;");
							int count = 1;
							Stack<Report> reports = new Stack<Report>();
							while(rs.next()) {
								Report temp = new Report(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
										rs.getString(7), rs.getInt(8) == 1, rs.getInt(9) == 1);
								reports.push(temp);
								count++;
								if (count >= NUM_REPORTS_PER_PAGE) {
									break;
								}
							}
							while (!reports.isEmpty()) {
								reports.pop().list(sender);
							}
							sender.sendMessage("§7=====");
							con.close();
						}
						catch(Exception e) {
							e.printStackTrace();
							sender.sendMessage("§4[§c§lMLMC§4] §cSomething went wrong!");
						}
						return true;
					}
					else if (args.length == 3 && StringUtils.isNumeric(args[2])) {
						int page = Integer.parseInt(args[2]);
						try{  
							Class.forName("com.mysql.jdbc.Driver");
							Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
							Statement stmt = con.createStatement();
							ResultSet rs;
							rs = stmt.executeQuery("SELECT * FROM neoreports_bugs WHERE is_resolved = 1 ORDER BY id DESC;");
							int count = 1;
							Stack<Report> reports = new Stack<Report>();
							while(rs.next()) {
								if (NUM_REPORTS_PER_PAGE * (page - 1) < count) {
									Report temp = new Report(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
											rs.getString(7), rs.getInt(8) == 1, rs.getInt(9) == 1);
									reports.push(temp);
								}
								count++;
								if (count >= NUM_REPORTS_PER_PAGE * page) {
									break;
								}
							}
							while (!reports.isEmpty()) {
								reports.pop().list(sender);
							}
							sender.sendMessage("§7=====");
							con.close();
						}
						catch(Exception e) {
							e.printStackTrace();
							sender.sendMessage("§4[§c§lMLMC§4] §cSomething went wrong!");
						}
						return true;
					}
				}
				else if (args.length > 2 && args[0].equalsIgnoreCase("resolve") && StringUtils.isNumeric(args[1])) {
					String desc = args[2];
					String comment;
					for (int i = 3; i < args.length; i++) {
						desc += " " + args[i];
					}
					comment = desc;
					desc = desc.replaceAll("'", "\\\\'");
					try{  
						Class.forName("com.mysql.jdbc.Driver");
						Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
						Statement stmt = con.createStatement();
						ResultSet rs;
						int resolved = stmt.executeUpdate("UPDATE neoreports_bugs SET `is_resolved` = 1, `comment` = '" + desc + "', `resolver` = '" + author + 
								"', `fixdate` = '" + dateformat.format(new Date()) + "' WHERE id = " + args[1] + ";");
						if (resolved > 0) {
							sender.sendMessage("§4[§c§lMLMC§4] §7Successfully resolved report!");
						}
						else {
							sender.sendMessage("§4[§c§lMLMC§4] §7Failed to resolve report!");
						}
						rs = stmt.executeQuery("SELECT * FROM neoreports_bugs WHERE id = " + args[1] + ";");
						if (rs.next()) {
							String bug = rs.getString(4);
							Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "mail send " + rs.getString(3) + " (" + bug + ") resolved by " + sender.getName() + "! Message: " + comment);
						}
						boolean is_urgent = rs.getInt(9) == 1;
						Main.numResolved++;
						if (is_urgent) {
							Main.numUrgent--;
						}
						else {
							Main.numBugs--;
						}
						con.close();
					}
					catch(Exception e) {
						e.printStackTrace();
						sender.sendMessage("§4[§c§lMLMC§4] §cSomething went wrong!");
					}
					return true;
				}
				else if (args.length == 2 && args[0].equalsIgnoreCase("unresolve") && StringUtils.isNumeric(args[1])) {
					try{  
						Class.forName("com.mysql.jdbc.Driver");
						Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
						Statement stmt = con.createStatement();
						ResultSet rs;
						int resolved = stmt.executeUpdate("UPDATE neoreports_bugs SET `is_resolved` = 0 WHERE id = " + args[1] + ";");
						if (resolved > 0) {
							sender.sendMessage("§4[§c§lMLMC§4] §7Successfully unresolved report!");
						}
						else {
							sender.sendMessage("§4[§c§lMLMC§4] §7Failed to unresolve report!");
						}
						rs = stmt.executeQuery("SELECT * FROM neoreports_bugs WHERE id = " + args[1] + ";");
						rs.next();
						boolean is_urgent = rs.getInt(9) == 1;
						if (is_urgent) {
							Main.numUrgent++;
						}
						else {
							Main.numBugs++;
						}
						Main.numResolved--;
						con.close();
					}
					catch(Exception e) {
						e.printStackTrace();
						sender.sendMessage("§4[§c§lMLMC§4] §cSomething went wrong!");
					}
					return true;
				}
				else if (args.length > 2 && args[0].equalsIgnoreCase("edit") && StringUtils.isNumeric(args[1])) {
					String desc = args[2];
					for (int i = 3; i < args.length; i++) {
						desc += " " + args[i];
					}
					try{  
						Class.forName("com.mysql.jdbc.Driver");
						Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
						Statement stmt = con.createStatement();
						int edited = stmt.executeUpdate("UPDATE neoreports_bugs SET `comment` = '" + desc + "' WHERE id = " + args[1] + ";");
						if (edited > 0) {
							sender.sendMessage("§4[§c§lMLMC§4] §7Successfully edited comment!");
						}
						else {
							sender.sendMessage("§4[§c§lMLMC§4] §7Failed to edit comment!");
						}
						con.close();
					}
					catch(Exception e) {
						e.printStackTrace();
						sender.sendMessage("§4[§c§lMLMC§4] §cSomething went wrong!");
					}
					return true;
				}
			}
			else {
				sender.sendMessage("§4[§c§lMLMC§4] §cInvalid command!");
				return true;
			}
		}
		return false;
	}
}