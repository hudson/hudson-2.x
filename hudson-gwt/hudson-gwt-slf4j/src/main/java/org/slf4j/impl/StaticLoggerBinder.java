/*
 * Copyright (c) 2004-2005 SLF4J.ORG
 * Copyright (c) 2004-2005 QOS.ch
 *
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute, and/or sell copies of  the Software, and to permit persons
 * to whom  the Software is furnished  to do so, provided  that the above
 * copyright notice(s) and this permission notice appear in all copies of
 * the  Software and  that both  the above  copyright notice(s)  and this
 * permission notice appear in supporting documentation.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR  A PARTICULAR PURPOSE AND NONINFRINGEMENT
 * OF  THIRD PARTY  RIGHTS. IN  NO EVENT  SHALL THE  COPYRIGHT  HOLDER OR
 * HOLDERS  INCLUDED IN  THIS  NOTICE BE  LIABLE  FOR ANY  CLAIM, OR  ANY
 * SPECIAL INDIRECT  OR CONSEQUENTIAL DAMAGES, OR  ANY DAMAGES WHATSOEVER
 * RESULTING FROM LOSS  OF USE, DATA OR PROFITS, WHETHER  IN AN ACTION OF
 * CONTRACT, NEGLIGENCE  OR OTHER TORTIOUS  ACTION, ARISING OUT OF  OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 * Except as  contained in  this notice, the  name of a  copyright holder
 * shall not be used in advertising or otherwise to promote the sale, use
 * or other dealings in this Software without prior written authorization
 * of the copyright holder.
 *
 */

package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * The binding of {@link LoggerFactory} class with an actual instance of 
 * {@link ILoggerFactory} is performed using information returned by this class. 
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

  /**
   * The unique instance of this class.
   * 
   */
  private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
  
  /**
   * Return the singleton of this class.
   * 
   * @return the StaticLoggerBinder singleton
   */
  public static final StaticLoggerBinder getSingleton() {
    return SINGLETON;
  }

  
  /**
   * Declare the version of the SLF4J API this implementation is compiled against. 
   * The value of this field is usually modified with each release. 
   */
  // to avoid constant folding by the compiler, this field must *not* be final
  public static String REQUESTED_API_VERSION = "1.6";  // !final

  
  private static final String loggerFactoryClassStr = org.slf4j.impl.JDK14LoggerFactory.class.getName();

  /** The ILoggerFactory instance returned by the {@link #getLoggerFactory} method
   * should always be the same object
   */
  private final ILoggerFactory loggerFactory;
  
  private StaticLoggerBinder() {
//  Note: JCL gets substituted at build time by an appropriate Ant task
    loggerFactory = new org.slf4j.impl.JDK14LoggerFactory();
  }
  
  public ILoggerFactory getLoggerFactory() {
    return loggerFactory;
  }
  
  public String getLoggerFactoryClassStr() {
    return loggerFactoryClassStr;
  }   
}
