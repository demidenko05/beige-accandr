/*
BSD 2-Clause License

Copyright (c) 2019, Beigesoft™
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.beigesoft.accandr;

import java.util.Map;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import android.app.Service;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.IBinder;
import android.util.Log;

import org.beigesoft.ajetty.BootEmbed;

/**
 * <p>A-Jetty Android service.</p>
 *
 * @author Yury Demidenko
 */
public class SrvAccJet extends Service {

  /**
   * <p>Notification ID.</p>
   **/
  public static final int NOTIFICATION_ID = 777;

  /**
   * <p>Action start.</p>
   **/
  public static final String ACTION_START =
    "org.beigesoft.accandr.START";

  /**
   * <p>Action stop.</p>
   **/
  public static final String ACTION_STOP
    = "org.beigesoft.accandr.STOP";

  /**
   * <p>Application beans map reference to lock.</p>
   **/
  private Map<String, Object> beansMap;

  /**
   * <p>Flag to avoid double invoke.</p>
   **/
  private boolean isActionPerforming = false;

  /**
   * <p>on create.</p>
   **/
  @Override
  public final synchronized void onCreate() {
    AppPlus appPlus = (AppPlus) getApplicationContext();
    this.beansMap = appPlus.getBeansMap();
  }

  /**
   * <p>onBind handler. No bind provide.</p>
   * @param pIntent Intent
   * @return IBinder IBinder
   **/
  @Override
  public final synchronized IBinder onBind(final Intent pIntent) {
    return null;
  }

