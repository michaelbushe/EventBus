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

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.awt.Component;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import javax.swing.JSeparator;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.KeyStroke;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;

import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.Message;

/**
 * A dialog that displays exception that occur in the AWT Event Queue.
 * <p>
 * It is fully customizable.
 * @author Michael Bushe michael@bushe.com
  */
public class ExceptionDialog extends JDialog {
   public static final int PREFERRED_WIDTH = 600;
   private Throwable throwable;
   private Component detailsComponent;
   private JSeparator separator;
   private String emailAddress;

   public ExceptionDialog(Frame ownerFrame, Throwable t, boolean modal) {
      this(ownerFrame, t, modal, null);
   }

   public ExceptionDialog(Frame ownerFrame, Throwable t, boolean modal, String emailAddress) {
      super(ownerFrame, "Application Error", modal);
      this.throwable = t;
      this.emailAddress = emailAddress;
      setupClose();
      initUI(t);
   }

   /**
    * Called during construction to initialze the components and calls doLayout() with them.
    * <p>
    * You can overridde this method to create your own components and layout.
    * @param t the throwable to show
    */
   protected void initUI(Throwable t) {
      detailsComponent = createDetailsComponent(t);
      JLabel errorIconLabel = createErrorIconLabel();
      JLabel messageLabel = createErrorMessageComponent();
      Component buttonPanel = createButtonPanelComponent();
      JLabel titleLabel = createTitleComponent();
      separator = createSeparator();
      doLayout(errorIconLabel, titleLabel, messageLabel, buttonPanel, detailsComponent, separator);
   }

   /**
    * Layouts the component on the context pane.
    * @param errorIconLabel the icon gotten from createErrorIconLabel()
    * @param messageLabel the
    * @param buttonPanel
    * @param detailsPanel
    */
   protected void doLayout(JLabel errorIconLabel, JLabel titleLabel, JLabel messageLabel, Component buttonPanel,
           Component detailsPanel, Component separator) {
      JPanel content = new JPanel(new BorderLayout());
      JPanel centerContentPanel = new JPanel(new GridBagLayout());
      content.add(centerContentPanel, BorderLayout.CENTER);
      centerContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
      setContentPane(content);
      GridBagConstraints gbc = new GridBagConstraints();
      Insets insets0t10l5b5r = new Insets(0, 10, 5, 5);
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.gridheight = 3;
      gbc.anchor = GridBagConstraints.NORTH;
      centerContentPanel.add(errorIconLabel, gbc);
      gbc.gridx = 1;
      gbc.gridy = 0;
      gbc.gridheight = 1;
      gbc.insets = insets0t10l5b5r;
      gbc.anchor = GridBagConstraints.WEST;
      centerContentPanel.add(titleLabel, gbc);
      gbc.gridx = 1;
      gbc.gridy = 1;
      gbc.insets = new Insets(0, 20, 5, 5);
      centerContentPanel.add(messageLabel, gbc);
      gbc.gridx = 0;
      gbc.gridy = 2;
      gbc.gridwidth = 2;
      gbc.insets = new Insets(0, 0, 5, 0);
      centerContentPanel.add(buttonPanel, gbc);
      gbc.gridx = 0;
      gbc.gridy = 3;
      gbc.gridwidth = 2;
      gbc.insets = new Insets(0, 5, 5, 0);
      centerContentPanel.add(separator, gbc);
      gbc.gridx = 0;
      gbc.gridy = 4;
      gbc.gridwidth = 2;
      gbc.insets = new Insets(0, 5, 5, 0);
      gbc.weightx = 1.0;
      gbc.weighty = 1.0;
      gbc.fill = GridBagConstraints.BOTH;
      centerContentPanel.add(detailsPanel, gbc);
   }

   /**
    * @return a JSeparator that gets swapped out with the detail pane.
    */
   protected JSeparator createSeparator() {
      JSeparator separator = new JSeparator();
      separator.setPreferredSize(new Dimension(PREFERRED_WIDTH, 3));
      if (getDefaultDetailsVisible()) {
         separator.setVisible(false);
      }
      return separator;
   }

