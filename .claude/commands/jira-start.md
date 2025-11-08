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

3. **Sync with remote repository**
   - Run `gh repo sync` to get latest changes from main

4. **Create feature branch**
   - Determine branch type based on ticket type:
     - Story/Task → `feature/SCRUM-XX`
     - Bug → `fix/SCRUM-XX`
     - Improvement → `refactor/SCRUM-XX`
   - Run `git checkout -b <branch-name>`

5. **Transition Jira ticket to "진행 중"**
   - Use `mcp__atlassian__transitionJiraIssue` to move ticket to "진행 중" status
   - Add a comment with the branch name

6. **Confirm completion**
   - Show current branch: `git branch --show-current`
   - Display Jira ticket new status
   - Show next steps: "Start working on the ticket, then use `/jira-test` when ready"

**Important:**
- Always sync with remote before creating branch
- Use consistent branch naming based on ticket type
- Verify ticket exists before creating branch
