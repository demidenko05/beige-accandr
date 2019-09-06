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

import java.util.List;
import java.util.Map;
import java.util.Date;

import android.database.Cursor;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import org.beigesoft.exc.ExcCode;
import org.beigesoft.mdl.IHasId;
import org.beigesoft.mdl.IRecSet;
import org.beigesoft.mdl.ColVals;
import org.beigesoft.rdb.SrvClVl;
import org.beigesoft.rdb.ARdb;

/**
 * <p>Implementation of database service on Android.
 * I.e. for A-Jetty case, user must waits for HTTP response, otherwise it
 * will received "Busy" error.
 * SQLLite makes autocommit itself: when "TRANSACTION START", then
 * autocommit is off, when "END/COMMIT TRANSACTION", then autocommit is on.
 * Here flag autocommit is used for locking during transaction and high
 * level logic, e.g. "rollback transaction (AUTC=OFF) or not (AUTC=ON)".
 * Another expensive solution according SQLite documentation is #3:
 * <pre>
 * ...
 * 2. Multi-thread. In this mode, SQLite can be safely used by multiple threads
 *  PROVIDED THAT NO SINGLE DATABASE connection is used simultaneously
 * in two or more threads.
 * 3. Serialized. In serialized mode, SQLite can be safely used by multiple
 *  threads WITH NO RESTRICTION.
 * ...
 * </pre>
 * It seems that #3 means that SQLite compiled in that way and it's used
 * serialized transaction isolation.
 * This is should be the fastest "single thread" implementation, but using
 * SQLiteOpenHelper to get DB for each thread without closing works fast and
 * with no problems, so use RdbMdb instead.
 * </p>
 *
 * @author Yury Demidenko
 */
public class Rdb extends ARdb<Cursor> {

  /**
   * <p>Generating insert/update and CV service.</p>
   **/
  private SrvClVl srvClVl;

  /**
   * <p>SQLiteDatabase one for all threads.</p>
   **/
  private SQLiteDatabase sqliteDb;

  /**
   * <p>Auto-commit flag, false means service is locked.</p>
   **/
  private boolean acmt = true;

  /**
   * <p>Locking flag, 0 - free, otherwise
   * time of starting non-autocommit transaction.</p>
   **/
  private long strt;

  /**
   * <p>Start transaction flag per thread holder.</p>
   **/
  private static final ThreadLocal<Boolean> HLDSTRT =
    new ThreadLocal<Boolean>() { };

  /**
   * <p>Get if a transaction is started.</p>
   * @throws Exception - an exception
   **/
  @Override
  public final synchronized boolean getAcmt() throws Exception {
    return this.acmt;
  }

  /**
   * <p>Sets shared autocommit flag and trying to locks this servise.</p>
   * @param pAcmt if autocommit
   * @throws Exception - an exception
   **/
  @Override
  public final synchronized void setAcmt(final boolean pAcmt) throws Exception {
    if (!pAcmt) { //begin new transaction
      long now = new Date().getTime();
      if (!this.acmt && this.strt != 0 && now - this.strt < 3000) {
         //another thread isn't completed transaction yet
        getLog().error(null, getClass(), "RDB busy!");
        throw new ExcCode(ExcCode.WRPR, "Busy");
      }
      this.acmt = pAcmt; //locking
      this.strt = now;
    } else {
      this.acmt = pAcmt;
      this.strt = 0;
    }
  }

  /**
   * <p>Sets transaction isolation level.
   * Always read-uncommited.</p>
   * @param pLevel transaction level
   * @throws Exception - an exception
   **/
  @Override
  public final synchronized void setTrIsl(
    final int pLevel) throws Exception {
    exec("PRAGMA read_uncommitted=1;");
  }

  /**
   * <p>Gets transaction isolation level.
   * Always read-uncommited.</p>
   * @return Level transaction always TRANSACTION_READ_UNCOMMITTED
   * @throws Exception - an exception
   **/
  @Override
  public final synchronized int getTrIsl() throws Exception {
    return TRRUC;
  }

  /**
   * <p>Creates savepoint.</p>
   * @param pSvpNm savepoint name
   * @throws Exception - an exception
   **/
  @Override
  public final synchronized void creSavPnt(
    final String pSvpNm) throws Exception {
    exec("SAVEPOINT " + pSvpNm + ";");
  }

