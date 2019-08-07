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
 * JQuery based input number. It requires bsNum.js.
 * author Yury Demidenko
 * 
 * It makes internationalized input number (substitute of HTML5 input number that not yet enough implemented).
 * NULL value is not allowed!
 * Initial value must be exactly number or empty (it will be 0) - dot separator without group separators, number without decimal separator will do, e.g. "0".
 * If you pass only "step" in parameter, then decimal places and nmin will be same as in "step". If you pass "decimal places" without "step", then step will be minimum.
 * Number will be always adjusted to the "previous step", e.g. if step=0.25 and edited value=12.27, then value will be adjusted to 12.25.
 * It has two custom events "increase" and "decrease" by "step".
 * adjustOnKeyUp is true, set it false then step has "more than one nonzero digit ", e.g "0.25" or "1.5"
 * frmOnIni - formats number on initializing
 * author Yury Demidenko
 */

(function($) {
  $.fn.bsInpNum = function(pVars) {
    //default vars:
    var vars = {
      step: 1,
      decPl: 0,
      nmin: 0,
      nmax: 999999999999999,
      adjustOnKeyUp: true,
      frmOnIni: true
    };
    $.extend(vars, pVars);
    if (pVars != null && pVars.step != null) {
      if (pVars.nmin == null) {
        vars.nmin = pVars.step;
      }
      if (pVars.decPl == null) {
        var steps = vars.step.toString();
        var dsIdx = steps.indexOf(".");
        var decPl = 0;
        if (dsIdx == -1) {
          vars.decPl = 0;
        } else {
          vars.decPl = steps.length - dsIdx - 1;
        }
      }
    } else if (pVars != null && pVars.decPl != null && pVars.step == null) {
      var stepMin = bsRound(1.0 / Math.pow(10, vars.decPl), vars.decPl);
      vars.step = stepMin;
    }
    var isInit0 = true;
    $.each(this, function(idx, inp) {
      if (isInit0 && inp.decPl != null) {
        isInit0 = false;
      }
      //add/set input scoped vars:
      inp.decPl = vars.decPl;
      inp.nstep = vars.step;
      inp.nmin = vars.nmin;
      inp.nmax = vars.nmax;
      inp.adjustOnKeyUp = vars.adjustOnKeyUp;
      if (vars.frmOnIni) {
        numMake(inp);
      }
    });
    //events handlers:
    function numIncr(e) {
      var vn = bsStrFlt(e.target.value);
      vn = bsRound(vn + e.target.nstep, e.target.decPl);
      if (vn <= e.target.nmax) {
        e.target.value = bsNumStr(vn.toString(), e.target.decPl);
      } else {
        e.target.value = bsNumStr(e.target.nmax.toString(), e.target.decPl);        
      }  
    };
    function numDecr(e) {
      var vn = bsStrFlt(e.target.value);
      vn = bsRound(vn - e.target.nstep, e.target.decPl);
      if (vn >= e.target.nmin) {
        e.target.value = bsNumStr(vn.toString(), e.target.decPl);      
      } else {
        e.target.value = bsNumStr(e.target.nmin.toString(), e.target.decPl);        
      }
    };
    function numChanged(e) {
      numMake(e.target);
    };
    function numKeydown(e) {
      //Allow: backspace, delete, tab, esc, enter, end, arrow navigation:
      if ($.inArray(e.keyCode, [8, 46, 9, 27, 13, 37, 39, 190]) !== -1
        //Allow Ctrl+V
        || (e.ctrlKey && e.keyCode === 86)
        //Allow Ctrl+A, Command+A
        || (e.keyCode === 65 && (e.ctrlKey || e.metaKey))
        //Allow: home, end, left, right, down, up:
        || (e.keyCode >= 35 && e.keyCode <= 40)) {
        return;
      }
      //Handle minus:
      if (e.key === "-") {
        if (e.target.nmin < 0 && e.target.value.indexOf("-") === -1 && e.target.selectionStart === 0
          || (e.target.value.startsWith("0") && e.target.selectionStart === 1)) {
          return;
        }
        e.preventDefault();
        return;
      }
      //Handle decimal separator:
      if (e.key === '.' || e.key === ',' || e.key === BSNUMVS.decSep) {
        if (e.target.decPl === 0) {
          e.preventDefault();
          return;
        }
        if (e.target.value.indexOf(BSNUMVS.decSep) !== -1) {
          var dotIdx = e.target.value.indexOf(BSNUMVS.decSep) + 1;
          e.target.selectionStart = dotIdx;
          e.target.selectionEnd = dotIdx;
          e.preventDefault();
          return;
        }
      } else if ((e.shiftKey || (e.keyCode < 48 || e.keyCode > 57)) && (e.keyCode < 96 || e.keyCode > 105)) {
        //Prevent non-numbers:
        e.preventDefault();
        return;
      }
      //new digit or DS
    };
    function numKeyup(e) {
      if (e.target.value != "" && e.target.value != "-" && e.target.prevVal != e.target.value) {
        var isNeg = e.target.value.startsWith("-")
        if (e.target.decPl > 0 && e.target.value.indexOf(BSNUMVS.decSep) == -1
          && (e.target.value.length > 2 || e.target.value.length == 2 && !isNeg)) {
          //delete decimal separator handling:
          e.target.value = e.target.prevVal;
          var dotIdx = e.target.value.indexOf(BSNUMVS.decSep);
          e.target.selectionStart = dotIdx;
          e.target.selectionEnd = dotIdx;
        } else if (e.key === "-") {
            if (e.target.value.startsWith("0-")) {
              //minus at start:
              e.target.value = e.target.value.substring(1);
              e.target.selectionStart = 1;
              e.target.selectionEnd = 1;
            } else {
              e.target.prevVal = e.target.value;
            }
        } else {
          var ss = e.target.selectionStart;
          var isFirstDig = false;
          //handling the first digit:
          if (ss == 1 && !isNeg || (ss == 2 && isNeg)) {
            var stp;
            if (isNeg) {
              stp = "-" + e.key + "0";
            } else {
              stp = e.key + "0";
            }
            if (e.target.decPl > 0) {
              stp += BSNUMVS.decSep;
              for (i = 0; i < e.target.decPl; i++) {
                stp += "0";
              }
            }
            isFirstDig = e.target.value == stp;
          }
          if (isFirstDig) {
            if (isNeg) {
              e.target.value = "-" + e.key;
            } else {
              e.target.value = e.key;
            }
            if (e.target.decPl > 0) {
              e.target.value += BSNUMVS.decSep;
              for (i = 0; i < e.target.decPl; i++) {
                e.target.value += "0";
              }
            }
            e.target.selectionStart = ss;
            e.target.selectionEnd = ss;
          } else {
            if (ss == 2 && e.target.value.startsWith("0")) {
              ss = ss - 1;
            }
            var vn = bsStrFlt(e.target.value);
            if (e.target.adjustOnKeyUp) {
              vn = adjustNum(e.target, vn);
            }
            e.target.value = bsNumStr(vn.toString(), e.target.decPl);
            if (e.target.value.length - e.target.prevVal.length == 2) {
              //adding group separator:
              e.target.selectionStart = ss + 1;
              e.target.selectionEnd = ss + 1;
            } else if (ss > 1 && e.target.value.length - e.target.prevVal.length == -2) {
              //deleting group separator except digit before zero:
              e.target.selectionStart = ss - 1;
              e.target.selectionEnd = ss - 1;
            } else if (ss == 0 && e.target.value.startsWith("0")) {
              e.target.selectionStart = 1;
              e.target.selectionEnd = 1;
            } else {
              e.target.selectionStart = ss;
              e.target.selectionEnd = ss;
            }
          }
          e.target.prevVal = e.target.value;
        }
      }
    };
    function numFocus(e) {
      if (e.target.value.startsWith("0")) {
        e.target.selectionStart = 1;
        e.target.selectionEnd = 1;
        e.preventDefault();
      }
    };
    if (isInit0) {
      $(this).on("keydown", numKeydown);
      $(this).on("keyup", numKeyup);
      $(this).on("change", numChanged);
      $(this).on("focus", numFocus);
      $(this).on("increase", numIncr);
      $(this).on("decrease", numDecr);
    }
    //Utils:
    function numMake(pInp) {
      if (pInp.value == "") {
        pInp.value = "0";
      }
      var vn = bsStrFlt(pInp.value);
      vn = adjustNum(pInp, vn);
      pInp.value = bsNumStr(vn.toString(), pInp.decPl);
      //add/set input scoped var:
      pInp.prevVal = pInp.value;
    };
    function adjustNum(pInp, pVn) {
      if (pVn > pInp.nmax) {
        pVn = pInp.nmax;
      } else if (pVn < pInp.nmin) {
        pVn = pInp.nmin;
      } else if (pVn != pInp.nmax && pVn != pInp.nmin) {
        var stepMin = bsRound(1.0 / Math.pow(10, pInp.decPl), pInp.decPl);
        if (pInp.nstep != stepMin) {
          //Java gives right result % on BigDecimal, e.g. 10%0.01==0
          //JS gives wrong result 10%0.01=0.009999999, so do it by hand:
          var vnds = (pVn / pInp.nstep).toString();
          var dotIdx = vnds.indexOf(".");
          if (dotIdx != -1) {
            var daf = parseFloat("0" + vnds.substring(dotIdx));
            var vnr = daf * pInp.nstep;
            pVn = pVn - vnr;
          }
        }
      }
      return pVn;
    };
  };
}(jQuery));
