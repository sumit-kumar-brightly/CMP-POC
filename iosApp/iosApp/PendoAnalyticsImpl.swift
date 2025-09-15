//
//  PendoAnalyticsImpl.swift
//  iosApp
//
//  Created by Sumit Kumar on 14/09/25.
//

import Foundation
import ComposeApp
import Pendo

class PendoAnalyticsImpl: NSObject, PendoAnalytics {
	
	func initialize(apiKey: String) {
//		let config = PendoConfig.Builder()
//			.withApiKey(apiKey)
//			.build()
 		PendoManager.shared().setup(apiKey)
	}
	
	func startSession(
		visitorId: String,
		accountId: String,
		visitorData: [String: Any]?,
		accountData: [String: Any]?
	) {
		 PendoManager.shared().startSession(
		 	visitorId,
		 	accountId: accountId,
		 	visitorData: visitorData ?? [:],
		 	accountData: accountData ?? [:]
		 )
	}
	
	func trackEvent(event: String, properties: [String: Any]?) {
		PendoManager.shared().track(event, properties: properties ?? [:])
	}
	func trackScreen(screenName: String, properties: [String: Any]?) {
		var params = properties ?? [:]
		params["screen_name"] = screenName
		PendoManager.shared().track("screen_view", properties: params)
	}
}
