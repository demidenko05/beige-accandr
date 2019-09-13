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

import android.database.Cursor;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.beigesoft.exc.ExcCode;
import org.beigesoft.mdl.IHasId;
import org.beigesoft.mdl.IRecSet;
import org.beigesoft.mdl.ColVals;

/**
 * <p>Maximum native implementation of database service for Android.
 * On hard job like webstore.refresh item in list (org.beigesoft.ws.prcRefrLst),
 * the database is often becomes locked partially, e.g. a request OK
 * but another one frozen, tables may be different or same,
 * this is for all - this, Rdb, RdbMdb. Log shows that all transactions was started,
 * marketed successful and ended successful, but in SQLite DB data is old.</p>
 *
 * @author Yury Demidenko
 */
public class Rdba extends ARdba {

  /**
   * <p>SQLiteOpenHelper one for all threads.</p>
   **/
  private SQLiteOpenHelper sqliteOh;

  /**
   * <p>Auto-commit flag per thread.</p>
   **/
  private static final ThreadLocal<Boolean> HLDACMT =
    new ThreadLocal<Boolean>() { };

  /**
   * <p>SQLite database (connection like) per thread holder.</p>
   **/
  private static final ThreadLocal<SQLiteDatabase> HLDSQLT =
    new ThreadLocal<SQLiteDatabase>() { };

  /**
   * <p>Get autocommit flag.</p>
   * @throws Exception - an exception
   **/
  @Override
  public final boolean getAcmt() throws Exception {
    Boolean acmt = HLDACMT.get();
    if (acmt == null) {
      acmt = Boolean.TRUE;
      HLDACMT.set(acmt);
    }
    return acmt;
  }

  /**
   * <p>Sets autocommit flag.</p>
   * @param pAcmt if autocommit
   * @throws Exception - an exception
   **/
  @Override
  public final void setAcmt(final boolean pAcmt) throws Exception {
    HLDACMT.set(pAcmt);
  }

  /**
   * <p>Sets transaction isolation level.
   * Either read-uncommited or serializable.</p>
   * @param pLevel transaction level
   * @throws Exception - an exception
   **/
  @Override
  public final void setTrIsl(final int pLevel) throws Exception {
    //nothing
  }

  /**
   * <p>Gets transaction isolation level.</p>
   * @return Level transaction
   * @throws Exception - an exception
   **/
  @Override
  public final int getTrIsl() throws Exception {
    Integer val = evInt("PRAGMA read_uncommitted;", "read_uncommitted");
    if (val == null || val == 0) {
      return TRSR;
    } else {
      return TRRUC;
    }
  }

  /**
   * <p>Creates savepoint.</p>
   * @param pSvpNm savepoint name
   * @throws Exception - an exception
   **/
  @Override
  public final void creSavPnt(
    final String pSvpNm) throws Exception {
    exec("SAVEPOINT " + pSvpNm + ";");
  }

  /**
   * <p>Releases savepoint.</p>
   * @param pSvpNm savepoint name
   * @throws Exception - an exception
   **/
  @Override
  public final void relSavPnt(
    final String pSvpNm) throws Exception {
    exec("RELEASE " + pSvpNm + ";");
  }

  /**
   * <p>Rollbacks transaction to savepoint.</p>
   * @param pSvpNm savepoint name
   * @throws Exception - an exception
   **/
  @Override
  public final void rollBack(
    final String pSvpNm) throws Exception {
    exec(";ROLLBACK TRANSACTION TO SAVEPOINT " + pSvpNm + ";");
  }

  /**
   * <p>Starts new transaction.</p>
   * @throws Exception - an exception
   **/
  @Override
  public final void begin() throws Exception {
    boolean dbgSh = getLog().getDbgSh(this.getClass(), 16005);
    if (dbgSh) {
      getLog().debug(null, getClass(), "try to begin...");
    }
    lazDb().beginTransactionNonExclusive();
  }

  /**
   * <p>Commits transaction and unlocks this service.</p>
   * @throws Exception - an exception
   **/
  @Override
  public final void commit() throws Exception {
    boolean dbgSh = getLog().getDbgSh(this.getClass(), 16006);
    if (dbgSh) {
      getLog().debug(null, getClass(), "try to commit...");
    }
    SQLiteDatabase db = HLDSQLT.get();
    try {
      db.setTransactionSuccessful();
      if (dbgSh) {
        getLog().debug(null, getClass(), "Set successful OK");
      }
    } finally {
      db.endTransaction();
      if (dbgSh) {
        getLog().debug(null, getClass(), "End transaction OK");
      }
    }
  }

