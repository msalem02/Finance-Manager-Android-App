# Finance Manager – Android App

Finance Manager is an Android application that helps users manage their personal finances:
track income and expenses, set budgets, and view clear summaries and charts.  
All data is stored **locally** on the device using SQLite – no backend server required.

This project was developed as part of a university Android development course.

---

## 📱 Key Features

### 🔐 Authentication

- **Sign up**
  - Email used as unique ID (primary key).
  - First & last name with length validation.
  - Password rules:
    - 6–12 characters
    - at least one uppercase letter  
    - at least one lowercase letter  
    - at least one digit
  - Confirm password must match.
- **Sign in**
  - Email + password.
  - “Remember me” checkbox – stores the last used email in `SharedPreferences`.
- **Logout**
  - Accessible from the navigation drawer.
  - Clears current user and returns to login screen.

---

### 🧭 Main Navigation

The main UI is a **Navigation Drawer Activity** with these sections:

- **Home** – dashboard with financial summary and charts.
- **Income** – record and manage income transactions.
- **Expenses** – record and manage expense transactions.
- **Budgets & Goals** – set monthly budgets per category.
- **Settings** – app theme, default period, and category management.
- **Profile** – view and edit account details.
- **Logout**

Drawer header shows:

- `Welcome <FirstName>`
- Logged-in user’s email.

---

### 🏠 Home – Summary & Reports

The **Home** screen provides an overview of the user’s financial situation.

- **Summary card**:
  - Total income
  - Total expenses
  - Balance (income − expenses)
- **Period filters**:
  - Day
  - Week
  - Month
  - Year
  - Custom date range (From / To)
- **Expenses by category**:
  - Text breakdown:  
    `Food: 120.00 (30.0%)`, `Bills: 200.00 (50.0%)`, …
  - **Pie chart** using MPAndroidChart:
    - Each category slice has its own color.
    - Percentages shown for each slice.
- Changing the period automatically updates the totals and the chart.

---

### 💰 Income Management

- Add income transactions with:
  - Amount (decimal)
  - Date (date picker)
  - Category (e.g., Salary, Scholarship, Other…)
  - Optional description
- List of all income transactions in a **RecyclerView**.
- **Edit income**
  - Opens styled dialog with current amount & description.
  - User can update values; changes are saved to SQLite.
- **Delete income**
  - Removes record from DB and updates Home summary and budgets.

---

### 💸 Expense Management

- Add expense transactions with:
  - Amount (decimal)
  - Date (date picker)
  - Category (Food, Bills, Transport, etc.)
  - Optional description
- List of all expenses in a **RecyclerView**.
- **Edit expense**
  - Dark-theme friendly dialog identical to income editing.
- **Delete expense**
  - Removes record and updates all summaries and budgets.

---

### 🎯 Budgets & Goals

- Set **monthly budgets per category**:
  - Choose category (e.g., Food, Bills).
  - Select month and year.
  - Enter budget amount.
- View budgets in a list (RecyclerView) with:
  - Budget amount
  - Spent amount for the month
  - Remaining amount
  - (Optional) visual indication of progress
- Budgets are automatically updated based on expense data.

---

### ⚙️ Settings

- **Theme**
  - Light / Dark modes.
  - Chosen theme saved in `SharedPreferences`.
  - Applied globally (login, main activity, fragments, dialogs).
- **Default period (Home)**
  - Day / Week / Month / Year.
  - Used when Home screen opens.
- **Category manager**
  - Separate lists for Income and Expense categories.
  - Add new custom categories.
  - Edit or delete existing categories.
  - Category changes are reflected immediately in Income/Expenses spinners.

---

### 👤 Profile

- Shows current user details:
  - Email (read-only)
  - First name
  - Last name
- User can:
  - Update first and last name.
  - Optionally change password (with confirmation + validation).
- After update:
  - Data saved to SQLite.
  - Drawer header updated (e.g., `Welcome Mohammed`).

---

## 🧱 Architecture & Tech Stack

- **Language:** Java  
- **Minimum SDK:** (according to `build.gradle`, e.g. 21/24)  
- **Architecture:** Single-activity, multi-fragment with Navigation Drawer  
- **Data storage:** SQLite (custom `DBHelper` handling tables and queries)  
- **Preferences:** `SharedPreferences` (theme, default period, remember-me email, current user)  
- **UI Components:**
  - `DrawerLayout`, `NavigationView`, `Toolbar`
  - `RecyclerView` + custom adapters
  - `ScrollView`, `LinearLayout`, `ConstraintLayout`
  - `EditText`, `TextView`, `Button`, `Spinner`, `RadioGroup`, `CheckBox`
  - `AlertDialog` for editing records


**Package name:**  
`birzeit.edu.a1203022_courseproject`

---

## 🗄 Database Design (Simplified)

**Users**

| Column      | Type     | Notes              |
|-------------|----------|--------------------|
| email       | TEXT PK  | unique ID per user |
| firstName   | TEXT     |                    |
| lastName    | TEXT     |                    |
| password    | TEXT     | hashed/plain for course use |

**Categories**

| Column     | Type    | Notes                                   |
|------------|---------|-----------------------------------------|
| id         | INTEGER PK AUTOINCREMENT |                        |
| userEmail  | TEXT   | FK → Users.email                         |
| name       | TEXT   | category name                            |
| type       | TEXT   | `"INCOME"` or `"EXPENSE"`                |

**Transactions**

| Column      | Type    | Notes                                   |
|-------------|---------|-----------------------------------------|
| id          | INTEGER PK AUTOINCREMENT |                        |
| userEmail   | TEXT   | FK → Users.email                         |
| type        | TEXT   | `"INCOME"` or `"EXPENSE"`                |
| amount      | REAL   |                                         |
| date        | TEXT   | stored as `YYYY-MM-DD`                  |
| categoryId  | INTEGER| FK → Categories.id                       |
| description | TEXT   | optional                                |

**Budgets**

| Column      | Type    | Notes                                   |
|-------------|---------|-----------------------------------------|
| id          | INTEGER PK AUTOINCREMENT |                        |
| userEmail   | TEXT   | FK → Users.email                         |
| categoryId  | INTEGER| FK → Categories.id                       |
| month       | INTEGER| 1–12                                    |
| year        | INTEGER| e.g. 2025                               |
| amount      | REAL   | budget amount                           |

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/msalem02/Finance-Manager-Android-App.git
cd Finance-Manager-Android-App
