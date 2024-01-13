package org.aalku.demo.jcef;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;

@SpringBootApplication
public class DemoJCEFApplication implements ApplicationRunner, InitializingBean, DisposableBean {

	private static ConfigurableApplicationContext ctx;
	private volatile MainFrame mainFrame;
	
	@Autowired
	private CefAppWrapperBean cefAppWrapperBean;
	
	@Autowired
	private ClockService clockService;

	private WindowAdapter onCloseShutdownEventHandler = new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
			new Thread() {
				{
					this.setDaemon(true);
				}

				public void run() {
					System.exit(0);
				};
			}.start();
		}
	};
	
	public static void main(String[] args) {
		// Boot backend
		SpringApplication app = new SpringApplication(DemoJCEFApplication.class);
		app.setHeadless(false);
		ctx = app.run(args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}

    @EventListener
    public void onApplicationEvent(final ServletWebServerInitializedEvent event) throws IOException, UnsupportedPlatformException, InterruptedException, CefInitializationException {
    	// Boot frontend
        int port = event.getWebServer().getPort();
		System.err.println("Opening main frame...");
		this.mainFrame = new MainFrame(cefAppWrapperBean.getCefApp());
		this.mainFrame.getMsgRouter().addHandler(new CefMessageRouterHandlerAdapter() {
        	@Override
        	public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent,
        			CefQueryCallback callback) {
        		if (request.equals("HelloWorld")) {
        			callback.success("Hello World!! Backend here!!");
        			return true;
        		} else if (request.equals("clock")) {
        			clockService.runNextSecond(new Runnable() {
        				public void run() {
        					callback.success(Instant.now().atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS).toString());
        				}
        			});
        			return true;
        		}
        		System.err.println("msgRouter query: " + (request.length() < 10000 ? request : ("Really long request. Len = " + request.length())));
        		return false;
        	}
		}, true);
		this.mainFrame.addWindowListener(onCloseShutdownEventHandler);
		this.mainFrame.loadUrl("http://localhost:" + port);
    }
    
	@Override
	public void destroy() throws Exception {
		System.err.println("Closing main frame...");
		this.mainFrame.removeWindowListener(onCloseShutdownEventHandler); // So it doesn't shut down the app by itself
		this.mainFrame.close();
	}

}
