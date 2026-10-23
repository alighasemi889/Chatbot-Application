# Chatbot Application

A simple ChatGPT-style Android chat app written in **Java**. Ask a question, get an answer — powered by any OpenAI-compatible chat API.

> This is an early prototype (v0.1).

## Features

- Clean chat interface with message bubbles
- Full-conversation context sent to the model on every request
- "Typing…" indicator while waiting for a reply
- Right-to-left (RTL) layout support, Persian UI strings
- Configurable API URL, API key and model name from the in-app settings
- **Demo mode:** works out of the box without an API key (returns a placeholder reply)
- Errors are shown directly in the chat

## Tech

| Item | Details |
|------|---------|
| Language | Java 17 |
| UI | Android Views (XML), RecyclerView, Material Components |
| Networking | `HttpURLConnection` + `org.json` (no extra libraries) |
| Build | Gradle (Android Gradle Plugin 8.5.2) |
| Min SDK | 24 (Android 7.0) |
| Target / Compile SDK | 34 |

## Getting Started

### Build with Android Studio

1. Clone the repository:
   ```bash
   git clone https://github.com/alighasemi889/Chatbot-Application.git
   ```
2. Open the project folder in Android Studio and wait for Gradle Sync to finish.
3. Run the app on an emulator or a physical device.

### Download a ready-made APK

Every push triggers a GitHub Actions build (`Build APK`).

1. Open the **Actions** tab and select the latest successful run.
2. Download the **app-debug** artifact (a zip containing the APK).
3. Install the APK on your phone (you may need to allow installs from unknown sources).

## Configuration

Open the menu (⋮) → **Settings** and fill in:

| Field | Default |
|-------|---------|
| API URL | `https://api.openai.com/v1/chat/completions` |
| API key | *(empty — demo mode)* |
| Model | `gpt-4o-mini` |

Any service that follows the OpenAI Chat Completions format can be used by changing the URL and model name.

> The API key is stored locally on the device (SharedPreferences) and is never hard-coded in the source. Do not commit keys to the repository.

## Project Structure

```
app/src/main/
├── java/com/example/aichat/
│   ├── MainActivity.java   # Chat screen, settings dialog, request flow
│   ├── ChatAdapter.java    # RecyclerView adapter for message bubbles
│   ├── ApiClient.java      # Chat completions HTTP client
│   └── Message.java        # Message model
└── res/                    # Layouts, drawables, strings, theme
```

## Roadmap

- [ ] Streaming responses
- [ ] Persistent chat history
- [ ] Multiple conversations
- [ ] Dark theme
- [ ] Custom app icon
- [ ] Markdown rendering

## Authors

Written by **Mahyar Mohammad Ebrahimi** and **Ali Ghasemi**.

## License

Released under the [MIT License](LICENSE).
