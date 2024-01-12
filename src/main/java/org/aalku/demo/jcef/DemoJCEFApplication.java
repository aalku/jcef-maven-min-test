package org.aalku.demo.jcef;

import java.io.IOException;

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
	
	public static void main(String[] args) {
	    System.setProperty("spring.devtools.restart.enabled", "false");
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
        int port = event.getWebServer().getPort();
		System.err.println("Opening main frame...");
		this.mainFrame = new MainFrame("http://localhost:" + port, cefAppWrapperBean.getCefApp());
    }

	@Override
	public void destroy() throws Exception {
		// Boot frontend
		System.err.println("Closing main frame...");
		this.mainFrame.close();
	}

}
