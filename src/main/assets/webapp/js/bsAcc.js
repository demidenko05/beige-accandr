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

//Opens subacc picker:
function bsPickSac(pFdNm, pPikng, pSaNm, pAdPr) {
  var acId=document.getElementById(pPikng + pFdNm + "Id").value;
  if(acId!="") {
    bsPick("Sacnt", pPikng, pSaNm, "&fopownrVlId=" + acId + "&fopownrOpr=eq&fopfrcd=ownr"+ pAdPr);
  } else {
    bsShwErr(BSMSG["choose_account_first"]);
  }
};
//Clears subacc picker:
function bsClearSac(pPikng, pSaNm) {
  document.getElementById(pPikng + pSaNm + "Id").value = "";
  document.getElementById(pPikng + pSaNm + "Ap").value = "";
  var saApVsb = document.getElementById(pPikng  + pSaNm + "ApVsb");
  saApVsb.value = "";
  bsInpChn(saApVsb);
};
//Selects/sets account by picker:
function bsSelAcc(pEntId, pEntAp, pIdDmPi, pSaId) {
  var whoPicking = BSSTATE["WhoPi"][pIdDmPi];
  var pref = whoPicking["pigEnt"] + whoPicking["piFld"];
  document.getElementById(pref +"Id").value = pEntId;
  var inpAp = document.getElementById(pref + "Ap");
  if (inpAp != null) { //invisible appearance to be sent
    inpAp.value=pEntAp;
  }
  var inpApVsb = document.getElementById(pref + "ApVsb");
  inpApVsb.value = pEntAp;
  bsInpChn(inpApVsb);
  var btSaPi = document.getElementById(pref + "SaPi");
  if (btSaPi != null) { //buttons subacc pick/clear:
    var btsSaDis = (pSaId == null);
    var btSaCl = document.getElementById(pref + "SaCl");
    btSaCl.onclick();
    btSaPi.disabled = btsSaDis;
    btSaCl.disabled = btsSaDis;
  }
  document.getElementById(pIdDmPi+"Dlg").close();
};
//set known or from returned invoice line cost for picked item, cost is already rounded and internationalized string value
function bsSetCost(pCost, pIdDmPi) {
  var whoPicking = BSSTATE["WhoPi"][pIdDmPi];
  var inpCostVsb = document.getElementById(whoPicking["pigEnt"] + "priVsb");
  var inpCost = document.getElementById(whoPicking["pigEnt"] + "pri");
  if (inpCost.value != pCost) {
    inpCost.value = pCost;
    if (inpCostVsb != null) {
      inpCostVsb.value = pCost;
      bsInpChn(inpCostVsb);
    } else {
      bsInpChn(inpCost);
    }
  }
};
//set UOM for picked item:
function bsSetUom(uomId, uomName, pIdDmPi) {
  var whoPicking = BSSTATE["WhoPi"][pIdDmPi];
  var inpUomId = document.getElementById(whoPicking["pigEnt"] + "uomId");
  if (inpUomId != null) {
    inpUomId.value = uomId;
    var uomApVsb = document.getElementById(whoPicking["pigEnt"] + "uomApVsb");
    uomApVsb.value = uomName;
    bsInpChn(uomApVsb);
  }
  var btTxDs = document.getElementById(whoPicking["pigEnt"] + "btTxDs");
  if (btTxDs != null) {
    //revealing dest tax cat:
    btTxDs.style.display="block";
  }
};
//clears subacc:
function bsClrSal(entitySimpleName) {
  document.getElementById(entitySimpleName + "saId").setAttribute("value", "");
  document.getElementById(entitySimpleName + "saNmAp").setAttribute("value", "");
  document.getElementById(entitySimpleName + "saNmApVsb").setAttribute("value", "");
};
//selects subacc:
function bsSelSac(saId, subaccType, subaccAp, pIdDmPi) {
  var whoPicking = BSSTATE["WhoPi"][pIdDmPi];
  document.getElementById(whoPicking["pigEnt"] + whoPicking["piFld"] + "Ap").value = subaccAp;
  document.getElementById(whoPicking["pigEnt"] + whoPicking["piFld"] + "Ty").value = subaccType;
  document.getElementById(whoPicking["pigEnt"] + whoPicking["piFld"] + "Id").value = saId;
  var inpVsb = document.getElementById(whoPicking["pigEnt"] + whoPicking["piFld"] + "ApVsb");
  inpVsb.value = subaccAp;
  bsInpChn(inpVsb);
  document.getElementById(pIdDmPi+"Dlg").close();
};
//Select chooseable specifics type
function bsSelChoSpTy(typeId, typeAp, pIdDmPi) {
  whoPicking = BSSTATE["WhoPi"][pIdDmPi];
  document.getElementById(whoPicking["pigEnt"] + whoPicking["piFld"] +"TyId").setAttribute("value", typeId);
  var inpAp = document.getElementById(whoPicking["pigEnt"] + whoPicking["piFld"] + "TyAp");
  inpAp.setAttribute("value", typeAp);
  var inpApVsb = document.getElementById(whoPicking["pigEnt"] + whoPicking["piFld"] + "TyApVsb");
  inpApVsb.value = typeAp;
  bsInpChn(inpApVsb);
};

