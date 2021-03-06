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
package com.xeiam.xchange.mtgox.v1.service.marketdata.polling;

import java.util.HashMap;
import java.util.List;

import com.xeiam.xchange.CachedDataSession;
import com.xeiam.xchange.CurrencyPair;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.PacingViolationException;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.marketdata.Trades;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.mtgox.v1.MtGoxAdapters;
import com.xeiam.xchange.mtgox.v1.MtGoxUtils;
import com.xeiam.xchange.mtgox.v1.dto.marketdata.MtGoxDepth;
import com.xeiam.xchange.mtgox.v1.dto.marketdata.MtGoxTicker;
import com.xeiam.xchange.mtgox.v1.dto.marketdata.MtGoxTrade;
import com.xeiam.xchange.service.BasePollingExchangeService;
import com.xeiam.xchange.service.marketdata.polling.PollingMarketDataService;
import com.xeiam.xchange.utils.Assert;

/**
 * <p>
 * Implementation of the market data service for Mt Gox
 * </p>
 * <ul>
 * <li>Provides access to various market data values</li>
 * </ul>
 */
public class MtGoxPollingMarketDataService extends BasePollingExchangeService implements PollingMarketDataService, CachedDataSession {

  /**
   * time stamps used to pace API calls
   */
  private long tickerRequestTimeStamp = 0L;
  private long orderBookRequestTimeStamp = 0L;
  private long fullOrderBookRequestTimeStamp = 0L;
  private long tradesRequestTimeStamp = 0L;

  /**
   * Configured from the super class reading of the exchange specification
   */
  private final String apiBase = String.format("%s/api/%s/", exchangeSpecification.getUri(), exchangeSpecification.getVersion());

  /**
   * @param exchangeSpecification The exchange specification
   */
  public MtGoxPollingMarketDataService(ExchangeSpecification exchangeSpecification) {

    super(exchangeSpecification);
  }

  @Override
  public Ticker getTicker(String tradableIdentifier, String currency) {

    verify(tradableIdentifier, currency);

    // check for pacing violation
    if (System.currentTimeMillis() < tickerRequestTimeStamp + getRefreshRate()) {
      tickerRequestTimeStamp = System.currentTimeMillis();
      throw new PacingViolationException("MtGox caches market data and refreshes every " + getRefreshRate() + " seconds.");
    }
    tickerRequestTimeStamp = System.currentTimeMillis();

    // Request data
    MtGoxTicker mtGoxTicker = httpTemplate.getForJsonObject(apiBase + tradableIdentifier + currency + "/public/ticker?raw", MtGoxTicker.class, mapper, new HashMap<String, String>());

    // Adapt to XChange DTOs
    return MtGoxAdapters.adaptTicker(mtGoxTicker);
  }

  @Override
  public OrderBook getPartialOrderBook(String tradableIdentifier, String currency) {

    verify(tradableIdentifier, currency);

    // Check for pacing violation
    if (System.currentTimeMillis() < orderBookRequestTimeStamp + getRefreshRate()) {
      orderBookRequestTimeStamp = System.currentTimeMillis();
      throw new PacingViolationException("MtGox caches market data and refreshes every " + getRefreshRate() + " seconds.");
    }
    orderBookRequestTimeStamp = System.currentTimeMillis();

    // Request data
    MtGoxDepth mtgoxDepth = httpTemplate.getForJsonObject(apiBase + tradableIdentifier + currency + "/public/depth?raw", MtGoxDepth.class, mapper, new HashMap<String, String>());

    // Adapt to XChange DTOs
    List<LimitOrder> asks = MtGoxAdapters.adaptOrders(mtgoxDepth.getAsks(), currency, "ask", "");
    List<LimitOrder> bids = MtGoxAdapters.adaptOrders(mtgoxDepth.getBids(), currency, "bid", "");

    return new OrderBook(asks, bids);
  }

  @Override
  public OrderBook getFullOrderBook(String tradableIdentifier, String currency) {

    verify(tradableIdentifier, currency);

    // check for pacing violation
    if (System.currentTimeMillis() < fullOrderBookRequestTimeStamp + getRefreshRate()) {
      fullOrderBookRequestTimeStamp = System.currentTimeMillis();
      throw new PacingViolationException("MtGox caches market data and refreshes every " + getRefreshRate() + " seconds.");
    }
    fullOrderBookRequestTimeStamp = System.currentTimeMillis();

    // Request data
    MtGoxDepth mtgoxFullDepth = httpTemplate.getForJsonObject(apiBase + tradableIdentifier + currency + "/public/fulldepth?raw", MtGoxDepth.class, mapper, new HashMap<String, String>());

    // Adapt to XChange DTOs
    List<LimitOrder> asks = MtGoxAdapters.adaptOrders(mtgoxFullDepth.getAsks(), currency, "ask", "");
    List<LimitOrder> bids = MtGoxAdapters.adaptOrders(mtgoxFullDepth.getBids(), currency, "bid", "");

    return new OrderBook(asks, bids);
  }

  @Override
  public Trades getTrades(String tradableIdentifier, String currency) {

    verify(tradableIdentifier, currency);
    // Check for pacing violation
    if (System.currentTimeMillis() < tradesRequestTimeStamp + getRefreshRate()) {
      tradesRequestTimeStamp = System.currentTimeMillis();
      throw new PacingViolationException("MtGox caches market data and refreshes every " + getRefreshRate() + " seconds.");
    }
    tradesRequestTimeStamp = System.currentTimeMillis();

    // Request data
    MtGoxTrade[] mtGoxTrades = httpTemplate.getForJsonObject(apiBase + tradableIdentifier + currency + "/public/trades?raw", MtGoxTrade[].class, mapper, new HashMap<String, String>());

    return MtGoxAdapters.adaptTrades(mtGoxTrades);
  }

  /**
   * Verify
   * 
   * @param tradableIdentifier
   * @param currency
   */
  private void verify(String tradableIdentifier, String currency) {

    Assert.notNull(tradableIdentifier, "tradableIdentifier cannot be null");
    Assert.notNull(currency, "currency cannot be null");
    Assert.isTrue(MtGoxUtils.isValidCurrencyPair(new CurrencyPair(tradableIdentifier, currency)), "currencyPair is not valid:" + tradableIdentifier + " " + currency);

  }

  @Override
  public int getRefreshRate() {

    return MtGoxUtils.REFRESH_RATE;
  }

  @Override
  public List<CurrencyPair> getExchangeSymbols() {

    return MtGoxUtils.CURRENCY_PAIRS;
  }
}
