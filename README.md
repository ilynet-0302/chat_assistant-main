# Chat Assistant Application

A full-stack demo chat assistant with user registration, login, session management, and robust OpenAI function-calling for backend automation tools. Built with Spring Boot (Java), a simple HTML/JS frontend, and integrates with OpenAI's GPT models via the OpenAI Java SDK.

---

## Features
- User registration and login (with session handling)
- Chat interface with LLM integration (OpenAI GPT)
- Login info panel and logout button
- Backend tools for registration, login, password change, user listing, and account deletion (exposed for LLM/API automation)
- All tool-calling is handled via OpenAI's function-calling API for reliability and scalability

---

## 1. Application Overview

### Frontend (User Interface)
- **Files:** `chat.html`, `app.js`, `style.css`
- **Features:**
  - Login modal (email + password)
  - Registration form
  - Chat window for conversation
  - Login info panel at the top (shows logged-in user)
  - Logout button

#### Login Flow
- User enters email and password in the modal.
- On submit, JavaScript (`app.js`) sends a POST request to `/api/login`.
- If successful, the modal closes and the login info panel updates.

#### Registration Flow
- User enters email and password in the registration form.
- On submit, JavaScript sends a POST request to `/api/register`.
- If successful, a message is shown and the user can log in.

#### Chat Flow
- User types a message and submits the chat form.
- JavaScript sends the message to the backend at `/api/chat`.
- The response from the backend (bot reply) is shown in the chat window.
- If the bot response includes login info, the banner updates accordingly.

#### Logout
- User clicks the logout button to end the session.

---

## 2. Backend (Spring Boot Application)
- **Main entry:** `ChatAssistantApplication.java`
- **Controllers:** `ChatController.java`, `AuthController.java`
- **Service:** `ChatService.java`, `AuthService.java`
- **User data:** Stored in `users.txt` as `example.com,email,password`

### Endpoints
- `POST /api/chat` — Receives chat messages, returns bot responses (with tool-calling support).
- `POST /api/login` — Receives login info, authenticates, updates session.
- `POST /api/register` — Registers a new user.
- `POST /api/logout` — Logs out the current user.
- `GET /api/chat/session` — Returns current session info (login state, email, site).

---

## 3. LLM Integration (OpenAI Function Calling)

- The backend uses the [OpenAI Java SDK](https://github.com/TheoKanning/openai-java) to connect to OpenAI's GPT models (e.g., `gpt-3.5-turbo-1106`).
- All backend tools are registered as OpenAI functions using the SDK's function-calling API.
- When a user sends a message, the backend sends the message and all available tools to OpenAI.
- If the LLM decides a tool should be called, it returns a function call with structured arguments, which the backend executes and returns the result.

### Example: Register Function

```java
public static class RegisterParams {
    public String email;
    public String password;
}

ChatFunction registerFunction = ChatFunction.builder()
    .name("register")
    .description("Register a new user")
    .executor(RegisterParams.class, params -> this.simulateRegister(params.email, params.password, session))
    .build();
```

---

## 4. Session Handling

- The backend uses HTTP sessions to remember if a user is "logged in" and their email/site.
- All authentication is simulated for demonstration.

---

## 5. Available Tools (Function-Calling)

The following tools are exposed to the LLM and can be called automatically:

| Tool Name         | Description                        | Parameters                                 | Returns                        |
|-------------------|------------------------------------|--------------------------------------------|---------------------------------|
| register          | Register a new user                | `email`, `password`                        | `{ success, message }`          |
| login             | Login a user                       | `email`, `password`                        | `{ message }`                   |
| getCurrentUser    | Get current user info              | *(none)*                                   | `{ loggedIn, email, site }`     |
| changePassword    | Change user password               | `email`, `oldPassword`, `newPassword`      | `{ success, message }`          |
| listUsers         | List all users                     | *(none)*                                   | `[ "user1@example.com", ... ]`  |
| deleteAccount     | Delete user account                | `email`, `password`                        | `{ success, message }`          |

---

## 6. Example JSON Payloads

### Register
```json
POST /api/register
{
  "email": "test@example.com",
  "password": "1234"
}
```

### Login
```json
POST /api/login
{
  "email": "test@example.com",
  "password": "1234"
}
```

---

## 7. Setup & Configuration

### Prerequisites

- Java 21+
- Maven

### 1. Clone the repository

```sh
git clone <your-repo-url>
cd chat_assistant-main
```

### 2. Add your OpenAI API key

Edit `src/main/resources/application.properties`:

```
spring.ai.openai.api-key=sk-...your_openai_key_here...
spring.ai.openai.model=gpt-3.5-turbo
```

### 3. Build and run

```sh
./mvnw clean install
./mvnw spring-boot:run
```

### 4. Open the app

Visit [http://localhost:8080/chat.html](http://localhost:8080/chat.html)

---

## 8. Example Prompts for LLM

- "Register me with email test@example.com and password 1234."
- "Log me in with email test@example.com and password 1234."
- "Who am I logged in as?"
- "Change my password from 1234 to newpass."
- "Show me all registered users."
- "Delete my account. My password is 1234."

The LLM will recognize these requests and call the appropriate backend tool, automating the process for the user.

---

## 9. Notes

- All user data is stored in plain text in `users.txt` for demo purposes.
- The tools are for demonstration and can be extended for more advanced automation or admin features.
- The backend uses robust OpenAI function-calling, not prompt/JSON parsing, for reliable tool invocation.

---

## 10. License

MIT 