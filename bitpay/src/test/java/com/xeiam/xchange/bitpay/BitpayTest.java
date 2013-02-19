/*
 * Copyright (C) 2013 Matija Mazi
 * Copyright (C) 2013 Xeiam LLC http://xeiam.com
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
package com.xeiam.xchange.bitpay;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.xeiam.xchange.bitpay.dto.Invoice;
import com.xeiam.xchange.rest.BasicAuthCredentials;
import com.xeiam.xchange.rest.RestProxyFactory;

/**
 * @author Matija Mazi <br/>
 */
public class BitpayTest {

  private static final Logger log = LoggerFactory.getLogger(BitpayTest.class);


  public static final String API_KEY = ""; // TODO

  @Test
  public void testBitpay() throws Exception {

    Bitpay bitpay = RestProxyFactory.createProxy(Bitpay.class, "https://bitpay.com");
    BasicAuthCredentials credentials = new BasicAuthCredentials(API_KEY, "");

    Invoice invoice = bitpay.createInvoice(credentials, new BigDecimal("12.3"), "EUR");

    log.debug("invoice = {}", invoice);
  }
}
