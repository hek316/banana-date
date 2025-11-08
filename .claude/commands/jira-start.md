---
description: Start working on a Jira ticket (create branch and transition to "진행 중")
---

You are helping the user start work on a Jira ticket. Follow these steps:

1. **Get the ticket number from the user's command**
   - The user will provide the ticket number (e.g., SCRUM-38)
   - If no ticket number is provided, ask for it

2. **Fetch the Jira ticket details**
   - Use `mcp__atlassian__getJiraIssue` to get ticket information
   - Display the ticket title and current status
   - Confirm with the user before proceeding

3. **Ensure on main branch**
   - Run `git checkout main` to switch to main branch
   - Run `git pull` or `gh repo sync` to get latest changes (if remote configured)

4. **Transition Jira ticket to "진행 중"**
   - Use `mcp__atlassian__transitionJiraIssue` to move ticket to "진행 중" status
   - Add a comment that work has started

5. **Confirm completion**
   - Show current branch: `git branch --show-current`
   - Display Jira ticket new status
   - Show next steps: "Start working on the ticket on main branch, then use `/jira-test` when ready"

**Important:**
- Work directly on main branch (no feature branches)
- Verify ticket exists before transitioning
- Ensure main branch is up to date before starting work
