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

function bsGtAjx(method,url) {
  var xmlhttp = new XMLHttpRequest();
  xmlhttp.onreadystatechange = function(){
    bsTakReq(xmlhttp);
  };
  xmlhttp.open(method, encodeURI(url), true);
  xmlhttp.send();
};

function bsSndFrmAjx(pFrm, pAddParams) {
  var xmlhttp = new XMLHttpRequest();
  var frmData = new FormData(pFrm);
  if (pAddParams != null) {
    pAddParams.split("&").forEach(function(paramValue) {
      var arrParamValue = paramValue.split("=");
      frmData.append(arrParamValue[0], arrParamValue[1]);
    });
  }
  xmlhttp.open(pFrm.method, pFrm.action, true);
  xmlhttp.onload = function(evnt) {
    bsTakReq(xmlhttp);
  };
  xmlhttp.send(frmData);
};

function bsTakReq(xmlhttp) {
  if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
    if (xmlhttp.responseText.indexOf('j_security_check') > 0) {
      location.reload();
    }
    var complRes = JSON.parse(xmlhttp.responseText);
    var arr=complRes.mlTrRsp;
    for (var i = 0; i < arr.length; i++) {
      if (arr[i].trgNm!=null) {
        var target = document.getElementById(arr[i].trgNm);
        if (target == null) {
          target = document.createElement('div');
          target.setAttribute("id", arr[i].trgNm);
          document.getElementById(arr[i].trgPrNm).appendChild(target);
        }
        target.innerHTML = arr[i].cont;
      }
      if (arr[i].jscr!=null) {
        eval(arr[i].jscr);
      }
    }
  }
  else if (xmlhttp.readyState == 4) {
    if (xmlhttp.responseText == "") {
      document.getElementById("errPl").innerHTML = "<h4>http status = " + xmlhttp.status
        +"!!! </h4>";
    } else {
      document.getElementById("errPl").innerHTML = xmlhttp.responseText;
    }
    document.getElementById("dlgErr").showModal();
  }
};
