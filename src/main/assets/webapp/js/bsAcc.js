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
function bsPickSac(pFdNm, pSaNm, pAdPr) {
  var acId=document.getElementById("Entr" + pFdNm + "Id").value;
  if(acId!="") {
    bsPick("Sacnt", "Entr", pSaNm, "&fopownrVlId=" + acId + "&fopownrOpr=eq&fopfrcd=ownr"+ pAdPr);
  } else {
    bsShwErr(BSMSG["choose_account_first"]);
  }
};

//Clears subacc picker:
function bsClearSac(pSaNm) {
  document.getElementById("Entr" + pSaNm + "Id").value = "";
  var saApVsb = document.getElementById("Entr"  + pSaNm + "ApVsb");
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
function setCost(pCost, pIdDmPi) {
  var whoPicking = BSSTATE["WhoPi"][pIdDmPi];
  var inpCostVsb = document.getElementById(whoPicking["pigEnt"] + "itsCostVsb");
  var inpCost = document.getElementById(whoPicking["pigEnt"] + "itsCost");
  if (inpCost.value != pCost) {
    inpCost.value = pCost;
    if (inpCostVsb != null) {
      inpCostVsb.value = pCost;
      inpCostVsb.onchange();
    } else {
      inpCost.onchange();
    }
  }
};

//set UOM for picked item (goods)
function setUom(uomId, uomName, pIdDmPi) {
  var whoPicking = BSSTATE["WhoPi"][pIdDmPi];
  var inpUomId = document.getElementById(whoPicking["pigEnt"] + "unitOfMeasureId");
  if (inpUomId != null) {
    inpUomId.value = uomId;
    var unitOfMeasureApVsb = document.getElementById(whoPicking["pigEnt"] + "unitOfMeasureApVsb");
    unitOfMeasureApVsb.value = uomName;
    unitOfMeasureApVsb.onchange();
  }
  var btnTaxDestination = document.getElementById(whoPicking["pigEnt"] + "btnTaxDestination");
  if (btnTaxDestination != null) {
    //revealing dest tax cat:
    btnTaxDestination.style.display="block";
  }
};

function selectSubacc(subaccId, subaccType, subaccAp, pIdDmPi) {
  var whoPicking = BSSTATE["WhoPi"][pIdDmPi];
  document.getElementById(whoPicking["pigEnt"] + whoPicking["piFld"] + "Ap").value = subaccAp;
  document.getElementById(whoPicking["pigEnt"] + whoPicking["piFld"] + "Ty").value = subaccType;
  document.getElementById(whoPicking["pigEnt"] + whoPicking["piFld"] + "Id").value = subaccId;
  var inpVsb = document.getElementById(whoPicking["pigEnt"] + whoPicking["piFld"] + "ApVsb");
  inpVsb.value = subaccAp;
  inpVsb.onchange();
  document.getElementById(pIdDmPi+"Dlg").close();
};

function selectChooseableSpecType(typeId, typeAp, pIdDmPi) {
  whoPicking = BSSTATE["WhoPi"][pIdDmPi];
  document.getElementById(whoPicking["pigEnt"] + whoPicking["piFld"] +"TyId").setAttribute("value", typeId);
  var inpAp = document.getElementById(whoPicking["pigEnt"] + whoPicking["piFld"] + "TyAp");
  inpAp.setAttribute("value", typeAp);
  var inpApVsb = document.getElementById(whoPicking["pigEnt"] + whoPicking["piFld"] + "TyApVsb");
  inpApVsb.value = typeAp;
  inpApVsb.onchange();
};

function clearSubaccLine(entitySimpleName) {
  document.getElementById(entitySimpleName + "subaccId").setAttribute("value", "");
  document.getElementById(entitySimpleName + "subaccNameAp").setAttribute("value", "");
  document.getElementById(entitySimpleName + "subaccNameApVsb").setAttribute("value", "");
};

function selectAccSubacc(pEntId, pEntAp, pIdDmPi) {
  var whoPicking = BSSTATE["WhoPi"][pIdDmPi];
  document.getElementById(whoPicking["pigEnt"] +"subaccId").setAttribute("value", pEntId);
  document.getElementById(whoPicking["pigEnt"] +"subaccNameAp").setAttribute("value", pEntAp);
  var inpApVsb = document.getElementById(whoPicking["pigEnt"] + "subaccNameApVsb");
  inpApVsb.value = pEntAp;
  inpApVsb.onchange();
  document.getElementById(pIdDmPi+"Dlg").close();
};

function makeTotalTax(pInp, nameEntity, totalGross, pTaxDp, pTaxRm) {
  var inpAllowance = document.getElementById(nameEntity + "allowance");
  var inpPlusAmount = document.getElementById(nameEntity + "plusAmount");
  var inpPercentage = document.getElementById(nameEntity + "itsPercentage");
  var allowance = strToFloat(inpAllowance.value);
  var plusAmount = strToFloat(inpPlusAmount.value);
  var itsPercentage = strToFloat(inpPercentage.value);
  var inpTotal = document.getElementById(nameEntity + "itsTotal");
  var total = numRound(plusAmount + (totalGross - allowance) * itsPercentage / 100, pTaxDp, pTaxRm);
  inpTotal.value = numToStr(total.toString(), pTaxDp);
  bsInpChn(inpTotal);
  bsInpChn(pInp);
};

function clearWageTaxes(nameEntity) {
  var inpTotalWageTaxes = document.getElementById(nameEntity + "totalWageTaxes");
  var inpTotalWageTaxesVsb = document.getElementById(nameEntity + "totalWageTaxesVsb");
  inpTotalWageTaxes.value = 0;
  inpTotalWageTaxesVsb.value = 0;
  bsInpChn(inpTotalWageTaxesVsb);
};

function tryToSetPercentagePlusAmount(itsPercentage, plusAmount, pIdDmPi) {
  var whoPicking = BSSTATE["WhoPi"][pIdDmPi];
  var inpPercentage = document.getElementById(whoPicking["pigEnt"] + "itsPercentage");
  if(inpPercentage != null) {
    var inpPlusAmount = document.getElementById(whoPicking["pigEnt"] + "plusAmount");
    if(inpPlusAmount != null) {
      inpPlusAmount.value = plusAmount;
      bsInpChn(inpPlusAmount);
    }
    inpPercentage.value = itsPercentage;
    inpPercentage.onchange();
  }
};

function makeFltrPaymentTot(pInp, pIdSelFlt) {
  var fldWas;
  var fldIs;
  if (pInp.options[pInp.selectedIndex].value == "ITSTOTAL") {
    fltIs = "ITSTOTAL";
    fltWas = "FOREIGNTOTAL";
  } else {
    fltIs = "FOREIGNTOTAL";
    fltWas = "ITSTOTAL";
  }
  var selFlt = document.getElementById(pIdSelFlt);
  for (var i=0; i < selFlt.options.length; i++) {
    selFlt.options[i].value = selFlt.options[i].value.replace(fltWas, fltIs);
  }  
};

function bnStLnAccentryMatchChanged(pInp) {
  var tbPrepPayEntry = document.getElementById("bnkStLnPrepPayEntry");
  var tbPrepPay = document.getElementById("bnkStLnPrepPay");
  var tbPrep = document.getElementById("bnkStLnPrep");
  var tbPay = document.getElementById("bnkStLnPay");
  var tbEntry = document.getElementById("bnkStLnEntry");
  var bnkStLnPrepMatch = document.getElementById("bnkStLnPrepMatch");
  var bnkStLnPayMatch = document.getElementById("bnkStLnPayMatch");
  if (pInp.selectedIndex == 0) {
    if (tbPrepPayEntry != null) { tbPrepPayEntry.style.display="table"; }
    if (tbPrepPay != null) { tbPrepPay.style.display="table"; }
    if (tbPrep != null) { tbPrep.style.display="table"; }
    if (tbPay != null) { tbPay.style.display="table"; }
    if (tbEntry != null) { tbEntry.style.display="table"; }
    if (bnkStLnPrepMatch != null) { bnkStLnPrepMatch.style.display="table"; }
    if (bnkStLnPayMatch != null) { bnkStLnPayMatch.style.display="table"; }
  } else {
    if (tbPrepPayEntry != null) { tbPrepPayEntry.style.display="none"; }
    if (tbPrepPay != null) { tbPrepPay.style.display="none"; }
    if (tbPrep != null) { tbPrep.style.display="none"; }
    if (tbPay != null) { tbPay.style.display="none"; }
    if (tbEntry != null) { tbEntry.style.display="none"; }
    if (bnkStLnPrepMatch != null) { bnkStLnPrepMatch.style.display="none"; }
    if (bnkStLnPayMatch != null) { bnkStLnPayMatch.style.display="none"; }
  }
};

function bnStLnPrepayMatchChanged(pInp) {
  var tbPrepPayEntry = document.getElementById("bnkStLnPrepPayEntry");
  var tbPrepPay = document.getElementById("bnkStLnPrepPay");
  var tbPrep = document.getElementById("bnkStLnPrep");
  var tbPay = document.getElementById("bnkStLnPay");
  var tbEntry = document.getElementById("bnkStLnEntry");
  var bnkStLnPayMatch = document.getElementById("bnkStLnPayMatch");
  var bnkStLnAccentryMatch = document.getElementById("bnkStLnAccentryMatch");
  if (pInp.selectedIndex == 0) {
    if (tbPrepPayEntry != null) { tbPrepPayEntry.style.display="table"; }
    if (tbPrepPay != null) { tbPrepPay.style.display="table"; }
    if (tbPrep != null) { tbPrep.style.display="table"; }
    if (tbPay != null) { tbPay.style.display="table"; }
    if (tbEntry != null) { tbEntry.style.display="table"; }
    if (bnkStLnPayMatch != null) { bnkStLnPayMatch.style.display="table"; }
    if (bnkStLnAccentryMatch != null) { bnkStLnAccentryMatch.style.display="table"; }
  } else {
    if (tbPrepPayEntry != null) { tbPrepPayEntry.style.display="none"; }
    if (tbPrepPay != null) { tbPrepPay.style.display="none"; }
    if (tbPrep != null) { tbPrep.style.display="none"; }
    if (tbPay != null) { tbPay.style.display="none"; }
    if (tbEntry != null) { tbEntry.style.display="none"; }
    if (bnkStLnPayMatch != null) { bnkStLnPayMatch.style.display="none"; }
    if (bnkStLnAccentryMatch != null) { bnkStLnAccentryMatch.style.display="none"; }
  }
};

function bnStLnPayMatchChanged(pInp) {
  var tbPrepPayEntry = document.getElementById("bnkStLnPrepPayEntry");
  var tbPrepPay = document.getElementById("bnkStLnPrepPay");
  var tbPrep = document.getElementById("bnkStLnPrep");
  var tbPay = document.getElementById("bnkStLnPay");
  var tbEntry = document.getElementById("bnkStLnEntry");
  var bnkStLnPrepMatch = document.getElementById("bnkStLnPrepMatch");
  var bnkStLnAccentryMatch = document.getElementById("bnkStLnAccentryMatch");
  if (pInp.selectedIndex == 0) {
    if (tbPrepPayEntry != null) { tbPrepPayEntry.style.display="table"; }
    if (tbPrepPay != null) { tbPrepPay.style.display="table"; }
    if (tbPrep != null) { tbPrep.style.display="table"; }
    if (tbPay != null) { tbPay.style.display="table"; }
    if (tbEntry != null) { tbEntry.style.display="table"; }
    if (bnkStLnPrepMatch != null) { bnkStLnPrepMatch.style.display="table"; }
    if (bnkStLnAccentryMatch != null) { bnkStLnAccentryMatch.style.display="table"; }
  } else {
    if (tbPrepPayEntry != null) { tbPrepPayEntry.style.display="none"; }
    if (tbPrepPay != null) { tbPrepPay.style.display="none"; }
    if (tbPrep != null) { tbPrep.style.display="none"; }
    if (tbPay != null) { tbPay.style.display="none"; }
    if (tbEntry != null) { tbEntry.style.display="none"; }
    if (bnkStLnPrepMatch != null) { bnkStLnPrepMatch.style.display="none"; }
    if (bnkStLnAccentryMatch != null) { bnkStLnAccentryMatch.style.display="none"; }
  }
};

function bnStLnDocTypeChanged(pInp) {
  var tbPrepPayEntry = document.getElementById("bnkStLnPrepPayEntry");
  var tbPrepPay = document.getElementById("bnkStLnPrepPay");
  var tbPrep = document.getElementById("bnkStLnPrep");
  var tbPay = document.getElementById("bnkStLnPay");
  var tbEntry = document.getElementById("bnkStLnEntry");
  var bnkStLnPrepMatch = document.getElementById("bnkStLnPrepMatch");
  var bnkStLnPayMatch = document.getElementById("bnkStLnPayMatch");
  var bnkStLnAccentryMatch = document.getElementById("bnkStLnAccentryMatch");
  if (pInp.selectedIndex == 0) {
    if (tbPrepPayEntry != null) { tbPrepPayEntry.style.display="none"; }
    if (tbPrepPay != null) { tbPrepPay.style.display="none"; }
    if (tbPrep != null) { tbPrep.style.display="none"; }
    if (tbPay != null) { tbPay.style.display="none"; }
    if (tbEntry != null) { tbEntry.style.display="none"; }
    if (bnkStLnPrepMatch != null) { bnkStLnPrepMatch.style.display="table"; }
    if (bnkStLnPayMatch != null) { bnkStLnPayMatch.style.display="table"; }
    if (bnkStLnAccentryMatch != null) { bnkStLnAccentryMatch.style.display="table"; }
  } else if (pInp.selectedIndex == 1) {
    if (tbPrepPayEntry != null) { tbPrepPayEntry.style.display="table"; }
    if (tbPrepPay != null) { tbPrepPay.style.display="table"; }
    if (tbPrep != null) { tbPrep.style.display="table"; }
    if (tbPay != null) { tbPay.style.display="none"; }
    if (tbEntry != null) { tbEntry.style.display="none"; }
    if (bnkStLnPrepMatch != null) { bnkStLnPrepMatch.style.display="none"; }
    if (bnkStLnPayMatch != null) { bnkStLnPayMatch.style.display="none"; }
    if (bnkStLnAccentryMatch != null) { bnkStLnAccentryMatch.style.display="none"; }
  } else if (pInp.selectedIndex == 2) {
    if (tbPrepPayEntry != null) { tbPrepPayEntry.style.display="table"; }
    if (tbPrepPay != null) { tbPrepPay.style.display="table"; }
    if (tbPrep != null) { tbPrep.style.display="none"; }
    if (tbPay != null) { tbPay.style.display="table"; }
    if (tbEntry != null) { tbEntry.style.display="none"; }
    if (bnkStLnPrepMatch != null) { bnkStLnPrepMatch.style.display="none"; }
    if (bnkStLnPayMatch != null) { bnkStLnPayMatch.style.display="none"; }
    if (bnkStLnAccentryMatch != null) { bnkStLnAccentryMatch.style.display="none"; }
  } else if (pInp.selectedIndex == 3) {
    if (tbPrepPayEntry != null) { tbPrepPayEntry.style.display="table"; }
    if (tbPrepPay != null) { tbPrepPay.style.display="none"; }
    if (tbPrep != null) { tbPrep.style.display="none"; }
    if (tbPay != null) { tbPay.style.display="none"; }
    if (tbEntry != null) { tbEntry.style.display="table"; }
    if (bnkStLnPrepMatch != null) { bnkStLnPrepMatch.style.display="none"; }
    if (bnkStLnPayMatch != null) { bnkStLnPayMatch.style.display="none"; }
    if (bnkStLnAccentryMatch != null) { bnkStLnAccentryMatch.style.display="none"; }
  }
};

function selectCsvPath(pVal, pPathAppr) {
  var CsvColumndataIndex = document.getElementById("CsvColumndataIndex");
  var CsvColumnfieldPath = document.getElementById("CsvColumnfieldPath");
  var dataPath = document.getElementById("dataPath");
  var scIdx = pVal.indexOf(";");
  if (scIdx == -1) {
    CsvColumndataIndex.value = pVal;
    CsvColumnfieldPath.value = "";
  } else {
    var arr = pVal.split(";");
    CsvColumndataIndex.value = arr[0];
    CsvColumnfieldPath.value = arr[1];
  }
  dataPath.value = pPathAppr;
  dataPath.onchange();
};

function clearCsvPath() {
  var CsvColumndataIndex = document.getElementById("CsvColumndataIndex");
  var dataPath = document.getElementById("dataPath");
  var CsvColumnfieldPath = document.getElementById("CsvColumnfieldPath");
  CsvColumnfieldPath.value = "";
  CsvColumndataIndex.value = "";
  dataPath.value = "";
  dataPath.onchange();
};

function openCsvPathPicker(pPath, pReadNm) {
  var picker = document.getElementById("pickersCsvPath" + pReadNm + "Dlg");
  if (picker != null) {
      picker.showModal();
  } else {
    bsGtAjx('GET', pPath + pReadNm);
  }
};

function calcPriceTax(pInp, nameEntity, pPriceNm, pPriceDp, pPriceRm, pIsTaxIncluded, pTaxDp, pTaxRm) {
  var inpTotal = document.getElementById(nameEntity + "itsTotal");
  var total = strToFloat(inpTotal.value);
  calcTax(nameEntity, total, pIsTaxIncluded, pTaxDp, pTaxRm);
  var inpQuantity = document.getElementById(nameEntity + "itsQuantity");
  var quantity = strToFloat(inpQuantity.value);
  var inpPrice = document.getElementById(nameEntity + pPriceNm);
  var price = numRound(total/quantity, pPriceDp, pPriceRm);
  inpPrice.value = numToStr(price.toString(), pPriceDp);
  var inpPriceVsb = document.getElementById(nameEntity + pPriceNm + "Vsb");
  if (inpPriceVsb != null) {
    inpPriceVsb.value = inpPrice.value;
    bsInpChn(inpPriceVsb);
  } else {
    bsInpChn(inpPrice);
  }
  bsInpChn(pInp);
};

function calcTotalTax(pInp, nameEntity, pPriceNm, pPriceDp, pPriceRm, pIsTaxIncluded, pTaxDp, pTaxRm) {
  var inpPrice = document.getElementById(nameEntity + pPriceNm);
  var price = strToFloat(inpPrice.value);
  var inpQuantity = document.getElementById(nameEntity + "itsQuantity");
  var quantity = strToFloat(inpQuantity.value);
  var inpTotal = document.getElementById(nameEntity + "itsTotal");
  var total = numRound(price * quantity, pPriceDp, pPriceRm);
  inpTotal.value = numToStr(total.toString(), pPriceDp);
  var inpTotalVsb = document.getElementById(nameEntity + "itsTotalVsb");
  if (inpTotalVsb != null) {
    inpTotalVsb.value = inpTotal.value;
    bsInpChn(inpTotalVsb);
  } else {
    bsInpChn(inpTotal);
  }
  bsInpChn(pInp);
  calcTax(nameEntity, total, pIsTaxIncluded, pTaxDp, pTaxRm);
};

function setTaxCat(pTcRate, pTcNm, pIdDomBasePicker, pIsTaxIncluded, pTaxDp, pTaxRm, pPriceDp) {
  var whoPicking = BSSTATE["WhoPi"][pIdDomBasePicker];
  var btnTaxDestination = document.getElementById(whoPicking["pigEnt"] + "btnTaxDestination");
  var inpTaxNm = document.getElementById(whoPicking["pigEnt"] + "taxCategory");
  var inpTaxRate = document.getElementById(whoPicking["pigEnt"] + "itsPercentage");
  if (btnTaxDestination == null) {
    inpTaxNm.value = pTcNm;
    if (inpTaxRate != null) { // aggregate or only rate
      inpTaxRate.value = numToStr(pTcRate.toString(), pTaxDp);
      calcTaxFromTot(whoPicking["pigEnt"], pIsTaxIncluded, pPriceDp, pTaxRm);
      bsInpChn(inpTaxRate);
    }
  } else {
    btnTaxDestination.style.display="inherit";
    inpTaxNm.value = "";
    if (inpTaxRate != null) { // aggregate or only rate
      inpTaxRate.value = "";
      calcTaxFromTot(whoPicking["pigEnt"], pIsTaxIncluded, pPriceDp, pTaxRm);
      bsInpChn(inpTaxRate);
    }
  }
  bsInpChn(inpTaxNm);
};

function setDestTaxCat(pTcRate, pTcNm, pEntityName, pIsTaxIncluded, pTaxDp, pTaxRm, pPriceDp) {
  var inpTaxNm = document.getElementById(pEntityName + "taxCategory");
  inpTaxNm.value = pTcNm;
  bsInpChn(inpTaxNm);
  var inpTaxRate = document.getElementById(pEntityName + "itsPercentage");
  if (inpTaxRate != null) { // aggregate or only rate
    inpTaxRate.value = numToStr(pTcRate.toString(), pTaxDp);
    bsInpChn(inpTaxRate);
    calcTaxFromTot(pNameEntity, pIsTaxIncluded, pPriceDp, pTaxRm);
  }
  var btnTaxDestination = document.getElementById(pEntityName + "btnTaxDestination");
  btnTaxDestination.style.display="none";
};

function calcTaxFromTot(nameEntity, pIsTaxIncluded, pPriceDp, pTaxRm) {
  var inpTotal = document.getElementById(nameEntity + "itsTotal");
  var total = strToFloat(inpTotal.value);
  calcTax(nameEntity, total, pIsTaxIncluded, pPriceDp, pTaxRm);
};

function calcTax(pNameEntity, pTotal, pIsTaxIncluded, pPriceDp, pTaxRm) {
  var inpTaxRate = document.getElementById(pNameEntity + "itsPercentage");
  var inpTaxTotal = document.getElementById(pNameEntity + "totalTaxes");
  var taxTotal;
  if (inpTaxRate.value == "") {
    taxTotal = 0.0;
  } else {
    var taxRate = strToFloat(inpTaxRate.value);
    if (pIsTaxIncluded) {
      taxTotal = numRound(pTotal-(pTotal/(1.0+taxRate/100.0)),  pPriceDp, pTaxRm);
    } else {
      taxTotal = numRound(pTotal*taxRate/100.0,  pPriceDp, pTaxRm);
    }
  }
  inpTaxTotal.value = numToStr(taxTotal.toString(), pPriceDp);
  bsInpChn(inpTaxTotal);
};
