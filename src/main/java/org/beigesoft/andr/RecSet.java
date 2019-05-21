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

import org.beigesoft.mdl.IRecSet;

/**
 * <p>Recordset adapter implementation on Android.</p>
 *
 * @author Yury Demidenko
 */
public class RecSet implements IRecSet<Cursor> {

  /**
   * <p>Android recordset.</p>
   **/
  private final Cursor resSet;

  /**
   * <p>Android recordset.</p>
   * @param pResSet result set
   **/
  public RecSet(final Cursor pResSet) {
    this.resSet = pResSet;
  }

  /**
   * <p>Geter for ResSet.</p>
   * @return ResSet
   **/
  @Override
  public final Cursor getRecSet() {
    return this.resSet;
  }

  /**
   * <p>Move cursor to next record.</p>
   * @return boolean if next record exist
   * @throws Exception - an exception
   **/
  @Override
  public final boolean next() throws Exception {
    return this.resSet.moveToNext();
  }

  /**
   * <p>Move cursor to first record (for Android compatible).</p>
   * @return boolean if next record exist
   * @throws Exception - an exception
   **/
  @Override
  public final boolean first() throws Exception {
    return this.resSet.moveToFirst();
  }

  /**
   * <p>Close resultset, for JDBC close statement.</p>
   * @throws Exception - an exception
   **/
  @Override
  public final void close() throws Exception {
    this.resSet.close();
  }

  /**
   * <p>Retrieve String column value.</p>
   * @param pColumnName column name
   * @return String result
   * @throws Exception - an exception
   **/
  @Override
  public final String getStr(final String pColumnName) throws Exception {
    int columnIndex = this.resSet.getColumnIndex(pColumnName);
    return this.resSet.getString(columnIndex);
  }

  /**
   * <p>Retrieve Double column value.</p>
   * @param pColumnName column name
   * @return Double result
   * @throws Exception - an exception
   **/
  @Override
  public final Double getDouble(final String pColumnName) throws Exception {
    int columnIndex = this.resSet.getColumnIndex(pColumnName);
    Double result = null;
    if (!this.resSet.isNull(columnIndex)) {
      result = this.resSet.getDouble(columnIndex);
    }
    return result;
  }

  /**
   * <p>Retrieve Float column value.</p>
   * @param pColumnName column name
   * @return Float result
   * @throws Exception - an exception
   **/
  @Override
  public final Float getFloat(final String pColumnName) throws Exception {
    int columnIndex = this.resSet.getColumnIndex(pColumnName);
    Float result = null;
    if (!this.resSet.isNull(columnIndex)) {
      result = this.resSet.getFloat(columnIndex);
    }
    return result;
  }

  /**
   * <p>Retrieve Integer column value.</p>
   * @param pColumnName column name
   * @return Integer result
   * @throws Exception - an exception
   **/
  @Override
  public final Integer getInt(final String pColumnName) throws Exception {
    int columnIndex = this.resSet.getColumnIndex(pColumnName);
    Integer result = null;
    if (!this.resSet.isNull(columnIndex)) {
      result = this.resSet.getInt(columnIndex);
    }
    return result;
  }

  /**
   * <p>Retrieve Long column value.</p>
   * @param pColumnName column name
   * @return Long result
   * @throws Exception - an exception
   **/
  @Override
  public final Long getLong(final String pColumnName) throws Exception {
    int columnIndex = this.resSet.getColumnIndex(pColumnName);
    Long result = null;
    if (!this.resSet.isNull(columnIndex)) {
      result = this.resSet.getLong(columnIndex);
    }
    return result;
  }
}
