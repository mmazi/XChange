/**
 * Copyright (C) 2012 Xeiam LLC http://xeiam.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.xeiam.xchange.dto.marketdata;

import java.math.BigDecimal;

import org.joda.money.BigMoney;
import org.joda.time.DateTime;

import com.xeiam.xchange.utils.DateUtils;

/**
 * A class encapsulating the most basic information a "Ticker" should contain. A ticker contains data representing the latest trade. This class is immutable.
 * 
 * @immutable
 */

public final class Ticker {

  private final String tradableIdentifier;
  private final BigMoney last;
  private final BigMoney bid;
  private final BigMoney ask;
  private final BigMoney high;
  private final BigMoney low;
  private final BigDecimal volume;
  private final DateTime timestamp;

  /**
   * Constructor
   * 
   * @param tradableIdentifier
   * @param last
   * @param bid
   * @param ask
   * @param high
   * @param low
   * @param volume
   */
  public Ticker(String tradableIdentifier, BigMoney last, BigMoney bid, BigMoney ask, BigMoney high, BigMoney low, BigDecimal volume) {

    this.tradableIdentifier = tradableIdentifier;
    this.last = last;
    this.bid = bid;
    this.ask = ask;
    this.high = high;
    this.low = low;
    this.volume = volume;
    this.timestamp = DateUtils.nowUtc();
  }

  public String getTradableIdentifier() {

    return tradableIdentifier;
  }

  public BigMoney getLast() {

    return last;
  }

  public BigMoney getBid() {

    return bid;
  }

  public BigMoney getAsk() {

    return ask;
  }

  public BigMoney getHigh() {

    return high;
  }

  public BigMoney getLow() {

    return low;
  }

  public BigDecimal getVolume() {

    return volume;
  }

  public DateTime getTimestamp() {

    return timestamp;
  }

  @Override
  public String toString() {

    return "Ticker [tradableIdentifier=" + tradableIdentifier + ", last=" + last + ", bid=" + bid + ", ask=" + ask + ", high=" + high + ", low=" + low + ", volume=" + volume + ", timestamp=" + timestamp + "]";
  }

}
