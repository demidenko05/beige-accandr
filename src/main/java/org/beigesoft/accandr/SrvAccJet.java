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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import android.app.Service;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.IBinder;

import org.beigesoft.log.ILog;

/**
 * <p>A-Jetty Android service.</p>
 *
 * @author Yury Demidenko
 */
public class SrvAccJet extends Service {

  /**
   * <p>Static reference for Jetty-Logger to get context.</p>
   **/
  public static SrvAccJet CNTX = null;

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
   * <p>Flag to avoid double invoke.</p>
   **/
  private boolean isActionPerforming = false;

  /**
   * <p>Shared state.</p>
   **/
  private SrvState srvState;

  /**
   * <p>Shared logger.</p>
   **/
  private ILog log;

  /**
   * <p>on create.</p>
   **/
  @Override
  public final void onCreate() {
    AppPlus appPlus = (AppPlus) getApplicationContext();
    this.srvState = (SrvState) appPlus.getBeansMap()
      .get(SrvState.class.getSimpleName());
    this.log = this.srvState.getLog();
    CNTX = this;
  }

  /**
   * <p>onBind handler. No bind provide.</p>
   * @param pIntent Intent
   * @return IBinder IBinder
   **/
  @Override
  public final IBinder onBind(final Intent pIntent) {
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
  public final int onStartCommand(final Intent pIntent,
    final int pFlags, final int pStartId) {
    if (!this.isActionPerforming) {
      if (pIntent.getAction().equals(ACTION_START)
        && !this.srvState.getBootEmbd().getIsStarted()) {
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
            this.log.error(null, getClass(), "Can't create notification", e);
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
            this.log.error(null, getClass(), "Can't start service", e);
            throw new RuntimeException(e);
          }
        } else {
          startForeground(R.string.srvStrt, ntfc);
        }
        StartThread stThread = new StartThread();
        stThread.start();
      } else if (pIntent.getAction().equals(ACTION_STOP)) {
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
   * <p>Thread to start A-Jetty.</p>
   */
  private class StartThread extends Thread {

    @Override
    public void run() {
      boolean cantStart = false;
      if (!SrvAccJet.this.srvState.getBootEmbd().getIsStarted()) {
        try {
          SrvAccJet.this.log.info(null, getClass(), "Staring server...");
          if (SrvAccJet.this.srvState.getBootEmbd().getServer() == null) {
            SrvAccJet.this.srvState.getBootEmbd().createServer();
            SrvAccJet.this.srvState.getBootEmbd().getWebAppContext()
              .setAttribute("AndrCtx", SrvAccJet.this);
          }
          SrvAccJet.this.srvState.getBootEmbd().startServer();
        } catch (Exception e) {
          SrvAccJet.this.log.error(null, getClass(), "Can't start server", e);
          cantStart = true;
        }
      }
      if (cantStart) {
        SrvAccJet.this.stopForeground(true);
        SrvAccJet.this.stopSelf();
      }
      SrvAccJet.this.isActionPerforming = false;
    }
  };

  /**
   * <p>Thread to stop A-Jetty.</p>
   */
  private class StopThread extends Thread {

    @Override
    public void run() {
      if (SrvAccJet.this.srvState.getBootEmbd().getIsStarted()) {
        try {
          SrvAccJet.this.log.info(null, getClass(), "Stopping server...");
          SrvAccJet.this.srvState.getBootEmbd().stopServer();
        } catch (Exception e) {
          SrvAccJet.this.log.error(null, getClass(), "Can't stop server", e);
        }
      }
      SrvAccJet.this.isActionPerforming = false;
    }
  };
}
