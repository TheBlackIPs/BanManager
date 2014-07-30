package me.confuser.banmanager.storage;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.events.PlayerBanEvent;
import me.confuser.banmanager.events.PlayerUnbanEvent;
import me.confuser.banmanager.util.DateUtils;

public class PlayerBanStorage extends BaseDaoImpl<PlayerBanData, byte[]> {
	private BanManager plugin = BanManager.getPlugin();
	private ConcurrentHashMap<UUID, PlayerBanData> bans = new ConcurrentHashMap<UUID, PlayerBanData>();

	public PlayerBanStorage(ConnectionSource connection, DatabaseTableConfig<PlayerBanData> tableConfig) throws SQLException {
		super(connection, tableConfig);
		
		CloseableIterator<PlayerBanData> itr = iterator();
		
		while(itr.hasNext()) {
			PlayerBanData ban = itr.next();
			
			bans.put(ban.getUUID(), ban);
		}
		
		itr.close();
	}
	
	public ConcurrentHashMap<UUID, PlayerBanData> getBans() {
		return bans;
	}
	
	public boolean isBanned(UUID uuid) {
		return bans.get(uuid) != null;
	}
	
	public boolean isBanned(String playerName) {
		return getBan(playerName) != null;
	}

	public PlayerBanData getBan(UUID uuid) {
		return bans.get(uuid);
	}
	
	public void addBan(PlayerBanData ban) {
		bans.put(ban.getUUID(), ban);
	}
	
	public void removeBan(PlayerBanData ban) {
		removeBan(ban.getUUID());
	}
	
	public void removeBan(UUID uuid) {
		bans.remove(uuid);
	}
	
	public PlayerBanData getBan(String playerName) {
		for (PlayerBanData ban : bans.values()) {
			if (ban.getPlayer().getName().equalsIgnoreCase(playerName))
				return ban;
		}
		
		return null;
	}
	
	public boolean ban(PlayerBanData ban) throws SQLException {
		PlayerBanEvent event = new PlayerBanEvent(ban);
		Bukkit.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
			return false;

		create(ban);
		bans.put(ban.getUUID(), ban);
		
		return true;
	}
	
	public boolean unban(PlayerBanData ban, PlayerData actor) throws SQLException {
		PlayerUnbanEvent event = new PlayerUnbanEvent(ban);
		Bukkit.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
			return false;

		delete(ban);
		bans.remove(ban.getUUID());
		
		plugin.getPlayerBanRecordStorage().addRecord(ban, actor);
		
		return true;
	}
	
	public CloseableIterator<PlayerBanData> findBans(long fromTime) throws SQLException {
		if (fromTime == 0)
			return iterator();
		
		long checkTime = fromTime + DateUtils.getTimeDiff();
		
		QueryBuilder<PlayerBanData, byte[]> query = queryBuilder();
		Where<PlayerBanData, byte[]> where = query.where();
		where
			.ge("created", checkTime)
			.or()
			.ge("updated", checkTime);
		
		query.setWhere(where);
		
		return query.iterator();
		
	}

	
}
