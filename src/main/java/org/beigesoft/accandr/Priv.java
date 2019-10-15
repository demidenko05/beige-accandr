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

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.net.Uri;
import android.support.v4.app.DialogFragment;

/**
 * <p>Privacy policy agreeing dialog.</p>
 *
 * @author Yury Demidenko
 */
public class Priv extends DialogFragment implements OnClickListener {

  /**
   * <p>Checkbox agree.</p>
   **/
  private CheckBox chbAcp;

  /**
   * <p>Button privacy policy.</p>
   **/
  private Button btUrlPrv;

  /**
   * <p>Instance of the interface to deliver action events.</p>
   */
  private PrivDlgLstn dlgLstn;

  /**
   * <p>Instantiates the NoticeDialogListener.</p>
   * @param pAct activity
   */
  @Override
  public void onAttach(final Activity pAct) {
    super.onAttach(pAct);
    // Verify that the host pAct implements the callback interface
    try {
      // Instantiate the NoticeDialogListener so we can send events to the host
      this.dlgLstn = (PrivDlgLstn) pAct;
    } catch (ClassCastException e) {
      // The pAct doesn't implement the interface, throw exception
      throw new ClassCastException(pAct.toString()
              + " must implement PrivDlgLstn");
    }
  }

  /**
   * <p>Main initializer.</p>
   * @param pSvInsStt saved instance state
   **/
  @Override
  public Dialog onCreateDialog(final Bundle pSvInsStt) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    // Get the layout inflater
    LayoutInflater inflater = getActivity().getLayoutInflater();
    // Inflate and set the layout for the dialog
    // Pass null as the parent view because its going in the dialog layout
    View view = inflater.inflate(R.layout.priv, null);
    this.chbAcp = (CheckBox) view.findViewById(R.id.chbAcp);
    SharedPreferences shrPrf = getActivity()
      .getPreferences(Context.MODE_PRIVATE);
    boolean acp = shrPrf.getBoolean(Bsa.PRIVAGREE, false);
    this.chbAcp.setChecked(acp);
    this.btUrlPrv = (Button) view.findViewById(R.id.btUrlPrv);
    this.btUrlPrv.setOnClickListener(this);
    builder.setView(view).setTitle(R.string.privTitle)
    // Add action buttons
      .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(final DialogInterface dialog, final int id) {
          Priv.this.dlgLstn.onPrivSaveClick(Priv.this.chbAcp.isChecked());
          Priv.this.getDialog().cancel();
        }
      })
     .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(final DialogInterface dialog, final int id) {
          Priv.this.getDialog().cancel();
        }
      });
    return builder.create();
  }

  /**
   * <p>onClick handler.</p>
   * @param pTarget button
   */
  @Override
  public final void onClick(final View pTarget) {
    if (pTarget == this.btUrlPrv) {
      String url;
      if (Locale.getDefault().getLanguage().equals("ru")) {
        url = "https://sites.google.com/site/beigesoftware/privacyeis-ru";
      } else {
        url = "https://sites.google.com/site/beigesoftware/privacyeis-en";
      }
      Intent i = new Intent(Intent.ACTION_VIEW);
      i.setData(Uri.parse(url));
      startActivity(i);
    }
  }

  /**
   * <p>Delegate of activity agree result event.</p>
   **/
  public interface PrivDlgLstn {

    /**
     * <p>Passes agree event to activity.</>
     * @param pPrivAgreed if privacy policy accepted
     */
    void onPrivSaveClick(boolean pPrivAgreed);
  }
}
