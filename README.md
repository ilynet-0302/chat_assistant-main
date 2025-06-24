# Chat Assistant Application

A full-stack demo chat assistant with user registration, login, session management, and LLM-driven automation tools. Built with Spring Boot (Java) and a simple HTML/JS frontend. Integrates with a local Ollama LLM server for advanced automation.

---

## Features
- User registration and login (with session handling)
- Chat interface with LLM integration
- Login info panel and logout button
- Advanced backend tools for registration, login, password change, user listing, and account deletion (exposed for LLM/API automation)

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
- `POST /api/chat` — Receives chat messages, returns bot responses.
- `POST /api/login` — Receives login info, authenticates, updates session.
- `POST /api/register` — Registers a new user.
- `POST /api/logout` — Logs out the current user.
- `GET /api/chat/session` — Returns current session info (login state, email, site).

---

## 3. LLM Integration
- The backend talks to a local Ollama server running Llama 3.
- The LLM is prompted to output structured JSON for automation commands (like login/register).
- The backend extracts and acts on these commands if present.

---

## 4. Session Handling
- The backend uses HTTP sessions to remember if a user is "logged in" and their email/site.
- No real authentication is performed; it's all simulated for demonstration.

---

## 5. Advanced Tools via @Tool in ChatController

The following tools are exposed for LLM or API-driven automation. These can be called by the LLM or via backend integration for advanced flows.

### 5.1. Simulate Login
**Method:** `simulateLogin(String email, String password, HttpSession session)`
- Simulates a login and sets session attributes.
- **Returns:** `{ action: "login", email, password, site, message }`

### 5.2. Simulate Registration
**Method:** `simulateRegister(String email, String password, HttpSession session)`
- Registers a new user.
- **Returns:** `{ action: "register", email, success, message }`

### 5.3. Get Current User
**Method:** `getCurrentUser(HttpSession session)`
- Returns info about the currently logged-in user.
- **Returns:** `{ loggedIn, email, site }`

### 5.4. Change Password
**Method:** `changePassword(String email, String oldPassword, String newPassword, HttpSession session)`
- Changes the password for the logged-in user (requires old password).
- **Returns:** `{ success, message }`

### 5.5. List All Users
**Method:** `listUsers()`
- Lists all registered user emails for `example.com`.
- **Returns:** `[ "user1@example.com", "user2@example.com", ... ]`

### 5.6. Simulate Account Deletion
**Method:** `simulateAccountDeletion(String email, String password, HttpSession session)`
- Deletes the logged-in user's account (sets password to `__DELETED__` and logs out).
- **Returns:** `{ success, message }`

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

### Change Password (via tool)
```json
{
  "email": "test@example.com",
  "oldPassword": "1234",
  "newPassword": "newpass"
}
```

### LLM Tool Call Example
The LLM can output:
```json
{
  "action": "register",
  "email": "newuser@example.com",
  "password": "pass123"
}
```
And the backend will process registration automatically.

---

## 7. Notes
- All user data is stored in plain text in `users.txt` for demo purposes.
- The tools are for demonstration and can be extended for more advanced automation or admin features.

---

## 8. Example Prompts for LLM

Here are example phrases you can type to the chat assistant to trigger each tool:

### Register
- "Register me with email test@example.com and password 1234."
- "I want to create a new account. My email is newuser@example.com and my password is pass123."

### Login
- "Log me in with email test@example.com and password 1234."
- "Sign in as test@example.com, password 1234."

### Get Current User
- "Who am I logged in as?"
- "Am I currently logged in?"

### Change Password
- "Change my password from 1234 to newpass."
- "I want to update my password. My old password is 1234, my new password is newpass."

### List All Users (admin/demo)
- "Show me all registered users."
- "List all user emails."

### Delete Account
- "Delete my account. My password is 1234."
- "I want to remove my account. Password: 1234."

The LLM will recognize these requests and call the appropriate backend tool, automating the process for the user. 

---

## 9. Why This Approach? Tool Calling, @Tool, and Scaling Options

### Why is the program designed this way?
This application demonstrates how to connect a local LLM (like Llama 3) to backend automation tools in a way that is both practical and compatible with open-source models. Instead of relying on advanced (and often paid) LLM features like "native function calling," the backend uses a combination of:
- **Prompt engineering:** The LLM is instructed to output a JSON object for specific actions (like registration, login, etc.).
- **Output parsing:** The backend checks if the LLM's response matches a tool call, and if so, executes the corresponding Java method.

This approach is robust, works with any LLM that can follow instructions, and is easy to extend for new tools.

### How does @Tool fit in?
The `@Tool` annotation is used in some frameworks (like Spring AI Tool Agent) to automatically expose backend methods as callable tools for an LLM agent. In this demo, the annotation is present for future compatibility, but **the actual tool calling is handled by prompt engineering and output parsing**—not by the annotation itself.

If you use a framework that supports @Tool (like Spring AI Tool Agent), the LLM agent can discover and call these methods automatically, making it even easier to add new tools without writing parsing logic.

### What if the app needs to scale to hundreds of functions?
For a small number of tools, this approach is simple and effective. For larger applications, consider these options:
- **Tool/Function Calling Agents:** Use frameworks like Spring AI Tool Agent (Java) or LangChain (Python) to register all your tools. The agent will handle intent detection and tool invocation automatically.
- **NLU Engines:** For very large or complex apps, use a Natural Language Understanding (NLU) engine (like Rasa, Dialogflow, or an LLM-based agent) to classify user intent and extract parameters, then call the right backend tool.
- **Hybrid Approach:** Combine prompt engineering, output parsing, and NLU for maximum coverage and flexibility.

### Summary Table
| Approach                | Best For         | Pros                        | Cons/Limitations                |
|-------------------------|------------------|-----------------------------|---------------------------------|
| Prompt + Parsing        | Few tools        | Simple, fast, no extra libs | Not scalable, manual updates    |
| Tool/Function Calling   | Many tools       | Scalable, easy to extend    | Needs agent/LLM support         |
| NLU/LLM Agents          | Complex apps     | Most flexible, robust       | More setup, sometimes paid      |

**This demo is designed to be easy to understand, easy to extend, and compatible with free/open-source LLMs.**

--- 