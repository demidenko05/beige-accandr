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

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.provider.OpenableColumns;
import android.content.DialogInterface;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;
import android.net.Uri;
import android.Manifest;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import org.beigesoft.exc.ExcCode;
import org.beigesoft.ajetty.crypto.CryptoService;
import org.beigesoft.log.ILog;
import org.beigesoft.log.LogFile;
import org.beigesoft.loga.Loga;

/**
 * <p>Beige Accounting Jetty activity.</p>
 *
 * @author Yury Demidenko
 */
public class Bsa extends FragmentActivity
//public class Bsa extends Activity
  implements OnClickListener, Priv.PrivDlgLstn {

  //SDK19 copies (they must be compared by content, not reference):
  public static final String SDK19ACTION_CREATE_DOCUMENT = "android.intent.action.CREATE_DOCUMENT";

  public static final String SDK19ACTION_OPEN_DOCUMENT = "android.intent.action.OPEN_DOCUMENT";

  //SDK28 copies:
  public static final String SDK28FOREGROUND_SERVICE = "android.permission.FOREGROUND_SERVICE";

  /**
   * <p>Preference privacy policy is agreed key.</p>
   **/
  public static final String PRIVAGREE = "privAgree";

  /**
   * <p>APP BASE dir.</p>
   **/
  public static final String APP_BASE = "webapp";

  /**
   * <p>Permissions request.</p>
   **/
  public static final int PERMISSIONS_REQUESTS = 2415;

  /**
   * <p>Action none.</p>
   **/
  public static final int NOACT = 0;

  /**
   * <p>Action starting.</p>
   **/
  public static final int STARTING = 1;

  /**
   * <p>Action stopping.</p>
   **/
  public static final int STOPPING = 2;

  /**
   * <p>Flag to refresh UI.</p>
   **/
  private boolean isNeedsToRefresh;

  /**
   * <p>Flag permissions INET OK.</p>
   **/
  private boolean isPermOk = false;

  /**
   * <p>Button start.</p>
   **/
  private Button btnStart;

  /**
   * <p>Button stop.</p>
   **/
  private Button btnStop;

  /**
   * <p>Button start browser.</p>
   **/
  private Button btnStartBrowser;

  /**
   * <p>Button privacy policy.</p>
   **/
  private Button btnPrivacy;

  /**
   * <p>Button export CA cert.</p>
   **/
  private Button btnExpCaCe;

  /**
   * <p>Button export pub. exch. key.</p>
   **/
  private Button btnExpPubExch;

  /**
   * <p>Button import pub. exch. key.</p>
   **/
  private Button btnImpPubExch;

  /**
   * <p>Combo-Box export log.</p>
   **/
  private Spinner cmbExpLog;

  /**
   * <p>Button share log file.</p>
   **/
  private Button btnExpLog;

  /**
   * <p>Combo-Box export en.log.</p>
   **/
  private Spinner cmbExpEnLog;

  /**
   * <p>Button share en.log file.</p>
   **/
  private Button btnExpEnLog;

  /**
   * <p>Combo-Box export SQLITE DB.</p>
   **/
  private Spinner cmbExpSqlt;

  /**
   * <p>Button export SQLITE DB.</p>
   **/
  private Button btnExpSqlt;

  /**
   * <p>Combo-Box export en. SQLITE DB.</p>
   **/
  private Spinner cmbExpEnSqlt;

  /**
   * <p>Button export en. SQLITE DB.</p>
   **/
  private Button btnExpEnSqlt;

  /**
   * <p>Combo-Box export signature of en. SQLITE DB.</p>
   **/
  private Spinner cmbExpEnSqltSg;

  /**
   * <p>Button export signature of en. SQLITE DB.</p>
   **/
  private Button btnExpEnSqltSg;

  /**
   * <p>Combo-Box export SK of en. SQLITE DB.</p>
   **/
  private Spinner cmbExpEnSqltSk;

  /**
   * <p>Button export SK of en. SQLITE DB.</p>
   **/
  private Button btnExpEnSqltSk;

  /**
   * <p>Combo-Box export signature of SK of en. SQLITE DB.</p>
   **/
  private Spinner cmbExpEnSqltSks;

  /**
   * <p>Button export signature of SK of en. SQLITE DB.</p>
   **/
  private Button btnExpEnSqltSks;

  /**
   * <p>Button import SQLITE.</p>
   **/
  private Button btnImpSqlite;

  /**
   * <p>Button import encrypted SQLITE.</p>
   **/
  private Button btnImpEnSqlt;

  /**
   * <p>Button import signature of encrypted SQLITE.</p>
   **/
  private Button btnImpEnSqltSg;

  /**
   * <p>Button import SK of encrypted SQLITE.</p>
   **/
  private Button btnImpEnSqltSk;

  /**
   * <p>Button import signature of SK of encrypted SQLITE.</p>
   **/
  private Button btnImpEnSqltSks;

  /**
   * <p>Combo-Box Port.</p>
   **/
  private Spinner cmbPort;

  /**
   * <p>A-Jetty instance number.</p>
   **/
  private EditText etAjettyIn;

  /**
   * <p>KS password.</p>
   **/
  private EditText etKsPassw;

  /**
   * <p>KS password repeated.</p>
   **/
  private EditText etKsPasswRep;

  /**
   * <p>Button permissions.</p>
   **/
  private Button btnPerm;

  /**
   * <p>Flag to avoid double invoke, 0 - no action, 1 - starting,
   * 2 - stopping.</p>
   **/
  private int actionPerforming = 0;

  /**
   * <p>Shared state.</p>
   **/
  private SrvState srvState;

  /**
   * <p>Shared logger.</p>
   **/
  private ILog log;

  /**
   * <p>Cashed privacy policy is agreed.</p>
   **/
  private Boolean privAgreed = null;

  //Hard-coded Log's names:
  private static String LOG_BSEISST0 = "bseisst0.log";

  private static String LOG_BSEISST1 = "bseisst1.log";

  private static String AJETTY_EIS0_LOG = "ajetty-eis0.log";

  private static String AJETTY_EIS1_LOG = "ajetty-eis1.log";

  private static String LOG_STD0 = "logStd0.log";

  private static String LOG_STD1 = "logStd1.log";

  private static String LOG_SEC0 = "logSec0.log";

  private static String LOG_SEC1 = "logSec1.log";

    //encrypted to export:
  private static String LOG_STDEN0 = "logStd0.logen";

  private static String LOG_STDEN1 = "logStd1.logen";

  private static String LOG_SECEN0 = "logSec0.logen";

  private static String LOG_SECEN1 = "logSec1.logen";

  private static String LOG_STDENSG0 = "logStd0.logen.sig";

  private static String LOG_STDENSG1 = "logStd1.logen.sig";

  private static String LOG_SECENSG0 = "logSec0.logen.sig";

  private static String LOG_SECENSG1 = "logSec1.logen.sig";

  private static String LOG_STDENSK0 = "logStd0.logen.sken";

  private static String LOG_STDENSK1 = "logStd1.logen.sken";

  private static String LOG_SECENSK0 = "logSec0.logen.sken";

  private static String LOG_SECENSK1 = "logSec1.logen.sken";

  private static String LOG_STDENSKS0 = "logStd0.logen.sken.sig";

  private static String LOG_STDENSKS1 = "logStd1.logen.sken.sig";

  private static String LOG_SECENSKS0 = "logSec0.logen.sken.sig";

  private static String LOG_SECENSKS1 = "logSec1.logen.sken.sig";

  /**
   * <p>Called when the activity is first created or recreated.</p>
   * @param pSavedInstanceState Saved Instance State
   */
  @Override
  public final void onCreate(final Bundle pSavedInstanceState) {
    super.onCreate(pSavedInstanceState);
    this.log = new Loga();
    File bsBkdir = new File(getFilesDir().getAbsolutePath() + "/Bseis");
    if (!bsBkdir.exists() && !bsBkdir.mkdirs()) {
      String msg = "Can't create dir " + bsBkdir;
      Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
      this.log.error(null, getClass(), msg);
      return;
    }
    /*revIsPermOk();*/
    try {
      setContentView(R.layout.beigeaccounting);
      this.etAjettyIn = (EditText) findViewById(R.id.etAjettyIn);
      this.etKsPassw = (EditText) findViewById(R.id.etKsPassw);
      this.etKsPasswRep = (EditText) findViewById(R.id.etKsPasswRep);
      this.cmbPort = (Spinner) findViewById(R.id.cmbPort);
      ArrayAdapter<Integer> cmpAdPort =
        new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item);
      cmpAdPort.add(Integer.valueOf(8443));
      cmpAdPort.add(Integer.valueOf(8444));
      cmpAdPort.add(Integer.valueOf(8445));
      cmpAdPort.add(Integer.valueOf(8446));
      this.cmbPort.setAdapter(cmpAdPort);
      this.cmbPort.setSelection(0);
      this.btnStart = (Button) findViewById(R.id.btnStart);
      this.btnStart.setOnClickListener(this);
      this.btnStartBrowser = (Button) findViewById(R.id.btnStartBrowser);
      this.btnStartBrowser.setOnClickListener(this);
      this.btnPrivacy = (Button) findViewById(R.id.btnPrivacy);
      this.btnPrivacy.setOnClickListener(this);
      this.btnStop = (Button) findViewById(R.id.btnStop);
      this.btnStop.setOnClickListener(this);
      this.btnExpCaCe = (Button) findViewById(R.id.btnExpCaCe);
      this.btnExpCaCe.setOnClickListener(this);
      this.btnExpPubExch = (Button) findViewById(R.id.btnExpPubExch);
      this.btnExpPubExch.setOnClickListener(this);
      this.btnImpPubExch = (Button) findViewById(R.id.btnImpPubExch);
      this.btnImpPubExch.setOnClickListener(this);
      this.btnExpLog = (Button) findViewById(R.id.btnExpLog);
      this.btnExpLog.setOnClickListener(this);
      this.cmbExpLog = (Spinner) findViewById(R.id.cmbExpLog);
      ArrayAdapter<String> cmpAdExLog =
        new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
      cmpAdExLog.add("-");
      this.cmbExpLog.setAdapter(cmpAdExLog);
      this.cmbExpLog.setSelection(0);
      File logf = new File(getFilesDir().getAbsolutePath() + "/" + LOG_BSEISST0);
      if (logf.exists()) {
        cmpAdExLog.add(LOG_BSEISST0);
      }
      logf = new File(getFilesDir().getAbsolutePath() + "/" + LOG_BSEISST1);
      if (logf.exists()) {
        cmpAdExLog.add(LOG_BSEISST1);
      }
      logf = new File(getFilesDir().getAbsolutePath() + "/" + AJETTY_EIS0_LOG);
      if (logf.exists()) {
        cmpAdExLog.add(AJETTY_EIS0_LOG);
      }
      logf = new File(getFilesDir().getAbsolutePath() + "/" + AJETTY_EIS1_LOG);
      if (logf.exists()) {
        cmpAdExLog.add(AJETTY_EIS1_LOG);
      }
      logf = new File(getFilesDir().getAbsolutePath() + "/" + AJETTY_EIS1_LOG);
      if (logf.exists()) {
        cmpAdExLog.add(AJETTY_EIS1_LOG);
      }
      String appDir = getFilesDir().getAbsolutePath() + "/" + APP_BASE + "/";
      logf = new File(appDir + LOG_STD0);
      if (logf.exists()) {
        cmpAdExLog.add(LOG_STD0);
      }
      logf = new File(appDir + LOG_STD1);
      if (logf.exists()) {
        cmpAdExLog.add(LOG_STD1);
      }
      logf = new File(appDir + LOG_SEC0);
      if (logf.exists()) {
        cmpAdExLog.add(LOG_SEC0);
      }
      logf = new File(appDir + LOG_SEC1);
      if (logf.exists()) {
        cmpAdExLog.add(LOG_SEC1);
      }
      this.btnExpEnLog = (Button) findViewById(R.id.btnExpEnLog);
      this.btnExpEnLog.setOnClickListener(this);
      this.cmbExpEnLog = (Spinner) findViewById(R.id.cmbExpEnLog);
      ArrayAdapter<String> cmpAdExEnLog =
        new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
      cmpAdExEnLog.add("-");
      this.cmbExpEnLog.setAdapter(cmpAdExEnLog);
      this.cmbExpEnLog.setSelection(0);
      String bkDr = getFilesDir().getAbsolutePath() + "/Bseis/";
      logf = new File(bkDr + LOG_STDEN0);
      if (logf.exists()) {
        cmpAdExEnLog.add(LOG_STDEN0);
      }
      logf = new File(bkDr + LOG_STDEN1);
      if (logf.exists()) {
        cmpAdExEnLog.add(LOG_STDEN1);
      }
      logf = new File(bkDr + LOG_SECEN0);
      if (logf.exists()) {
        cmpAdExEnLog.add(LOG_SECEN0);
      }
      logf = new File(bkDr + LOG_SECEN1);
      if (logf.exists()) {
        cmpAdExEnLog.add(LOG_SECEN1);
      }
      logf = new File(bkDr + LOG_STDENSG0);
      if (logf.exists()) {
        cmpAdExEnLog.add(LOG_STDENSG0);
      }
      logf = new File(bkDr + LOG_STDENSG1);
      if (logf.exists()) {
        cmpAdExEnLog.add(LOG_STDENSG1);
      }
      logf = new File(bkDr + LOG_SECENSG0);
      if (logf.exists()) {
        cmpAdExEnLog.add(LOG_SECENSG0);
      }
      logf = new File(bkDr + LOG_SECENSG1);
      if (logf.exists()) {
        cmpAdExEnLog.add(LOG_SECENSG1);
      }
      logf = new File(bkDr + LOG_STDENSK0);
      if (logf.exists()) {
        cmpAdExEnLog.add(LOG_STDENSK0);
      }
      logf = new File(bkDr + LOG_STDENSK1);
      if (logf.exists()) {
        cmpAdExEnLog.add(LOG_STDENSK1);
      }
      logf = new File(bkDr + LOG_SECENSK0);
      if (logf.exists()) {
        cmpAdExEnLog.add(LOG_SECENSK0);
      }
      logf = new File(bkDr + LOG_SECENSK1);
      if (logf.exists()) {
        cmpAdExEnLog.add(LOG_SECENSK1);
      }
      logf = new File(bkDr + LOG_STDENSKS0);
      if (logf.exists()) {
        cmpAdExEnLog.add(LOG_STDENSKS0);
      }
      logf = new File(bkDr + LOG_STDENSKS1);
      if (logf.exists()) {
        cmpAdExEnLog.add(LOG_STDENSKS1);
      }
      logf = new File(bkDr + LOG_SECENSKS0);
      if (logf.exists()) {
        cmpAdExEnLog.add(LOG_SECENSKS0);
      }
      logf = new File(bkDr + LOG_SECENSKS1);
      if (logf.exists()) {
        cmpAdExEnLog.add(LOG_SECENSKS1);
      }
      this.btnExpSqlt = (Button) findViewById(R.id.btnExpSqlt);
      this.btnExpSqlt.setOnClickListener(this);
      this.cmbExpSqlt = (Spinner) findViewById(R.id.cmbExpSqlt);
      ArrayAdapter<String> cmpAdExSqlt =
        new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
      cmpAdExSqlt.add("-");
      this.cmbExpSqlt.setAdapter(cmpAdExSqlt);
      this.cmbExpSqlt.setSelection(0);
      String dbpth = getFilesDir().getAbsolutePath()
        .replace("files", "databases"); //this is Android's DB's hardcoded dir!
      File dbDr = new File(dbpth);
      if (dbDr.exists()) {
        File[] fls = dbDr.listFiles();
        if (fls != null) {
          for (File fl : fls) {
            if (fl.getName().endsWith(".sqlite")) {
              cmpAdExSqlt.add(fl.getName());
            }
          }
        }
      }
      this.btnExpEnSqlt = (Button) findViewById(R.id.btnExpEnSqlt);
      this.btnExpEnSqlt.setOnClickListener(this);
      this.cmbExpEnSqlt = (Spinner) findViewById(R.id.cmbExpEnSqlt);
      ArrayAdapter<String> cmpAdExEnSqlt =
        new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
      cmpAdExEnSqlt.add("-");
      this.cmbExpEnSqlt.setAdapter(cmpAdExEnSqlt);
      this.cmbExpEnSqlt.setSelection(0);
      File[] fls = bsBkdir.listFiles();
      if (fls != null) {
        for (File fl : fls) {
          if (fl.getName().endsWith(".sqlten")) {
            cmpAdExEnSqlt.add(fl.getName());
          }
        }
      }
      this.btnExpEnSqltSg = (Button) findViewById(R.id.btnExpEnSqltSg);
      this.btnExpEnSqltSg.setOnClickListener(this);
      this.cmbExpEnSqltSg = (Spinner) findViewById(R.id.cmbExpEnSqltSg);
      ArrayAdapter<String> cmpAdExEnSqltSg =
        new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
      cmpAdExEnSqltSg.add("-");
      this.cmbExpEnSqltSg.setAdapter(cmpAdExEnSqltSg);
      this.cmbExpEnSqltSg.setSelection(0);
      fls = bsBkdir.listFiles();
      if (fls != null) {
        for (File fl : fls) {
          if (fl.getName().endsWith(".sqlten.sig")) {
            cmpAdExEnSqltSg.add(fl.getName());
          }
        }
      }
      this.btnExpEnSqltSk = (Button) findViewById(R.id.btnExpEnSqltSk);
      this.btnExpEnSqltSk.setOnClickListener(this);
      this.cmbExpEnSqltSk = (Spinner) findViewById(R.id.cmbExpEnSqltSk);
      ArrayAdapter<String> cmpAdExEnSqltSk =
        new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
      cmpAdExEnSqltSk.add("-");
      this.cmbExpEnSqltSk.setAdapter(cmpAdExEnSqltSk);
      this.cmbExpEnSqltSk.setSelection(0);
      fls = bsBkdir.listFiles();
      if (fls != null) {
        for (File fl : fls) {
          if (fl.getName().endsWith(".sqlten.sken")) {
            cmpAdExEnSqltSk.add(fl.getName());
          }
        }
      }
      this.btnExpEnSqltSks = (Button) findViewById(R.id.btnExpEnSqltSks);
      this.btnExpEnSqltSks.setOnClickListener(this);
      this.cmbExpEnSqltSks = (Spinner) findViewById(R.id.cmbExpEnSqltSks);
      ArrayAdapter<String> cmpAdExEnSqltSks =
        new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
      cmpAdExEnSqltSks.add("-");
      this.cmbExpEnSqltSks.setAdapter(cmpAdExEnSqltSks);
      this.cmbExpEnSqltSks.setSelection(0);
      fls = bsBkdir.listFiles();
      if (fls != null) {
        for (File fl : fls) {
          if (fl.getName().endsWith(".sqlten.sken.sig")) {
            cmpAdExEnSqltSks.add(fl.getName());
          }
        }
      }
      this.btnImpSqlite = (Button) findViewById(R.id.btnImpSqlite);
      this.btnImpSqlite.setOnClickListener(this);
      this.btnImpEnSqlt = (Button) findViewById(R.id.btnImpEnSqlt);
      this.btnImpEnSqlt.setOnClickListener(this);
      this.btnImpEnSqltSg = (Button) findViewById(R.id.btnImpEnSqltSg);
      this.btnImpEnSqltSg.setOnClickListener(this);
      this.btnImpEnSqltSk = (Button) findViewById(R.id.btnImpEnSqltSk);
      this.btnImpEnSqltSk.setOnClickListener(this);
      this.btnImpEnSqltSks = (Button) findViewById(R.id.btnImpEnSqltSks);
      this.btnImpEnSqltSks.setOnClickListener(this);
      this.btnPerm = (Button) findViewById(R.id.btnPerm);
      this.btnPerm.setOnClickListener(this);
    } catch (Exception e) {
      this.log.error(null, getClass(),
        "Cant create interface", e);
    }
    AppPlus appPlus = (AppPlus) getApplicationContext();
    if (appPlus.getBeansMap().size() > 0) { // onResume
      this.srvState = (SrvState) appPlus.getBeansMap()
        .get(SrvState.class.getSimpleName());
      this.log = this.srvState.getLog();
    } else {
      try {
        LogFile lg = new LogFile();
        lg.setPath(getFilesDir().getAbsolutePath() + "/bseisst");
        lg.setClsImm(true);
        //it will fail without permissions:
        lg.info(null, getClass(), "Logger created: " + lg.getPath());
        this.log = lg;
        this.srvState = new SrvState();
        this.srvState.setLog(this.log);
      } catch (Exception e) {
        if (this.log == null || !(this.log instanceof Loga)) {
          this.log = new Loga();
        }
        this.log.error(null, getClass(), "Can't create starter file log", e);
        Toast.makeText(getApplicationContext(), getResources()
          .getString(R.string.errStartLog), Toast.LENGTH_LONG).show();
      }
      if (this.srvState != null) {
        try {
          initSrv();
          appPlus.getBeansMap().put(SrvState.class.getSimpleName(),
            this.srvState);
        } catch (Exception e) {
          this.srvState = null;
          this.log.error(null, getClass(), "Cant create server", e);
        }
      }
    }
    if (!lazPrivAgreed()) {
      showPrivDlg();
    }
  }

  /**
   * <p>Passes agree event to activity.</>
   * @param pPrivAgreed if privacy policy accepted
   */
  @Override
  public final void onPrivSaveClick(final boolean pPrivAgreed) {
    SharedPreferences shrPrf = getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor ed = shrPrf.edit();
    ed.putBoolean(PRIVAGREE, pPrivAgreed);
    ed.apply();
    this.privAgreed = pPrivAgreed;
  }

  /**
   * <p>onClick handler.</p>
   * @param pTarget button
   */
  @Override
  public final void onClick(final View pTarget) {
    if (this.actionPerforming == NOACT) {
      if (this.srvState == null && pTarget == this.btnStart) {
        startMan();
      } else if (pTarget == this.btnStart) {
        if (!this.srvState.getBootEmbd().getIsStarted()) {
          this.actionPerforming = STARTING;
          if (!this.srvState.getIsKeystoreCreated()) {
            try {
              this.srvState.setAjettyIn(Integer
                .parseInt(etAjettyIn.getText().toString()));
            } catch (Exception e) {
              this.log.error(null, getClass(), "Error!", e);
            }
          }
          if (this.srvState.getAjettyIn() == null) {
            Toast.makeText(getApplicationContext(), getResources()
              .getString(R.string.EnterAjettyNumber), Toast.LENGTH_SHORT).show();
            this.actionPerforming = NOACT;
            return;
          }
          refreshView();
          try {
            startAjetty();
          } catch (Exception e) {
            String msg = getResources().getString(R.string.cantStart);
            this.log.error(null, getClass(), msg, e);
            Toast.makeText(getApplicationContext(), msg,
              Toast.LENGTH_LONG).show();
          }
          refreshView();
        }
      } else if (pTarget == this.btnStop) {
        if (this.srvState.getBootEmbd().getIsStarted()) {
          this.actionPerforming = STOPPING;
          refreshView();
          Intent intent = new Intent(this, SrvAccJet.class);
          intent.setAction(SrvAccJet.ACTION_STOP);
          ContextCompat.startForegroundService(this, intent);
          refreshView();
        }
      } else if (pTarget == this.btnStartBrowser) {
        startBrowser();
      } else if (pTarget == this.btnPrivacy) {
        showPrivDlg();
      } else if (pTarget == this.btnPerm) {
        revIsPermOk();
      } else if (this.srvState.getBootEmbd().getIsStarted()) {
        if (pTarget == this.btnExpCaCe) {
          exportCaCe();
        } else if (pTarget == this.btnExpPubExch) {
          exportPubExch();
        } else if (pTarget == this.btnImpPubExch) {
          impPubExch();
        } else if (pTarget == this.btnExpLog) {
          exportLog();
        } else if (pTarget == this.btnExpEnLog) {
          exportEnLog();
        } else if (pTarget == this.btnExpSqlt) {
          exportSqlt();
        } else if (pTarget == this.btnExpEnSqlt) {
          exportEnSqlt();
        } else if (pTarget == this.btnExpEnSqltSg) {
          exportEnSqltSg();
        } else if (pTarget == this.btnExpEnSqltSk) {
          exportEnSqltSk();
        } else if (pTarget == this.btnExpEnSqltSks) {
          exportEnSqltSks();
        } else if (pTarget == this.btnImpSqlite) {
          impSqlite();
        } else if (pTarget == this.btnImpEnSqlt) {
          impEnSqlt();
        } else if (pTarget == this.btnImpEnSqltSg) {
          impEnSqltSg();
        } else if (pTarget == this.btnImpEnSqltSk) {
          impEnSqltSk();
        } else if (pTarget == this.btnImpEnSqltSks) {
          impEnSqltSks();
        }
      }
    }
  }

  /**
   * <p>onResume handler.</p>
   */
  @Override
  public final void onResume() {
    isNeedsToRefresh = true;
    new Refresher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
      (Void[]) null);
    super.onResume();
  }

  /**
   * <p>onPause handler.</p>
   */
  @Override
  public final void onPause() {
    this.isNeedsToRefresh = false;
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      this.log.error(null, getClass(), "Error!", e);
    }
    super.onPause();
  }

  /**
   * <p>Checks that permissions granted and starts server.
   * It must be invoked only by user clicking.</p>
   */
  public final void startMan() {
    AppPlus appPlus = (AppPlus) getApplicationContext();
    try {
      LogFile lg = new LogFile();
      lg.setPath(getFilesDir().getAbsolutePath() + "/bseisst");
      lg.setClsImm(true);
      //it will fail without permissions:
      lg.info(null, getClass(), "Logger created: " + lg.getPath());
      this.log = lg;
      this.srvState = new SrvState();
      this.srvState.setLog(this.log);
    } catch (Exception e) {
      Toast.makeText(getApplicationContext(), getResources()
        .getString(R.string.errStartLog), Toast.LENGTH_LONG).show();
      this.log.error(null, getClass(), "Can't create starter file log", e);
    }
    if (this.srvState != null) {
      try {
        initSrv();
        appPlus.getBeansMap().put(SrvState.class.getSimpleName(),
          this.srvState);
      } catch (Exception e) {
        this.srvState = null;
        this.log.error(null, getClass(), "Cant create server", e);
      }
    }
  }

  /**
   * <p>Lazy get is privacy policy accepted.</p>
   * @return if privacy policy accepted
   **/
  public Boolean lazPrivAgreed() {
    if (this.privAgreed == null) {
      this.privAgreed = getPreferences(Context.MODE_PRIVATE)
        .getBoolean(PRIVAGREE, false);
    }
    return this.privAgreed;
  }

  /**
   * <p>Called when the server is dead.</p>
   * @throws Exception an Exception
   */
  public final void initSrv() throws Exception {
    try {
      this.srvState.setCryptoService(new CryptoService());
      File jettyBase = new File(getFilesDir().getAbsolutePath()
       + "/" + APP_BASE);
      PackageInfo packageInfo = getPackageManager()
        .getPackageInfo(getPackageName(), 0);
      String nameFileVersion = "version" + packageInfo.versionCode;
      File fileVersion = new File(getFilesDir().getAbsolutePath()
       + "/" + APP_BASE + "/" + nameFileVersion);
      if (!jettyBase.exists()) { //new install
        if (!jettyBase.mkdirs()) {
          String msg = "Can't create dir " + jettyBase;
          this.log.error(null, getClass(), msg);
          throw new ExcCode(ExcCode.WR, msg);
        }
        copyAssets(APP_BASE);
        Toast.makeText(getApplicationContext(), getResources()
          .getString(R.string.dirCrFlCop), Toast.LENGTH_SHORT).show();
        if (!fileVersion.createNewFile()) {
          String msg = "Cant't create file " + fileVersion;
          this.log.error(null, getClass(), msg);
          throw new ExcCode(ExcCode.WR, msg);
        }
      } else if (!fileVersion.exists()) { // upgrade
        onPrivSaveClick(false);
        //hard coded deleting jquery-3.3.1.min.js:
        File oldJquery = new File(getFilesDir().getAbsolutePath()
         + "/" + APP_BASE + "/js/" + "jquery-3.3.1.min.js");
        if (oldJquery.exists()) {
          if (!oldJquery.delete()) {
            this.log.error(null, getClass(), "Can't delete " + oldJquery);
          } else {
            this.log.info(null, getClass(), "Deleted " + oldJquery);
          }
        }
        oldJquery = new File(getFilesDir().getAbsolutePath()
         + "/" + APP_BASE + "/js/" + "jquery-3.4.1.min.js");
        if (oldJquery.exists()) {
          if (!oldJquery.delete()) {
            this.log.error(null, getClass(), "Can't delete " + oldJquery);
          } else {
            this.log.info(null, getClass(), "Deleted " + oldJquery);
          }
        }
        copyAssets(APP_BASE); // refresh from upgrade package
        if (!fileVersion.createNewFile()) {
          String msg = "Cant't create file " + fileVersion;
          this.log.error(null, getClass(), msg);
          throw new ExcCode(ExcCode.WR, msg);
        }
        Toast.makeText(getApplicationContext(), getResources()
          .getString(R.string.newFlCop), Toast.LENGTH_SHORT).show();
      }
    } catch (ExcCode e) {
      this.log.error(null, getClass(), null, e);
      Toast.makeText(getApplicationContext(),
        e.getShMsg(), Toast.LENGTH_LONG).show();
      throw e;
    } catch (Exception e) {
      this.log.error(null, getClass(), null, e);
      Toast.makeText(getApplicationContext(), getResources()
        .getString(R.string.wasErr), Toast.LENGTH_LONG).show();
      throw e;
    }
    // keystore placed into [webappdir-parent]/ks folder:
    File ksDir = new File(getFilesDir().getAbsolutePath() + "/ks");
    if (!ksDir.exists() && !ksDir.mkdir()) {
      String msg = getResources().getString(R.string.cantCrDir) + ": " + ksDir;
      this.log.error(null, getClass(), msg);
      Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
      throw new ExcCode(ExcCode.WR, msg);
    }
    File[] lstFl = ksDir.listFiles();
    String nmpref = "ajettykeystore.";
    if (lstFl != null) {
      if (lstFl.length > 1
        || lstFl.length == 1 && !lstFl[0].isFile()) {
        String msg = getResources().getString(R.string.ksDirRules);
        this.log.error(null, getClass(), msg);
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
      } else if (lstFl.length == 1 && lstFl[0].isFile()
        && lstFl[0].getName().startsWith(nmpref)) {
        String ajettyInStr = lstFl[0].getName().replace(nmpref, "");
        this.srvState.setAjettyIn(Integer.parseInt(ajettyInStr));
        this.srvState.setIsKeystoreCreated(true);
      }
    }
    this.srvState.getBootEmbd().setCryptoProviderName("BC");
    this.srvState.getBootEmbd()
      .setFactoryAppBeans(this.srvState.getJetFctApp());
    this.srvState.getBootEmbd().setWebAppPath(getFilesDir().getAbsolutePath()
       + "/" + APP_BASE);
    try {
      this.srvState.getCryptoService().init();
    } catch (Exception e) {
      String msg = getResources().getString(R.string.cantInitCrypto);
      this.log.error(null, getClass(), msg);
      Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
      throw e;
    }
  }

  /**
   * <p>Shows privacy policy dialog.</p>
   **/
  public final void showPrivDlg() {
    Priv priv = new Priv();
    priv.show(getSupportFragmentManager(), "priv");
    //priv.show(getManager(), "priv");
  }

  private static final int RQC_EXP_CAPEM = 43;

  private final void exportCaCe() {
    String flNm = "ajetty-ca" + this.srvState.getAjettyIn() + ".pem";
    File pemFl = new File(getFilesDir().getAbsolutePath() + "/" + flNm);
    if (pemFl.exists()) {
      Intent intent = new Intent(SDK19ACTION_CREATE_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("application/octet-stream");
      intent.putExtra(Intent.EXTRA_TITLE, flNm);
      startActivityForResult(intent, RQC_EXP_CAPEM);
    }
  }

  private static final int RQC_EXP_PUBEXCH = 44;

  private final void exportPubExch() {
    String flNm = "ajetty-file-exch" + this.srvState.getAjettyIn() + ".kpub";
    File peFl = new File(getFilesDir().getAbsolutePath() + "/" + flNm);
    if (peFl.exists()) {
      Intent intent = new Intent(SDK19ACTION_CREATE_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("application/octet-stream");
      intent.putExtra(Intent.EXTRA_TITLE, flNm);
      startActivityForResult(intent, RQC_EXP_PUBEXCH);
    }
  }

  private static final int RQC_EXP_LOGEISST0 = 52;

  private static final int RQC_EXP_LOGEISST1 = 53;

  private static final int RQC_EXP_JETLOG0 = 54;

  private static final int RQC_EXP_JETLOG1 = 55;

  private static final int RQC_EXP_LOGSTD0 = 56;

  private static final int RQC_EXP_LOGSTD1 = 57;

  private static final int RQC_EXP_LOGSEC0 = 58;

  private static final int RQC_EXP_LOGSEC1 = 59;

  private final void exportLog() {
    String res = (String) cmbExpLog.getSelectedItem();
    String flPth = null;
    String flNm = null;
    int reqCd = -1;
    if (res.equals(LOG_BSEISST0)) {
      flPth = getFilesDir().getAbsolutePath() + "/";
      flNm = LOG_BSEISST0;
      reqCd = RQC_EXP_LOGEISST0;
    } else if (res.equals(LOG_BSEISST1)) {
      flPth = getFilesDir().getAbsolutePath() + "/";
      flNm = LOG_BSEISST1;
      reqCd = RQC_EXP_LOGEISST1;
    } else if (res.equals(AJETTY_EIS0_LOG)) {
      flPth = getFilesDir().getAbsolutePath() + "/";
      flNm = AJETTY_EIS0_LOG;
      reqCd = RQC_EXP_JETLOG0;
    } else if (res.equals(AJETTY_EIS1_LOG)) {
      flPth = getFilesDir().getAbsolutePath() + "/";
      flNm = AJETTY_EIS1_LOG;
      reqCd = RQC_EXP_JETLOG1;
    } else if (res.equals(LOG_STD0)) {
      flPth = getFilesDir().getAbsolutePath() + "/" + APP_BASE + "/";
      flNm = LOG_STD0;
      reqCd = RQC_EXP_LOGSTD0;
    } else if (res.equals(LOG_STD1)) {
      flPth = getFilesDir().getAbsolutePath() + "/" + APP_BASE + "/";
      flNm = LOG_STD1;
      reqCd = RQC_EXP_LOGSTD1;
    } else if (res.equals(LOG_SEC0)) {
      flPth = getFilesDir().getAbsolutePath() + "/" + APP_BASE + "/";
      flNm = LOG_SEC0;
      reqCd = RQC_EXP_LOGSEC0;
    } else if (res.equals(LOG_SEC1)) {
      flPth = getFilesDir().getAbsolutePath() + "/" + APP_BASE + "/";
      flNm = LOG_SEC1;
      reqCd = RQC_EXP_LOGSEC1;
    }
    if (flNm == null) {
      return;
    }
    File fl = new File(flPth + flNm);
    if (fl.exists()) {
      Intent intent = new Intent(SDK19ACTION_CREATE_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("text/plain");
      intent.putExtra(Intent.EXTRA_TITLE, flNm);
      startActivityForResult(intent, reqCd);
    }
  }

  private static final int RQC_EXP_LOGSTDEN0 = 60;

  private static final int RQC_EXP_LOGSTDEN1 = 61;

  private static final int RQC_EXP_LOGSECEN0 = 62;

  private static final int RQC_EXP_LOGSECEN1 = 63;

  private static final int RQC_EXP_LOGSTDENSG0 = 64;

  private static final int RQC_EXP_LOGSTDENSG1 = 65;

  private static final int RQC_EXP_LOGSECENSG0 = 66;

  private static final int RQC_EXP_LOGSECENSG1 = 67;

  private static final int RQC_EXP_LOGSTDENSK0 = 68;

  private static final int RQC_EXP_LOGSTDENSK1 = 69;

  private static final int RQC_EXP_LOGSECENSK0 = 70;

  private static final int RQC_EXP_LOGSECENSK1 = 71;

  private static final int RQC_EXP_LOGSTDENSKS0 = 72;

  private static final int RQC_EXP_LOGSTDENSKS1 = 73;

  private static final int RQC_EXP_LOGSECENSKS0 = 74;

  private static final int RQC_EXP_LOGSECENSKS1 = 75;

  private final void exportEnLog() {
    String res = (String) cmbExpEnLog.getSelectedItem();
    String flNm = null;
    int reqCd = -1;
    if (res.equals(LOG_STDEN0)) {
      flNm = LOG_STDEN0;
      reqCd = RQC_EXP_LOGSTDEN0;
    } else if (res.equals(LOG_STDEN1)) {
      flNm = LOG_STDEN1;
      reqCd = RQC_EXP_LOGSTDEN1;
    } else if (res.equals(LOG_SECEN0)) {
      flNm = LOG_SECEN0;
      reqCd = RQC_EXP_LOGSECEN0;
    } else if (res.equals(LOG_SECEN1)) {
      flNm = LOG_SECEN1;
      reqCd = RQC_EXP_LOGSECEN1;
    } else if (res.equals(LOG_STDENSG0)) {
      flNm = LOG_STDENSG0;
      reqCd = RQC_EXP_LOGSTDENSG0;
    } else if (res.equals(LOG_STDENSG1)) {
      flNm = LOG_STDENSG1;
      reqCd = RQC_EXP_LOGSTDENSG1;
    } else if (res.equals(LOG_SECENSG0)) {
      flNm = LOG_SECENSG0;
      reqCd = RQC_EXP_LOGSECENSG0;
    } else if (res.equals(LOG_SECENSG1)) {
      flNm = LOG_SECENSG1;
      reqCd = RQC_EXP_LOGSECENSG1;
    } else if (res.equals(LOG_STDENSK0)) {
      flNm = LOG_STDENSK0;
      reqCd = RQC_EXP_LOGSTDENSK0;
    } else if (res.equals(LOG_STDENSK1)) {
      flNm = LOG_STDENSK1;
      reqCd = RQC_EXP_LOGSTDENSK1;
    } else if (res.equals(LOG_SECENSK0)) {
      flNm = LOG_SECENSK0;
      reqCd = RQC_EXP_LOGSECENSK0;
    } else if (res.equals(LOG_SECENSK1)) {
      flNm = LOG_SECENSK1;
      reqCd = RQC_EXP_LOGSECENSK1;
    } else if (res.equals(LOG_STDENSKS0)) {
      flNm = LOG_STDENSKS0;
      reqCd = RQC_EXP_LOGSTDENSKS0;
    } else if (res.equals(LOG_STDENSKS1)) {
      flNm = LOG_STDENSKS1;
      reqCd = RQC_EXP_LOGSTDENSKS1;
    } else if (res.equals(LOG_SECENSKS0)) {
      flNm = LOG_SECENSKS0;
      reqCd = RQC_EXP_LOGSECENSKS0;
    } else if (res.equals(LOG_SECENSKS1)) {
      flNm = LOG_SECENSKS1;
      reqCd = RQC_EXP_LOGSECENSKS1;
    }
    if (flNm == null) {
      return;
    }
    File fl = new File(getFilesDir().getAbsolutePath() + "/Bseis/" + flNm);
    if (fl.exists()) {
      Intent intent = new Intent(SDK19ACTION_CREATE_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("application/octet-stream");
      intent.putExtra(Intent.EXTRA_TITLE, flNm);
      startActivityForResult(intent, reqCd);
    }
  }

  private static final int RQC_EXP_SQLT = 76;

  private final void exportSqlt() {
    String flNm = (String) cmbExpSqlt.getSelectedItem();
    if (flNm.equals("-")) {
      return;
    }
    File fl = new File(getFilesDir().getAbsolutePath() + "/" + APP_BASE + "/" + flNm);
    if (fl.exists()) {
      Intent intent = new Intent(SDK19ACTION_CREATE_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("application/octet-stream");
      intent.putExtra(Intent.EXTRA_TITLE, flNm);
      startActivityForResult(intent, RQC_EXP_SQLT);
    }
  }

  private static final int RQC_EXP_ENSQLT = 77;

  private final void exportEnSqlt() {
    String flNm = (String) cmbExpEnSqlt.getSelectedItem();
    if (flNm.equals("-")) {
      return;
    }
    File fl = new File(getFilesDir().getAbsolutePath() + "/Bseis/" + flNm);
    if (fl.exists()) {
      Intent intent = new Intent(SDK19ACTION_CREATE_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("application/octet-stream");
      intent.putExtra(Intent.EXTRA_TITLE, flNm);
      startActivityForResult(intent, RQC_EXP_ENSQLT);
    }
  }

  private static final int RQC_EXP_ENSQLTSG = 78;

  private final void exportEnSqltSg() {
    String flNm = (String) cmbExpEnSqltSg.getSelectedItem();
    if (flNm.equals("-")) {
      return;
    }
    File fl = new File(getFilesDir().getAbsolutePath() + "/Bseis/" + flNm);
    if (fl.exists()) {
      Intent intent = new Intent(SDK19ACTION_CREATE_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("application/octet-stream");
      intent.putExtra(Intent.EXTRA_TITLE, flNm);
      startActivityForResult(intent, RQC_EXP_ENSQLTSG);
    }
  }

  private static final int RQC_EXP_ENSQLTSK = 79;

  private final void exportEnSqltSk() {
    String flNm = (String) cmbExpEnSqltSk.getSelectedItem();
    if (flNm.equals("-")) {
      return;
    }
    File fl = new File(getFilesDir().getAbsolutePath() + "/Bseis/" + flNm);
    if (fl.exists()) {
      Intent intent = new Intent(SDK19ACTION_CREATE_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("application/octet-stream");
      intent.putExtra(Intent.EXTRA_TITLE, flNm);
      startActivityForResult(intent, RQC_EXP_ENSQLTSK);
    }
  }

  private static final int RQC_EXP_ENSQLTSKS = 80;

  private final void exportEnSqltSks() {
    String flNm = (String) cmbExpEnSqltSks.getSelectedItem();
    if (flNm.equals("-")) {
      return;
    }
    File fl = new File(getFilesDir().getAbsolutePath() + "/Bseis/" + flNm);
    if (fl.exists()) {
      Intent intent = new Intent(SDK19ACTION_CREATE_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("application/octet-stream");
      intent.putExtra(Intent.EXTRA_TITLE, flNm);
      startActivityForResult(intent, RQC_EXP_ENSQLTSKS);
    }
  }

  private static final int RQC_IMP_PUBEXCH = 175;

  private final void impPubExch() {
    Intent intent = new Intent(SDK19ACTION_OPEN_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("application/octet-stream");
    intent.putExtra(Intent.EXTRA_TITLE, "*.kpub");
    startActivityForResult(intent, RQC_IMP_PUBEXCH);
  }

  private static final int RQC_IMP_SQLITE = 176;

  private final void impSqlite() {
    Intent intent = new Intent(SDK19ACTION_OPEN_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("application/octet-stream");
    intent.putExtra(Intent.EXTRA_TITLE, "*.sqlite");
    startActivityForResult(intent, RQC_IMP_SQLITE);
  }

  private static final int RQC_IMP_ENSQLT = 177;

  private final void impEnSqlt() {
    Intent intent = new Intent(SDK19ACTION_OPEN_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("application/octet-stream");
    intent.putExtra(Intent.EXTRA_TITLE, "*.sqlten");
    startActivityForResult(intent, RQC_IMP_ENSQLT);
  }

  private static final int RQC_IMP_ENSQLTSG = 178;

  private final void impEnSqltSg() {
    Intent intent = new Intent(SDK19ACTION_OPEN_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("application/octet-stream");
    intent.putExtra(Intent.EXTRA_TITLE, "*.sqlten.sig");
    startActivityForResult(intent, RQC_IMP_ENSQLTSG);
  }

  private static final int RQC_IMP_ENSQLTSK = 179;

  private final void impEnSqltSk() {
    Intent intent = new Intent(SDK19ACTION_OPEN_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("application/octet-stream");
    intent.putExtra(Intent.EXTRA_TITLE, "*.sqlten.sken");
    startActivityForResult(intent, RQC_IMP_ENSQLTSK);
  }

  private static final int RQC_IMP_ENSQLTSKS = 180;

  private final void impEnSqltSks() {
    Intent intent = new Intent(SDK19ACTION_OPEN_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("application/octet-stream");
    intent.putExtra(Intent.EXTRA_TITLE, "*.sqlten.sken.sig");
    startActivityForResult(intent, RQC_IMP_ENSQLTSKS);
  }

  @Override
  public void onActivityResult(int pRcCd, int pRsCd, Intent pRsDt) {
    if (pRsCd != Activity.RESULT_OK || pRsDt == null) {
      return;
    }
    Uri uri = pRsDt.getData();
    String flNm = null;
    int impCd = -1;
    int expCd = -1;
    if (pRcCd == RQC_EXP_CAPEM) {
      expCd = RQC_EXP_CAPEM;
      flNm = getFilesDir().getAbsolutePath()
        + "/ajetty-ca" + this.srvState.getAjettyIn() + ".pem";
    } else if (pRcCd == RQC_EXP_PUBEXCH) {
      expCd = RQC_EXP_PUBEXCH;
      flNm = getFilesDir().getAbsolutePath()
        + "/ajetty-file-exch" + this.srvState.getAjettyIn() + ".kpub";
    } else if (pRcCd == RQC_EXP_JETLOG0) {
      expCd = RQC_EXP_JETLOG0;
      flNm = getFilesDir().getAbsolutePath() + "/" + AJETTY_EIS0_LOG;
    } else if (pRcCd == RQC_EXP_JETLOG1) {
      expCd = RQC_EXP_JETLOG1;
      flNm = getFilesDir().getAbsolutePath() + "/" + AJETTY_EIS1_LOG;
    } else if (pRcCd == RQC_EXP_LOGEISST0) {
      expCd = RQC_EXP_LOGEISST0;
      flNm = getFilesDir().getAbsolutePath() + "/" + LOG_BSEISST0;
    } else if (pRcCd == RQC_EXP_LOGEISST1) {
      expCd = RQC_EXP_LOGEISST1;
      flNm = getFilesDir().getAbsolutePath() + "/" + LOG_BSEISST1;
    } else if (pRcCd == RQC_EXP_LOGSTD0) {
      expCd = RQC_EXP_LOGSTD0;
      flNm = getFilesDir().getAbsolutePath() + "/" + APP_BASE + "/" + LOG_STD0;
    } else if (pRcCd == RQC_EXP_LOGSTD1) {
      expCd = RQC_EXP_LOGSTD1;
      flNm = getFilesDir().getAbsolutePath() + "/" + APP_BASE + "/" + LOG_STD1;
    } else if (pRcCd == RQC_EXP_LOGSEC0) {
      expCd = RQC_EXP_LOGSEC0;
      flNm = getFilesDir().getAbsolutePath() + "/" + APP_BASE + "/" + LOG_SEC0;
    } else if (pRcCd == RQC_EXP_LOGSEC1) {
      expCd = RQC_EXP_LOGSEC1;
      flNm = getFilesDir().getAbsolutePath() + "/" + APP_BASE + "/" + LOG_SEC1;
    } else if (pRcCd == RQC_EXP_LOGSTDEN0) {
      expCd = RQC_EXP_LOGSTDEN0;
      flNm = getFilesDir().getAbsolutePath() + "/Bseis/" + LOG_STDEN0;
    } else if (pRcCd == RQC_EXP_LOGSTDEN1) {
      expCd = RQC_EXP_LOGSTDEN1;
      flNm = getFilesDir().getAbsolutePath() + "/Bseis/" + LOG_STDEN1;
    } else if (pRcCd == RQC_EXP_LOGSECEN0) {
      expCd = RQC_EXP_LOGSECEN0;
      flNm = getFilesDir().getAbsolutePath() + "/Bseis/" + LOG_SECEN0;
    } else if (pRcCd == RQC_EXP_LOGSECEN1) {
      expCd = RQC_EXP_LOGSECEN1;
      flNm = getFilesDir().getAbsolutePath() + "/Bseis/" + LOG_SECEN1;
    } else if (pRcCd == RQC_EXP_LOGSTDENSG0) {
      expCd = RQC_EXP_LOGSTDENSG0;
      flNm = getFilesDir().getAbsolutePath() + "/Bseis/" + LOG_STDENSG0;
    } else if (pRcCd == RQC_EXP_LOGSTDENSG1) {
      expCd = RQC_EXP_LOGSTDENSG1;
      flNm = getFilesDir().getAbsolutePath() + "/Bseis/" + LOG_STDENSG1;
    } else if (pRcCd == RQC_EXP_LOGSECENSG0) {
      expCd = RQC_EXP_LOGSECENSG0;
      flNm = getFilesDir().getAbsolutePath() + "/Bseis/" + LOG_SECENSG0;
    } else if (pRcCd == RQC_EXP_LOGSECENSG1) {
      expCd = RQC_EXP_LOGSECENSG1;
      flNm = getFilesDir().getAbsolutePath() + "/Bseis/" + LOG_SECENSG1;
    } else if (pRcCd == RQC_EXP_LOGSTDENSK0) {
      expCd = RQC_EXP_LOGSTDENSK0;
      flNm = getFilesDir().getAbsolutePath() + "/Bseis/" + LOG_STDENSK0;
    } else if (pRcCd == RQC_EXP_LOGSTDENSK1) {
      expCd = RQC_EXP_LOGSTDENSK1;
      flNm = getFilesDir().getAbsolutePath() + "/Bseis/" + LOG_STDENSK1;
    } else if (pRcCd == RQC_EXP_LOGSECENSK0) {
      expCd = RQC_EXP_LOGSECENSK0;
      flNm = getFilesDir().getAbsolutePath() + "/Bseis/" + LOG_SECENSK0;
    } else if (pRcCd == RQC_EXP_LOGSECENSK1) {
      expCd = RQC_EXP_LOGSECENSK1;
      flNm = getFilesDir().getAbsolutePath() + "/Bseis/" + LOG_SECENSK1;
    } else if (pRcCd == RQC_EXP_LOGSTDENSKS0) {
      expCd = RQC_EXP_LOGSTDENSKS0;
      flNm = getFilesDir().getAbsolutePath() + "/Bseis/" + LOG_STDENSKS0;
    } else if (pRcCd == RQC_EXP_LOGSTDENSKS1) {
      expCd = RQC_EXP_LOGSTDENSKS1;
      flNm = getFilesDir().getAbsolutePath() + "/Bseis/" + LOG_STDENSKS1;
    } else if (pRcCd == RQC_EXP_LOGSECENSKS0) {
      expCd = RQC_EXP_LOGSECENSKS0;
      flNm = getFilesDir().getAbsolutePath() + "/Bseis/" + LOG_SECENSKS0;
    } else if (pRcCd == RQC_EXP_LOGSECENSKS1) {
      expCd = RQC_EXP_LOGSECENSKS1;
      flNm = getFilesDir().getAbsolutePath() + "/Bseis/" + LOG_SECENSKS1;
    } else if (pRcCd == RQC_EXP_SQLT) {
      expCd = RQC_EXP_SQLT;
      flNm = (String) cmbExpSqlt.getSelectedItem();
      if (flNm.equals("-")) {
        return;
      }
      flNm = getFilesDir().getAbsolutePath() + "/" + APP_BASE + "/" + flNm;
    } else if (pRcCd == RQC_EXP_ENSQLT) {
      expCd = RQC_EXP_ENSQLT;
      flNm = (String) cmbExpEnSqlt.getSelectedItem();
      if (flNm.equals("-")) {
        return;
      }
      flNm = getFilesDir().getAbsolutePath() + "/Bseis/" + flNm;
    } else if (pRcCd == RQC_EXP_ENSQLTSG) {
      expCd = RQC_EXP_ENSQLTSG;
      flNm = (String) cmbExpEnSqltSg.getSelectedItem();
      if (flNm.equals("-")) {
        return;
      }
      flNm = getFilesDir().getAbsolutePath() + "/Bseis/" + flNm;
    } else if (pRcCd == RQC_EXP_ENSQLTSK) {
      expCd = RQC_EXP_ENSQLTSK;
      flNm = (String) cmbExpEnSqltSk.getSelectedItem();
      if (flNm.equals("-")) {
        return;
      }
      flNm = getFilesDir().getAbsolutePath() + "/Bseis/" + flNm;
    } else if (pRcCd == RQC_EXP_ENSQLTSKS) {
      expCd = RQC_EXP_ENSQLTSKS;
      flNm = (String) cmbExpEnSqltSks.getSelectedItem();
      if (flNm.equals("-")) {
        return;
      }
      flNm = getFilesDir().getAbsolutePath() + "/Bseis/" + flNm;
    } else if (pRcCd == RQC_IMP_SQLITE) {
      impCd = RQC_IMP_SQLITE;
      String dbpth = getFilesDir().getAbsolutePath()
        .replace("files", "databases"); //this is Android's DB's hardcoded dir!
      Cursor retCu = getContentResolver().query(uri, null, null, null, null);
      int nmIdx = retCu.getColumnIndex(OpenableColumns.DISPLAY_NAME);
      retCu.moveToFirst();
      flNm = dbpth + "/" + retCu.getString(nmIdx);
    } else if (pRcCd == RQC_IMP_PUBEXCH) {
      impCd = RQC_IMP_PUBEXCH;
      String dirPth = getFilesDir().getAbsolutePath() + "/Bseis/pub-exch";
      File dir = new File(dirPth);
      if (!dir.exists() && !dir.mkdirs()) {
        String msg = "Cant't create dir " + dir;
        this.log.error(null, getClass(), msg);
        return;
      }
      File[] fls = dir.listFiles();
      if (fls != null) {
        for (int i = 0; i < fls.length; i++) {
          if (fls[i] != null && !fls[i].delete()) {
            String msg = "Cant't delete file " + fls[i];
            this.log.error(null, getClass(), msg);
          }
        }
      }
      Cursor retCu = getContentResolver().query(uri, null, null, null, null);
      int nmIdx = retCu.getColumnIndex(OpenableColumns.DISPLAY_NAME);
      retCu.moveToFirst();
      flNm = dirPth + "/" + retCu.getString(nmIdx);
    } else if (pRcCd == RQC_IMP_ENSQLT || pRcCd == RQC_IMP_ENSQLTSG
      || pRcCd == RQC_IMP_ENSQLTSK || pRcCd == RQC_IMP_ENSQLTSKS) {
      impCd = pRcCd;
      String dirPth = getFilesDir().getAbsolutePath() + "/Bseis";
      File dir = new File(dirPth);
      if (!dir.exists() && !dir.mkdirs()) {
        String msg = "Cant't create dir " + dir;
        this.log.error(null, getClass(), msg);
        return;
      }
      Cursor retCu = getContentResolver().query(uri, null, null, null, null);
      int nmIdx = retCu.getColumnIndex(OpenableColumns.DISPLAY_NAME);
      retCu.moveToFirst();
      flNm = dirPth + "/" + retCu.getString(nmIdx);
    } else {
      this.log.error(null, getClass(), "Unknown activity request code:" + pRcCd);
    }
    if (flNm != null) {
      if (expCd != -1) {
        File fl = new File(flNm);
        if (fl.exists()) {
          copyFileTo(fl, uri);
        }
      } else if (impCd != -1) {
        File fl = new File(flNm);
        copyFileFrom(uri, fl);
      }
    }
  }

  private boolean copyFileTo(File pFile, Uri pUriTo) {
    OutputStream outs = null;
    FileInputStream ins = null;
    ParcelFileDescriptor pfd = null;
    boolean res = false;
    try {
      ins = new FileInputStream(pFile);
      pfd = getContentResolver().openFileDescriptor(pUriTo, "w");
      FileOutputStream fous = new FileOutputStream(pfd.getFileDescriptor());
      outs = new BufferedOutputStream(fous);
      byte[] data = new byte[1024];
      int count;
      while ((count = ins.read(data)) != -1) {
        outs.write(data, 0, count);
      }
      outs.flush();
      res = true;
    } catch (Exception e) {
      res = false;
      this.log.error(null, getClass(), null, e);
    } finally {
      if (pfd != null) {
        try {
          pfd.close();
        } catch (Exception e1) {
          this.log.error(null, getClass(), "Error!", e1);
        }
      }
      if (ins != null) {
        try {
          ins.close();
        } catch (Exception e2) {
          this.log.error(null, getClass(), "Error!", e2);
        }
      }
      if (outs != null) {
        try {
          outs.close();
        } catch (Exception e3) {
          this.log.error(null, getClass(), "Error!", e3);
        }
      }
    }
    return res;
  }

  private void copyFileFrom(Uri pUriFr, File pFile) {
    OutputStream outs = null;
    FileInputStream ins = null;
    ParcelFileDescriptor pfd = null;
    try {
      pfd = getContentResolver().openFileDescriptor(pUriFr, "r");
      ins = new FileInputStream(pfd.getFileDescriptor());
      FileOutputStream fous = new FileOutputStream(pFile);
      outs = new BufferedOutputStream(fous);
      byte[] data = new byte[1024];
      int count;
      while ((count = ins.read(data)) != -1) {
        outs.write(data, 0, count);
      }
      outs.flush();
      Toast.makeText(getApplicationContext(), getResources()
        .getString(R.string.fileImpd) + ": " + pFile.getAbsolutePath()
          , Toast.LENGTH_LONG).show();
    } catch (Exception e) {
      this.log.error(null, getClass(), null, e);
    } finally {
      if (pfd != null) {
        try {
          pfd.close();
        } catch (Exception e1) {
          this.log.error(null, getClass(), "Error!", e1);
        }
      }
      if (ins != null) {
        try {
          ins.close();
        } catch (Exception e2) {
          this.log.error(null, getClass(), "Error!", e2);
        }
      }
      if (outs != null) {
        try {
          outs.close();
        } catch (Exception e3) {
          this.log.error(null, getClass(), "Error!", e3);
        }
      }
    }
  }

  /**
   * <p>It starts A-Jetty.</p>
   * @throws Exception an Exception
   **/
  public final void startAjetty() throws Exception {
    if (this.etKsPassw.getText() == null) {
      Toast.makeText(getApplicationContext(), getResources()
        .getString(R.string.enterPassw), Toast.LENGTH_SHORT).show();
      this.actionPerforming = NOACT;
      return;
    }
    char[] ksPassword = this.etKsPassw.getText().toString().toCharArray();
    File pks12File = new File(getFilesDir().getAbsolutePath()
      + "/ks/ajettykeystore." + this.srvState.getAjettyIn());
    KeyStore pkcs12Store = null;
    if (!pks12File.exists()) {
      if (this.etKsPasswRep.getText() == null) {
        Toast.makeText(getApplicationContext(), getResources()
          .getString(R.string.enterPassw), Toast.LENGTH_SHORT).show();
        this.actionPerforming = NOACT;
        return;
      }
      char[] ksPasswordc = this.etKsPasswRep.getText().toString().toCharArray();
      boolean noMatch = false;
      if (ksPassword.length != ksPasswordc.length) {
        noMatch = true;
      } else {
        for (int i = 0; i < ksPassword.length; i++) {
          if (ksPassword[i] != ksPasswordc[i]) {
            noMatch = true;
            break;
          }
        }
      }
      if (noMatch) {
        Toast.makeText(getApplicationContext(), getResources()
          .getString(R.string.passwNoMatch), Toast.LENGTH_SHORT).show();
        this.actionPerforming = NOACT;
        return;
      }
      String isPswStrRez = this.srvState.getCryptoService()
        .isPasswordStrong(ksPassword);
      if (isPswStrRez != null) {
        Toast.makeText(getApplicationContext(),
          isPswStrRez, Toast.LENGTH_LONG).show();
        this.actionPerforming = NOACT;
        return;
      }
      this.srvState.getCryptoService().createKeyStoreWithCredentials(
        getFilesDir().getAbsolutePath() + "/ks",
          this.srvState.getAjettyIn(), ksPassword);
      FileInputStream fis = null;
      Certificate certCa = null;
      PublicKey fileExchPub = null;
      try {
        pkcs12Store = KeyStore.getInstance("PKCS12", "BC");
        fis = new FileInputStream(pks12File);
        pkcs12Store.load(fis, ksPassword);
        this.srvState.setIsKeystoreCreated(true);
        certCa = pkcs12Store.getCertificate("AJettyCa"
          + this.srvState.getAjettyIn());
        fileExchPub = pkcs12Store.getCertificate("AJettyFileExch"
          + this.srvState.getAjettyIn()).getPublicKey();
      } finally {
        if (fis != null) {
          try {
            fis.close();
          } catch (Exception e2) {
            this.log.error(null, getClass(), null, e2);
          }
        }
      }
      if (certCa != null) {
        File pemFl = new File(getFilesDir().getAbsolutePath() + "/ajetty-ca"
            + this.srvState.getAjettyIn() + ".pem");
        JcaPEMWriter pemWriter = null;
        try {
          OutputStreamWriter osw = new OutputStreamWriter(
            new FileOutputStream(pemFl), Charset.forName("ASCII").newEncoder());
          pemWriter = new JcaPEMWriter(osw);
          pemWriter.writeObject(certCa);
          pemWriter.flush();
        } finally {
          if (pemWriter != null) {
            try {
              pemWriter.close();
            } catch (Exception e2) {
              this.log.error(null, getClass(), null, e2);
            }
          }
        }
        File pubFl = new File(getFilesDir().getAbsolutePath()
          + "/ajetty-file-exch"
            + this.srvState.getAjettyIn() + ".kpub");
        FileOutputStream fos = null;
        try {
          fos = new FileOutputStream(pubFl);
          fos.write(fileExchPub.getEncoded());
          fos.flush();
          Toast.makeText(getApplicationContext(), getResources()
            .getString(R.string.ajettycacopied), Toast.LENGTH_SHORT).show();
        } finally {
          if (fos != null) {
            try {
              fos.close();
            } catch (Exception e2) {
              this.log.error(null, getClass(), null, e2);
            }
          }
        }
      }
    } else {
      FileInputStream fis = null;
      try {
        pkcs12Store = KeyStore.getInstance("PKCS12", "BC");
        fis = new FileInputStream(pks12File);
        pkcs12Store.load(fis, ksPassword);
      } catch (Exception e) {
        pkcs12Store = null;
        this.log.error(null, getClass(), null, e);
      } finally {
        if (fis != null) {
          try {
            fis.close();
          } catch (Exception e2) {
            this.log.error(null, getClass(), null, e2);
          }
        }
      }
      if (pkcs12Store == null) {
        Toast.makeText(getApplicationContext(), getResources()
          .getString(R.string.passwordWrong), Toast.LENGTH_SHORT).show();
        this.actionPerforming = NOACT;
        return;
      }
    }
    this.srvState.getBootEmbd().setHttpsAlias(
      "AJettyHttps" + this.srvState.getAjettyIn());
    this.srvState.getBootEmbd().setPkcs12File(pks12File);
    this.srvState.getBootEmbd().setPassword(new String(ksPassword));
    this.srvState.getBootEmbd().setKeyStore(pkcs12Store);
    this.srvState.getBootEmbd().setAjettyIn(this.srvState.getAjettyIn());
    this.srvState.getBootEmbd().setPort((Integer) cmbPort.getSelectedItem());
    Toast.makeText(getApplicationContext(), getResources().getString(
      R.string.sendingStart), Toast.LENGTH_SHORT).show();
    Intent intent = new Intent(this, SrvAccJet.class);
    intent.setAction(SrvAccJet.ACTION_START);
    ContextCompat.startForegroundService(this, intent);
  }

  /**
   * <p>Start browser.</p>
   */
  private void startBrowser() {
    String url = this.btnStartBrowser.getText().toString();
    Intent i = new Intent(Intent.ACTION_VIEW);
    i.setData(Uri.parse(url));
    startActivity(i);
  }

  /**
   * <p>Refresh view.</p>
   */
  private void refreshView() {
    if (this.srvState == null) {
      this.cmbPort.setEnabled(false);
      this.etAjettyIn.setEnabled(false);
      this.etKsPassw.setEnabled(false);
      this.etKsPasswRep.setEnabled(false);
      this.btnStop.setEnabled(false);
      this.btnStartBrowser.setEnabled(false);
      this.btnStartBrowser.setText("");
      this.btnStart.setEnabled(this.privAgreed != null && this.privAgreed);
    } else {
      if (this.actionPerforming == STARTING && !this.srvState.getBootEmbd()
      .getIsStarted() || this.actionPerforming == STOPPING
        && this.srvState.getBootEmbd().getIsStarted()) {
        this.cmbPort.setEnabled(false);
        this.etAjettyIn.setEnabled(false);
        this.etKsPassw.setEnabled(false);
        this.etKsPasswRep.setEnabled(false);
        this.btnStart.setEnabled(false);
        this.btnStop.setEnabled(false);
        this.btnStartBrowser.setEnabled(false);
        if (this.actionPerforming == STARTING) {
          this.btnStartBrowser.setText(getResources()
            .getString(R.string.starting));
        } else {
          this.btnStartBrowser.setText(getResources()
            .getString(R.string.stopping));
        }
      } else {
        if (this.srvState.getBootEmbd().getIsStarted()) {
          if (this.actionPerforming == STARTING) {
            this.actionPerforming = NOACT;
          }
          this.cmbPort.setEnabled(false);
          this.btnStart.setEnabled(false);
          this.etAjettyIn.setEnabled(false);
          if (this.srvState.getIsKeystoreCreated()) {
            this.etAjettyIn.setText(this.srvState.getAjettyIn().toString());
          }
          this.etKsPassw.setEnabled(false);
          this.etKsPasswRep.setEnabled(false);
          this.btnStop.setEnabled(true);
          this.btnImpEnSqlt.setEnabled(true);
          this.btnImpEnSqltSg.setEnabled(true);
          this.btnImpEnSqltSk.setEnabled(true);
          this.btnImpEnSqltSks.setEnabled(true);
          this.btnImpPubExch.setEnabled(true);
          this.btnImpSqlite.setEnabled(true);
          this.btnExpCaCe.setEnabled(true);
          this.btnExpEnLog.setEnabled(true);
          this.btnExpEnSqlt.setEnabled(true);
          this.btnExpEnSqltSg.setEnabled(true);
          this.btnExpEnSqltSk.setEnabled(true);
          this.btnExpEnSqltSks.setEnabled(true);
          this.btnExpLog.setEnabled(true);
          this.btnExpPubExch.setEnabled(true);
          this.btnExpSqlt.setEnabled(true);
          this.btnStartBrowser.setEnabled(true);
          this.btnStartBrowser.setText("https://localhost:"
          + this.cmbPort.getSelectedItem() + "/bsa"
              + this.cmbPort.getSelectedItem());
        } else {
          if (this.actionPerforming == STOPPING) {
            this.actionPerforming = NOACT;
          }
          boolean prvAcp = this.privAgreed != null && this.privAgreed;
          if (this.srvState.getIsKeystoreCreated()) {
            this.etAjettyIn.setEnabled(false);
            this.etKsPasswRep.setEnabled(false);
            this.etAjettyIn.setText(this.srvState.getAjettyIn().toString());
          } else {
            this.etAjettyIn.setEnabled(prvAcp);
            this.etKsPasswRep.setEnabled(prvAcp);
          }
          this.etKsPassw.setEnabled(prvAcp);
          this.cmbPort.setEnabled(prvAcp);
          this.btnStart.setEnabled(prvAcp);
          this.btnStop.setEnabled(false);
          this.btnImpEnSqlt.setEnabled(false);
          this.btnImpEnSqltSg.setEnabled(false);
          this.btnImpEnSqltSk.setEnabled(false);
          this.btnImpEnSqltSks.setEnabled(false);
          this.btnImpPubExch.setEnabled(false);
          this.btnImpSqlite.setEnabled(false);
          this.btnExpCaCe.setEnabled(false);
          this.btnExpEnLog.setEnabled(false);
          this.btnExpEnSqlt.setEnabled(false);
          this.btnExpEnSqltSg.setEnabled(false);
          this.btnExpEnSqltSk.setEnabled(false);
          this.btnExpEnSqltSks.setEnabled(false);
          this.btnExpLog.setEnabled(false);
          this.btnExpPubExch.setEnabled(false);
          this.btnExpSqlt.setEnabled(false);
          this.btnStartBrowser.setEnabled(false);
          this.btnStartBrowser.setText("");
        }
      }
    }
  }

  /**
   * <p>Recursively copy assets.</p>
   * @param pCurrDir current directory assets
   * @throws Exception an Exception
   */
  private void copyAssets(final String pCurrDir) throws Exception {
    AssetManager assetManager = getAssets();
    String[] files = assetManager.list(pCurrDir);
    for (String flNm : files) {
      String createdPath = getFilesDir().getAbsolutePath()
        + "/" + pCurrDir + "/" + flNm;
      if (!flNm.contains(".")) {
        File subdir = new File(createdPath);
        if (!subdir.exists()) {
          if (!subdir.mkdirs()) {
            String msg = "Cant't create dir " + subdir;
            this.log.error(null, getClass(), msg);
            throw new ExcCode(ExcCode.WR, msg);
          } else {
            this.log.info(null, getClass(),
              "Created : " + subdir);
          }
        }
        copyAssets(pCurrDir + "/" + flNm);
      } else {
        InputStream ins = null;
        OutputStream outs = null;
        try {
          ins = getAssets().open(pCurrDir + "/" + flNm);
          outs = new BufferedOutputStream(
            new FileOutputStream(createdPath));
          byte[] data = new byte[1024];
          int count;
          while ((count = ins.read(data)) != -1) {
            outs.write(data, 0, count);
          }
          outs.flush();
          this.log.info(null, getClass(),
            "Copied: " + pCurrDir + "/" + flNm);
        } finally {
          if (ins != null) {
            try {
              ins.close();
            } catch (Exception e2) {
              this.log.error(null, getClass(), "Error!", e2);
            }
          }
          if (outs != null) {
            try {
              outs.close();
            } catch (Exception e3) {
              this.log.error(null, getClass(), "Error!", e3);
            }
          }
        }
      }
    }
  }

  /**
   * <p>Long term obviously useless decision.
   * https://github.com/aosp-mirror/platform_frameworks_base/blob/master/core/res/AndroidManifest.xml
   * shows that permissions are not dangerous.
   * But this always goes to non-granted? Maybe because:
     * These permissions
     * must be requested in your manifest, they should not be granted to your app,
     * and they should have protection level {@link
     * android.content.pm.PermissionInfo#PROTECTION_DANGEROUS dangerous}
   * but during install/update user already asked and accept all permissions!
   * In app-settings user can do this.
   * </p>
   **/
  private boolean revIsPermOk() {
    if (this.isPermOk) {
      return this.isPermOk;
    }
    if (ContextCompat.checkSelfPermission(this, Manifest.permission
      .INTERNET) != PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(this,
          SDK28FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[] {SDK28FOREGROUND_SERVICE,
          Manifest.permission.INTERNET},
            PERMISSIONS_REQUESTS);
    } else {
      this.log.info(null, getClass(), getResources().getString(R.string.permAlGr));
      Toast.makeText(getApplicationContext(), getResources()
        .getString(R.string.permAlGr), Toast.LENGTH_LONG).show();
      this.isPermOk = true;
    }
    return this.isPermOk;
  }

  @Override
  public void onRequestPermissionsResult(int pRqCd, String[] pPerms,
          int[] pGrRess) {
    String prmstr = "Request permissions code=" + pRqCd + "; perms[";
    for (int i = 0; i < pPerms.length; i++) {
      prmstr += " " + pPerms[i];
    }
    prmstr += " ]; grand rss[";
    for (int i = 0; i < pGrRess.length; i++) {
      prmstr += " " + pGrRess[i];
    }
    prmstr += " ]";
    this.log.info(null, getClass(), prmstr);
    if (pRqCd == PERMISSIONS_REQUESTS) {
      if (pGrRess.length > 0 &&
                   pGrRess[0] == PackageManager.PERMISSION_GRANTED) {
        this.log.info(null, getClass(), getResources().getString(R.string.permGr));
        Toast.makeText(getApplicationContext(), getResources()
          .getString(R.string.permGr), Toast.LENGTH_LONG).show();
        this.isPermOk = true;
      } else {
        this.log.info(null, getClass(), getResources().getString(R.string.permNoGr));
        Toast.makeText(getApplicationContext(), getResources()
          .getString(R.string.permNoGr), Toast.LENGTH_LONG).show();
        this.isPermOk = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.perm))
          .setTitle(getResources().getString(R.string.permTi))
            .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface pDlg, int pBtn) {
                pDlg.dismiss();
              }});
        builder.create().show();
      }
    }
  }

  /**
   * <p>Refresher thread.</p>
   */
  private class Refresher extends AsyncTask<Void, Void, Void> {

    /**
     * <p>doInBackground check is need refresh.</p>
     */
    @Override
    protected final Void doInBackground(final Void... params) {
      while (Bsa.this.isNeedsToRefresh) {
        publishProgress((Void[]) null);
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          Bsa.this.log.error(null, getClass(), "error!", e);
        }
      }
      return null;
    }

    /**
     * <p>onProgressUpdate call refresh.</p>
     */
    @Override
    protected final void onProgressUpdate(final Void... values) {
      Bsa.this.refreshView();
      super.onProgressUpdate(values);
    }
  }
}
