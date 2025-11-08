---
description: Run tests before completing Jira ticket (Playwright for frontend, API tests for backend)
---

You are helping the user test their changes before completing a Jira ticket. Follow these steps:

1. **Detect what changed**
   - Run `git status` to see modified files
   - Run `git diff --name-only main` to see all changes vs main
   - Determine if changes are frontend, backend, or both

2. **Run appropriate tests based on changes**

   **If frontend changes detected:**
   - Ask user if they want to run Playwright tests
   - Start frontend dev server if needed: `cd frontend && npm run dev`
   - Use Playwright MCP to:
     - Navigate to affected pages
     - Test user interactions
     - Verify UI rendering
     - Take screenshots for verification
   - Stop dev server after testing

   **If backend changes detected:**
   - Ask user if they want to run API tests
   - Start backend if needed: `cd backend && ./gradlew bootRun`
   - Run integration tests: `cd backend && ./gradlew test`
   - If specific endpoints changed, offer to make actual API calls to verify

   **If both changed:**
   - Run both frontend and backend tests sequentially

3. **Check build process**
   - Frontend: `cd frontend && npm run build`
   - Backend: `cd backend && ./gradlew build`
   - Report any errors or warnings

4. **Summary report**
   - Display test results
   - Show build status
   - List any issues found
   - If all tests pass, suggest: "Ready to create PR! Use `/jira-pr SCRUM-XX`"

**Important:**
- Always run tests appropriate to what changed
- Don't skip tests even if changes seem minor
- Report all test failures clearly
- Verify builds succeed before allowing PR creation
