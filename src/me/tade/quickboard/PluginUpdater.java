package me.tade.quickboard;

import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 *
 * @author The_TadeSK
 */
public class PluginUpdater {

	private QuickBoard plugin;
	private boolean needUpdate;
	private String[] updateInfo;

	public PluginUpdater(QuickBoard plugin) {
		this.plugin = plugin;

		new BukkitRunnable(){
			public void run(){
				doUpdate();
			}
		}.runTaskTimerAsynchronously(plugin, 20, 1800 * 20);
	}

	public void doUpdate(){
		String response = getResponse();

		if(response == null){
			return;
		}
		updateInfo = response.split(";");

		if(plugin.getDescription().getVersion().equalsIgnoreCase(updateInfo[0]))
			return;

		System.out.println(" ");
		System.out.println("QuickBoard I got new Update! Check it out on spigot page.");

		needUpdate = true;

		plugin.sendUpdateMessage();
	}

	public String getResponse(){
		try {
			URL post = new URL("https://raw.githubusercontent.com/tadeas-drab/QuickBoard/master/VERSION");

			String result = get(post);
			return result;
		} catch (IOException ex) {

		}
		return null;
	}

	public String get(URL url){
		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader(url.openStream()));

			String line;

			StringBuilder sb = new StringBuilder();

			while ((line = br.readLine()) != null) {

				sb.append(line);
				sb.append(System.lineSeparator());
			}

			return sb.toString();
		} catch (IOException e) {

		} finally {

			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {

				}
			}
		}
		return null;
	}

	public boolean needUpdate() {
		return needUpdate;
	}

	public String[] getUpdateInfo() {
		return updateInfo;
	}
}
