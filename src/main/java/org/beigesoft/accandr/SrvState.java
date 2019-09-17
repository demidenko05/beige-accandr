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

import org.beigesoft.ajetty.FctAppEmb;
import org.beigesoft.ajetty.BootEmbed;
import org.beigesoft.ajetty.crypto.CryptoService;

import org.beigesoft.log.ILog;

/**
 * <p>Service state holder.</p>
 *
 * @author Yury Demidenko
 */
public class SrvState {

  /**
   * <p>A-Jetty application beans factory.</p>
   **/
  private final FctAppEmb jetFctApp = new FctAppEmb();

  /**
   * <p>Bootstrap.</p>
   **/
  private final BootEmbed bootEmbd = new BootEmbed();

  /**
   * <p>A-Jetty instance number.</p>
   **/
  private Integer ajettyIn;

  /**
   * <p>Crypto service.</p>
   **/
  private CryptoService cryptoService;

  /**
   * <p>Flag is keystore created.</p>
   **/
  private boolean isKeystoreCreated = false;

  /**
   * <p>Shared logger.</p>
   **/
  private ILog log;

  //Simple getters and setters:
  /**
   * <p>Getter for jetFctApp.</p>
   * @return FctAppEmb
   **/
  public final FctAppEmb getJetFctApp() {
    return this.jetFctApp;
  }

  /**
   * <p>Getter for bootEmbd.</p>
   * @return BootEmbed
   **/
  public final BootEmbed getBootEmbd() {
    return this.bootEmbd;
  }

  /**
   * <p>Getter for ajettyIn.</p>
   * @return Integer
   **/
  public final Integer getAjettyIn() {
    return this.ajettyIn;
  }

  /**
   * <p>Setter for ajettyIn.</p>
   * @param pAjettyIn reference
   **/
  public final void setAjettyIn(final Integer pAjettyIn) {
    this.ajettyIn = pAjettyIn;
  }

  /**
   * <p>Getter for cryptoService.</p>
   * @return CryptoService
   **/
  public final CryptoService getCryptoService() {
    return this.cryptoService;
  }

  /**
   * <p>Setter for cryptoService.</p>
   * @param pCryptoService reference
   **/
  public final void setCryptoService(final CryptoService pCryptoService) {
    this.cryptoService = pCryptoService;
  }

  /**
   * <p>Getter for isKeystoreCreated.</p>
   * @return boolean
   **/
  public final boolean getIsKeystoreCreated() {
    return this.isKeystoreCreated;
  }

  /**
   * <p>Setter for isKeystoreCreated.</p>
   * @param pIsKeystoreCreated reference
   **/
  public final void setIsKeystoreCreated(final boolean pIsKeystoreCreated) {
    this.isKeystoreCreated = pIsKeystoreCreated;
  }

  /**
   * <p>Getter for log.</p>
   * @return ILog
   **/
  public final ILog getLog() {
    return this.log;
  }

  /**
   * <p>Setter for log.</p>
   * @param pLog reference
   **/
  public final void setLog(final ILog pLog) {
    this.log = pLog;
  }
}
