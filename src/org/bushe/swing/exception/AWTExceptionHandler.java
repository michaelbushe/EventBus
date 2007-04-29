/**
 * Copyright 2005 Bushe Enterprises, Inc., Hopkinton, MA, USA, www.bushe.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bushe.swing.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.awt.Frame;
import javax.swing.JDialog;

/**
 * Plug this class into the AWT Thread to handle Swing Exceptions. To plug in the exception handler, add it as a JVM
 * property like so:
 * <pre>
 * java -Dsun.awt.exception.handler=org.bushe.swing.exception.AWTExceptionHandler
 * </pre>
 * <p/>
 * When this property is set, this class is instantiated and called by the EDT to when exceptions occur in the AWT
 * Thread
 * <p/>
 * This class is hopefully customizable enough for most any application.
 * <p/>
 *
 * @author Michael Bushe michael@bushe.com
 * @see java.awt.EventDispatchThread#handleException(Throwable)
 */
public class AWTExceptionHandler {

   private static String emailAddress;
   private static AWTErrorLogger logger;

   /**
    * An implementation of this interface can be passed to the AWTExcpetionHandler to handle logging on behalf of the
    * AWTExceptionHandler.
    */
   public static interface AWTErrorLogger {
      public void log(Throwable t);
   }

   public AWTExceptionHandler() {
   }

   /**
    * This returns null by default and if it returns null, no Email button is shown in the error dialog.
    *
    * @return the current email address that the Email button on the error dialog will send to.
    */
   public static String getErrorEmailAddress() {
      return emailAddress;
   }

   /**
    * If set to a non-null value, the Email button will be shown in the error dialog that will trigger the user's email
    * client to send an error email to the error email address.
    *
    * @param emailAddy current email address that the Email button on the error dialog will send to.
    */
   public static void setErrorEmailAddress(String emailAddy) {
      emailAddress = emailAddy;
   }

   /**
    * By default the handler logs errors to system.err.  To log elsehwere, set an implementation of AWTErrorLogger,
    * probably wrapping your loggin implementation.
    */
   public static void setLogger(AWTErrorLogger logger) {
      AWTExceptionHandler.logger = logger;
   }

   /**
    * Called by the AWT EventQueue to handle the exception
    *
    * @param t the throwable to handle
    */
   public void handle(Throwable t) {
      Frame f = getFrameForMessageDialog();
      if (f == null) {
         handleThrowableWithoutFrame(t);
      } else {
         handleThrowableWithFrame(f, t);
      }
   }

   /**
    * This method is called when there is no frame.  Typically you will log, but just prints to system.err, or the
    * supplied AWTErrorLogger.
    *
    * @param t
    */
   protected void handleThrowableWithoutFrame(Throwable t) {
      logError(t);
   }

   /**
    * This method is called when there is a frame.  It pops up the ErrorDialog with the message
    *
    * @param t the throwable to handle
    */
   protected void handleThrowableWithFrame(Frame f, Throwable t) {
      logError(t);
      JDialog dialog = createErrorDialog(f, t);
      dialog.pack();
      dialog.setVisible(true);
   }

   /**
    * Logs the error the current implementation of the Logger.
    *
    * @param t the throwable to log.
    */
   protected void logError(Throwable t) {
      if (logger == null) {
         System.err.println(t);
         t.printStackTrace(System.err);
      } else {
         logger.log(t);
      }
   }

   /**
    * Creates an ErrorDialog.  Feel free to override with yout own.
    *
    * @param f the owning frame from getFrameForMessageDialog()
    * @param t the throwable to display
    *
    * @return a dialog to show
    */
   protected JDialog createErrorDialog(Frame f, Throwable t) {
      return new ExceptionDialog(f, t, false);
   }

   /**
    * Finds the frame for hte error message.
    *
    * @return the best frame to use for placement of the error dialog
    */
   protected Frame getFrameForMessageDialog() {
      Frame f = null;
      Frame[] frames = Frame.getFrames();
      //Some possibly better schemes apps can use:
      //- Have each JFrame register with an active frame
      //-Have the exsception implement an interface that provides the associated component
      if (frames != null && frames.length > 0) {
         f = frames[0];
      }
      return f;
   }

   /**
    * Given a throwable, it returns the stack trace as a string
    *
    * @param t the throwable to processes
    *
    * @return the Stack trace as it would look in t.printStackTrace()
    */
   public static String stackTraceToString(Throwable t) {
      if (t == null) {
         return "No stack trace available.";
      }
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      t.printStackTrace(printWriter);
      printWriter.flush();
      return stringWriter.getBuffer().toString();
   }


}