   /**
    * Ensures the dialog disposes on close, escape, or X.
    */
   protected void setupClose() {
      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
      Action actionListener = new AbstractAction() {
         public void actionPerformed(ActionEvent actionEvent) {
           setVisible(false);
        }
      };
      InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
      inputMap.put(stroke, "ESCAPE");
      getRootPane().getActionMap().put("ESCAPE", actionListener);
   }

   /**
    * The component that shows the component details message.  Calles getComponentDetailMessage(throwable) for the text.
    * @param t the throwable being displayed
    * @return a scroll pane with a text area by default.
    */
   protected Component createDetailsComponent(Throwable t) {
      JTextArea throwableTextArea = new JTextArea(getDetailComponentMessageText(), 25, 80);
      throwableTextArea.setEditable(false);
      JScrollPane scrollPane = new JScrollPane(throwableTextArea);
      if (!getDefaultDetailsVisible()) {
         scrollPane.setVisible(false);
      }
      scrollPane.setPreferredSize(new Dimension(PREFERRED_WIDTH, scrollPane.getPreferredSize().height));
      return scrollPane;
   }

   /**
    * @return the string in the details message pane, returns determineDetailMessage(throwable) by default
    */
   protected String getDetailComponentMessageText() {
      return determineDetailMessage(throwable);
   }

   /**
    * @return the string in mail message, returns determineDetailMessage(throwable) by default
    */
   protected String getEmailMessageText() {
      return determineDetailMessage(throwable);
   }

