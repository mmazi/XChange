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
package com.xeiam.xchange.service;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xeiam.xchange.ExchangeException;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.streaming.socketio.SocketIO;
import com.xeiam.xchange.utils.Assert;

/**
 * <p>
 * Socket IO exchange service to provide the following to streaming market data API:
 * </p>
 * <ul>
 * <li>Connection to an upstream exchange data source with a configured provider</li>
 * </ul>
 */
public abstract class BaseSocketIOExchangeService extends BaseExchangeService implements StreamingExchangeService {

  private final Logger log = LoggerFactory.getLogger(BaseSocketIOExchangeService.class);

  private final ExecutorService executorService;
  private final BlockingQueue<ExchangeEvent> exchangeEvents = new ArrayBlockingQueue<ExchangeEvent>(1024);

  private SocketIO socketIO;
  private RunnableExchangeEventProducer runnableMarketDataEventProducer = null;

  /**
   * Constructor
   * 
   * @param exchangeSpecification The exchange specification providing the required connection data
   */
  public BaseSocketIOExchangeService(ExchangeSpecification exchangeSpecification) throws IOException {

    super(exchangeSpecification);

    // Assert.notNull(exchangeSpecification.getHost(), "host cannot be null");

    executorService = Executors.newSingleThreadExecutor();
  }

  @Override
  public synchronized void connect(String url, RunnableExchangeEventListener runnableExchangeEventListener) {

    // Validate inputs
    Assert.notNull(runnableExchangeEventListener, "runnableMarketDataListener cannot be null");

    // Validate state
    if (executorService.isShutdown()) {
      throw new IllegalStateException("Service has been stopped. Create a new one rather than reuse a reference.");
    }

    try {
      log.debug("Attempting to open a socketIO against {}:{}", url, exchangeSpecification.getPort());
      this.runnableMarketDataEventProducer = new RunnableSocketIOEventProducer(socketIO, exchangeEvents);
      this.socketIO = new SocketIO(url, (RunnableSocketIOEventProducer) runnableMarketDataEventProducer);
    } catch (IOException e) {
      throw new ExchangeException("Failed to open socket: " + e.getMessage(), e);
    }

    runnableExchangeEventListener.setExchangeEventQueue(exchangeEvents);
    executorService.submit(runnableMarketDataEventProducer);

    log.debug("Started OK");

  }

  @Override
  public void send(String message) {

    this.socketIO.send(message);
  }

  @Override
  public synchronized void disconnect() {

    if (!executorService.isShutdown()) {
      // We close on the socket to get an immediate result
      // otherwise the producer would block until the exchange
      // sent a message which could be forever
      if (socketIO != null) {
        socketIO.disconnect();
      }
    }
    executorService.shutdownNow();
    log.debug("Stopped");
  }

  @Override
  public RunnableExchangeEventProducer getRunnableMarketDataEventProducer() {

    return runnableMarketDataEventProducer;
  }

  @Override
  public void setRunnableMarketDataEventProducer(RunnableExchangeEventProducer runnableMarketDataEventProducer) {

    this.runnableMarketDataEventProducer = runnableMarketDataEventProducer;
  }

}