  /**
   * <p>Releases savepoint.</p>
   * @param pSvpNm savepoint name
   * @throws Exception - an exception
   **/
  @Override
  public final synchronized void relSavPnt(
    final String pSvpNm) throws Exception {
    exec("RELEASE " + pSvpNm + ";");
  }

  /**
   * <p>Rollbacks transaction to savepoint.</p>
   * @param pSvpNm savepoint name
   * @throws Exception - an exception
   **/
  @Override
  public final synchronized void rollBack(
    final String pSvpNm) throws Exception {
    exec(";ROLLBACK TRANSACTION TO SAVEPOINT " + pSvpNm + ";");
  }

  /**
   * <p>Starts new transaction.</p>
   * @throws Exception - an exception
   **/
  @Override
  public final synchronized void begin() throws Exception {
    exec("BEGIN TRANSACTION;");
    HLDSTRT.set(Boolean.TRUE);
  }

  /**
   * <p>Commits transaction and unlocks this service.</p>
   * @throws Exception - an exception
   **/
  @Override
  public final synchronized void commit() throws Exception {
    exec("COMMIT TRANSACTION;");
    this.acmt = true; //unlocking
    this.strt = 0;
    HLDSTRT.remove();
  }

  /**
   * <p>Rollbacks transaction.</p>
   * @throws Exception - an exception
   **/
  @Override
  public final synchronized void rollBack() throws Exception {
    if (HLDSTRT.get() != null) { // transaction started
      exec("ROLLBACK TRANSACTION;");
      this.acmt = true; //unlocking
      this.strt = 0;
    } //else busy exception
  }

  /**
   * <p>Releases only unneeded memory.</p>
   * @throws Exception - an exception
   **/
  @Override
  public final synchronized void release() throws Exception {
    if (this.sqliteDb != null) {
      this.sqliteDb.releaseMemory();
    }
  }

  /**
   * <p>Retrieves records from DB.</p>
   * @param pSelect query SELECT
   * @return IRecSet record set
   * @throws Exception - if an exception occurred
   **/
  @Override
  public final synchronized IRecSet<Cursor> retRs(
    final String pSelect) throws Exception {
    boolean dbgSh = getLog().getDbgSh(this.getClass(), 16000);
    try {
      if (dbgSh) {
        getLog().debug(null, getClass(), "try to retrieve records: " + pSelect);
      }
      Cursor rs = this.sqliteDb.rawQuery(pSelect, null);
      RecSet rsa = new RecSet(rs);
      if (dbgSh) {
        getLog().debug(null, getClass(), "Recordset: " + rsStr(rsa));
      }
      return rsa;
    } catch (Exception ex) {
      String msg = ex.getMessage() + ", query:\n" + pSelect;
      ExcCode ewc = new ExcCode(SQLEX, msg);
      ewc.setStackTrace(ex.getStackTrace());
      throw ewc;
    }
  }

  /**
   * <p>Executes any SQL query that returns no data.
   * E.g. PRAGMA, etc.</p>
   * @param pQuery query
   * @throws Exception - if an exception occurred
   **/
  @Override
  public final synchronized void exec(final String pQuery) throws Exception {
    boolean dbgSh = getLog().getDbgSh(this.getClass(), 16001);
    try {
      if (dbgSh) {
        getLog().debug(null, getClass(), "try to execute query: " + pQuery);
      }
      this.sqliteDb.execSQL(pQuery);
    } catch (Exception ex) {
      String msg = ex.getMessage() + ", query:\n" + pQuery;
      ExcCode ewc = new ExcCode(SQLEX, msg);
      ewc.setStackTrace(ex.getStackTrace());
      throw ewc;
    }
  }

