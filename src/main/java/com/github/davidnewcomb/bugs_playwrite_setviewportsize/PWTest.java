package com.github.davidnewcomb.bugs_playwrite_setviewportsize;

import java.util.Map;
import java.util.Random;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.FunctionCallback;

public class PWTest {

	public static void main(String[] args) {
		new PWTest().run();
	}

	public void run() {
		Playwright playwright = Playwright.create();

		// Doesn't work
		Map<String, Object> ffUserPref = Map.of( //
				"privacy.resistFingerprinting", true, //
				"privacy.resistFingerprinting.letterboxing", true //
		);
		LaunchOptions launchOptions = new BrowserType.LaunchOptions() //
				.setHeadless(false) //
				.setSlowMo(50) //
				.setFirefoxUserPrefs(ffUserPref) //
		;

		// Works
//		launchOptions = new BrowserType.LaunchOptions() //
//				.setHeadless(false) //
//				.setSlowMo(50) //
//		;

		BrowserType browserType = playwright.firefox();

		Browser browser = browserType.launch(launchOptions);

		BrowserContext browserContext = browser.newContext();

		browserContext.exposeFunction("myPageListener", new FunctionCallback() {

			@Override
			public Object call(Object... args) {
				System.out.println("Message from browser: '" + args[0] + "'");
				return null;
			}
		});

		// Straight away
//		browserContext.addInitScript("""
//				(() => {
//				window.myPageListener('Hello from Javascript!');
//				})();
//				""");

		// Deferred
		browserContext.addInitScript("""
				(() => {
				setTimeout(() => {
					window.myPageListener('Hello from Javascript!');
					}, 2000);
				})();
				""");

		// Be-bounced
//		browserContext.addInitScript("""
//				(() => {
//				console.log('init script');
//					let lastScrollY = window.scrollY;
//					let lastWidth = window.innerWidth;
//					let lastHeight = window.innerHeight;
//
//					let deboundTimer = [null, null];
//					let SCROLL = 0;
//					let RESIZE = 1;
//					let debounce = (obj, to, idx) => {
//						clearTimeout(deboundTimer[idx]);
//						deboundTimer[idx] = setTimeout(() => {
//							console.log('calling myPageListener b4');
//							window.myPageListener(obj);
//							console.log('calling myPageListener after');
//						}, to);
//					};
//
//					window.addEventListener('resize', () => {
//						if (window.innerWidth !== lastWidth || window.innerHeight !== lastHeight) {
//							lastWidth = window.innerWidth;
//							lastHeight = window.innerHeight;
//							debounce({
//								type: 'resize',
//								width: lastWidth,
//								height: lastHeight,
//								x: window.screenX,
//								y: window.screenY,
//								test:'1'
//							}, 500, RESIZE);
//						}
//				})();
//				""");

		Page page = browserContext.newPage();
		page.navigate("https://www.google.com");

		int i = 1;
		for (; i < 10; ++i) {
			int w = new Random().nextInt(300, 1001);
			int h = new Random().nextInt(300, 1001);
			System.out.println("[" + i++ + "](" + w + "x" + h + ")");
			page.setViewportSize(w, h);
		}

		System.out.println("Hurray we got to the end");
		playwright.close();

	}

}
