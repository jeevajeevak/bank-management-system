# 📤 GitHub Upload Guide — JavaBank

Follow these steps exactly to publish your project and get an automatic EXE build.

---

## PART 1 — Create GitHub Repository

1. Go to https://github.com and sign in
2. Click the **+** icon (top right) → **New repository**
3. Fill in:
   - Repository name: `bank-management-system`
   - Description: `Java Swing desktop banking application with MySQL`
   - Visibility: **Public** (required for free GitHub Actions)
   - ❌ Do NOT check "Add README" (we already have one)
4. Click **Create repository**
5. Copy the URL shown — looks like:
   `https://github.com/YOUR_USERNAME/bank-management-system.git`

---

## PART 2 — Update Your Username in README

Open `README.md` and replace every occurrence of:
```
YOUR_USERNAME
```
with your actual GitHub username.

---

## PART 3 — Push Code to GitHub

Open a terminal / Command Prompt in the `BankManagementSystem/` folder:

```bash
# Step 1: Initialize git
git init

# Step 2: Add all files
git add .

# Step 3: First commit
git commit -m "Initial commit: JavaBank Management System v1.0"

# Step 4: Set main branch
git branch -M main

# Step 5: Connect to GitHub (paste YOUR repo URL)
git remote add origin https://github.com/YOUR_USERNAME/bank-management-system.git

# Step 6: Push!
git push -u origin main
```

---

## PART 4 — Create a Release (triggers EXE build)

```bash
# Tag version 1.0.0
git tag v1.0.0

# Push the tag — this starts the GitHub Actions EXE build!
git push origin v1.0.0
```

Then go to your repo on GitHub → **Actions** tab → watch the build run (~5 minutes).

When done → **Releases** tab → your EXE will be there!

---

## PART 5 — Verify Everything Looks Good

Your GitHub repo should have:
```
📁 .github/workflows/build-and-release.yml   ← auto EXE builder
📁 setup/                                     ← wizard for manual setup
📁 sql/schema.sql                             ← database setup
📁 src/bank/...                               ← all Java source files
📄 pom.xml                                   ← Maven build
📄 README.md                                 ← with badges
📄 LICENSE                                   ← MIT
📄 .gitignore                                ← no passwords committed
```

---

## ⚠️ Security Checklist

Before pushing, confirm these files are NOT in your commit:
- ❌ `src/bank/util/DBConnection.java` (listed in .gitignore — has your password)
- ❌ `lib/*.jar` (listed in .gitignore — too large for GitHub)
- ❌ `out/` or `target/` (compiled files — not needed)

The `.gitignore` handles all of this automatically.

---

## 🎉 Result

After following these steps you will have:
- ✅ A professional GitHub repository
- ✅ Automatic Windows EXE builds on every release tag
- ✅ Download page with .exe and .jar files
- ✅ No passwords in your code
- ✅ CI/CD badges in your README
