//
//  AppDelegate.swift
//  iosApp
//
//  Created by Sumit Kumar on 10/09/25.
//

import UIKit
import Pendo

class AppDelegate: NSObject, UIApplicationDelegate {
	func application(
		_ application: UIApplication,
		didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
	) -> Bool {
	print("App did finish launching")
//		let apiKey = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhY2VudGVyIjoidXMiLCJrZXkiOiJmNzM1YjMzNmRkYmM2ODU5MWFjMDkwNmE3ZDg3MGYzYjAwNzQ1Y2NkMWFlZDkxN2I1YTgzYmNjY2NhOGE3YzVmMmM1MDNjZGZiODNlNjQxYjI1MWM0ZjI5MDc3OTJiMDhjNTZlYmRkMmIxMTI0YWZmMmIzN2IwNjI4MTNiMzM5N2JmMTRlODZkYjQzZmE2NWExMmFkMmFkMDZjYWQ1ZmM4Zjc0Mjg3NjBlODZjMGI2NzVlOWIwMTY0NDc5NTM0MmQ4OTQ1MzNjODc5NTUyYTMyNjY3NDQyMzFiYTU5M2RiMDIzMjM4MzVhZGEyMDI5YmVkNWY2ZGUzMjBkNjk3OWI0NDU2NDk2NjM2ZTc5ZWFmM2U1YzkwYjhkNzZkZjQyNDIuMjM0NDYwZjgxZWZmZjhmNDE1ZDU1ZTdmOTAyN2Y0NGYuMGNmM2NkYjM4ZTQ3ZGIyMTliN2UxOWFkM2JkZDEwZmY4NDIxYWY3ZTBjOWVjZDM1MDVkZTliNWU2MmE4YzI5ZSJ9.TZ0zFVTqcpIWCTIYsF_05xJcYCicoRGP4zAKyoINUp2CH8rlUHbxjcTbcOjuCT8-Q1yC1H2SQwp2P9uoZmaTGOy2Owi46SNNjw-U2U6opqQXTtuYJ7sA03fLWX7wDBMDKn1LDX2ECL-5JR_ktW0RMJ_6QPN3RJPjJp6UGWy2JrA"
//        PendoManager.shared().setup(apiKey)
//		PendoManager.shared().setDebugMode(true)
		
		return true
	}
	func applicationWillTerminate(_ application: UIApplication) {
		print("App will terminate")
	}

	func application( _ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey: Any] = [:] ) -> Bool {
		if url.scheme?.range(of: "pendo") != nil {
					PendoManager.shared().initWith(url)
					return true
		}
		print("App opened via URL")
		return true
	}
	// MARK: - Handling Notifications
	func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
		print("Registered for remote notifications with token:")
	}

	func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
		print("Failed to register for remote notifications: \(error.localizedDescription)")
	}
}
