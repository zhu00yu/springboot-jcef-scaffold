package com.zhu00yu.springbootjcefscaffold.browser;

import com.formdev.flatlaf.FlatLightLaf;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.cef.CefApp;
import org.cef.CefApp.CefAppState;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefFocusHandlerAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

// Copyright (c) 2014 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

/**
 * This is a simple example application using JCEF.
 * It displays a JFrame with a JTextField at its top and a CefBrowser in its
 * center. The JTextField is used to enter and assign an URL to the browser UI.
 * No additional handlers or callbacks are used in this example.
 *
 * The number of used JCEF classes is reduced (nearly) to its minimum and should
 * assist you to get familiar with JCEF.
 *
 * For a more feature complete example have also a look onto the example code
 * within the package "tests.detailed".
 */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = -5570653778104813836L;
    private CefApp cefApp_;
    private CefClient client_;
    private CefBrowser browser_;
    private Component browerUI_;
    private boolean browserFocus_ = true;

    static {
        UIManager.put( "TitlePane.menuBarEmbedded", false );
        UIManager.put( "TitlePane.useWindowDecorations", false );
        FlatLightLaf.setup();
    }


    /**
     * To display a simple browser window, it suffices completely to create an
     * instance of the class CefBrowser and to assign its UI component to your
     * application (e.g. to your content pane).
     * But to be more verbose, this CTOR keeps an instance of each object on the
     * way to the browser UI.
     */
    public MainFrame(String startURL, boolean useOSR, boolean isTransparent, String[] args) throws UnsupportedPlatformException, CefInitializationException, IOException, InterruptedException {

        initCef(startURL, useOSR, isTransparent, args);
        JMenuBar mb = createMenuBar();

        this.setJMenuBar(mb);
        getContentPane().add(browerUI_, BorderLayout.CENTER);
        pack();
        setSize(800, 600);

        initWindowEventHandler();
        openFrame();
    }

    private void initCef(String startURL, boolean useOSR, boolean isTransparent, String[] args) throws InterruptedException, UnsupportedPlatformException, CefInitializationException, IOException {
        // (0) Initialize CEF using the maven loader
        CefAppBuilder builder = new CefAppBuilder();
        // windowless_rendering_enabled must be set to false if not wanted.
        builder.getCefSettings().windowless_rendering_enabled = useOSR;
        // USE builder.setAppHandler INSTEAD OF CefApp.addAppHandler!
        // Fixes compatibility issues with MacOSX
        builder.setAppHandler(new MavenCefAppHandlerAdapter() {
            @Override
            public void stateHasChanged(CefAppState state) {
                // Shutdown the app if the native CEF part is terminated
                if (state == CefAppState.TERMINATED) System.exit(0);
            }
        });

        if (args.length > 0) {
            builder.addJcefArgs(args);
        }

        // (1) The entry point to JCEF is always the class CefApp. There is only one
        //     instance per application and therefore you have to call the method
        //     "getInstance()" instead of a CTOR.
        //
        //     CefApp is responsible for the global CEF context. It loads all
        //     required native libraries, initializes CEF accordingly, starts a
        //     background task to handle CEF's message loop and takes care of
        //     shutting down CEF after disposing it.
        //
        //     WHEN WORKING WITH MAVEN: Use the builder.build() method to
        //     build the CefApp on first run and fetch the instance on all consecutive
        //     runs. This method is thread-safe and will always return a valid app
        //     instance.
        cefApp_ = builder.build();

//        CefApp.addAppHandler(new CefAppHandlerAdapter(null) {
//            @Override
//            public void stateHasChanged(org.cef.CefApp.CefAppState state) {
//                // Shutdown the app if the native CEF part is terminated
//                if (state == CefAppState.TERMINATED) System.exit(0);
//            }
//        });
//        CefSettings settings = new CefSettings();
//        settings.windowless_rendering_enabled = useOSR;
//        cefApp_ = CefApp.getInstance(settings);


        // (2) JCEF can handle one to many browser instances simultaneous. These
        //     browser instances are logically grouped together by an instance of
        //     the class CefClient. In your application you can create one to many
        //     instances of CefClient with one to many CefBrowser instances per
        //     client. To get an instance of CefClient you have to use the method
        //     "createClient()" of your CefApp instance. Calling an CTOR of
        //     CefClient is not supported.
        //
        //     CefClient is a connector to all possible events which come from the
        //     CefBrowser instances. Those events could be simple things like the
        //     change of the browser title or more complex ones like context menu
        //     events. By assigning handlers to CefClient you can control the
        //     behavior of the browser. See tests.detailed.MainFrame for an example
        //     of how to use these handlers.
        client_ = cefApp_.createClient();

        // (3) Create a simple message router to receive messages from CEF.
        CefMessageRouter msgRouter = CefMessageRouter.create();

        msgRouter.addHandler(new MessageRouterHandlerEx(client_), false);

        client_.addMessageRouter(msgRouter);

        // (4) One CefBrowser instance is responsible to control what you'll see on
        //     the UI component of the instance. It can be displayed off-screen
        //     rendered or windowed rendered. To get an instance of CefBrowser you
        //     have to call the method "createBrowser()" of your CefClient
        //     instances.
        //
        //     CefBrowser has methods like "goBack()", "goForward()", "loadURL()",
        //     and many more which are used to control the behavior of the displayed
        //     content. The UI is held within a UI-Compontent which can be accessed
        //     by calling the method "getUIComponent()" on the instance of CefBrowser.
        //     The UI component is inherited from a java.awt.Component and therefore
        //     it can be embedded into any AWT UI.
        browser_ = client_.createBrowser(startURL, useOSR, isTransparent);
        browerUI_ = browser_.getUIComponent();

        // (5) For this minimal browser, we need only a text field to enter an URL
        //     we want to navigate to and a CefBrowser window to display the content
        //     of the URL. To respond to the input of the user, we're registering an
        //     anonymous ActionListener. This listener is performed each time the
        //     user presses the "ENTER" key within the address field.
        //     If this happens, the entered value is passed to the CefBrowser
        //     instance to be loaded as URL.


        // Clear focus from the address field when the browser gains focus.
        client_.addFocusHandler(new CefFocusHandlerAdapter() {
            @Override
            public void onGotFocus(CefBrowser browser) {
                if (browserFocus_) return;
                browserFocus_ = true;
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
                browser.setFocus(true);
            }

            @Override
            public void onTakeFocus(CefBrowser browser, boolean next) {
                browserFocus_ = false;
            }
        });
    }

    private JMenuBar createMenuBar() {
        // (6) All UI components are assigned to the default content pane of this
        //     JFrame and afterwards the frame is made visible to the user.
        JMenuBar mb = new JMenuBar();
        JMenu fileJMenu = new JMenu("File");
        mb.add(fileJMenu);
        JMenuItem reloadJMenuItem = new JMenuItem("reload");
        fileJMenu.add(reloadJMenuItem);
        reloadJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.browser_.reload();
                MainFrame.this.repaint();
            }
        });
        JMenuItem exitJMenuItem = new JMenuItem("exit");
        fileJMenu.add(exitJMenuItem);
        exitJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CefApp.getInstance().dispose();
                dispose();
            }
        });
        JMenu helpJMenu = new JMenu("help");
        JMenuItem infoJMenuItem = new JMenuItem("info");
        helpJMenu.add(infoJMenuItem);
        mb.add(helpJMenu);
        infoJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(MainFrame.this, "Thank you for using ZK integration with JCEF");
            }
        });

        return mb;
    }

    private void initWindowEventHandler() {
        // (7) To take care of shutting down CEF accordingly, it's important to call
        //     the method "dispose()" of the CefApp instance if the Java
        //     application will be closed. Otherwise you'll get asserts from CEF.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                CefApp.getInstance().dispose();
                dispose();
            }
        });
    }

    private void openFrame() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();

        // 假设扩展屏是第二块屏幕
        Rectangle extendedBounds = screens[screens.length - 1].getDefaultConfiguration().getBounds();

        int x = extendedBounds.x + (extendedBounds.width - getWidth()) / 2;
        int y = extendedBounds.y + (extendedBounds.height - getHeight()) / 2;
        setLocation(x, y);

        setVisible(true);
        toFront();
        repaint();
    }
}
