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
 * Numeric JS library.
 * author Yury Demidenko
 */

//Request scoped variables that are applied to whole page:
var BSNUMVS = {
  decSep: ".",
  decGrSep: ",",
  digInGr: 3
};

function bsSetNumVs(pDecSep, pDecGrSep, pDigInGr) {
  BSNUMVS.decSep = pDecSep;
  BSNUMVS.decGrSep = pDecGrSep;
  BSNUMVS.digInGr=pDigInGr;
};

/*
 * <p>Prints number formatted by given decimal separator, decimal group
 * separator, decimal places after dot and digits in group. It never do rounding,
 * it just rips exceeded digits or to trail with zeros.</p>
 * @param pNumber string e.g. "12146678.12"
 * @param pDecPl decimal places after dot, e.g. 2
 * @return internationalized number, e.g. "12 146 678,12"
 */
function bsNumStr(pNumber, pDecPl) {
  if (pNumber == null || "" === pNumber) {
    return "";
  }
  var dotIdx = pNumber.indexOf(".");
  var leftWing;
  var rightWing;
  if (dotIdx === -1) {
    leftWing = pNumber;
    rightWing = null;
  } else {
    leftWing = pNumber.substring(0, dotIdx);
    rightWing = pNumber.substring(dotIdx + 1);
  }
  var sb = "";
  if (leftWing.charAt(0) === "-") {
    leftWing = leftWing.substring(1);
    sb = "-";
  }
  for (i = 0; i < leftWing.length; i++) {
    var ch = leftWing.charAt(i);
    sb += ch;
    var idxFl = leftWing.length - i;
    if (BSNUMVS.digInGr === 2) {
      //hard-coded Indian style 12,12,14,334
      if (idxFl === 4) {
        sb += BSNUMVS.decGrSep;
      } else {
        idxFl -= 3;
      }
    }
    if (idxFl >= BSNUMVS.digInGr) {
      var gc = bsRound(idxFl / BSNUMVS.digInGr, 0, 1);
      if (gc > 0.0) {
        var rem;
        if (gc == 1) {
          rem = idxFl % BSNUMVS.digInGr;
        } else {
          rem = idxFl % (BSNUMVS.digInGr * gc);
        }
        if (bsRound(rem, 0, 1) === 1.0) {
          sb += BSNUMVS.decGrSep;
        }
      }
    }
  }
  if (pDecPl > 0) {
    sb += BSNUMVS.decSep;
    for (i = 0; i < pDecPl; i++) {
      if (rightWing != null && rightWing.length > i) {
        sb += rightWing.charAt(i);
      } else {
        sb += "0";
      }
    }
  }
  return sb;
};

/*
 * <p>Converts formatted number string to float number.</p>
 * @param pNumber string e.g. "1 2146 678,12"
 * @return number, e.g. 12146678.12
 */
function bsStrFlt(pNumber) {
  if (BSNUMVS.decGrSep !== "") {
    pNumber = pNumber.replace(new RegExp(BSNUMVS.decGrSep, "g"), "");
  }
  if (BSNUMVS.decSep !== ".") {
    pNumber = pNumber.replace(BSNUMVS.decSep, ".");
  }
  return parseFloat(pNumber);
};

/*
 * <p>Rounds with modes:
 * UP = 0
 * DOWN = 1
 * CEILING = 2
 * FLOOR = 3
 * HALF_UP = 4
 * HALF_DOWN = 5
 * HALF_EVEN = 6
</p> 
 * @param pNumber float
 * @param pDecPl decimal places integer
 * @param pMode integer Java like NULL means HALF_UP
 * @return rounded
 */
function bsRound(pNumber, pDecPl, pMode) {
  var zi = 0;
  var zf = 0.0;
  if (pNumber == zf) {
    return pNumber;
  }
  var nums = pNumber.toString();
  var dotIdx = nums.indexOf(".");
  if (dotIdx == -1) {
    return pNumber;
  }
  if (nums.length == dotIdx + pDecPl + 1) {
    return pNumber;
  }
  var rez = parseFloat(nums.substring(0, dotIdx + pDecPl + 1));
  rst = parseFloat(nums.substring(dotIdx + pDecPl + 1, dotIdx + pDecPl + 2) + "." + nums.substring(dotIdx + pDecPl + 2));
  if (rst == zf) {
    return pNumber;
  }
  var md = pMode;
  if (md == null) {
    md = 4;
  }
  var ad = 1.0 / Math.pow(10.0, pDecPl);
  //JS 1.12 + 0.01 may gives 1.1299999, so add add-fraction:
  ad = ad + (ad * 0.3);
  var ff = 5.0
  if (md === 1) {
    return rez;    
  } else if (md === zi || (md === 5 && rst > ff) || (md === 4 && rst >= ff)) {
    if (pNumber > zf) {
      rez = rez + ad;
    } else {
      rez = rez - ad;
    }
  } else if (md === 2 && pNumber > zf) {
    rez = rez + ad;
  } else if (md === 3 && pNumber < zf) {
    rez = rez - ad;
  } else if (md === 6) {
    var rezs = rez.toString();
    var lstdi = parseInt(rezs.charAt(rezs.length - 1));
    if (lstdi % 2 != zi && rst >= ff) {
      if (pNumber > zf) {
        rez = rez + ad;
      } else {
        rez = rez - ad;
      }
    }
  }
  //removing add-fraction:
  var rezgs = rez.toFixed(pDecPl);
  rez = parseFloat(rezgs);
  return rez;
};
