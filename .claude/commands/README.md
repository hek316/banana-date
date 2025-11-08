# Claude Code Commands for Jira Workflow

This directory contains custom slash commands for automating Jira ticket workflow with GitHub integration.

## Available Commands

### 1. `/jira-start SCRUM-XX`
**Purpose**: Start working on a Jira ticket

**What it does:**
- Syncs with remote repository (`gh repo sync`)
- Creates feature branch (`feature/SCRUM-XX`, `fix/SCRUM-XX`, etc.)
- Transitions Jira ticket to "진행 중"
- Adds comment to Jira with branch name

**Example:**
```bash
/jira-start SCRUM-38
```

---

### 2. `/jira-test`
**Purpose**: Run tests before completing ticket

**What it does:**
- Detects changed files (frontend/backend)
- Runs appropriate tests:
  - Frontend: Playwright MCP tests + build
  - Backend: Integration tests + build
- Reports test results and build status

**Example:**
```bash
/jira-test
```

---

### 3. `/jira-pr SCRUM-XX`
**Purpose**: Create Pull Request for ticket

**What it does:**
- Verifies branch and commits
- Pushes branch to remote
- Creates PR with auto-generated title and description
- Adds PR link as comment to Jira ticket
- Optionally transitions ticket to "검토 중"

**Example:**
```bash
/jira-pr SCRUM-38
```

---

### 4. `/jira-complete SCRUM-XX`
**Purpose**: Complete ticket (merge PR and close Jira)

**What it does:**
- Finds associated PR
- Verifies PR is ready (checks passed)
- Merges PR with chosen strategy (squash/merge/rebase)
- Transitions Jira ticket to "완료"
- Adds completion comment with PR link
- Deletes feature branch
- Switches back to main

**Example:**
```bash
/jira-complete SCRUM-38
```

---

### 5. `/jira-status`
**Purpose**: Check current work status

**What it does:**
- Shows current branch and git status
- Displays Jira ticket details
- Shows PR status if exists
- Suggests next steps

**Example:**
```bash
/jira-status
```

---

## Typical Workflow

```bash
# 1. Start working on ticket
/jira-start SCRUM-38

# 2. Make your changes, commit regularly
git add .
git commit -m "feat(scope): description"

# 3. Test your changes
/jira-test

# 4. Create Pull Request
/jira-pr SCRUM-38

# 5. After review, complete the ticket
/jira-complete SCRUM-38

# 6. Check status anytime
/jira-status
```

## Requirements

- **GitHub CLI**: `gh` must be installed and authenticated
- **Atlassian MCP**: Jira integration must be configured
- **Playwright MCP**: For frontend testing (optional)
- **Git**: Repository must be initialized

## Tips

- Use `/jira-status` to check where you are in the workflow
- Run `/jira-test` before creating PR to ensure everything works
- The commands will prompt for confirmation before destructive actions
- All commands follow the workflow defined in `CLAUDE.md`

## Command Files

- `jira-start.md` - Start ticket workflow
- `jira-test.md` - Testing automation
- `jira-pr.md` - PR creation
- `jira-complete.md` - Ticket completion
- `jira-status.md` - Status checker

## Customization

You can modify these commands by editing the `.md` files in this directory. Each file contains:
- `description`: Short description shown in command list
- Detailed instructions for Claude to follow
