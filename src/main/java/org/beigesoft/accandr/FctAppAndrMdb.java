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

import java.util.HashSet;
import java.util.Map;
import java.security.KeyStore;

import android.content.Context;
import android.database.Cursor;

import org.eclipse.jetty.security.DataBaseLoginService;

import org.beigesoft.mdl.IRecSet;
import org.beigesoft.fct.IFctNm;
import org.beigesoft.fct.IFctAsm;
import org.beigesoft.fct.FctBlc;
import org.beigesoft.fct.FctDbCp;
import org.beigesoft.prc.IPrc;
import org.beigesoft.hld.IAttrs;
import org.beigesoft.andr.FctRdbMdb;
import org.beigesoft.rdb.IRdb;
import org.beigesoft.rdb.Orm;
import org.beigesoft.web.FctMail;
import org.beigesoft.ajetty.FctPrcNtr;
import org.beigesoft.ajetty.GetUsrCrd;
import org.beigesoft.ajetty.IHpCrypt;
import org.beigesoft.ajetty.HpCrypt;

/**
 * <p>Final configuration factory for Android.</p>
 *
 * @author Yury Demidenko
 */
public class FctAppAndrMdb implements IFctAsm<Cursor> {

  /**
   * <p>Main only factory.</p>
   **/
  private FctBlc<Cursor> fctBlc;

  /**
   * <p>Only constructor.</p>
   * @throws Exception - an exception
   */
  public FctAppAndrMdb() throws Exception {
    this.fctBlc = new FctBlc<Cursor>();
    this.fctBlc.setIsAndr(true);
  }

  /**
   * <p>Get bean in lazy mode (if bean is null then initialize it).</p>
   * @param pRqVs request scoped vars
   * @param pBnNm - bean name
   * @return Object - requested bean or exception if not found
   * @throws Exception - an exception
   */
  @Override
  public final Object laz(final Map<String, Object> pRqVs,
    final String pBnNm) throws Exception {
    return this.fctBlc.laz(pRqVs, pBnNm);
  }

  /**
   * <p>Releases memory.</p>
   * @param pRqVs request scoped vars
   * @throws Exception - an exception
   */
  @Override
  public final void release(final Map<String, Object> pRqVs) throws Exception {
    this.fctBlc.release(pRqVs);
  }


  /**
   * <p>Puts beans by external AUX factory.</p>
   * @param pRqVs request scoped vars
   * @param pBnNm - bean name
   * @param pBean - bean
   * @throws Exception - an exception, e.g. if bean exists
   **/
  @Override
  public final void put(final Map<String, Object> pRqVs,
    final String pBnNm, final Object pBean) throws Exception {
    this.fctBlc.put(pRqVs, pBnNm, pBean);
  }

  /**
   * <p>Gets main factory for setting configuration parameters.</p>
   * @return Object - requested bean
   */
  @Override
  public final FctBlc<Cursor> getFctBlc() {
    return this.fctBlc;
  }

  /**
   * <p>Initializes factory.</p>
   * @param pRvs request scoped vars
   * @param pCtxAttrs context attributes
   * @throws Exception - an exception, e.g. if bean exists
   */
  @Override
  public final void init(final Map<String, Object> pRvs,
    final IAttrs pCtxAttrs) throws Exception {
    Context cntx = (Context) pCtxAttrs.getAttr("AndrCtx");
    HashSet<IFctNm<IPrc>> fpasad = new HashSet<IFctNm<IPrc>>();
    FctPrcNtrAd fctPrcNtrAj = new FctPrcNtrAd();
    fctPrcNtrAj.setFctApp(this);
    fctPrcNtrAj.setCntx(cntx);
    fpasad.add(fctPrcNtrAj);
    this.fctBlc.setFctsPrcAd(fpasad);
    HashSet<IFctNm<IPrc>> fpas = new HashSet<IFctNm<IPrc>>();
    FctPrcNtr<Cursor> fctPrcNtrAjb = new FctPrcNtr<Cursor>();
    fctPrcNtrAjb.setFctApp(this);
    fpas.add(fctPrcNtrAjb);
    this.fctBlc.setFctsPrc(fpas);
    this.fctBlc.getFctsAux().add(new FctDbCp<Cursor>());
    this.fctBlc.getFctsAux().add(new FctMail<Cursor>());
    FctAndr fctAndr = new FctAndr();
    fctAndr.setCntx(cntx);
    this.fctBlc.getFctsAux().add(fctAndr);
    FctRdbMdb frdb = new FctRdbMdb();
    frdb.setCntx(cntx);
    this.fctBlc.getFctsAux().add(frdb);
    //creating/upgrading DB on start:
    Orm<Cursor> orm = this.fctBlc.lazOrm(pRvs);
    orm.init(pRvs);
      //free memory:
    orm.getSetng().release();
    DataBaseLoginService srvDbl = (DataBaseLoginService) pCtxAttrs
      .getAttr("JDBCRealm");
    GetUsrCrd<Cursor> srvCr = new GetUsrCrd<Cursor>();
    @SuppressWarnings("unchecked")
    IRdb<Cursor> rdb = (IRdb<Cursor>) laz(pRvs, IRdb.class.getSimpleName());
    srvCr.setRdb(rdb);
    srvDbl.setSrvGetUserCredentials(srvCr);
    srvDbl.getUsers().clear();
    //crypto init:
    HpCrypt ch = (HpCrypt) laz(pRvs, IHpCrypt.class.getSimpleName());
    KeyStore ks = (KeyStore) pCtxAttrs.getAttr("ajettyKeystore");
    ch.setKeyStore(ks);
    String passw = (String) pCtxAttrs.getAttr("ksPassword");
    ch.setKsPassword(passw.toCharArray());
    Integer ajettyIn = (Integer) pCtxAttrs.getAttr("ajettyIn");
    ch.setAjettyIn(ajettyIn);
    boolean isDbgSh = this.fctBlc.lazLogStd(pRvs).getDbgSh(getClass())
      && this.fctBlc.lazLogStd(pRvs).getDbgFl() < 13001 && this.fctBlc
        .lazLogStd(pRvs).getDbgCl() > 12999;
    if (isDbgSh) {
      IRecSet<Cursor> rs = null;
      StringBuffer sb = new StringBuffer(" compile_options:\n");
      try {
        rdb.setAcmt(false);
        rdb.setTrIsl(IRdb.TRRUC);
        rdb.begin();
        rs = rdb.retRs("PRAGMA compile_options;");
        if (rs.first()) {
          do {
            sb.append(rs.getStr("compile_options") + " | ");
          } while (rs.next());
        }
        rs.close();
        rs = rdb.retRs("PRAGMA locking_mode;");
        if (rs.first()) {
          sb.append("\nlocking_mode: " + rs.getStr("locking_mode"));
        }
        rs.close();
        rdb.commit();
        this.fctBlc.lazLogStd(pRvs).debug(pRvs, getClass(), "thread="
          + Thread.currentThread().getId() + " SQLITE settings : " + sb);
      } catch (Exception e) {
        if (!rdb.getAcmt()) {
          rdb.rollBack();
        }
        throw e;
      } finally {
        rdb.release();
      }
    }
  }
}
