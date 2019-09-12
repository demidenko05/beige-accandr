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
import java.util.Map;
import java.lang.reflect.Method;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.nio.charset.Charset;

import android.content.ContextWrapper;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Build;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;
import android.net.Uri;
import android.Manifest;
import android.util.Log;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import org.beigesoft.exc.ExcCode;
import org.beigesoft.ajetty.FctAppEmb;
import org.beigesoft.ajetty.BootEmbed;
import org.beigesoft.ajetty.crypto.CryptoService;
import org.beigesoft.log.ILog;
import org.beigesoft.log.LogFile;
import org.beigesoft.loga.Loga;

/**
 * <p>Beige Accounting Jetty activity.</p>
 *
 * @author Yury Demidenko
 */
public class Bsa extends Activity implements OnClickListener {

  /**
   * <p>APP BASE dir.</p>
   **/
  public static final String APP_BASE = "webapp";

  /**
   * <p>Permissions request.</p>
   **/
  public static final int PERMISSIONS_REQUESTS = 2415;

  /**
   * <p>Flag to refresh UI.</p>
   **/
  private boolean isNeedsToRefresh;

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
   * <p>Combo-Box Port.</p>
   **/
  private Spinner cmbPort;

  /**
   * <p>Application beans map reference to lock.</p>
   **/
  private Map<String, Object> beansMap;

  /**
   * <p>A-Jetty application beans factory.</p>
   **/
  private final FctAppEmb jettyFactoryAppBeans =
    new FctAppEmb();

  /**
   * <p>Bootstrap.</p>
   **/
  private final BootEmbed bootStrapEmbeddedHttps =
    new BootEmbed();

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
   * <p>A-Jetty instance number.</p>
   **/
  private Integer ajettyIn;

  /**
   * <p>Crypto service.</p>
   **/
  private CryptoService cryptoService;

  /**
   * <p>Flag is starting.</p>
   **/
  private boolean isStarting;

  /**
   * <p>Flag is stopping.</p>
   **/
  private boolean isStopping;

  /**
   * <p>Flag is keystore created.</p>
   **/
  private boolean isKeystoreCreated;

  /**
   * <p>Log.</p>
   **/
  private ILog log;

  /**
   * <p>Loga.</p>
   **/
  private Loga loga = new Loga();

