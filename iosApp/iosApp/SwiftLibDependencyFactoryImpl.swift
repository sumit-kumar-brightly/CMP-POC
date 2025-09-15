//
//  SwiftLibDependencyFactoryImpl.swift
//  iosApp
//
//  Created by Sumit Kumar on 14/09/25.
//

import Foundation
import ComposeApp

class SwiftLibDependencyFactoryImpl: NSObject, SwiftLibDependencyFactory {
	
	static let shared = SwiftLibDependencyFactoryImpl()
	
	func providePendoAnalyticsImpl() -> any PendoAnalytics {
		return PendoAnalyticsImpl()
	}
}
