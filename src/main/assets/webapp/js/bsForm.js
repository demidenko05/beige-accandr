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
/*
 * implements logic "form has been changed", "nothing to send", send form with AJAX...
 * @author Yury Demidenko
 */
 
//Conversation state variables:
var BSSTATE = {
  "WhoPi" : {},
};
window.onbeforeunload = function(e) {
  if(bsAnyOpFrmChd()) {
    return BSMSG["formHasBeenChanged"];
  }
};
window.onload=function(e){
  bsInitJs();
};
function bsInitJs(){
  BSSTATE["WhoPi"]={};
};
//Invoices GET AJAX with conformation
function bsGtAjxCnf(method, url) {
  var funcYes = function() {
    document.getElementById('dlgCnf').close();
    bsGtAjx(method,url);
  };
  bsShwCnf(BSMSG['are_you_sure'], funcYes);
};
//Invokes GET AJAX carefully (checks if any opened form changed)
function bsGtAjxCf(method, url) {
  if (bsAnyOpFrmChd()) {
    var funcYes = function() {
      document.getElementById('dlgCnf').close();
      bsGtAjx(method,url);
    };
    bsShwCnf(BSMSG["formHasBeenChanged"], funcYes);
  } else {
    bsGtAjx(method,url);
  }
};
//Submits form by AJAX with conformation:
function bsSbFrmAjxCf(pIdFrm, pIsMustHasChanges, pAddParams) {
  var funcYes = function() {
    document.getElementById('dlgCnf').close();
    var frm = document.getElementById(pIdFrm);
    if (bsChkFrm(frm, pIsMustHasChanges)) {
      bsSndFrmAjx(frm, pAddParams);
    }
  };
  bsShwCnf(BSMSG['are_you_sure'], funcYes);
};
//Submits form by AJAX:
function bsSbFrmAjx(pIdFrm, pIsMustHasChanges, pAddParams) {
  var frm = document.getElementById(pIdFrm);
  if (bsChkFrm(frm, pIsMustHasChanges)) {
    bsSndFrmAjx(frm, pAddParams);
  }
};
//Submits form and opens new tab:
function bsSbFrmNw(pIdFrm, pIsMustHasChanges) {
  var frm = document.getElementById(pIdFrm);
  if (bsChkFrm(frm, pIsMustHasChanges)) {
    frm.submit();
    bsRemFrmCngs(frm);
  }
};
//Submits form with conformation:
function bsCnfSbm(inpSbmt, msg) {
  var funcYes = function() {
    inpSbmt.form.submit();
    document.getElementById('dlgCnf').close();
  };
  bsShwCnf(msg, funcYes);
};
//Sets new address(URL) with conformation:
function bsCnfHref(inpHref, msg) {
  var funcYes = function() {
    window.location.assign(inpHref.href);
    document.getElementById('dlgCnf').close();
  };
  bsShwCnf(msg, funcYes);
};
//Any opened form has been changed:
function bsAnyOpFrmChd() {
  var forms = document.querySelectorAll('form');
  for(var i=0; i < forms.length; i++) {
    var formDlg = document.getElementById(forms[i].id.replace("Frm", "Dlg"));
    if(formDlg != null && formDlg.open && bsIfFrmChd(forms[i])) {
      return true;
    }
  }
  return false;
};
//Input value has been changed:
function bsInpChn(inpt) {
  switch (inpt.type) {
  case "select-one":
    if (!inpt.options[inpt.selectedIndex].defaultSelected && !inpt.classList.contains('changed')) {
      inpt.classList.add('changed');
      return true;
    } else if(inpt.options[inpt.selectedIndex].defaultSelected && inpt.classList.contains('changed')) {
      inpt.classList.remove('changed');
      return true;
    }
    return false;
  case "checkbox":
    return bsChekbChd(inpt);
  case "radio":
    var radios = document.getElementsByName(inpt.name);
    var isChanged = false;
    for (i = 0; i < radios.length; i++) {
      if (radios[i].type == "radio") { //filter hidden from main form
        if (bsChekbChd(radios[i])) {
          isChanged = true;
        }
      }
    }
    return isChanged;
  default:
    if (inpt.value != inpt.defaultValue && !inpt.classList.contains('changed')) {
      inpt.classList.add('changed');
      return true;
    } else if(inpt.value == inpt.defaultValue && inpt.classList.contains('changed')) {
      inpt.classList.remove('changed');
      return true;
    }
    return false;
  }
};
//Checkable changed event:
function bsChekbChd(inpt) {
  if(inpt.checked != inpt.defaultChecked && !inpt.labels[0].classList.contains('changed')) {
    inpt.labels[0].classList.add('changed');
    return true;
  } else if(inpt.checked == inpt.defaultChecked && inpt.labels[0].classList.contains('changed')) {
    inpt.labels[0].classList.remove('changed');
    return true;
  }
  return false;
};
//Filter's operator changed event:
function bsFltOprChd(inpt, nameInput) {
  bsInpChn(inpt);
  var isDisabled = !(inpt.options[inpt.selectedIndex].value == "gt"
    || inpt.options[inpt.selectedIndex].value == "lt"
    || inpt.options[inpt.selectedIndex].value == "eq");
  document.getElementById(nameInput).disabled = isDisabled;
};
//String filter's operator changed event:
function bsFltStrChd(inpt, nameInput1) {
  bsInpChn(inpt);
  var isDisabled = (inpt.options[inpt.selectedIndex].value == "disabled");
  document.getElementById(nameInput1).disabled = isDisabled;
};
//Checks/validates form:
function bsChkFrm(pFrm, pIsMustHasChanges) {
  if (!pFrm.checkValidity()){
    document.getElementById(pFrm.id + 'Fs').click();
    return false;
  }
  if (pIsMustHasChanges == null || pIsMustHasChanges) {
    if (!bsIfFrmChd(pFrm)){
      bsShwWrn(BSMSG["nothingToSend"]);
      return false;
    }
  }
  //validation of owned entity
  reqiredChildSelected = true;
  inputs = pFrm.querySelectorAll('input[type="hidden"][required]');
  for (var i=0; i < inputs.length; i++)
    if(inputs[i].value==""){
      reqiredChildSelected=false;
      break;
    }
  if(!reqiredChildSelected) {
    bsShwWrn(BSMSG['select_child']);
    return false;
  }
  return true;
};
//Checks if form changed:
function bsIfFrmChd(pFrm) {
  var childrenChanged = pFrm.querySelectorAll(".changed");
  if (childrenChanged.length > 0) {
    return true;
  }
  return false;
};
//Removes form's changes:
function bsRemFrmCngs(pFrm) {
  inputs = pFrm.querySelectorAll(".changed");
  for (var i=0; i < inputs.length; i++) {
    inputs[i].classList.remove('changed');
  }
  var inputs = pFrm.querySelectorAll('input[type="radio"]:not([disabled])');
  for (var i=0; i < inputs.length; i++){
    inputs[i].defaultChecked = inputs[i].checked;     
  }
  inputs = pFrm.querySelectorAll('select:not([disabled])');
  for (var i=0; i < inputs.length; i++) {
    for (var j=0; j < inputs[i].options.length; j++) {
      inputs[i].options[j].defaultSelected = false;
    }
    inputs[i].options[inputs[i].selectedIndex].defaultSelected = true;
  }
  inputs = pFrm.querySelectorAll('input[type="checkbox"]:not([disabled])');
  for (var i=0; i < inputs.length; i++) {
    inputs[i].defaultChecked = inputs[i].checked;
  }
  inputs = pFrm.querySelectorAll('input[type="text"]');
  for (var i=0; i < inputs.length; i++) {
    inputs[i].defaultValue=inputs[i].value;
  }
  inputs = pFrm.querySelectorAll('textarea');
  for (var i=0; i < inputs.length; i++) {
    inputs[i].defaultValue=inputs[i].value;
  }
  inputs = pFrm.querySelectorAll('input[type="hidden"]:not([disabled])');
  for (var i=0; i < inputs.length; i++) {
    inputs[i].defaultValue=inputs[i].value;
  }
  inputs = pFrm.querySelectorAll('input[type="textarea"]:not([disabled])');
  for (var i=0; i < inputs.length; i++) {
    inputs[i].defaultValue=inputs[i].value;
  }
  inputs = pFrm.querySelectorAll('input[type="datetime-local"]:not([disabled])');
  for (var i=0; i < inputs.length; i++) {
    inputs[i].defaultValue=inputs[i].value;
  }
  inputs = pFrm.querySelectorAll('input[type="date"]:not([disabled])');
  for (var i=0; i < inputs.length; i++) {
    inputs[i].defaultValue=inputs[i].value;
  }
  inputs = pFrm.querySelectorAll('input[type="number"]:not([disabled])');
  for (var i=0; i < inputs.length; i++) {
    inputs[i].defaultValue=inputs[i].value;
  }
};
//Makes related input requiring:
function bsMkRelInp(pInp, pIdRelInp) {
  var inpRel = document.getElementById(pIdRelInp);
  if (pInp.value != "") {
    inpRel.required = true;
  } else {
    inpRel.required = false;
    inpRel.value = "";
  }
};
//Already uploaded file path changed:
function bsFlUpPhChd(pInp, pInpFlUpNm, pInpParNmFlUpNm) {
  if (pInp.value != "") {
    var inpFileUp = document.getElementById(pInpFlUpNm);
    inpFileUp.value = "";
    if (inpFileUp.required) {
      pInp.required = true;
      inpFileUp.required = false;
    }
    bsInpChn(inpFileUp);
    var inpParamNameFileToUpload = document.getElementById(pInpParNmFlUpNm);
    inpParamNameFileToUpload.disabled = true;
  }
  bsInpChn(pInp);
};
//File upload changed:
function bsFlUpChd(pInp, pInpFileUpPathNm, pInpParNmFlUpNm) {
  if (pInp.value != "") {
    var inpFileUpPath = document.getElementById(pInpFileUpPathNm);
    inpFileUpPath.value = "";
    if (inpFileUpPath.required) {
      pInp.required = true;
      inpFileUpPath.required = false;
    }
    bsInpChn(inpFileUpPath);
    var inpParamNameFileToUpload = document.getElementById(pInpParNmFlUpNm);
    inpParamNameFileToUpload.disabled = false;
  }
  bsInpChn(pInp);
};
//Selects/sets entity by picker:
function bsSelEnt(pEntId, pEntAp, pIdDmPi) {
  var whoPicking = BSSTATE["WhoPi"][pIdDmPi];
  var pref = whoPicking["pigEnt"] + whoPicking["piFld"];
  document.getElementById(pref +"Id").value = pEntId;
  var inpAp = document.getElementById(pref + "Ap");
  if (inpAp != null) { //invisible appearance to be sent
    inpAp.value = pEntAp;
  }
  var inpApVsb = document.getElementById(pref + "ApVsb");
  inpApVsb.value = pEntAp;
  bsInpChn(inpApVsb);
  document.getElementById(pIdDmPi+"Dlg").close();
};
//Clears pickable entity:
function bsClrSelEnt(idDomBaseProperty) {
  var inpId = document.getElementById(idDomBaseProperty + "Id");
  if (inpId.value != "") {
    inpId.value = "";
    var inpAp = document.getElementById(idDomBaseProperty + "Ap");
    if (inpAp != null) { //invisible appearence to be sent
      inpAp.value="";
    }
    var inpApVsb = document.getElementById(idDomBaseProperty + "ApVsb");
    inpApVsb.value = "";
    bsInpChn(inpApVsb);
  }
};
//Opens entity picker:
function bsPick(pidEnt, pigEnt, piFld, adPr){
  var piPl = "pisPl";
  var piForEnt = document.getElementById(piPl + pidEnt + "Dlg");
  var piRnd = null;
  if (piForEnt != null) {
    if (!piForEnt.open) {
      if (BSSTATE["WhoPi"][piPl + pidEnt + "adPr"] == adPr) {
        piForEnt.showModal();
      } else {
        piRnd = "phj";
      }
    } else {
      if (adPr != null) {
        adPr.replace("fop", "fopd");
      }
      pikerPlace = "pisPlD";
      piForEnt = document.getElementById(piPl + pidEnt + "Dlg");
      if (piForEnt != null) {
        if (!piForEnt.open) {
          if (BSSTATE["WhoPi"][piPl + pidEnt + "adPr"] == adPr) {
              piForEnt.showModal();
          } else {
            piRnd = "pdhj";
          }
        } else {
          bsShwErr(BSMSG['2_pickers_opened_already_for'] + pidEnt);
        }
      } else {
        piRnd = "pdhj";
      }
    }
  } else {
    piRnd = "phj";
  }
  if (piRnd != null) {
    var paramsStr = "srv/?act=lst&pg=1&rnd=" + piRnd + "&ent=" + pidEnt;
    BSSTATE["WhoPi"][piPl + pidEnt + "adPr"] = adPr;
    if (adPr != null) {
      bsGtAjx('GET', paramsStr + adPr);
    } else {
      bsGtAjx('GET', paramsStr);
    }
  }
  BSSTATE["WhoPi"][piPl + pidEnt] = {pigEnt: pigEnt, piFld: piFld};
};
//Opens HTML5 dialog:
function bsOpnDlg(idDlg) {
  document.getElementById(idDlg).showModal();
};
//Closes HTML5 dialog:
function bsClsDlg(nameDlg) {
  document.getElementById(nameDlg).close();
};
//Closes HTML5 dialog with conformation:
function bsClsDlgCf(idDomBase) {
  var pFrm=document.getElementById(idDomBase + "Frm");
  if (bsIfFrmChd(pFrm)) {
    var funcYes = function() {
      document.getElementById(idDomBase + "Dlg").close();
      document.getElementById('dlgCnf').close();
    };
    bsShwCnf(BSMSG["formHasBeenChanged"], funcYes);
  } else {
    document.getElementById(idDomBase + "Dlg").close();
  }
};
//Clears changes and closes dialog:
function bsClChnCsDlg(idDomBase) {
  document.getElementById(idDomBase + "Dlg").close();
  bsRemFrmCngs(document.getElementById(idDomBase + "Frm"));
};
//Shows confirm dialog:
function bsShwCnf(msg, yesHandler) {
  document.getElementById("cnfPl").innerHTML = msg;
  document.getElementById("dlgCnf").showModal();
  document.getElementById("cnfYes").onclick = yesHandler;
};
//Shows warning dialog:
function bsShwWrn(msg) {
  document.getElementById("wrnPl").innerHTML = msg;
  document.getElementById("dlgWrn").showModal();
};
//Shows error dialog:
function bsShwErr(msg) {
  document.getElementById("errPl").innerHTML = msg;
  document.getElementById("dlgErr").showModal();
};
//Shows success dialog:
function bsShwSuc(msg) {
  document.getElementById("sucPl").innerHTML = msg;
  document.getElementById("dlgSuc").style.display = "block";
  setTimeout(bsClsSuc, 3000);
};
//Closes success dialog:
function bsClsSuc() {
  document.getElementById("dlgSuc").style.display = "none";
};
/*
 * <p>Initializes BS inputs for class "bsNum[dpm]", where dpm:
 * 0 - 0 DP; 1 - 1 DP; 2m - 2DP, nmin -999 999 999 999 999.99.
 * </p>
 * @param pUsedDpm DPM
 * @param pParent ID of parent DOM
 */
