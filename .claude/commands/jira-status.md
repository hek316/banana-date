---
description: Check current Jira ticket and git status
---

You are helping the user check their current work status. Follow these steps:

1. **Git Status**
   - Run `git branch --show-current` to show current branch
   - Run `git status` to show working tree status
   - If on feature branch, extract ticket number from branch name

2. **Show uncommitted changes**
   - If there are uncommitted changes, list them
   - Show staged vs unstaged files

3. **Show commits ahead of main**
   - Run `git log --oneline main..HEAD`
   - Count number of commits

4. **Jira Ticket Status (if on feature branch)**
   - Extract ticket number (e.g., SCRUM-38) from branch name
   - Use `mcp__atlassian__getJiraIssue` to get ticket details
   - Display:
     - Ticket title
     - Current status
     - Assignee
     - Created/Updated dates

5. **Check for open PR**
   - Run `gh pr list --head <current-branch>`
   - If PR exists, show PR number and URL

6. **Summary**
   - Display concise status report:
     ```
     📋 Ticket: SCRUM-XX - {title}
     📊 Status: {Jira status}
     🌿 Branch: {branch name}
     💾 Commits: {count} ahead of main
     📝 Changes: {X} modified, {Y} staged
     🔗 PR: {#number or "Not created"}
     ```

7. **Next steps suggestion**
   - Based on status, suggest next command:
     - Uncommitted changes → "Commit your changes first"
     - No PR → "Run `/jira-test` then `/jira-pr SCRUM-XX`"
     - PR exists → "Run `/jira-complete SCRUM-XX` to merge"

**Important:**
- Works on any branch
- Provides quick overview without making changes
- Helpful for checking where you left off
