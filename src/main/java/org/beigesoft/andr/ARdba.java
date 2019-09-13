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

import android.database.Cursor;
import android.content.ContentValues;

import org.beigesoft.mdl.IHasId;
import org.beigesoft.mdl.ColVals;
import org.beigesoft.rdb.SrvClVl;
import org.beigesoft.rdb.ARdb;

/**
 * <p>Basic database service with utils for Android.</p>
 *
 * @author Yury Demidenko
 */
public abstract class ARdba extends ARdb<Cursor> {

  /**
   * <p>Generating insert/update and CV service.</p>
   **/
  private SrvClVl srvClVl;

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
}
