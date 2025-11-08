---
description: Create GitHub Pull Request for Jira ticket and add comment to Jira
---

You are helping the user create a Pull Request for their Jira ticket. Follow these steps:

1. **Get the ticket number**
   - Extract from user's command (e.g., SCRUM-38)
   - If not provided, try to extract from current branch name
   - If still not found, ask the user

2. **Verify current state**
   - Check current branch: `git branch --show-current`
   - Verify branch matches ticket (feature/SCRUM-XX, fix/SCRUM-XX, etc.)
   - Check if there are uncommitted changes: `git status`
   - If uncommitted changes exist, ask user to commit first

3. **Get Jira ticket details**
   - Use `mcp__atlassian__getJiraIssue` to fetch ticket info
   - Extract title, description, and current status

4. **Review changes**
   - Show commit history: `git log --oneline main..HEAD`
   - Show file changes: `git diff --name-only main`
   - Summarize what changed (files, features, fixes)

5. **Push branch to remote**
   - Run `git push -u origin <current-branch>`

6. **Create Pull Request**
   - Generate PR title: `[SCRUM-XX] {Jira ticket title}`
   - Generate PR body with:
     ```markdown
     ## Summary
     {Brief summary of changes based on git diff and commits}

     ## Changes
     - {List key changes from commits}

     ## Testing
     - [ ] Playwright tests passed (for frontend)
     - [ ] API tests passed (for backend)
     - [ ] Build successful
     - [ ] Manual testing completed

     ## Jira
     Related to SCRUM-XX

     {Original Jira ticket description}
     ```
   - Create PR: `gh pr create --title "..." --body "..."`

7. **Add comment to Jira ticket**
   - Use `mcp__atlassian__addCommentToJiraIssue`
   - Comment: "Pull Request created: {PR URL}"

8. **Transition to "검토 중" (optional)**
   - Ask user if they want to move ticket to "검토 중" status
   - If yes, use `mcp__atlassian__transitionJiraIssue`

9. **Display PR URL and next steps**
   - Show PR link
   - Suggest: "Review the PR, then use `/jira-complete SCRUM-XX` to merge and complete"

**Important:**
- Always push branch before creating PR
- Include comprehensive PR description
- Link PR back to Jira ticket
- Verify PR was created successfully
