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
//Opens cart to edit or add item:
function bsSetCartItm(pItTyp, pItId, pItNm, pPri, pQuan, pAvQuan, pLnId, pUomId, pUomNm, pUnSt, pPriDp, pPriRm) {
  var pref = "";
  if (pLnId != null) {
    pref = "Edit";
    var lnId = document.getElementById("lnId" + pref);
    lnId.value = pLnId;
  }
  var itNm = document.getElementById("itNm" + pref);
  itNm.value = pItNm;
  var quan = document.getElementById("quan" + pref);
  $(quan).bsInpNum({step: pUnSt, nmax: pAvQuan});
  var pri = document.getElementById("pri" + pref);
  pri.value = bsNumStr(pPri.toString(), pPriDp);
  var uomNm = document.getElementById("uomNm" + pref);
  uomNm.value = pUomNm;
  var uomId = document.getElementById("uomId" + pref);
  uomId.value = pUomId;
  quan.value = bsNumStr(pQuan.toString(), quan.decPl);
  var avQuan = document.getElementById("avQuan" + pref);
  avQuan.value = pAvQuan;
  var unSt = document.getElementById("unSt" + pref);
  unSt.value = pUnSt;
  var itTyp = document.getElementById("itTyp" + pref);
  itTyp.value = pItTyp;
  var itId = document.getElementById("itId" + pref);
  itId.value = pItId;
  bsRefCaItTo(pref, pPriDp, pPriRm);
};
//refreshes cart item total:
function bsRefCaItTo(pPref, pPriDp, pPriRm) {
  var pri = document.getElementById("pri" + pPref);
  var quan = document.getElementById("quan" + pPref);
  var tot = document.getElementById("tot" + pPref);
  var pr = bsStrFlt(pri.value);
  var qu = bsStrFlt(quan.value);
  var to = bsRound(pr * qu, pPriDp, pPriRm);
  tot.value = bsNumStr(to.toString(), pPriDp);
};
//deletes item from cart:
function bsDelCartItm(pItTyp, pItId, pItNm, pPri, pQuan, pLnId, pPriDp) {
  var itNm = document.getElementById("itNmDel");
  itNm.value = pItNm;
  var pri = document.getElementById("priDel");
  pri.value = pPri;
  var quan = document.getElementById("quanDel");
  quan.value = pQuan;
  var tot = document.getElementById("totDel");
  var to = pPri * pQuan;
  tot.value = bsNumStr(to.toString(), pPriDp);
  var lnId = document.getElementById("lnIdDel");
  lnId.value = pLnId;
};
//handles on filter number operator changed event:
function bsFltNumOpChg(pSelect, pVal1Id, pVal2Id) {
  var isDisabledV1 = pSelect.options[pSelect.selectedIndex].value == "";
  document.getElementById(pVal1Id).disabled = isDisabledV1;
  var isDisabledV2 = pSelect.options[pSelect.selectedIndex].value != "BETWEEN_INCLUDE";
  document.getElementById(pVal2Id).disabled = isDisabledV2;
};
//handles on catalog operator changed event:
function bsCatOprChg(pSelect, pFltCtValId, pRowCatId) {
  var fltCtVal = document.getElementById(pFltCtValId);
  var rowCat = document.getElementById(pRowCatId);
  bsFltCatChg(pSelect, fltCtVal, rowCat);
};
//handles on catalog value changed event:
function bsCatValChg(pSelect, pFltCtOpId, pRowCatId) {
  var fltCtOp = document.getElementById(pFltCtOpId);
  var rowCat = document.getElementById(pRowCatId);
  bsFltCatChg(fltCtOp, pSelect, rowCat);
};
//handles on filter catalog changed event:
function bsFltCatChg(pFltCtOp, pFltCtVal, pRowCat) {
  var isDisabledCatalog = pFltCtOp.options[pFltCtOp.selectedIndex].value != "";
  if (!isDisabledCatalog) {
    var i;
    for (i = 0; i < pFltCtVal.length; i++) {
      if (pFltCtVal.options[i].selected) {
        isDisabledCatalog = false;
        break;
      }
    }
  }
  pFltCtVal.disabled = !isDisabledCatalog;
  if (isDisabledCatalog && !pRowCat.classList.contains('dimmed')) {
    pRowCat.classList.add('dimmed');
  } else if (!isDisabledCatalog && pRowCat.classList.contains('dimmed')) {
    pRowCat.classList.remove('dimmed');
  }
};