  /**
   * <p>Executes SQL UPDATE that returns affected rows.
   * It is to adapt Android insert/update/delete interface.
   * </p>
   * @param <T> entity type
   * @param pCls entity class
   * @param pCv type-safe map column name - column value
   * @param pWhe where conditions e.g. "itsId=2"
   * @return row count affected
   * @throws Exception - if an exception occurred
   **/
  @Override
  public final synchronized <T extends IHasId<?>> int update(
    final Class<T> pCls, final ColVals pCv,
      final String pWhe) throws Exception {
    boolean dbgSh = getLog().getDbgSh(this.getClass(), 16002);
    try {
      ContentValues cntVals = cnvToCntValsUpd(pCls, pCv);
      if (dbgSh) {
        getLog().debug(null, getClass(), "try to update : " + pCls + " where: "
        + pWhe + " cv: " + this.srvClVl.str(pCls, pCv) + ", ACV: " + cntVals);
      }
      return this.sqliteDb
        .update(pCls.getSimpleName().toUpperCase(), cntVals, pWhe, null);
    } catch (Exception ex) {
      String msg = ex.getMessage() + ", cls: " + pCls + ", cv: "
        + this.srvClVl.str(pCls, pCv) + ", where: " + pWhe;
      ExcCode ewc = new ExcCode(SQLEX, msg);
      ewc.setStackTrace(ex.getStackTrace());
      throw ewc;
    }
  }

  /**
   * <p>Executes SQL INSERT that returns affected rows.
   * It is to adapt Android insert/update/delete interface.
   * </p>
   * @param <T> entity type
   * @param pCls entity class
   * @param pCv type-safe map column name - column value
   * @return Number inserted row (auto-generated or not) or -1 if error
   * @throws Exception - if an exception occurred
   **/
  @Override
  public final synchronized <T extends IHasId<?>> long insert(
    final Class<T> pCls, final ColVals pCv) throws Exception {
    boolean dbgSh = getLog().getDbgSh(this.getClass(), 16003);
    try {
      ContentValues cntVals = cnvToCntValsIns(pCls, pCv);
      if (dbgSh) {
        getLog().debug(null, getClass(), "try to insert : " + pCls + " cv: "
          + this.srvClVl.str(pCls, pCv) + " ACV: " + cntVals);
      }
      long result = this.sqliteDb
        .insert(pCls.getSimpleName().toUpperCase(), null, cntVals);
      if (dbgSh) {
        getLog().debug(null, getClass(), "result insert: " + result);
      }
      if (result == -1) {
        throw new Exception("Result = -1!");
      }
      return result;
    } catch (Exception ex) {
      String msg = ex.getMessage() + ", cls: " + pCls + ", cv: "
        + this.srvClVl.str(pCls, pCv);
      ExcCode ewc = new ExcCode(SQLEX,
        msg);
      ewc.setStackTrace(ex.getStackTrace());
      throw ewc;
    }
  }

  /**
   * <p>Executes SQL DELETE that returns affected rows.
   * It is to adapt Android insert/update/delete interface.
   * </p>
   * @param pTbl table name
   * @param pWhe where conditions e.g. "itsId=2" or NULL -delete all
   * @return row count affected
   * @throws Exception - if an exception occurred
   **/
  @Override
  public final synchronized int delete(final String pTbl,
    final String pWhe) throws Exception {
    boolean dbgSh = getLog().getDbgSh(this.getClass(), 16004);
    try {
      if (dbgSh) {
        getLog().debug(null, getClass(), "try to delete t: " + pTbl
          + " where: " + pWhe);
      }
      return this.sqliteDb.delete(pTbl, pWhe, null);
    } catch (Exception ex) {
      String msg = ex.getMessage() + ", table: " + pTbl
        + ", where: " + pWhe;
      ExcCode ewc = new ExcCode(SQLEX, msg);
      ewc.setStackTrace(ex.getStackTrace());
      throw ewc;
    }
  }

