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
   * <p>Combo-Box export log.</p>
   **/
  private Spinner cmbExpLog;

  /**
   * <p>Button share log file.</p>
   **/
  private Button btnExpLog;

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
   * <p>Button import SQLITE.</p>
   **/
  private Button btnImpSqlite;

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

  private static String AJETTY_EIS0_LOG = "ajetty-eis0.log";
  private static String AJETTY_EIS1_LOG = "ajetty-eis1.log";

  /**
   * <p>Called when the activity is first created or recreated.</p>
   * @param pSavedInstanceState Saved Instance State
   */
  @Override
  public final void onCreate(final Bundle pSavedInstanceState) {
    super.onCreate(pSavedInstanceState);
    this.log = new Loga();
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
      this.cmbExpLog = (Spinner) findViewById(R.id.cmbExpLog);
      ArrayAdapter<String> cmpAdExLog =
        new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
      cmpAdExLog.add("-");
      File logf = new File(getFilesDir().getAbsolutePath() + "/" + AJETTY_EIS0_LOG);
      if (logf.exists()) {
        cmpAdExLog.add(AJETTY_EIS0_LOG);
      }
      logf = new File(getFilesDir().getAbsolutePath() + "/" + AJETTY_EIS1_LOG);
      if (logf.exists()) {
        cmpAdExLog.add(AJETTY_EIS1_LOG);
      }
      this.cmbExpLog.setAdapter(cmpAdExLog);
      this.cmbExpLog.setSelection(0);
      this.btnExpLog = (Button) findViewById(R.id.btnExpLog);
      this.btnExpLog.setOnClickListener(this);
      this.btnImpSqlite = (Button) findViewById(R.id.btnImpSqlite);
      this.btnImpSqlite.setOnClickListener(this);
      this.btnPerm = (Button) findViewById(R.id.btnPerm);
      this.btnPerm.setOnClickListener(this);
    } catch (Exception e) {
      this.log.error(null, getClass(),
        "Cant create interface", e);
    }
    if (!lazPrivAgreed()) {
      showPrivDlg();
    }
    File bseisdir = new File(getFilesDir().getAbsolutePath() + "/Bseis");
    if (!bseisdir.exists() && !bseisdir.mkdirs()) {
      String msg = "Can't create dir " + bseisdir;
      Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
      return;
    }
    AppPlus appPlus = (AppPlus) getApplicationContext();
    if (appPlus.getBeansMap().size() > 0) { // onResume
      this.srvState = (SrvState) appPlus.getBeansMap()
        .get(SrvState.class.getSimpleName());
      this.log = this.srvState.getLog();
    } else {
      try {
        LogFile lg = new LogFile();
        lg.setPath(getFilesDir().getAbsolutePath() + "/Bseis/bseisst");
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
      } else if (pTarget == this.btnStart
        && !this.srvState.getBootEmbd().getIsStarted()) {
        /*if (!revIsPermOk()) {
          return;
        }*/
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
      } else if (pTarget == this.btnStop
        && this.srvState.getBootEmbd().getIsStarted()) {
        this.actionPerforming = STOPPING;
        refreshView();
        Intent intent = new Intent(this, SrvAccJet.class);
        intent.setAction(SrvAccJet.ACTION_STOP);
        ContextCompat.startForegroundService(this, intent);
        refreshView();
      } else if (pTarget == this.btnStartBrowser) {
        startBrowser();
      } else if (pTarget == this.btnPrivacy) {
        showPrivDlg();
      } else if (pTarget == this.btnExpCaCe) {
        exportCaCe();
      } else if (pTarget == this.btnExpLog) {
        exportLog();
      } else if (pTarget == this.btnPerm) {
        revIsPermOk();
      } else if (pTarget == this.btnImpSqlite) {
        impSqlite();
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
      lg.setPath(getFilesDir().getAbsolutePath() + "/Bseis/bseisst");
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

  private static final int REQCD_SEND_CAPEM = 43;

  private final void exportCaCe() {
    String fileName = "ajetty-ca" + this.srvState.getAjettyIn() + ".pem";
    File pemFl = new File(getFilesDir().getAbsolutePath() + "/Bseis/" + fileName);
    if (pemFl.exists()) {
      Intent intent = new Intent(SDK19ACTION_CREATE_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("application/octet-stream");
      intent.putExtra(Intent.EXTRA_TITLE, fileName);
      startActivityForResult(intent, REQCD_SEND_CAPEM);
    }
  }

  private static final int REQCD_SEND_JETLOG0 = 44;

  private final void exportLog() {
    String res = (String) cmbExpLog.getSelectedItem();
    if (res.equals(AJETTY_EIS0_LOG)) {
      File fl = new File(getFilesDir().getAbsolutePath() + "/" + AJETTY_EIS0_LOG);
      if (fl.exists()) {
        Intent intent = new Intent(SDK19ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, AJETTY_EIS0_LOG);
        startActivityForResult(intent, REQCD_SEND_JETLOG0);
      }
    }
  }

  private static final int REQCD_IMP_SQLITE = 45;

  private final void impSqlite() {
    Intent intent = new Intent(SDK19ACTION_OPEN_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("application/octet-stream");
    intent.putExtra(Intent.EXTRA_TITLE, "*.sqlite");
    startActivityForResult(intent, REQCD_IMP_SQLITE);
  }

  @Override
  public void onActivityResult(int pRcCd, int pRsCd, Intent pRsDt) {
    if (pRsCd != Activity.RESULT_OK || pRsDt == null) {
      return;
    }
    if (pRcCd == REQCD_SEND_CAPEM) {
      Uri uri = pRsDt.getData();
      String fileName = "ajetty-ca" + this.srvState.getAjettyIn() + ".pem";
      File pemFl = new File(getFilesDir().getAbsolutePath() + "/Bseis/" + fileName);
      if (pemFl.exists()) {
        copyFileTo(pemFl, uri);
      }
    } else if (pRcCd == REQCD_SEND_JETLOG0) {
      Uri uri = pRsDt.getData();
      File fl = new File(getFilesDir().getAbsolutePath() + "/" + AJETTY_EIS0_LOG);
      if (fl.exists()) {
        copyFileTo(fl, uri);
      }
    } else if (pRcCd == REQCD_IMP_SQLITE) {
      Uri uri = pRsDt.getData();
      String dbpth = getFilesDir().getAbsolutePath()
        .replace("files", "databases"); //TODO 0 files maybe will do but upgrade?
      Cursor retCu = getContentResolver().query(uri, null, null, null, null);
      int nmIdx = retCu.getColumnIndex(OpenableColumns.DISPLAY_NAME);
      retCu.moveToFirst();
      File fl = new File(dbpth + "/" + retCu.getString(nmIdx));
      copyFileFrom(uri, fl);
    } else {
      this.log.error(null, getClass(), "Unknown activity request code:" + pRcCd);
    }
  }

  private void copyFileTo(File pFile, Uri pUriTo) {
    OutputStream outs = null;
    FileInputStream ins = null;
    ParcelFileDescriptor pfd = null;
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
        File pemFl = new File(getFilesDir().getAbsolutePath() + "/Bseis/ajetty-ca"
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
          + "/Bseis/ajetty-file-exch"
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
    for (String fileName : files) {
      String createdPath = getFilesDir().getAbsolutePath()
        + "/" + pCurrDir + "/" + fileName;
      if (!fileName.contains(".")) {
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
        copyAssets(pCurrDir + "/" + fileName);
      } else {
        InputStream ins = null;
        OutputStream outs = null;
        try {
          ins = getAssets().open(pCurrDir + "/" + fileName);
          outs = new BufferedOutputStream(
            new FileOutputStream(createdPath));
          byte[] data = new byte[1024];
          int count;
          while ((count = ins.read(data)) != -1) {
            outs.write(data, 0, count);
          }
          outs.flush();
          this.log.info(null, getClass(),
            "Copied: " + pCurrDir + "/" + fileName);
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
