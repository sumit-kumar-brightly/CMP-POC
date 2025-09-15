import UIKit
import SwiftUI
import Pendo
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

struct ContentView: View {
	init() {
		// ComposeApp.PendoInterop_iosKt.initializePendo()
		// ComposeApp.PendoInterop_iosKt.startPendoSession(visitorId: "John Doe", accountId: "ACME")
//		let apiKey = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhY2VudGVyIjoidXMiLCJrZXkiOiIxOTM4Mzc4NWZlNmUzNDE3OTBmOGI1ODkyMjNlNmY2M2IxZTkwZDlmOTk3MTA3NWQ1NzczMzZhZDY5OTlmY2FhMWU4NzhiZmU1MTFlY2I5MDk1YTZiMWRiMDg5MjM1YjMxODVjNzkzM2U0YzMzNTY5ZTZkM2NlMTZmMWVkOGNkZmJjZjk3YzBjN2MyNWQ0YTViMjRiMTI4M2Y4YTU3NmM4MWNhYzMyZmQ4YTY3NTJlNTg2MTc3ZTczZjEyYTRjYjZjM2ZiMTY0Zjg4OGQwMWIxZjNjYmNiNzA4MmQyYmQ0NWMzYmQxNzc2MzI1MDNiNzk4NDk0NGM3YjA5NzA3NGNmNmNlZGNkYzUwOWQwZGMxYzZmZTc4YjcxOWUwOTNiOTAuZGIzNjY1NWE1ODk4N2Y2MDhkNDFjYzVhODZmNzljOGQuZTJlNDlhMzRmZjFhMzk2ZTk2MGM0ZWMwNGM5MTY3NGQ5ZjM3N2RkODY1NTU5YmViZDc1ZWRiNzM5NDhhMjEyNCJ9.c_tj83XKbLP-iQMJ2C62hXqZUC0uapjFxARPYTWXyka08jeDlWp3TtzGiWmPepV29oF9dBomCCxHafWsFfv3-9cDa6VYyK2DxFHf3wgMoaa-PN12ycyy54LPSCJ9kucEQvVSRXxr2V2AySTyipeRhERSBvQM2Zc-DoX_pr6xDE0"
//			PendoManager.shared().setup(apiKey)
//		let visitorId = "John Doe"
//		let accountId = "ACME"
//		let visitorData: [String : any Hashable] = ["product": "MME"]
//		let accountData: [String : any Hashable] = ["name": "Brightly Internal"]
//		PendoManager.shared().startSession(visitorId, accountId:accountId, visitorData:visitorData, accountData:accountData)
		
	}
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
//			.onAppear {
//				PendoManager.shared().startSession(
//					"bdf640fa-f270-4861-b297-1db8718f2943",
//							accountId: "accountId",
//							visitorData: ["product": "MME"] ,
//							accountData: ["name": "Brightly Internal"]
//				)
//			}

    }
}