function bsIniInpNum(pUsedDpm, pParent) {
  $.each(pUsedDpm.split(","), function (idx, dpm) {
    var dp = parseInt(dpm.charAt(0));
    var ndef = {decPl: dp, frmOnIni: true};
    if (dpm.length > 1) {
      nmins = "-999999999999999";
      if (dp > 0) {
        nmins = nmins + ".9";
        for (i = 1; i < dp; i++) {
          nmins = nmins + "9";
        }
        ndef.nmin = parseFloat(nmins);
      } else {
        ndef.nmin = parseInt(nmins);
      }
    }
    if (pParent == null) {
      $(".bsNum" + dpm).bsInpNum(ndef);
    } else {
      $("#" + pParent).find(".bsNum" + dpm).bsInpNum(ndef);
    }
  });
};
/*
 * <p>Initializes BS inputs for class "bsNum[step]", where step:
 * 0d25 - 0.25, 1 - 1, 2d5 - 2.5.
 * It's useful for initialize "quantity" that has minimum=step, shopping cart
 * may has items with different quantity's step.
 * </p>
 * @param pUsedSteps comma separated steps
 * @param pParent ID of parent DOM
 */
function bsIniInpNumStep(pUsedSteps, pParent) {
  $.each(pUsedSteps.split(","), function (idx, sts) {
    var stsd = sts.replace("d", ".");
    var st = parseFloat(stsd);
    var ndef = {step: st, frmOnIni: true};
    if (pParent == null) {
      $(".bsNum" + sts).bsInpNum(ndef);
    } else {
      $("#" + pParent).find(".bsNum" + sts).bsInpNum(ndef);
    }
  });
};
//selects/sets CSV path
function bsSetCsvPth(pVal, pPathAppr) {
  var CsvCldtIdx = document.getElementById("CsvCldtIdx");
  var CsvClfldPh = document.getElementById("CsvClfldPh");
  var dataPath = document.getElementById("dataPath");
  var scIdx = pVal.indexOf(";");
  if (scIdx == -1) {
    CsvCldtIdx.value = pVal;
    CsvClfldPh.value = "";
  } else {
    var arr = pVal.split(";");
    CsvCldtIdx.value = arr[0];
    CsvClfldPh.value = arr[1];
  }
  dataPath.value = pPathAppr;
  bsInpChn(dataPath);
};
//Clears CSV path:
function bsClCsvPth() {
  var CsvCldtIdx = document.getElementById("CsvCldtIdx");
  var dataPath = document.getElementById("dataPath");
  var CsvClfldPh = document.getElementById("CsvClfldPh");
  CsvClfldPh.value = "";
  CsvCldtIdx.value = "";
  dataPath.value = "";
  bsInpChn(dataPath);
};
//picks CSV path:
function bsPickCsvPth(pPath,pReadNm) {
  var picker = document.getElementById("piCsvPth" + pReadNm + "Dlg");
  if (picker != null) {
      picker.showModal();
  } else {
    bsGtAjx('GET', pPath + pReadNm);
  }
};
