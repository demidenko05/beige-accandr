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
import java.io.File;

import android.os.Environment;
import android.content.Context;
import android.database.Cursor;

import org.beigesoft.fct.IFctAux;
import org.beigesoft.fct.FctBlc;
import org.beigesoft.rdb.IRdb;
import org.beigesoft.ajetty.IHpCrypt;
import org.beigesoft.ajetty.HpCrypt;
import org.beigesoft.ajetty.UsrPwd;
import org.beigesoft.ajetty.HnFirstUsr;

/**
 * <p>Auxiliary factory for A-Jetty additional services.</p>
 *
 * @author Yury Demidenko
 */
public class FctAndr implements IFctAux<Cursor> {

  /**
   * <p>Android context.</p>
   **/
  private Context cntx;

  /**
   * <p>Creates requested bean and put into given main factory.
   * The main factory is already synchronized when invokes this.</p>
   * @param pRvs request scoped vars
   * @param pBnNm - bean name
   * @param pFctApp main factory
   * @return Object - requested bean or NULL
   * @throws Exception - an exception
   */
  @Override
  public final Object crePut(final Map<String, Object> pRvs,
    final String pBnNm, final FctBlc<Cursor> pFctApp) throws Exception {
    Object rz = null;
    if (HnFirstUsr.class.getSimpleName().equals(pBnNm)) {
      rz = crPuHnFirstUsr(pRvs, pFctApp);
    }
    if (UsrPwd.class.getSimpleName().equals(pBnNm)) {
      rz = crPuUsrPwd(pRvs, pFctApp);
    }
    if (IHpCrypt.class.getSimpleName().equals(pBnNm)) {
      rz = crPuHpCrypt(pRvs, pFctApp);
    }
    return rz;
  }

  /**
   * <p>Releases state when main factory is releasing.</p>
   * @param pRvs request scoped vars
   * @param pFctApp main factory
   * @throws Exception - an exception
   */
  @Override
  public final void release(final Map<String, Object> pRvs,
    final FctBlc<Cursor> pFctApp) throws Exception {
    //nothing
  }

  /**
   * <p>Creates and puts into MF HnFirstUsr.</p>
   * @param pRvs request scoped vars
   * @param pFctApp main factory
   * @return HnFirstUsr
   * @throws Exception - an exception
   */
  private HnFirstUsr crPuHnFirstUsr(final Map<String, Object> pRvs,
    final FctBlc<Cursor> pFctApp) throws Exception {
    HnFirstUsr rz = new HnFirstUsr();
    @SuppressWarnings("unchecked")
    UsrPwd<Cursor> upw = (UsrPwd<Cursor>) pFctApp
      .laz(pRvs, UsrPwd.class.getSimpleName());
    rz.setUsrPwd(upw);
    pFctApp.put(pRvs, HnFirstUsr.class.getSimpleName(), rz);
    pFctApp.lazLogStd(pRvs).info(pRvs, getClass(),
      HnFirstUsr.class.getSimpleName() + " has been created");
    return rz;
  }

  /**
   * <p>Lazy gets UsrPwd.</p>
   * @param pRvs request scoped vars
   * @param pFctApp main factory
   * @return UsrPwd
   * @throws Exception - an exception
   */
  private UsrPwd<Cursor> crPuUsrPwd(final Map<String, Object> pRvs,
    final FctBlc<Cursor> pFctApp) throws Exception {
    UsrPwd<Cursor> rz = new UsrPwd<Cursor>();
    @SuppressWarnings("unchecked")
    IRdb<Cursor> rdb = (IRdb<Cursor>) pFctApp
      .laz(pRvs, IRdb.class.getSimpleName());
    rz.setRdb(rdb);
    pFctApp.put(pRvs, UsrPwd.class.getSimpleName(), rz);
    pFctApp.lazLogStd(pRvs).info(pRvs, getClass(),
      UsrPwd.class.getSimpleName() + " has been created");
    return rz;
  }

  /**
   * <p>Lazy gets HpCrypt.</p>
   * @param pRvs request scoped vars
   * @param pFctApp main factory
   * @return HpCrypt
   * @throws Exception - an exception
   */
  private HpCrypt crPuHpCrypt(final Map<String, Object> pRvs,
    final FctBlc pFctApp) throws Exception {
    HpCrypt rz = new HpCrypt();
    rz.setKsDirPath(this.cntx.getFilesDir().getAbsolutePath() + "/ks");
    File bkDir = new File(Environment.getExternalStorageDirectory()
      .getAbsolutePath() + "/BsaBk");
    if (!bkDir.exists() && !bkDir.mkdirs()) {
      throw new Exception("Can't create directory: " + bkDir);
    }
    File peDir = new File(bkDir + "/pub-exch");
    if (!peDir.exists() && !peDir.mkdir()) {
      throw new Exception("Can't create directory: " + peDir);
    }
    rz.setPublicKeyDir(peDir.getPath());
    pFctApp.put(pRvs, IHpCrypt.class.getSimpleName(), rz);
    pFctApp.lazLogStd(pRvs).info(pRvs, getClass(),
      HpCrypt.class.getSimpleName() + " has been created");
    return rz;
  }

  //Simple getters and setters:
  /**
   * <p>Getter for cntx.</p>
   * @return Context
   **/
  public final Context getCntx() {
    return this.cntx;
  }

  /**
   * <p>Setter for cntx.</p>
   * @param pCntx reference
   **/
  public final void setCntx(final Context pCntx) {
    this.cntx = pCntx;
  }
}