  /**
   * <p>Rollbacks transaction.</p>
   * @throws Exception - an exception
   **/
  @Override
  public final void rollBack() throws Exception {
    boolean dbgSh = getLog().getDbgSh(this.getClass(), 16007);
    if (dbgSh) {
      getLog().debug(null, getClass(), "try to rollback...");
    }
    SQLiteDatabase db = HLDSQLT.get();
    db.endTransaction(); //without setTransactionSuccessful
  }

  /**
   * <p>Closing DB.</p>
   * @throws Exception - an exception
   **/
  @Override
  public final void release() throws Exception {
    SQLiteDatabase sqliteDb = HLDSQLT.get();
    if (sqliteDb != null) {
      sqliteDb.close();
      HLDSQLT.remove();
      boolean dbgSh = getLog().getDbgSh(this.getClass(), 16008);
      if (dbgSh) {
        getLog().debug(null, getClass(), "Release DB OK");
      }
    }
  }

  /**
   * <p>Retrieves records from DB.</p>
   * @param pSelect query SELECT
   * @return IRecSet record set
   * @throws Exception - if an exception occurred
   **/
  @Override
  public final IRecSet<Cursor> retRs(
    final String pSelect) throws Exception {
    boolean dbgSh = getLog().getDbgSh(this.getClass(), 16000);
    try {
      if (dbgSh) {
        getLog().debug(null, getClass(), "try to retrieve records: " + pSelect);
      }
      Cursor rs = lazDb().rawQuery(pSelect, null);
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
  public final void exec(final String pQuery) throws Exception {
    boolean dbgSh = getLog().getDbgSh(this.getClass(), 16001);
    try {
      if (dbgSh) {
        getLog().debug(null, getClass(), "try to execute query: " + pQuery);
      }
      lazDb().execSQL(pQuery);
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
  public final <T extends IHasId<?>> int update(final Class<T> pCls,
    final ColVals pCv, final String pWhe) throws Exception {
    boolean dbgSh = getLog().getDbgSh(this.getClass(), 16002);
    try {
      ContentValues cntVals = cnvToCntValsUpd(pCls, pCv);
      if (dbgSh) {
        getLog().debug(null, getClass(), "try to update : " + pCls + " where: "
        + pWhe + " cv: " + getSrvClVl().str(pCls, pCv) + ", ACV: " + cntVals);
      }
      return lazDb()
        .update(pCls.getSimpleName().toUpperCase(), cntVals, pWhe, null);
    } catch (Exception ex) {
      String msg = ex.getMessage() + ", cls: " + pCls + ", cv: "
        + getSrvClVl().str(pCls, pCv) + ", where: " + pWhe;
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
  public final <T extends IHasId<?>> long insert(final Class<T> pCls,
    final ColVals pCv) throws Exception {
    boolean dbgSh = getLog().getDbgSh(this.getClass(), 16003);
    try {
      ContentValues cntVals = cnvToCntValsIns(pCls, pCv);
      if (dbgSh) {
        getLog().debug(null, getClass(), "try to insert : " + pCls + " cv: "
          + getSrvClVl().str(pCls, pCv) + " ACV: " + cntVals);
      }
      long result = lazDb()
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
        + getSrvClVl().str(pCls, pCv);
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
  public final int delete(final String pTbl,
    final String pWhe) throws Exception {
    boolean dbgSh = getLog().getDbgSh(this.getClass(), 16004);
    try {
      if (dbgSh) {
        getLog().debug(null, getClass(), "try to delete t: " + pTbl
          + " where: " + pWhe);
      }
      return lazDb().delete(pTbl, pWhe, null);
    } catch (Exception ex) {
      String msg = ex.getMessage() + ", table: " + pTbl
        + ", where: " + pWhe;
      ExcCode ewc = new ExcCode(SQLEX, msg);
      ewc.setStackTrace(ex.getStackTrace());
      throw ewc;
    }
  }

  /**
   * <p>Lazy gets database for thread.</p>
   * @return SQLite DB
   **/
  private SQLiteDatabase lazDb() {
    SQLiteDatabase sqliteDb = HLDSQLT.get();
    if (sqliteDb == null) {
      sqliteDb = this.sqliteOh.getWritableDatabase();
      HLDSQLT.set(sqliteDb);
    }
    return sqliteDb;
  }

  //Simple getters and setters:
  /**
   * <p>Getter for sqliteOh.</p>
   * @return SQLiteOpenHelper
   **/
  public final SQLiteOpenHelper getSqliteOh() {
    return this.sqliteOh;
  }

  /**
   * <p>Setter for sqliteOh.</p>
   * @param pSqliteOh reference
   **/
  public final void setSqliteOh(final SQLiteOpenHelper pSqliteOh) {
    this.sqliteOh = pSqliteOh;
  }
}
