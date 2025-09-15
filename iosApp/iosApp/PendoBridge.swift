//
//  PendoBridge.swift
//  iosApp
//
//  Created by Sumit Kumar on 11/09/25.
//
import Foundation
//import Pendo

@objc public class PendoBridge: NSObject {
	
	@objc public static let shared = PendoBridge()
	
	@objc public func setupPendo(apiKey: String) {
//		let config = PendoConfig.Builder()
//			.withApiKey(apiKey)
//			.build()
//		PendoManager.shared().setupWith(config)
	}
	
	@objc public func startSession(
		visitorId: String,
		accountId: String,
		visitorData: [String: Any],
		accountData: [String: Any]
	) {
//		PendoManager.shared().startSession(
//			visitorId,
//			accountId: accountId,
//			visitorData: visitorData,
//			accountData: accountData
//		)
	}

	@objc public func trackEvent(
		eventName: String,
		properties: [String: Any]
	) {
//		PendoManager.shared().track(eventName, properties: properties)
	}
}
