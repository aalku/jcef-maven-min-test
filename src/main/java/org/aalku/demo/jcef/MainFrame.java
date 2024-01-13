package org.aalku.demo.jcef;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.handler.CefContextMenuHandlerAdapter;
import org.cef.handler.CefFocusHandlerAdapter;
import org.cef.handler.CefRequestHandlerAdapter;
import org.cef.handler.CefResourceHandler;
import org.cef.handler.CefResourceHandlerAdapter;
import org.cef.handler.CefResourceRequestHandler;
import org.cef.handler.CefResourceRequestHandlerAdapter;
import org.cef.misc.BoolRef;
import org.cef.network.CefRequest;

import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	
	// private final JTextField address_;
    private final CefClient client_;
    private final CefBrowser browser_;
    private final Component browerUI_;
    private boolean browserFocus_ = true;
    boolean isTransparent = true;
    private AtomicBoolean closed = new AtomicBoolean(false);

	private CefMessageRouter msgRouter;
	
	public MainFrame(CefApp cefApp_) throws IOException, UnsupportedPlatformException, InterruptedException, CefInitializationException {
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
        // Disable context menu
        client_.addContextMenuHandler(new CefContextMenuHandlerAdapter() {
        	@Override
        	public void onBeforeContextMenu(CefBrowser browser, CefFrame frame, CefContextMenuParams params,
        			CefMenuModel model) {
        		model.clear();
        		model.addItem(CefMenuModel.MenuId.MENU_ID_RELOAD_NOCACHE, "Reload app");
        	}
		});

        msgRouter = CefMessageRouter.create();
        client_.addMessageRouter(getMsgRouter());

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
        browser_ = client_.createBrowser(null, false, isTransparent);
        browerUI_ = browser_.getUIComponent();
		browser_.createImmediately();

        // (5) For this minimal browser, we need only a text field to enter an URL
        //     we want to navigate to and a CefBrowser window to display the content
        //     of the URL. To respond to the input of the user, we're registering an
        //     anonymous ActionListener. This listener is performed each time the
        //     user presses the "ENTER" key within the address field.
        //     If this happens, the entered value is passed to the CefBrowser
        //     instance to be loaded as URL.
//        JTextField address_ = new JTextField("http://localhost:123", 100);
//        address_.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                browser_.loadURL(address_.getText());
//            }
//        });

        // Update the address field when the browser URL changes.
//        client_.addDisplayHandler(new CefDisplayHandlerAdapter() {
//            @Override
//            public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
//                address_.setText(url);
//            }
//        });

        // Clear focus from the browser when the address field gains focus.
//        address_.addFocusListener(new FocusAdapter() {
//            @Override
//            public void focusGained(FocusEvent e) {
//                if (!browserFocus_) return;
//                browserFocus_ = false;
//                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
//                address_.requestFocus();
//            }
//        });

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

        // (6) All UI components are assigned to the default content pane of this
        //     JFrame and afterwards the frame is made visible to the user.
//        getContentPane().add(address_, BorderLayout.NORTH);
        getContentPane().add(browerUI_, BorderLayout.CENTER);
        pack();
        setSize(800, 600);
        setVisible(true);

        // (7) To take care of shutting down CEF accordingly, it's important to call
        //     the method "dispose()" of the CefApp instance if the Java
        //     application will be closed. Otherwise you'll get asserts from CEF.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            	if (!closed.getAndSet(true)) {
           			dispose();
				}
            }
        });
	}
	
	public void close() {
		this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	public CefMessageRouter getMsgRouter() {
		return msgRouter;
	}

	public void loadUrl(String string) {
		client_.addRequestHandler(new CefRequestHandlerAdapter() {
			@Override
			public CefResourceRequestHandler getResourceRequestHandler(CefBrowser browser, CefFrame frame,
					CefRequest request, boolean isNavigation, boolean isDownload, String requestInitiator,
					BoolRef disableDefaultHandling) {
				if (true) {
					disableDefaultHandling.set(false);
					return null;
				} else {
					disableDefaultHandling.set(true);
					return new CefResourceRequestHandlerAdapter() {
						@Override
						public CefResourceHandler getResourceHandler(CefBrowser browser, CefFrame frame,
								CefRequest request) {
							return new CefResourceHandlerAdapter() {
								// TODO
							};
						}
					};
				}
			}
		});
		browser_.loadURL(string);
	}
}