  /**
   * <p>Called when we receive an Intent. When we receive an intent sent
   * to us via startService(), this is the method that gets called.
   * So here we react appropriately depending on the
   * Intent's action, which specifies what is being requested of us.</p>
   * @param pIntent Intent
   * @param pFlags flags
   * @param pStartId startId
   * @return int status
   */
  @Override
  public final synchronized int onStartCommand(final Intent pIntent,
    final int pFlags, final int pStartId) {
    if (!this.isActionPerforming) {
      BootEmbed bootStrap;
      synchronized (this.beansMap) {
        bootStrap = getBootStrap();
      }
      String action = pIntent.getAction();
      if (bootStrap != null && action.equals(ACTION_START)
        && !bootStrap.getIsStarted()) {
        this.isActionPerforming = true;
        Notification.Builder nfBld;
        NotificationManager notfMan = (NotificationManager)
          getSystemService(NOTIFICATION_SERVICE);
        //Simple reflection way to avoid additional compile libraries
        if (android.os.Build.VERSION.SDK_INT >= 26) {
          try {
            final String ntfChnlId = "BSEISCH";
            Class[] argTypes = new Class[] {String.class, CharSequence.class,
              Integer.TYPE};
            Class ntfCnlCls = Class.forName("android.app.NotificationChannel");
            Constructor ntfChnl = ntfCnlCls.getConstructor(argTypes);
            argTypes = new Class[] {ntfCnlCls};
            Method crNtfChnl = NotificationManager.class
              .getDeclaredMethod("createNotificationChannel", argTypes);
            int impDef = 3; //from source 29
            crNtfChnl.invoke(notfMan, ntfChnl.newInstance(ntfChnlId,
              ntfChnlId, impDef));
            /*notfMan.createNotificationChannel(new NotificationChannel(
                    ntfChnlId, ntfChnlId,
                    NotificationManager.IMPORTANCE_DEFAULT));*/
            argTypes = new Class[] {Context.class, String.class};
            Constructor<Notification.Builder> nfBldCn = Notification.Builder
              .class.getConstructor(argTypes);
            nfBld = nfBldCn.newInstance(this, ntfChnlId);
            //nfBld = new Notification.Builder(this, ntfChnlId);
          } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Can't create notification", e);
            throw new RuntimeException(e);
          }
        } else {
          nfBld = new Notification.Builder(this);
        }
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
          new Intent(this, Bsa.class), 0);
        CharSequence text = getText(R.string.srvStrt);
        // Set the info for the views that show in the ntfc panel.
        Notification ntfc = nfBld.setSmallIcon(R.drawable.bsnotf)  // the icon
          //.setTicker(text)  // the status text
          .setWhen(System.currentTimeMillis())  // the time stamp
          .setContentTitle(getText(R.string.app_name))  // the label
          .setContentText(text)  // the contents of the entry
          .setContentIntent(contentIntent)  // The intent to send when clicked
          .build();
        if (android.os.Build.VERSION.SDK_INT >= 26) {
          try {
            Class[] argTypes = new Class[] {Integer.TYPE, Notification.class};
            Method stFrg = Service.class
              .getDeclaredMethod("startForeground", argTypes);
            stFrg.invoke(this, R.string.srvStrt, ntfc);
            //startForeground(R.string.srvStrt, ntfc);
          } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Can't start service", e);
            throw new RuntimeException(e);
          }
        } else {
          startForeground(R.string.srvStrt, ntfc);
        }
        StartThread stThread = new StartThread();
        stThread.start();
      } else if (action.equals(ACTION_STOP)) {
        this.isActionPerforming = true;
        StopThread stThread = new StopThread();
        stThread.start();
        stopForeground(true);
        stopSelf();
      }
    }
    // We want this service to continue running until it is explicitly
    // stopped, so return sticky.
    return START_STICKY;
  }

  /**
   * <p>Android Service destroy.
   * @see android.app.Service#onDestroy().</p>
   */
  @Override
  public final synchronized void onDestroy() {
    if (!this.isActionPerforming) {
      this.isActionPerforming = true;
      StopThread stThread = new StopThread();
      stThread.start();
    }
  }

  /**
   * <p>Get BootStrapEmbedded from app-context.
   * It invoked by start/stop threads.</p>
   * @return BootStrapEmbedded BootStrapEmbedded
   */
  private synchronized BootEmbed getBootStrap() {
    BootEmbed bootStrap = null;
    // this.beansMap already synchronized
    Object srvStateo = this.beansMap
      .get(SrvState.class.getSimpleName());
    if (srvStateo != null) {
      SrvState srvState = (SrvState) srvStateo;
      bootStrap = srvState.getBootEmbd();
    } else {
      Log.e(getClass().getSimpleName(), "There is no srvState");
    }
    return bootStrap;
  }

  /**
   * <p>Thread to start A-Jetty.</p>
   */
  private class StartThread extends Thread {

    @Override
    public void run() {
      boolean cantStart = false;
      synchronized (SrvAccJet.this.beansMap) {
        BootEmbed bootStrap = getBootStrap();
        if (bootStrap == null) {
          cantStart = true;
        } else if (!bootStrap.getIsStarted()) {
          try {
            if (bootStrap.getServer() == null) {
              bootStrap.createServer();
              bootStrap.getWebAppContext()
                .setAttribute("AndrCtx", SrvAccJet.this);
            }
            bootStrap.startServer();
          } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Can't start server", e);
            cantStart = true;
          }
        }
      }
      synchronized (SrvAccJet.this) {
        if (cantStart) {
          SrvAccJet.this.stopForeground(true);
          SrvAccJet.this.stopSelf();
        }
        SrvAccJet.this.isActionPerforming = false;
      }
    }
  };

  /**
   * <p>Thread to stop A-Jetty.</p>
   */
  private class StopThread extends Thread {

    @Override
    public void run() {
      synchronized (SrvAccJet.this.beansMap) {
        BootEmbed bootStrap = SrvAccJet.this.getBootStrap();
        if (bootStrap != null && bootStrap.getIsStarted()) {
          try {
            bootStrap.stopServer();
          } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Can't stop server", e);
          }
        }
      }
      synchronized (SrvAccJet.this) {
        SrvAccJet.this.isActionPerforming = false;
      }
    }
  };

  //Simple getters and setters:
  /**
   * <p>Getter for beansMap.</p>
   * @return Map<String, Object>
   **/
  public final Map<String, Object> getBeansMap() {
    return this.beansMap;
  }

  /**
   * <p>Setter for beansMap.</p>
   * @param pBeansMap reference
   **/
  public final void setBeansMap(final Map<String, Object> pBeansMap) {
    this.beansMap = pBeansMap;
  }

  /**
   * <p>Getter for isActionPerforming.</p>
   * @return boolean
   **/
  public final boolean getIsActionPerforming() {
    return this.isActionPerforming;
  }
}