  //Utils:
  /**
   * <p>Converts org.beigesoft.mdl.ColVals
   * to android.content.ContentValues for insert.</p>
   * @param <T> entity type
   * @param pCls entity class
   * @param pCv Columns Values
   * @return ContentValues
   * @throws Exception - an exception
   **/
  public final synchronized <T extends IHasId<?>> ContentValues cnvToCntValsIns(
    final Class<T> pCls, final ColVals pCv) throws Exception {
    ContentValues cntVals = new ContentValues();
    List<String> idNms = this.srvClVl.getSetng().lazIdFldNms(pCls);
    if (pCv.getInts() != null) {
      for (Map.Entry<String, Integer> enr : pCv.getInts().entrySet()) {
        if (enr.getValue() != null || !idNms.contains(enr.getKey())) {
          cntVals.put(enr.getKey().toUpperCase(), enr.getValue());
        }
      }
    }
    if (pCv.getLongs() != null) {
      for (Map.Entry<String, Long> enr : pCv.getLongs().entrySet()) {
        if (enr.getValue() != null || !idNms.contains(enr.getKey())) {
          cntVals.put(enr.getKey().toUpperCase(), enr.getValue());
        }
      }
    }
    if (pCv.getStrs() != null) {
      for (Map.Entry<String, String> enr : pCv.getStrs().entrySet()) {
        cntVals.put(enr.getKey().toUpperCase(), enr.getValue());
      }
    }
    if (pCv.getFloats() != null) {
      for (Map.Entry<String, Float> enr : pCv.getFloats().entrySet()) {
        cntVals.put(enr.getKey().toUpperCase(), enr.getValue());
      }
    }
    if (pCv.getDoubles() != null) {
      for (Map.Entry<String, Double> enr : pCv.getDoubles().entrySet()) {
        cntVals.put(enr.getKey().toUpperCase(), enr.getValue());
      }
    }
    return cntVals;
  }

  /**
   * <p>Converts org.beigesoft.mdl.ColVals
   * to android.content.ContentValues for update.</p>
   * @param <T> entity type
   * @param pCls entity class
   * @param pCv Columns Values
   * @return ContentValues
   * @throws Exception - an exception
   **/
  public final synchronized <T extends IHasId<?>> ContentValues cnvToCntValsUpd(
    final Class<T> pCls, final ColVals pCv) throws Exception {
    ContentValues cntVals = new ContentValues();
    List<String> idNms = this.srvClVl.getSetng().lazIdFldNms(pCls);
    if (pCv.getInts() != null) {
      for (Map.Entry<String, Integer> enr : pCv.getInts().entrySet()) {
        if (!idNms.contains(enr.getKey())) {
          cntVals.put(enr.getKey().toUpperCase(), enr.getValue());
        }
      }
    }
    if (pCv.getLongs() != null) {
      for (Map.Entry<String, Long> enr : pCv.getLongs().entrySet()) {
        if (!idNms.contains(enr.getKey())) {
          cntVals.put(enr.getKey().toUpperCase(), enr.getValue());
        }
      }
    }
    if (pCv.getStrs() != null) {
      for (Map.Entry<String, String> enr : pCv.getStrs().entrySet()) {
        cntVals.put(enr.getKey().toUpperCase(), enr.getValue());
      }
    }
    if (pCv.getFloats() != null) {
      for (Map.Entry<String, Float> enr : pCv.getFloats().entrySet()) {
        cntVals.put(enr.getKey().toUpperCase(), enr.getValue());
      }
    }
    if (pCv.getDoubles() != null) {
      for (Map.Entry<String, Double> enr : pCv.getDoubles().entrySet()) {
        cntVals.put(enr.getKey().toUpperCase(), enr.getValue());
      }
    }
    return cntVals;
  }

  /**
   * <p>Prints record-set.</p>
   * @param pRs record-set
   * @return representation
   **/
  public final synchronized String rsStr(final RecSet pRs) {
    StringBuffer columns = new StringBuffer();
    for (String cn :  pRs.getRecSet().getColumnNames()) {
      columns.append(" " + cn);
    }
    return "Columns total: " + pRs.getRecSet().getColumnCount()
      + "\nRows total: " + pRs.getRecSet().getCount()
      + "\nColumns: " + columns.toString();
  }

  //Simple getters and setters:
  /**
   * <p>Getter for srvClVl.</p>
   * @return SrvClVl
   **/
  public final synchronized SrvClVl getSrvClVl() {
    return this.srvClVl;
  }

  /**
   * <p>Setter for srvClVl.</p>
   * @param pSrvClVl reference
   **/
  public final synchronized void setSrvClVl(final SrvClVl pSrvClVl) {
    this.srvClVl = pSrvClVl;
  }

  /**
   * <p>Geter for sqliteDb.</p>
   * @return SQLiteDatabase
   **/
  public final synchronized SQLiteDatabase getSqliteDb() {
    return this.sqliteDb;
  }

  /**
   * <p>Setter for sqliteDb.</p>
   * @param pSqliteDb reference
   **/
  public final synchronized void setSqliteDb(final SQLiteDatabase pSqliteDb) {
    this.sqliteDb = pSqliteDb;
  }
}
