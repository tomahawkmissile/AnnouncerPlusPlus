package com.gmail.tomahawkmissile2.announcer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements Listener, CommandExecutor {

	public static Main plugin;
	
	public final static String INVALID_SYNTAX = ChatColor.RED+"[Announcer++] "+ChatColor.DARK_RED+"Invalid syntax. Type /announcer to access the help page.";
	
	public static volatile ConcurrentHashMap<Integer,List<String>> messages = new ConcurrentHashMap<Integer,List<String>>();
	
	public static AtomicBoolean stopAnnouncer=new AtomicBoolean(false);
	
	@Override
	public void onEnable() {
		Main.plugin=this;
		this.getServer().getPluginManager().registerEvents(this, this);
		
		if(!new File(this.getDataFolder()+"/config.yml").exists()) {
			try {
				new File(this.getDataFolder()+"/config.yml").createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Config.setDefaults();
		}
		for(DefaultConfigValue v:DefaultConfigValue.values()) {
			if(Config.get(v.getPath())==null || Config.get(v.getPath()).equals("")) {
				Config.setDefault(v);
			}
		}
		this.getCommand("announcer").setExecutor(this);
		this.loadMessages();
		this.runAnnouncerThread();
	}
	public void onDisable() {
		stopAnnouncer.set(true);
		System.out.println(ChatColor.DARK_GREEN+"[Announcer++]"+ChatColor.GREEN+" Successfully stopped announcer messaging thread.");
	}
	
	public void runAnnouncerThread() {
		Thread t = new Thread() {
			public void run() {
				while(!Main.stopAnnouncer.get()) {
					int cooldown=90000;
					try {
						cooldown=Integer.parseInt(Config.get("interval").toString());
					} catch(NumberFormatException|NullPointerException e) {
						cooldown=90000;
					}
					try {
						Thread.sleep(cooldown);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(messages==null||messages.keySet().isEmpty()) {
						System.out.println(ChatColor.RED+"[Announcer++] Warning: there are no messages in the config!");
					} else {
						int id = (int)(Math.random()*(messages.keySet().size()));
						String prefix = ChatColor.DARK_GRAY+"["+ChatColor.RED+"!"+ChatColor.DARK_GRAY+"] ";
						for(String line : messages.get(id)) {
							for(Player p:Bukkit.getOnlinePlayers()) {
								p.sendMessage(prefix+ChatColor.RESET+line);
							}
						}
					}
				}
			}
		};
		System.out.println(ChatColor.DARK_GREEN+"[Announcer++]"+ChatColor.GREEN+" Successfully started announcer messaging thread.");
		t.start();
	}
	public void loadMessages() {
		List<String> messagesConfig = Config.getStringList("messages");
		messages.clear();
		if(messagesConfig==null||messagesConfig.size()==0) {
			System.out.println(ChatColor.RED+"[Announcer++] Warning: there are no messages in the config!");
			return;
		}
		for(int i=0;i<messagesConfig.size();i++) {
			String colored = ChatColor.translateAlternateColorCodes('&',messagesConfig.get(i));
			Main.messages.putIfAbsent(i, new ArrayList<String>() {
				/**
				 * 
				 */
				private static final long serialVersionUID = -3587169469797272765L;
			{
				add(colored);
			}});
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if((cmd.getName().equalsIgnoreCase("announcer"))) {
				if(!p.hasPermission("announcer.use")) {
					p.sendMessage(ChatColor.RED+"Error: "+ChatColor.DARK_RED+"You do not have permission!");
				} else {
					if(args.length==0) {
						this.sendHelpPage(p);
					} else if(args.length==1) {
						if(args[0].equalsIgnoreCase("reload")) {
							this.loadMessages();
							p.sendMessage(ChatColor.GREEN+"[Announcer++] "+ChatColor.DARK_GREEN+"Config reloaded.");
						} else if(args[0].equalsIgnoreCase("help")) {
							this.sendHelpPage(p);
						} else if(args[0].equalsIgnoreCase("test")) {
							if(messages.keySet()==null||messages.keySet().size()==0) {
								System.out.println(ChatColor.RED+"[Announcer++] Warning: there are no messages in the config!");
								p.sendMessage(ChatColor.RED+"Error: "+ChatColor.DARK_RED+"there are no messages in the config!");
							} else {
								int id = (int)(Math.random()*(messages.keySet().size()));
								String prefix = ChatColor.DARK_GRAY+"["+ChatColor.RED+"!"+ChatColor.DARK_GRAY+"] ";
								for(String line : messages.get(id)) {
									Bukkit.getServer().broadcastMessage(prefix+ChatColor.RESET+line);
								}
							}
						} else if(args[0].equalsIgnoreCase("list")) {
							p.sendMessage(ChatColor.GREEN+"[Announcer++] "+ChatColor.DARK_GREEN+"Announcements:");
							this.loadMessages();
							for(int i=0;i<messages.values().size();i++) {
								List<String> msg = messages.get(i);
								String sendMsg = "ID: "+i+" - ";
								for(String s:msg) {
									sendMsg+=s;
								}
								p.sendMessage(ChatColor.GREEN+sendMsg);
								if(msg==null||msg.isEmpty()) {
									p.sendMessage(ChatColor.GREEN+"[Announcer++] "+ChatColor.DARK_GREEN+"There are no announcements. Type /announcer help to see how to add one!");
								}
							}
						} else {
							p.sendMessage(INVALID_SYNTAX);
						}
					} else if(args.length==2) {
						if(args[0].equalsIgnoreCase("add")) {
							String add="";
							for(int i=1;i<args.length;i++) {
								add+=args[i]+" ";
							}
							Config.addStringToList("messages", add);
							this.loadMessages();
							p.sendMessage(ChatColor.GREEN+"[Announcer++] "+ChatColor.DARK_GREEN+"Added announcement");
						} else if(args[0].equalsIgnoreCase("remove")) {
							int id=0;
							try {
								id=Integer.parseInt(args[1]);
							} catch(NumberFormatException e) {
								p.sendMessage(ChatColor.RED+"Error: "+ChatColor.DARK_RED+"that ID is not a number!");
								return true;
							}
							if(id>Config.getStringList("messages").size()-1) {
								p.sendMessage(ChatColor.RED+"Error: "+ChatColor.DARK_RED+"that ID is too high!");
							} else {
								Config.removeStringFromList("messages", id);
								this.loadMessages();
								p.sendMessage(ChatColor.GREEN+"[Announcer++] "+ChatColor.DARK_GREEN+"Removed announcement");
							}
						} else {
							p.sendMessage(INVALID_SYNTAX);
						}
					} else {
						if(args[0].equalsIgnoreCase("add")) {
							String add="";
							for(int i=1;i<args.length;i++) {
								add+=args[i]+" ";
							}
							Config.addStringToList("messages", add);
							this.loadMessages();
							p.sendMessage(ChatColor.GREEN+"[Announcer++] "+ChatColor.DARK_GREEN+"Added announcement");
						} else {
							p.sendMessage(INVALID_SYNTAX);
						}
					}
				}
				return true;
			}
		}
		return false;
	}
	public void sendHelpPage(Player p) {
		p.sendMessage(ChatColor.GREEN+"[Announcer++]"+ChatColor.DARK_GREEN+" Help page shown below:");
		p.sendMessage(ChatColor.GREEN+"/announcer | "+ChatColor.DARK_GREEN+"Show this page.");
		p.sendMessage(ChatColor.GREEN+"/announcer help | "+ChatColor.DARK_GREEN+"Show this page.");
		p.sendMessage(ChatColor.GREEN+"/announcer test | "+ChatColor.DARK_GREEN+"Test a random announcement by sending it to the server.");
		p.sendMessage(ChatColor.GREEN+"/announcer add <message> | "+ChatColor.DARK_GREEN+"Add an announcement. Standard color codes are supported.");
		p.sendMessage(ChatColor.GREEN+"/announcer remove <id> | "+ChatColor.DARK_GREEN+"Remove an announcement. To get the ID type /announcer list.");
		p.sendMessage(ChatColor.GREEN+"/announcer list | "+ChatColor.DARK_GREEN+"List announcements and show IDs.");
		p.sendMessage(ChatColor.GREEN+"/announcer reload | "+ChatColor.DARK_GREEN+"Reload configuration.");
	}
}
