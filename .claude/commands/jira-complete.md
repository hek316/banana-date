---
description: Complete Jira ticket (merge PR and transition ticket to "완료")
---

You are helping the user complete their Jira ticket work. Follow these steps:

1. **Get the ticket number**
   - Extract from user's command (e.g., SCRUM-38)
   - If not provided, ask the user

2. **Find the associated PR**
   - Run `gh pr list` to list open PRs
   - Look for PR with [SCRUM-XX] in title
   - If multiple found, ask user to select
   - If none found, inform user to create PR first with `/jira-pr`

3. **Verify PR is ready to merge**
   - Run `gh pr view <number>` to check status
   - Check if:
     - PR is approved (if required)
     - CI/CD checks passed
     - No merge conflicts
   - If not ready, report issues and stop

4. **Confirm with user**
   - Display PR details (title, changes, checks status)
   - Ask: "Ready to merge and complete SCRUM-XX? (yes/no)"
   - If no, stop process

5. **Merge Pull Request**
   - Ask user merge strategy preference:
     - Squash (recommended for clean history)
     - Merge (keeps all commits)
     - Rebase
   - Run `gh pr merge <number> --squash --delete-branch`
   - Or `gh pr merge <number> --merge --delete-branch`
   - Capture PR URL for Jira comment

6. **Sync main branch locally**
   - Run `git checkout main`
   - Run `gh repo sync`

7. **Transition Jira ticket to "완료"**
   - Use `mcp__atlassian__transitionJiraIssue` to move to "완료"
   - Add final comment with:
     ```
     ✅ Completed

     PR merged: {PR URL}
     Merged at: {timestamp}
     ```

8. **Clean up**
   - Delete local feature branch: `git branch -d <branch-name>`
   - Confirm deletion

9. **Display completion summary**
   - Show:
     - ✅ PR merged: {URL}
     - ✅ Branch deleted
     - ✅ Jira ticket completed
     - ✅ Back on main branch
   - Suggest: "Ready for next ticket! Use `/jira-start SCRUM-XX`"

**Important:**
- Only merge if PR checks pass
- Always transition Jira AFTER successful merge
- Add PR link to Jira completion comment
- Clean up local branches
- Verify on main branch before finishing
