package com.brightlysoftware.brightlypoc

//import cocoapods.Pendo.PendoManager
import platform.Foundation.NSMutableDictionary
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSString

/**
 * Initializes the Pendo SDK with the given API key.
 * Visible in Swift as `ComposeApp.initializePendo`.
 */
@OptIn(ExperimentalForeignApi::class)
fun initializePendo() {
    println("initializePendo called")
//    PendoManager.sharedManager().setup("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhY2VudGVyIjoidXMiLCJrZXkiOiJmNzM1YjMzNmRkYmM2ODU5MWFjMDkwNmE3ZDg3MGYzYjAwNzQ1Y2NkMWFlZDkxN2I1YTgzYmNjY2NhOGE3YzVmMmM1MDNjZGZiODNlNjQxYjI1MWM0ZjI5MDc3OTJiMDhjNTZlYmRkMmIxMTI0YWZmMmIzN2IwNjI4MTNiMzM5N2JmMTRlODZkYjQzZmE2NWExMmFkMmFkMDZjYWQ1ZmM4Zjc0Mjg3NjBlODZjMGI2NzVlOWIwMTY0NDc5NTM0MmQ4OTQ1MzNjODc5NTUyYTMyNjY3NDQyMzFiYTU5M2RiMDIzMjM4MzVhZGEyMDI5YmVkNWY2ZGUzMjBkNjk3OWI0NDU2NDk2NjM2ZTc5ZWFmM2U1YzkwYjhkNzZkZjQyNDIuMjM0NDYwZjgxZWZmZjhmNDE1ZDU1ZTdmOTAyN2Y0NGYuMGNmM2NkYjM4ZTQ3ZGIyMTliN2UxOWFkM2JkZDEwZmY4NDIxYWY3ZTBjOWVjZDM1MDVkZTliNWU2MmE4YzI5ZSJ9.TZ0zFVTqcpIWCTIYsF_05xJcYCicoRGP4zAKyoINUp2CH8rlUHbxjcTbcOjuCT8-Q1yC1H2SQwp2P9uoZmaTGOy2Owi46SNNjw-U2U6opqQXTtuYJ7sA03fLWX7wDBMDKn1LDX2ECL-5JR_ktW0RMJ_6QPN3RJPjJp6UGWy2JrA")
//    PendoManager.sharedManager().setup("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhY2VudGVyIjoidXMiLCJrZXkiOiIxOTM4Mzc4NWZlNmUzNDE3OTBmOGI1ODkyMjNlNmY2M2IxZTkwZDlmOTk3MTA3NWQ1NzczMzZhZDY5OTlmY2FhMWU4NzhiZmU1MTFlY2I5MDk1YTZiMWRiMDg5MjM1YjMxODVjNzkzM2U0YzMzNTY5ZTZkM2NlMTZmMWVkOGNkZmJjZjk3YzBjN2MyNWQ0YTViMjRiMTI4M2Y4YTU3NmM4MWNhYzMyZmQ4YTY3NTJlNTg2MTc3ZTczZjEyYTRjYjZjM2ZiMTY0Zjg4OGQwMWIxZjNjYmNiNzA4MmQyYmQ0NWMzYmQxNzc2MzI1MDNiNzk4NDk0NGM3YjA5NzA3NGNmNmNlZGNkYzUwOWQwZGMxYzZmZTc4YjcxOWUwOTNiOTAuZGIzNjY1NWE1ODk4N2Y2MDhkNDFjYzVhODZmNzljOGQuZTJlNDlhMzRmZjFhMzk2ZTk2MGM0ZWMwNGM5MTY3NGQ5ZjM3N2RkODY1NTU5YmViZDc1ZWRiNzM5NDhhMjEyNCJ9.c_tj83XKbLP-iQMJ2C62hXqZUC0uapjFxARPYTWXyka08jeDlWp3TtzGiWmPepV29oF9dBomCCxHafWsFfv3-9cDa6VYyK2DxFHf3wgMoaa-PN12ycyy54LPSCJ9kucEQvVSRXxr2V2AySTyipeRhERSBvQM2Zc-DoX_pr6xDE0")
}

/**
 * Starts a Pendo session; call from Swift after login.
 */
@OptIn(ExperimentalForeignApi::class)
fun startPendoSession(visitorId: String, accountId: String) {
    println("initializePendo called")
    val visitorData: Map<Any?, *> = mapOf("product" to "MME")
    val accountData: Map<Any?, *> = mapOf("name" to "Brightly Internal")
//    PendoManager.sharedManager().startSession(
//        visitorId,
//        accountId = accountId,
//        visitorData = visitorData,
//        accountData = accountData
//    )
}
