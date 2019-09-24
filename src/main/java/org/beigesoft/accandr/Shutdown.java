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

import android.content.Intent;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.util.Log;

import org.beigesoft.log.ILog;

/**
 * <p>A-Jetty system shutdown listener.</p>
 *
 * @author Yury Demidenko
 */
public class Shutdown extends BroadcastReceiver {

  /**
   * <p>Stops the server on system shutdown.</p>
   * @param context The context under which the receiver is running.
   * @param intent The intent being received.
   */
  @Override
  public final void onReceive(final Context context, final Intent intent) {
    Log.i(getClass().getSimpleName(), "Shutdown...");
    AppPlus appPlus = (AppPlus) context;
    SrvState srvState = (SrvState) appPlus.getBeansMap()
      .get(SrvState.class.getSimpleName());
    if (srvState != null && srvState.getBootEmbd().getIsStarted()) {
      ILog log = srvState.getLog();
      try {
        log.info(null, getClass(), "Stopping server on shutdown...");
        srvState.getBootEmbd().stopServer();
      } catch (Exception e) {
        log.error(null, getClass(), "Can't stop server", e);
      }
    }
  }
}
