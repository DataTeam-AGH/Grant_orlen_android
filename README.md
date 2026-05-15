# TEMPORARY DOCUMENTATION

*Will be moved in the future. For now, it is placed in README for easier readability.*

---

## List of Contents

1. [Android Studio Version](#android-studio-version)
2. [Project Installation](#project-installation)
3. [Connecting GitHub with Android Studio](#connecting-github-with-android-studio)
4. [Git and GitHub Workflow](#git-and-github-workflow)
5. [Android Studio Workflow](#android-studio-workflow)

   * [API level](#api-level)
   * [Virtual devices](#virtual-devices)
   * [Local passwords, tokens, and keys](#local-passwords,-tokens,-and-keys)
   * [Code structure](#code-structure)

---

## Android Studio Version

To develop the project, install the latest available version of Android Studio:

**Android Studio Panda | 2025.3.3**

[Download Android Studio](https://developer.android.com/studio)

If you cannot find an installer for your operating system, check the archive:

[Android Studio Archive](https://developer.android.com/studio/archive)

---

## Project Installation

After successfully installing Android Studio, there are two ways to clone the project and run it.

Make sure you have the required permissions to clone and modify the repository. If not, contact your team.

### a. Through Android Studio

**If running Android Studio for the first time:**

* Switch to the **Projects** tab (left side)
* Click **Clone Repository** (top right)
* If not connected to GitHub:

  * Select **Repository URL**
  * URL:
    `https://github.com/DataTeam-AGH/Grant_orlen_android.git`
  * Choose your working directory
  * Ensure **Version Control = Git**
* If connected to GitHub:

  * Select **GitHub**
  * Search for `DataTeam-AGH/Grant_orlen_android`
  * Choose your working directory
* Click **Clone** and wait for the project to load

**If another project is already open:**

* Go to:
  `File -> New -> Project from Version Control...`
* Then follow the steps above

---

### b. Through Git Bash

* Create a working directory
* Right-click → **Git Bash Here**
* Run:

  ```bash
  git clone https://github.com/DataTeam-AGH/Grant_orlen_android.git
  ```

Then open Android Studio:

**First run:**

* Go to **Projects**
* Click **Open**
* Select the `Orlen_app` directory

**If another project is open:**

* Go to:
  `File -> Open`
* Select the project directory

If Gradle errors appear on first launch:

* Restart Android Studio
* If the issue persists, contact your team

---

## Connecting GitHub with Android Studio

To use built-in Git integration:

* Go to:
  `Git -> GitHub -> Manage Accounts...`
* Click **+**

Try **Log in via GitHub** first.

If you encounter issues with pulling/pushing or repeated login prompts, proceed to the token method below.

---

### Token

If GitHub login fails:

* Go to:
  `Git -> GitHub -> Manage Accounts...`
* Remove existing account
* Open GitHub in browser
* Navigate to:
  `Settings -> Developer settings -> Personal access tokens -> Tokens (classic)`
* Click:
  **Generate new token (classic)**
* Add a note (e.g., project name)
* Set expiration (recommended: 1 year)
* Select scopes:
    - repo (read/write access)
* Generate token
* **Save it securely (treat like a password!)**
* Back in Android Studio:

  * Click **+**
  * Server: `github.com`
  * Paste token → **Add Account**

If it still doesn’t work, verify scopes or contact your team.

---

## Git and GitHub Workflow

We follow **Trunk-Based Development** (with minor modifications):

[Learn more](https://www.atlassian.com/continuous-delivery/continuous-integration/trunk-based-development)

### Key Principles

* Make small, frequent changes
* Split tasks when possible
* Create branches from `main`:

  * `feature/...` – new features
  * `fix/...` – bug fixes

### Workflow

* Push your branch
* [Rebase](https://git-scm.com/docs/git-rebase) with `main`
* Resolve conflicts
* Create a Pull Request
* Return to `main` for next task

### Rules

* Never merge to `main` without team leader approval
* Fix issues on the same branch (do not create new ones)
* Avoid branching from anything other than `main`

---

### Issues

All tasks are tracked here:
https://github.com/DataTeam-AGH/Grant_orlen_android/issues

Each issue includes:

* Unique number
* Title
* Description
* Label (feature/bug)

Rules:

* Assign yourself before starting
* Issues are created by team leaders or after discussion

---

### Branch Naming

Format:

```
feature/<issue_number>_<camelCaseName>
fix/<issue_number>_<camelCaseName>
```

Example:

```
feature/3_createLogInView
```

---

### Tags

Format:

```
vX.X.X
```

Example:

```
v0.1.2
```

Meaning:

* First number → major version
* Second → minor changes
* Third → patches (bug fixes)

---

### Pull Requests

Steps:

* Open GitHub
* Switch to your branch
* Click **Compare & Pull Request**

Title format:

```
<issue_number>_<camelCaseTitle>
```

Example:

```
3_createLogInView
```

Description must include:

```
close #<issue_number>
```

Also:

* Assign reviewer and assignees as team leader
* In case the branch requires any fixes, the team leader will reassign it to the person responsible for making the necessary changes.

---

## Android Studio Workflow

### API Level

* API level: **28 (Android Pie)**
* Ensure all libraries support API ≥ 28

---

### Virtual Devices

Recommended:

* **Pixel 6a**

Test multiple resolutions:

* 414x896
* 360x800
* 390x844
* 384x832
* 393x873

Values are in **dp**, not px

---

### Local Passwords, Tokens, and Keys

Never store secrets or passwords in code:

```java
String pass = "PasswordABC";
```

Instead:

* Use `local.properties`
* Do not commit sensitive data
* Share secrets securely within the team

---

### Code Structure

* Use generated structure when possible
* Create an Activity for new views

Guidelines:

* Place class-level variables at the top
* Use getters/setters instead of public fields
* Use proper naming conventions:

| Element           | Convention           |
| ----------------- | -------------------- |
| Variables/Methods | camelCase            |
| Classes           | PascalCase           |
| Layouts/Resources | snake_case           |
| Constants         | SCREAMING_SNAKE_CASE |

* Store UI text in `strings.xml`
* Store colors in `colors.xml`

This ensures clean, maintainable code.
