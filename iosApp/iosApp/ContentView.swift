import UIKit
import SwiftUI
import ComposeApp

/// Debug builds talk to the API running on the Mac (the simulator shares its localhost); release builds to production.
enum ApiConfig {
    #if DEBUG
    static let baseUrl = "http://localhost:5090"
    #else
    static let baseUrl = "https://libris-api.billfolder.app"
    #endif
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(baseUrl: ApiConfig.baseUrl)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}
