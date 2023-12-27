package com.zhu00yu.springbootjcefscaffold;

import com.zhu00yu.springbootjcefscaffold.browser.MainFrame;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class SpringbootJcefScaffoldApplication {

	public static void main(String[] args) {

		SpringApplicationBuilder springBuilder = new SpringApplicationBuilder(SpringbootJcefScaffoldApplication.class);
		springBuilder.headless(false);
		ConfigurableApplicationContext context = springBuilder.run(args);
		/* Run JCEF application in its own thread*/
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				//Create a new CefAppBuilder instance
				CefAppBuilder cefBuilder = new CefAppBuilder();
				//Configure the builder instance
				cefBuilder.setInstallDir(new File("jcef-bundle")); //Default
				cefBuilder.setProgressHandler(new ConsoleProgressHandler()); //Default
				cefBuilder.getCefSettings().windowless_rendering_enabled = true; //Default - select OSR mode
				// Windowed rendering mode is used by default. If you want to test OSR mode set |useOsr| to true and recompile.
				boolean useOsr = false;
				try {
					new MainFrame("http://localhost:8080", useOsr, false, args);
				} catch (UnsupportedPlatformException | CefInitializationException | IOException
						| InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

}