function selectAccSubacc(pEntId, pEntAp, pIdDmPi) {
  var whoPicking = BSSTATE["WhoPi"][pIdDmPi];
  document.getElementById(whoPicking["pigEnt"] +"saId").setAttribute("value", pEntId);
  document.getElementById(whoPicking["pigEnt"] +"saNmAp").setAttribute("value", pEntAp);
  var inpApVsb = document.getElementById(whoPicking["pigEnt"] + "saNmApVsb");
  inpApVsb.value = pEntAp;
  bsInpChn(inpApVsb);
  document.getElementById(pIdDmPi+"Dlg").close();
};
//Makes wages tax line:
function bsMkWgToTx(pInp, pEntNm, pWgGr, pTxDp, pTxRm) {
  var inpAlw = document.getElementById(pEntNm + "alw");
  var inpPlAm = document.getElementById(pEntNm + "plAm");
  var inpRate = document.getElementById(pEntNm + "rate");
  var alwc = bsStrFlt(inpAlw.value);
  var plAm = bsStrFlt(inpPlAm.value);
  var rate = bsStrFlt(inpRate.value);
  var inpTot = document.getElementById(pEntNm + "tot");
  var total = bsRound(plAm + (pWgGr - alwc) * rate / 100, pTxDp, pTxRm);
  inpTot.value = bsNumStr(total.toString(), pTxDp);
  bsInpChn(inpTot);
  bsInpChn(pInp);
};
//TODO?
function bsClrWgTxs(pEntNm) {
  var inpTotalWageTaxes = document.getElementById(pEntNm + "toWgTx");
  var inpTotalWageTaxesVsb = document.getElementById(pEntNm + "toWgTxVsb");
  inpTotalWageTaxes.value = 0;
  inpTotalWageTaxesVsb.value = 0;
  bsInpChn(inpTotalWageTaxesVsb);
};
//TODO?
function bsSelWgRtPlAm(rate, plAm, pIdDmPi) {
  var whoPicking = BSSTATE["WhoPi"][pIdDmPi];
  var inpPercentage = document.getElementById(whoPicking["pigEnt"] + "rate");
  if(inpPercentage != null) {
    var inpPlusAmount = document.getElementById(whoPicking["pigEnt"] + "plAm");
    if(inpPlusAmount != null) {
      inpPlusAmount.value = plAm;
      bsInpChn(inpPlusAmount);
    }
    inpPercentage.value = rate;
    bsInpChn(inpPercentage);
  }
};
//makes filter invoice total payment:
function bsMkFltPaTo(pInp, pIdSelFlt) {
  var fldWas;
  var fldIs;
  if (pInp.options[pInp.selectedIndex].value == "TOT") {
    toIs = "TOT";
    paIs = "TOPA";
    toWas = "TOFC";
    paWas = "PAFC";
  } else {
    toIs = "TOFC";
    paIs = "PAFC";
    toWas = "TOT";
    paWas = "TOPA";
  }
  var selFlt = document.getElementById(pIdSelFlt);
  for (var i=0; i < selFlt.options.length; i++) {
    //JS [str].replace(s1,s2); is actually replaceFirst!!!
    var nv = selFlt.options[i].value.replace(new RegExp(toWas, "g"), toIs);
    nv = nv.replace(new RegExp(paWas, "g"), paIs);
    selFlt.options[i].value = nv;
  }  
};
//Bank statement line entry matching changed:
function bsBslEnrMtcChg(pInp) {
  var tbPrepPayEntry = document.getElementById("bslPrPaEnr");
  var tbPrepPay = document.getElementById("bslPrPa");
  var tbPrep = document.getElementById("bslPrep");
  var tbPay = document.getElementById("bslPay");
  var tbEntry = document.getElementById("bslEnr");
  var bslPrepMtc = document.getElementById("bslPrepMtc");
  var bslPayMtc = document.getElementById("bslPayMtc");
  if (pInp.selectedIndex == 0) {
    if (tbPrepPayEntry != null) { tbPrepPayEntry.style.display="table"; }
    if (tbPrepPay != null) { tbPrepPay.style.display="table"; }
    if (tbPrep != null) { tbPrep.style.display="table"; }
    if (tbPay != null) { tbPay.style.display="table"; }
    if (tbEntry != null) { tbEntry.style.display="table"; }
    if (bslPrepMtc != null) { bslPrepMtc.style.display="table"; }
    if (bslPayMtc != null) { bslPayMtc.style.display="table"; }
  } else {
    if (tbPrepPayEntry != null) { tbPrepPayEntry.style.display="none"; }
    if (tbPrepPay != null) { tbPrepPay.style.display="none"; }
    if (tbPrep != null) { tbPrep.style.display="none"; }
    if (tbPay != null) { tbPay.style.display="none"; }
    if (tbEntry != null) { tbEntry.style.display="none"; }
    if (bslPrepMtc != null) { bslPrepMtc.style.display="none"; }
    if (bslPayMtc != null) { bslPayMtc.style.display="none"; }
  }
};
//Bank statement line prepayment matching changed:
function bsBslPrMtcChg(pInp) {
  var tbPrepPayEntry = document.getElementById("bslPrPaEnr");
  var tbPrepPay = document.getElementById("bslPrPa");
  var tbPrep = document.getElementById("bslPrep");
  var tbPay = document.getElementById("bslPay");
  var tbEntry = document.getElementById("bslEnr");
  var bslPayMtc = document.getElementById("bslPayMtc");
  var bslEnrMtc = document.getElementById("bslEnrMtc");
  if (pInp.selectedIndex == 0) {
    if (tbPrepPayEntry != null) { tbPrepPayEntry.style.display="table"; }
    if (tbPrepPay != null) { tbPrepPay.style.display="table"; }
    if (tbPrep != null) { tbPrep.style.display="table"; }
    if (tbPay != null) { tbPay.style.display="table"; }
    if (tbEntry != null) { tbEntry.style.display="table"; }
    if (bslPayMtc != null) { bslPayMtc.style.display="table"; }
    if (bslEnrMtc != null) { bslEnrMtc.style.display="table"; }
  } else {
    if (tbPrepPayEntry != null) { tbPrepPayEntry.style.display="none"; }
    if (tbPrepPay != null) { tbPrepPay.style.display="none"; }
    if (tbPrep != null) { tbPrep.style.display="none"; }
    if (tbPay != null) { tbPay.style.display="none"; }
    if (tbEntry != null) { tbEntry.style.display="none"; }
    if (bslPayMtc != null) { bslPayMtc.style.display="none"; }
    if (bslEnrMtc != null) { bslEnrMtc.style.display="none"; }
  }
};
//Bank statement line payment match changed:
function bsBslPayMtchCng(pInp) {
  var tbPrepPayEntry = document.getElementById("bslPrPaEnr");
  var tbPrepPay = document.getElementById("bslPrPa");
  var tbPrep = document.getElementById("bslPrep");
  var tbPay = document.getElementById("bslPay");
  var tbEntry = document.getElementById("bslEnr");
  var bslPrepMtc = document.getElementById("bslPrepMtc");
  var bslEnrMtc = document.getElementById("bslEnrMtc");
  if (pInp.selectedIndex == 0) {
    if (tbPrepPayEntry != null) { tbPrepPayEntry.style.display="table"; }
    if (tbPrepPay != null) { tbPrepPay.style.display="table"; }
    if (tbPrep != null) { tbPrep.style.display="table"; }
    if (tbPay != null) { tbPay.style.display="table"; }
    if (tbEntry != null) { tbEntry.style.display="table"; }
    if (bslPrepMtc != null) { bslPrepMtc.style.display="table"; }
    if (bslEnrMtc != null) { bslEnrMtc.style.display="table"; }
  } else {
    if (tbPrepPayEntry != null) { tbPrepPayEntry.style.display="none"; }
    if (tbPrepPay != null) { tbPrepPay.style.display="none"; }
    if (tbPrep != null) { tbPrep.style.display="none"; }
    if (tbPay != null) { tbPay.style.display="none"; }
    if (tbEntry != null) { tbEntry.style.display="none"; }
    if (bslPrepMtc != null) { bslPrepMtc.style.display="none"; }
    if (bslEnrMtc != null) { bslEnrMtc.style.display="none"; }
  }
};
//Bank statement line document type changed:
function bsBslDcTyChg(pInp) {
  var tbPrepPayEntry = document.getElementById("bslPrPaEnr");
  var tbPrepPay = document.getElementById("bslPrPa");
  var tbPrep = document.getElementById("bslPrep");
  var tbPay = document.getElementById("bslPay");
  var tbEntry = document.getElementById("bslEnr");
  var bslPrepMtc = document.getElementById("bslPrepMtc");
  var bslPayMtc = document.getElementById("bslPayMtc");
  var bslEnrMtc = document.getElementById("bslEnrMtc");
  if (pInp.selectedIndex == 0) {
    if (tbPrepPayEntry != null) { tbPrepPayEntry.style.display="none"; }
    if (tbPrepPay != null) { tbPrepPay.style.display="none"; }
    if (tbPrep != null) { tbPrep.style.display="none"; }
    if (tbPay != null) { tbPay.style.display="none"; }
    if (tbEntry != null) { tbEntry.style.display="none"; }
    if (bslPrepMtc != null) { bslPrepMtc.style.display="table"; }
    if (bslPayMtc != null) { bslPayMtc.style.display="table"; }
    if (bslEnrMtc != null) { bslEnrMtc.style.display="table"; }
  } else if (pInp.selectedIndex == 1) {
    if (tbPrepPayEntry != null) { tbPrepPayEntry.style.display="table"; }
    if (tbPrepPay != null) { tbPrepPay.style.display="table"; }
    if (tbPrep != null) { tbPrep.style.display="table"; }
    if (tbPay != null) { tbPay.style.display="none"; }
    if (tbEntry != null) { tbEntry.style.display="none"; }
    if (bslPrepMtc != null) { bslPrepMtc.style.display="none"; }
    if (bslPayMtc != null) { bslPayMtc.style.display="none"; }
    if (bslEnrMtc != null) { bslEnrMtc.style.display="none"; }
  } else if (pInp.selectedIndex == 2) {
    if (tbPrepPayEntry != null) { tbPrepPayEntry.style.display="table"; }
    if (tbPrepPay != null) { tbPrepPay.style.display="table"; }
    if (tbPrep != null) { tbPrep.style.display="none"; }
    if (tbPay != null) { tbPay.style.display="table"; }
    if (tbEntry != null) { tbEntry.style.display="none"; }
    if (bslPrepMtc != null) { bslPrepMtc.style.display="none"; }
    if (bslPayMtc != null) { bslPayMtc.style.display="none"; }
    if (bslEnrMtc != null) { bslEnrMtc.style.display="none"; }
  } else if (pInp.selectedIndex == 3) {
    if (tbPrepPayEntry != null) { tbPrepPayEntry.style.display="table"; }
    if (tbPrepPay != null) { tbPrepPay.style.display="none"; }
    if (tbPrep != null) { tbPrep.style.display="none"; }
    if (tbPay != null) { tbPay.style.display="none"; }
    if (tbEntry != null) { tbEntry.style.display="table"; }
    if (bslPrepMtc != null) { bslPrepMtc.style.display="none"; }
    if (bslPayMtc != null) { bslPayMtc.style.display="none"; }
    if (bslEnrMtc != null) { bslEnrMtc.style.display="none"; }
  }
};
//calculate price and tax cause total/quantity changed:
function bsClcPriTx(pInp, pEntNm, pPriNm, pPrDp, pPriceRm, pInTx, pTxDp, pTxRm) {
  var inpTotal = document.getElementById(pEntNm + "tot");
  var total = bsStrFlt(inpTotal.value);
  bsClcTx(pEntNm, total, pInTx, pTxDp, pTxRm);
  var inpQuantity = document.getElementById(pEntNm + "quan");
  var quantity = bsStrFlt(inpQuantity.value);
  var inpPrice = document.getElementById(pEntNm + pPriNm);
  var price = bsRound(total/quantity, pPrDp, pPriceRm);
  inpPrice.value = bsNumStr(price.toString(), pPrDp);
  var inpPriceVsb = document.getElementById(pEntNm + pPriNm + "Vsb");
  if (inpPriceVsb != null) {
    inpPriceVsb.value = inpPrice.value;
    bsInpChn(inpPriceVsb);
  } else {
    bsInpChn(inpPrice);
  }
  bsInpChn(pInp);
};
//calculate total and tax cause price/quantity changed:
function bsClcToTx(pInp, pEntNm, pPriNm, pPrDp, pPriceRm, pInTx, pTxDp, pTxRm) {
  var inpPrice = document.getElementById(pEntNm + pPriNm);
  var price = bsStrFlt(inpPrice.value);
  var inpQuantity = document.getElementById(pEntNm + "quan");
  var quantity = bsStrFlt(inpQuantity.value);
  var inpTotal = document.getElementById(pEntNm + "tot");
  var total = bsRound(price * quantity, pPrDp, pPriceRm);
  inpTotal.value = bsNumStr(total.toString(), pPrDp);
  var inpTotalVsb = document.getElementById(pEntNm + "totVsb");
  if (inpTotalVsb != null) {
    inpTotalVsb.value = inpTotal.value;
    bsInpChn(inpTotalVsb);
  } else {
    bsInpChn(inpTotal);
  }
  bsInpChn(pInp);
  bsClcTx(pEntNm, total, pInTx, pTxDp, pTxRm);
};
//set tax category from selected item:
function bsSetTxCt(pRate, pTcNm, pIdDom, pInTx, pTxDp, pTxRm, pPrDp) {
  var whoPicking = BSSTATE["WhoPi"][pIdDom];
  var btTxDs = document.getElementById(whoPicking["pigEnt"] + "btTxDs");
  var inpTaxNm = document.getElementById(whoPicking["pigEnt"] + "txCt");
  var inpTaxRate = document.getElementById(whoPicking["pigEnt"] + "rate");
  if (btTxDs == null) {
    inpTaxNm.value = pTcNm;
    if (inpTaxRate != null) { // aggregate or only rate
      inpTaxRate.value = bsNumStr(pRate.toString(), pTxDp);
      bsClcTxFrTo(whoPicking["pigEnt"], pInTx, pPrDp, pTxRm);
      bsInpChn(inpTaxRate);
    }
  } else {
    btTxDs.style.display="inherit";
    inpTaxNm.value = "";
    if (inpTaxRate != null) { // aggregate or only rate
      inpTaxRate.value = "";
      bsClcTxFrTo(whoPicking["pigEnt"], pInTx, pPrDp, pTxRm);
      bsInpChn(inpTaxRate);
    }
  }
  bsInpChn(inpTaxNm);
};
//set revealed tax category from tax destination:
function bsSetDstTx(pRate, pTcNm, pEntNm, pInTx, pTxDp, pTxRm, pPrDp) {
  var inpTaxNm = document.getElementById(pEntNm + "txCt");
  inpTaxNm.value = pTcNm;
  bsInpChn(inpTaxNm);
  var inpTaxRate = document.getElementById(pEntNm + "rate");
  if (inpTaxRate != null) { // aggregate or only rate
    inpTaxRate.value = bsNumStr(pRate.toString(), pTxDp);
    bsInpChn(inpTaxRate);
    bsClcTxFrTo(pEntNm, pInTx, pPrDp, pTxRm);
  }
  var btTxDs = document.getElementById(pEntNm + "btTxDs");
  btTxDs.style.display="none";
};
//calculate tax after setting tax category:
function bsClcTxFrTo(pEntNm, pInTx, pPrDp, pTxRm) {
  var inpTotal = document.getElementById(pEntNm + "tot");
  var total = bsStrFlt(inpTotal.value);
  bsClcTx(pEntNm, total, pInTx, pPrDp, pTxRm);
};
//calculate tax:
function bsClcTx(pEntNm, pTot, pInTx, pPrDp, pTxRm) {
  var inpTaxRate = document.getElementById(pEntNm + "rate");
  var inpTaxTotal = document.getElementById(pEntNm + "toTx");
  var taxTotal;
  if (inpTaxRate.value == "") {
    taxTotal = 0.0;
  } else {
    var taxRate = bsStrFlt(inpTaxRate.value);
    if (pInTx) {
      taxTotal = bsRound(pTot-(pTot/(1.0+taxRate/100.0)),  pPrDp, pTxRm);
    } else {
      taxTotal = bsRound(pTot*taxRate/100.0,  pPrDp, pTxRm);
    }
  }
  inpTaxTotal.value = bsNumStr(taxTotal.toString(), pPrDp);
  bsInpChn(inpTaxTotal);
};
//calculate total = price*quantity
function bsClcTot(pInp, pEntNm, pPriNm, pDecPl, pRm) {
  var inpPrice = document.getElementById(pEntNm + pPriNm);
  var price = bsStrFlt(inpPrice.value);
  var inpQuantity = document.getElementById(pEntNm + "quan");
  var quantity = bsStrFlt(inpQuantity.value);
  var inpTotal = document.getElementById(pEntNm + "tot");
  var total = bsRound(price * quantity, pDecPl, pRm);
  var totals = bsNumStr(total.toString(), pDecPl);
  inpTotal.value = totals;
  var inpTotalVisible = document.getElementById(pEntNm + "totVsb");
  if (inpTotalVisible != null) {
    inpTotalVisible.value = totals;
    bsInpChn(inpTotalVisible);
  } else {
    bsInpChn(inpTotal);
  }
  bsInpChn(pInp);
};
//calculate price = total/quantity
function bsClcPri(pInp, pEntNm, pPriNm, pDecPl, pRm) {
  var inpTotal = document.getElementById(pEntNm + "tot");
  var total = bsStrFlt(inpTotal.value);
  var inpQuantity = document.getElementById(pEntNm + "quan");
  var quantity = bsStrFlt(inpQuantity.value);
  var inpPrice = document.getElementById(pEntNm + pPriNm);
  var price = bsRound(total/quantity, pDecPl, pRm);
  var prices = bsNumStr(price.toString(), pDecPl);
  inpPrice.value = prices;
  var inpPriceVisible = document.getElementById(pEntNm + pPriNm + "Vsb");
  if (inpPriceVisible != null) {
    inpPriceVisible.value = prices;
    bsInpChn(inpPriceVisible);
  } else {
    bsInpChn(inpPrice);
  }
  bsInpChn(pInp);
};
