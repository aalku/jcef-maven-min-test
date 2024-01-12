package org.aalku.demo.jcef;

import java.io.IOException;

import org.cef.CefApp;
import org.cef.CefApp.CefAppState;
import org.springframework.stereotype.Component;

import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import me.friwi.jcefmaven.UnsupportedPlatformException;

/**
 * TODO This should be a different JAR so we could tell dev-tools not to reload
 * it.
 * 
 * In and that way auto-reload might work. In that case we should avoid those
 * calls to System.exit() that are around and exit the app only when the user
 * really wants to close it. Not sure how we could tell.
 * 
 */
@Component
public class CefAppWrapperBean {
	private static CefApp cefApp;
	static {
		try {
			System.err.println("initJCEF");
			cefApp = initJCEF(new String[0]);
		} catch (IOException | UnsupportedPlatformException | InterruptedException | CefInitializationException e) {
			throw new RuntimeException(e);
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				cefApp.dispose();
			}
		});
	}
	
	private static CefApp initJCEF(String[] args) throws IOException, UnsupportedPlatformException, InterruptedException, CefInitializationException {
        // (0) Initialize CEF using the maven loader
        CefAppBuilder builder = new CefAppBuilder();
        // windowless_rendering_enabled must be set to false if not wanted. 
        builder.getCefSettings().windowless_rendering_enabled = false;
        // USE builder.setAppHandler INSTEAD OF CefApp.addAppHandler!
        // Fixes compatibility issues with MacOSX
        builder.setAppHandler(new MavenCefAppHandlerAdapter() {
            @Override
            public void stateHasChanged(org.cef.CefApp.CefAppState state) {
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
        return builder.build();
	}
	
	public CefApp getCefApp() {
		return cefApp;
	}
}
