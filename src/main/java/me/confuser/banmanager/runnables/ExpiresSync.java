package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import lombok.Getter;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.IpRangeBanData;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.storage.*;
import me.confuser.banmanager.util.DateUtils;

import java.sql.SQLException;

public class ExpiresSync implements Runnable {

  private BanManager plugin = BanManager.getPlugin();
  private PlayerBanStorage banStorage = plugin.getPlayerBanStorage();
  private PlayerBanRecordStorage banRecordStorage = plugin.getPlayerBanRecordStorage();
  private PlayerMuteStorage muteStorage = plugin.getPlayerMuteStorage();
  private PlayerMuteRecordStorage muteRecordStorage = plugin.getPlayerMuteRecordStorage();
  private IpBanStorage ipBanStorage = plugin.getIpBanStorage();
  private IpBanRecordStorage ipBanRecordStorage = plugin.getIpBanRecordStorage();
  private IpRangeBanStorage ipRangeBanStorage = plugin.getIpRangeBanStorage();
  private IpRangeBanRecordStorage ipRangeBanRecordStorage = plugin.getIpRangeBanRecordStorage();
  @Getter
  private boolean isRunning = false;

  @Override
  public void run() {
    isRunning = true;
    long now = (System.currentTimeMillis() / 1000L) + DateUtils.getTimeDiff();

    CloseableIterator<PlayerBanData> bans = null;
    try {
      bans = banStorage.queryBuilder().where().ne("expires", 0).and()
                       .le("expires", now).iterator();

      while (bans.hasNext()) {
        PlayerBanData ban = bans.next();
        banRecordStorage.addRecord(ban, plugin.getPlayerStorage().getConsole());

        banStorage.removeBan(ban);
        banStorage.delete(ban);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      bans.closeQuietly();
    }

    CloseableIterator<PlayerMuteData> mutes = null;
    try {
      mutes = muteStorage.queryBuilder().where().ne("expires", 0).and().le("expires", now).iterator();

      while (mutes.hasNext()) {
        PlayerMuteData mute = mutes.next();
        muteRecordStorage.addRecord(mute, plugin.getPlayerStorage().getConsole());

        muteStorage.removeMute(mute);
        muteStorage.delete(mute);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      mutes.closeQuietly();
    }

    CloseableIterator<IpBanData> ipBans = null;
    try {
      ipBans = ipBanStorage.queryBuilder().where().ne("expires", 0).and()
                           .le("expires", now).iterator();

      while (ipBans.hasNext()) {
        IpBanData ban = ipBans.next();
        ipBanRecordStorage.addRecord(ban, plugin.getPlayerStorage().getConsole());

        ipBanStorage.removeBan(ban);
        ipBanStorage.delete(ban);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      ipBans.closeQuietly();
    }

    CloseableIterator<IpRangeBanData> ipRangeBans = null;
    try {
      ipRangeBans = ipRangeBanStorage.queryBuilder().where().ne("expires", 0).and()
                           .le("expires", now).iterator();

      while (ipRangeBans.hasNext()) {
        IpRangeBanData ban = ipRangeBans.next();
        ipRangeBanRecordStorage.addRecord(ban, plugin.getPlayerStorage().getConsole());

        ipRangeBanStorage.removeBan(ban);
        ipRangeBanStorage.delete(ban);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      ipRangeBans.closeQuietly();
    }

    isRunning = false;
  }
}
