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

package org.beigesoft.andr;

import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import org.beigesoft.fct.IFctAux;
import org.beigesoft.fct.FctBlc;
import org.beigesoft.rdb.IRdb;

/**
 * <p>Auxiliary factory for Android RDBMS service.</p>
 *
 * @author Yury Demidenko
 */
public class FctRdba implements IFctAux<Cursor> {

  /**
   * <p>Android context.</p>
   **/
  private Context cntx;

  /**
   * <p>SQLiteOpenHelper one for all threads.</p>
   **/
  private SQLiteOpenHelper sqliteOh;

  /**
   * <p>Creates requested bean and put into given main factory.
   * The main factory is already synchronized when invokes this.</p>
   * @param pRqVs request scoped vars
   * @param pBnNm - bean name
   * @param pFctApp main factory
   * @return Object - requested bean or NULL
   * @throws Exception - an exception
   */
  @Override
  public final Object crePut(final Map<String, Object> pRqVs,
    final String pBnNm, final FctBlc<Cursor> pFctApp) throws Exception {
    Object rz = null;
    if (IRdb.class.getSimpleName().equals(pBnNm)) {
      rz = crPuRdb(pRqVs, pFctApp);
    }
    return rz;
  }

  /**
   * <p>Releases state when main factory is releasing.</p>
   * @param pRqVs request scoped vars
   * @param pFctApp main factory
   * @throws Exception - an exception
   */
  @Override
  public final void release(final Map<String, Object> pRqVs,
    final FctBlc<Cursor> pFctApp) throws Exception {
    if (this.sqliteOh != null) {
      pFctApp.lazLogStd(null).info(pRqVs, getClass(), "Try close DB...");
      try {
        this.sqliteOh.close();
      } catch (Exception ex) {
        ex.printStackTrace();
      } finally {
        this.sqliteOh = null;
      }
    }
  }

  /**
   * <p>Creates and puts into MF Rdb.</p>
   * @param pRqVs request scoped vars
   * @param pFctApp main factory
   * @return Rdb
   * @throws Exception - an exception
   */
  private Rdba crPuRdb(final Map<String, Object> pRqVs,
    final FctBlc<Cursor> pFctApp) throws Exception {
    Rdba rdb = new Rdba();
    rdb.setLog(pFctApp.lazLogStd(pRqVs));
    rdb.setSrvClVl(pFctApp.lazSrvClVl(pRqVs));
    this.sqliteOh = new SqltOpHp(this.cntx, pFctApp.getFctDt().getDbUrl(),
      null, 1);
    rdb.setSqliteOh(this.sqliteOh);
    pFctApp.put(pRqVs, IRdb.class.getSimpleName(), rdb);
    pFctApp.lazLogStd(pRqVs).info(pRqVs, getClass(), IRdb.class.getSimpleName()
      + " has been created");
    return rdb;
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