  /**
   * <p>Called when the activity is first created or recreated.</p>
   * @param pSavedInstanceState Saved Instance State
   */
  @Override
  public final void onCreate(final Bundle pSavedInstanceState) {
    super.onCreate(pSavedInstanceState);
    //Simple reflection way to avoid additional compile libraries
    if (android.os.Build.VERSION.SDK_INT >= 23) {
      try {
        Class[] argTypes = new Class[] {String.class};
        Method checkSelfPermission = ContextWrapper.class
          .getDeclaredMethod("checkSelfPermission", argTypes);
        Object result = checkSelfPermission.invoke(getApplicationContext(),
          Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Integer chSlfPer = (Integer) result;
        if (chSlfPer != PackageManager.PERMISSION_GRANTED) {
          argTypes = new Class[] {String[].class, Integer.TYPE};
          Method requestPermissions = Activity.class
            .getDeclaredMethod("requestPermissions", argTypes);
          String[] args = new String[]
            {Manifest.permission.READ_EXTERNAL_STORAGE,
              Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET};
          requestPermissions.invoke(this, (Object) args,
            PERMISSIONS_REQUESTS);
        }
      } catch (Exception e) {
        this.loga.error(null, getClass(),
          "Cant get permissions", e);
      }
    }
    if (this.log == null) {
      try {
        LogFile lg = new LogFile();
        lg.setPath(Environment.getExternalStorageDirectory()
          .getAbsolutePath() + "/bseisst");
        lg.setClsImm(true);
        this.log = lg;
      } catch (Exception e) {
        this.log = new Loga();
        this.log.error(null, getClass(),
          "Cant create starter file log", e);
      }
    }
    try {
      AppPlus appPlus = (AppPlus) getApplicationContext();
      this.beansMap = appPlus.getBeansMap();
      setContentView(R.layout.beigeaccounting);
      this.etAjettyIn = (EditText) findViewById(R.id.etAjettyIn);
      this.etKsPassw = (EditText) findViewById(R.id.etKsPassw);
      this.etKsPasswRep = (EditText) findViewById(R.id.etKsPasswRep);
      this.cmbPort = (Spinner) findViewById(R.id.cmbPort);
      ArrayAdapter<Integer> cmbAdapter =
        new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item);
      cmbAdapter.add(new Integer(8443));
      cmbAdapter.add(new Integer(8444));
      cmbAdapter.add(new Integer(8445));
      cmbAdapter.add(new Integer(8446));
      cmbPort.setAdapter(cmbAdapter);
      cmbPort.setSelection(0);
      this.btnStart = (Button) findViewById(R.id.btnStart);
      this.btnStartBrowser = (Button) findViewById(R.id.btnStartBrowser);
      this.btnStartBrowser.setOnClickListener(this);
      this.btnStop = (Button) findViewById(R.id.btnStop);
      this.btnStart.setOnClickListener(this);
      this.btnStop.setOnClickListener(this);
    } catch (Exception e) {
      log.error(null, getClass(),
        "Cant create interface", e);
    }
    try {
      this.cryptoService = new CryptoService();
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
        Toast.makeText(getApplicationContext(),
          getResources().getString(R.string.dirCrFlCop),
            Toast.LENGTH_SHORT).show();
        if (!fileVersion.createNewFile()) {
          String msg = "Cant't create file " + fileVersion;
          this.log.error(null, getClass(), msg);
          throw new ExcCode(ExcCode.WR, msg);
        }
      } else if (!fileVersion.exists()) { // upgrade
        copyAssets(APP_BASE); // refresh from upgrade package
        if (!fileVersion.createNewFile()) {
          String msg = "Cant't create file " + fileVersion;
          this.log.error(null, getClass(), msg);
          throw new ExcCode(ExcCode.WR, msg);
        }
        Toast.makeText(getApplicationContext(),
          getResources().getString(R.string.newFlCop),
            Toast.LENGTH_SHORT).show();
      }
    } catch (ExcCode e) {
      this.log.error(null, getClass(), null, e);
      Toast.makeText(getApplicationContext(),
        e.getShMsg(), Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
      this.log.error(null, getClass(), null, e);
      Toast.makeText(getApplicationContext(),
        getResources().getString(R.string.wasErr),
          Toast.LENGTH_SHORT).show();
    }
    // keystore placed into [webappdir-parent]/ks folder:
    File ksDir = new File(getFilesDir().getAbsolutePath() + "/ks");
    if (!ksDir.exists() && !ksDir.mkdir()) {
      String msg = getResources().getString(R.string.cantCrDir) + ": " + ksDir;
      this.log.error(null, getClass(), msg);
      Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
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
        this.ajettyIn = Integer.parseInt(ajettyInStr);
        this.isKeystoreCreated = true;
      }
    }
    this.bootStrapEmbeddedHttps.setCryptoProviderName("BC");
    this.bootStrapEmbeddedHttps.setFactoryAppBeans(this.jettyFactoryAppBeans);
    this.bootStrapEmbeddedHttps.setWebAppPath(getFilesDir().getAbsolutePath()
       + "/" + APP_BASE);
    try {
      this.cryptoService.init();
    } catch (Exception e) {
      String msg = getResources().getString(R.string.cantInitCrypto);
      this.log.error(null, getClass(), msg);
      Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
    this.beansMap.put(BootEmbed.class.getCanonicalName(),
      this.bootStrapEmbeddedHttps);
  }

