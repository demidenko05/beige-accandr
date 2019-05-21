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

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

/**
 * <p>SQLiteOpenHelper implementation only for getting database.</p>
 *
 * @author Yury Demidenko
 */
public class SqltOpHp extends SQLiteOpenHelper {

  /**
   * <p>Constructor.</p>
   * @param pCtx context
   * @param pNm DB name
   * @param pCuFc CursorFactory
   * @param pVer DB version, starts from 1
   **/
  public SqltOpHp(final Context pCtx, final String pNm,
    final SQLiteDatabase.CursorFactory pCuFc, final int pVer) {
    super(pCtx, pNm, pCuFc, pVer);
  }

  /**
   * <p>On create DB handler.</p>
   * @param pDb DB
   **/
  @Override
  public final void onCreate(final SQLiteDatabase pDb) {
    //nothing
  }

  /**
   * <p>On upgrade DB handler.</p>
   * @param pDb DB
   * @param pOldVr old version
   * @param pNewVr new version
   **/
  @Override
  public final void onUpgrade(final SQLiteDatabase pDb, final int pOldVr,
    final int pNewVr) {
    //nothing
  }
}