   /**
    * Get the panel of control buttons.
    * @return by default a panel with OK, Copy, and, if emailAddress is not null, Email
    */
   protected JComponent createButtonPanelComponent() {
      JComponent buttonPanel = new JPanel(new GridBagLayout());
      JPanel leftButtonPanel = new JPanel();
      //Why am I not using BasicAction?  Because this class should only depend on java or org.bushe.swing
      AbstractAction action = new AbstractAction("OK") {
         public void actionPerformed(ActionEvent e) {
            close();
         }
      };
      JButton okButton = new JButton(action);

      leftButtonPanel.add(okButton);
      JButton copyButton = new JButton("Copy");
      copyButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            copy();
         }
      });
      leftButtonPanel.add(copyButton);
      if (AWTExceptionHandler.getErrorEmailAddress() != null) {
         JButton emailButton = new JButton("Email");
         emailButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               email();
            }
         });
         leftButtonPanel.add(emailButton);
      }

      if (detailsComponent != null) {
         boolean defaultDetailsVisible = getDefaultDetailsVisible();
         final JButton detailsButton = new JButton(defaultDetailsVisible?"Details >>":"<< Details");
         detailsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               if ("<< Details".equals(detailsButton.getText())) {
                  detailsComponent.setVisible(true);
                  separator.setVisible(false);
                  detailsButton.setText("Details >>");
               } else {
                  detailsComponent.setVisible(false);
                  separator.setVisible(true);
                  detailsButton.setText("<< Details");
               }
               pack();
            }
         });
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.anchor = GridBagConstraints.WEST;
         buttonPanel.add(leftButtonPanel, gbc);
         gbc.gridx = 1;
         gbc.anchor = GridBagConstraints.EAST;
         gbc.fill = GridBagConstraints.BOTH;
         gbc.weightx = 1.0;
         JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         rightPanel.add(detailsButton);
         buttonPanel.add(rightPanel, gbc);
         buttonPanel.setBackground(Color.BLACK);
      }

      getRootPane().setDefaultButton(okButton);
      return buttonPanel;
   }

   /**
    * Should the details component be visible by default?
    * @return flase by default
    */
   protected boolean getDefaultDetailsVisible() {
      return false;
   }

   /**
    * @return a JLabel with the getErrorIcon() for an icon.
    */
   protected JLabel createErrorIconLabel() {
      return new JLabel(getErrorIcon());
   }

   /**
    * @return a JLabel with "The following error occurred:"
    */
   protected JLabel createTitleComponent() {
      return new JLabel("The following error occurred:");
   }

   /**
    *@return a JLabel with the text of getMessageText(throwable)
    */
   protected JLabel createErrorMessageComponent() {
      return new JLabel(getMessageText(throwable));
   }

   /**
    * @return the TSOpenPane's error icon from the look and feel (UIManager.getIcon("OptionPane.errorIcon"))
    */
   protected Icon getErrorIcon() {
      return UIManager.getIcon("OptionPane.errorIcon");
   }

   /**
    * Called to copy the text in the details component (actually getDetailComponentMessageText()) to the system
    * clipboard.
    */
   protected void copy() {
      StringSelection ss = new StringSelection(getDetailComponentMessageText());
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
   }

   /**
    * Called when the user clicks the email button.
    */
   protected void email() {
      if (emailAddress != null) {
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                  try {
                     Message emailMessage = new Message();
                     List to = new ArrayList();
                     to.add(emailAddress);
                     emailMessage.setToAddrs(to);
                     emailMessage.setSubject(getErrorEmailSubject());
                     emailMessage.setBody(getEmailMessageText());
                     Desktop.mail(emailMessage);
                  } catch (Throwable ex) {
                     //May catch DesktopException or ClassNotFoundException, etc.
                     throw new RuntimeException("Could not email previous error.  Likely a Java Desktop Integration configuraiton issue.  "+ex, ex);
                  }
               }
            });
      }
   }

   /**
    * Computes the detail shown in the details component.
    * @param t the throwable we are throwing an exception for
    * @return by default a timestamp, the root Cause and stack, and full stack
    */
   protected String determineDetailMessage(Throwable t) {
      String msg = ""+new Date()+"\n";
      Throwable root = getRootCause(t);
      if (root == t) {
         msg = msg + t.getMessage()+"\n";
         msg = msg + AWTExceptionHandler.stackTraceToString(t);
      } else {
         msg = msg + "Root Cause:"+ root.getMessage()+"\n";
         msg = msg + AWTExceptionHandler.stackTraceToString(root);
         msg = msg + "Full Trace:"+ t.getMessage()+"\n";
         msg = msg + AWTExceptionHandler.stackTraceToString(t);
      }
      String jvmProps =  "\n" + "JVM properties:";
      Properties props = System.getProperties();
      Iterator keyIt = props.keySet().iterator();
      while (keyIt.hasNext()) {
         Object o =  keyIt.next();
         jvmProps = jvmProps + "\n" + o +"="+props.get(o);
      }
      return msg + jvmProps;
   }

   /**
    * Gets the message from the root cause, breaks it up into an 80 character
    * wide html message.
    * @param t  the throwable.
    * @return a nice message for a throwable, ready to be JLabel'ed.
    */
   protected String getMessageText(Throwable t) {
      if (t == null) {
         return "No throwable available.";
      }
      t = getRootCause(t);
      String msg = "<html>";
      String fullMessage = t.getMessage();
      if (fullMessage == null) {
         msg = msg + "No message available";
      } else if (fullMessage.length() < 80) {
         msg = msg + fullMessage;
      } else {
         //Break the message up into 80 character wide bits
         String line = "";
         StringTokenizer tok = new StringTokenizer(fullMessage, " \throwable\n\r\f", true);
         while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            if (line.length() + token.length() > 80) {
               msg = msg + line+"<br>";
               line = "";
            } else {
               line = line + token;
            }
         }
         msg = msg + line;
      }
      return msg + "</html>";
   }

   /**
    * @return return the subject of the error email.  "Application Error" by default.
    */
   protected String getErrorEmailSubject() {
      return "Application Error";
   }

   /**
    * Called on window close to dispose.
    */
   protected void close() {
      dispose();
   }

   //Isn'throwable this in some later JDK API?  I know I saw it somewhere...
   private Throwable getRootCause(Throwable t) {
      while(t.getCause() != null) {
         t = t.getCause();
      }
      return t;
   }

   public void setEmailAddress(String emailAddress) {
      this.emailAddress = emailAddress;
   }
}