  /**
   * <p>onClick handler.</p>
   * @param pTarget button
   */
  @Override
  public final void onClick(final View pTarget) {
    if (!this.isStarting && !this.isStopping) {
      if (pTarget == this.btnStart
        && !this.bootStrapEmbeddedHttps.getIsStarted()) {
        this.isStarting = true;
        if (!this.isKeystoreCreated) {
          try {
            this.ajettyIn = Integer.parseInt(etAjettyIn.getText().toString());
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        if (this.ajettyIn == null) {
          Toast.makeText(getApplicationContext(),
            getResources().getString(R.string.EnterAjettyNumber),
              Toast.LENGTH_SHORT).show();
          this.isStarting = false;
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
        && this.bootStrapEmbeddedHttps.getIsStarted()) {
        this.isStopping = true;
        refreshView();
        Intent intent = new Intent(this, SrvAccJet.class);
        intent.setAction(SrvAccJet.ACTION_STOP);
        startService(intent);
        refreshView();
      } else if (pTarget == this.btnStartBrowser) {
        startBrowser();
      }
    }
  }

  /**
   * <p>It starts A-Jetty.</p>
   * @throws Exception an Exception
   **/
  public final void startAjetty() throws Exception {
    if (this.etKsPassw.getText() == null) {
      Toast.makeText(getApplicationContext(),
        getResources().getString(R.string.enterPassw),
          Toast.LENGTH_SHORT).show();
      this.isStarting = false;
      return;
    }
    char[] ksPassword = this.etKsPassw.getText().toString().toCharArray();
    File pks12File = new File(getFilesDir().getAbsolutePath()
      + "/ks/ajettykeystore." + this.ajettyIn);
    KeyStore pkcs12Store = null;
    if (!pks12File.exists()) {
      if (this.etKsPasswRep.getText() == null) {
        Toast.makeText(getApplicationContext(),
          getResources().getString(R.string.enterPassw),
            Toast.LENGTH_SHORT).show();
        this.isStarting = false;
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
        Toast.makeText(getApplicationContext(),
          getResources().getString(R.string.passwNoMatch),
            Toast.LENGTH_SHORT).show();
        this.isStarting = false;
        return;
      }
      String isPswStrRez = this.cryptoService.isPasswordStrong(ksPassword);
      if (isPswStrRez != null) {
        Toast.makeText(getApplicationContext(),
          isPswStrRez, Toast.LENGTH_SHORT).show();
        this.isStarting = false;
        return;
      }
      this.cryptoService.createKeyStoreWithCredentials(getFilesDir()
        .getAbsolutePath() + "/ks", this.ajettyIn, ksPassword);
      FileInputStream fis = null;
      Certificate certCa = null;
      PublicKey fileExchPub = null;
      try {
        pkcs12Store = KeyStore.getInstance("PKCS12", "BC");
        fis = new FileInputStream(pks12File);
        pkcs12Store.load(fis, ksPassword);
        this.isKeystoreCreated = true;
        certCa = pkcs12Store.getCertificate("AJettyCa" + this.ajettyIn);
        fileExchPub = pkcs12Store
          .getCertificate("AJettyFileExch" + this.ajettyIn).getPublicKey();
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
        File pemFl = new File(Environment.getExternalStorageDirectory()
    .getAbsolutePath() + File.separator + "ajetty-ca" + this.ajettyIn + ".pem");
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
        File pubFl = new File(Environment.getExternalStorageDirectory()
          + File.separator + "ajetty-file-exch" + this.ajettyIn + ".kpub");
        FileOutputStream fos = null;
        try {
          fos = new FileOutputStream(pubFl);
          fos.write(fileExchPub.getEncoded());
          fos.flush();
          Toast.makeText(getApplicationContext(),
            getResources().getString(R.string.ajettycacopied),
              Toast.LENGTH_SHORT).show();
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
        Toast.makeText(getApplicationContext(),
          getResources().getString(R.string.passwordWrong),
            Toast.LENGTH_SHORT).show();
        this.isStarting = false;
        return;
      }
    }
    this.bootStrapEmbeddedHttps.setHttpsAlias("AJettyHttps" + this.ajettyIn);
    this.bootStrapEmbeddedHttps.setPkcs12File(pks12File);
    this.bootStrapEmbeddedHttps.setPassword(new String(ksPassword));
    this.bootStrapEmbeddedHttps.setKeyStore(pkcs12Store);
    this.bootStrapEmbeddedHttps.setAjettyIn(this.ajettyIn);
    this.bootStrapEmbeddedHttps.setPort((Integer) cmbPort.getSelectedItem());
    Toast.makeText(getApplicationContext(), getResources().getString(
      R.string.sendingStart), Toast.LENGTH_SHORT)
        .show();
    Intent intent = new Intent(this, SrvAccJet.class);
    intent.setAction(SrvAccJet.ACTION_START);
    startService(intent);
  }

  /**
   * <p>onResume handler.</p>
   */
  @Override
  public final void onResume() {
    isNeedsToRefresh = true;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      new Refresher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
        (Void[]) null);
    } else {
      new Refresher().execute((Void[]) null);
    }
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
      e.printStackTrace();
    }
    super.onPause();
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
    if (this.isStarting && !this.bootStrapEmbeddedHttps.getIsStarted()
      || this.isStopping && this.bootStrapEmbeddedHttps.getIsStarted()) {
      this.cmbPort.setEnabled(false);
      this.etAjettyIn.setEnabled(false);
      this.etKsPassw.setEnabled(false);
      this.etKsPasswRep.setEnabled(false);
      this.btnStart.setEnabled(false);
      this.btnStop.setEnabled(false);
      this.btnStartBrowser.setEnabled(false);
      if (this.isStarting) {
        this.btnStartBrowser.setText(getResources()
          .getString(R.string.starting));
      } else {
        this.btnStartBrowser.setText(getResources()
          .getString(R.string.stopping));
      }
    } else {
      if (this.bootStrapEmbeddedHttps.getIsStarted()) {
        if (this.isStarting) {
          this.isStarting = false;
        }
        this.cmbPort.setEnabled(false);
        this.btnStart.setEnabled(false);
        this.etAjettyIn.setEnabled(false);
        this.etKsPassw.setEnabled(false);
        this.etKsPasswRep.setEnabled(false);
        this.btnStop.setEnabled(true);
        this.btnStartBrowser.setEnabled(true);
        this.btnStartBrowser.setText("https://localhost:"
        + this.cmbPort.getSelectedItem() + "/bsa"
            + this.cmbPort.getSelectedItem());
      } else {
        if (this.isStopping) {
          this.isStopping = false;
        }
        if (this.isKeystoreCreated) {
          this.etAjettyIn.setEnabled(false);
          this.etKsPasswRep.setEnabled(false);
          this.etAjettyIn.setText(this.ajettyIn.toString());
        } else {
          this.etAjettyIn.setEnabled(true);
          this.etKsPasswRep.setEnabled(true);
        }
        this.etKsPassw.setEnabled(true);
        this.cmbPort.setEnabled(true);
        this.btnStart.setEnabled(true);
        this.btnStop.setEnabled(false);
        this.btnStartBrowser.setEnabled(false);
        this.btnStartBrowser.setText("");
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
            Log.i(getClass().getSimpleName(),
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
          Log.i(getClass().getSimpleName(),
            "Copied: " + pCurrDir + "/" + fileName);
        } finally {
          if (ins != null) {
            try {
              ins.close();
            } catch (Exception e2) {
              e2.printStackTrace();
            }
          }
          if (outs != null) {
            try {
              outs.close();
            } catch (Exception e3) {
              e3.printStackTrace();
            }
          }
        }
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
          e.printStackTrace();
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

  //SGS:
  /**
   * <p>Getter for log.</p>
   * @return ILog
   **/
  public final ILog getLog() {
    return this.log;
  }

  /**
   * <p>Setter for log.</p>
   * @param pLog reference
   **/
  public final void setLog(final ILog pLog) {
    this.log = pLog;
  }
}
