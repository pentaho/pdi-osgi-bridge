/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.di.plugins.examples.helloworld;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by nbaker on 11/12/14.
 */
public class HelloWorldServlet extends HttpServlet {
  private String message;
  private MessageFormatter messageFormatter = new DefaultMessageFormatter();

  public MessageFormatter getMessageFormatter() {
    return messageFormatter;
  }

  public void setMessageFormatter( MessageFormatter messageFormatter ) {
    this.messageFormatter = messageFormatter;
  }

  public String getMessage() {
    return messageFormatter.format( message );
  }

  public void setMessage( String message ) {
    this.message = message;
  }

  @Override
  protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
    resp.getWriter().write( getMessage() );
  }
}
