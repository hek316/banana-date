# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

바나나 데이트 (Banana Date) - 서울 지역 기반 데이트 코스 추천 및 매칭 서비스

**모노레포 구조**: Frontend (Next.js) + Backend (Spring Boot) + Infrastructure (Terraform)

## Architecture

### Monorepo Structure
```
banana-date/
├── frontend/          # Next.js 16 + TypeScript + Tailwind CSS
├── backend/           # Spring Boot 3.3.5 + Java 21 + PostgreSQL
├── terraform/         # AWS Infrastructure as Code (ECS, RDS, ALB, VPC)
└── .github/workflows/ # CI/CD (Frontend/Backend 자동 배포)
```

### Infrastructure
- **Frontend**: Next.js App Router with standalone output for Docker
- **Backend**: Spring Boot with JPA, OAuth2 (Kakao/Google), JWT
- **Database**: PostgreSQL 16
- **Deployment**: AWS ECS Fargate + ALB + RDS (Terraform managed)
- **CI/CD**: GitHub Actions로 ECR → ECS (코드 푸시 시 자동 배포)

### Key Integration Points
- **API Communication**: Frontend calls Backend via `/api/*` paths (ALB routing)
- **Authentication**: OAuth2 (Kakao, Google) + JWT tokens
- **External APIs**: Kakao Local API, Kakao Map API, OpenAI/Claude for recommendations

## Development Commands

### Local Development (Recommended)

**1. Start PostgreSQL only:**
```bash
docker-compose -f docker-compose.dev.yml up -d
```

**2. Run Backend:**
```bash
cd backend
./gradlew bootRun
# Runs on http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

**3. Run Frontend:**
```bash
cd frontend
npm run dev
# Runs on http://localhost:3000
```

### Backend Commands

```bash
cd backend

# Build
./gradlew build

# Run tests
./gradlew test

# Clean build
./gradlew clean build

# Skip tests
./gradlew build -x test

# Refresh dependencies
./gradlew build --refresh-dependencies
```

### Frontend Commands

```bash
cd frontend

# Development
npm run dev

# Production build
npm run build

# Start production server (after build)
npm start

# Lint
npm run lint
```

### Docker Commands

```bash
# Full stack (all services)
docker-compose up

# PostgreSQL only (dev mode)
docker-compose -f docker-compose.dev.yml up -d

# Stop services
docker-compose down

# View logs
docker-compose logs postgres
docker-compose logs -f backend
```

### Terraform Commands

**Infrastructure is managed manually from local (NOT automated via GitHub Actions)**

```bash
cd terraform

# Initialize
terraform init

# Plan changes
terraform plan

# Apply changes
terraform apply

# View outputs
terraform output
terraform output alb_dns_name

# Destroy infrastructure
terraform destroy
```

## Environment Variables

### Backend (.env or environment)
```bash
DB_HOST=localhost
DB_USERNAME=bananadate
DB_PASSWORD=bananadate123
JWT_SECRET=your-secret-key-minimum-256-bits
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

### Frontend (.env.local)
```bash
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_KAKAO_REST_API_KEY=your_key
NEXT_PUBLIC_GOOGLE_CLIENT_ID=your_client_id
NEXT_PUBLIC_KAKAO_MAP_API_KEY=your_map_key
```

## Code Architecture

### Backend (Spring Boot)
- **Package Structure**: `com.bananadate.*`
- **Main Application**: `BananaDateApplication.java`
- **Database**: JPA with QueryDSL, PostgreSQL driver
- **Security**: Spring Security + OAuth2 + JWT
- **API Docs**: SpringDoc OpenAPI (Swagger)
- **Profiles**: `local` (dev), `prod` (production)

### Frontend (Next.js)
- **App Router**: All routes in `app/` directory
- **Config**: `next.config.ts` with `output: 'standalone'` for Docker
- **TypeScript**: Strict mode enabled, path alias `@/*`
- **Styling**: Tailwind CSS 4

### Infrastructure (Terraform)
- **Modules**: VPC, ECR, ECS, RDS, ALB
- **Environment**: Production only (`prod`)
- **State**: S3 backend with DynamoDB locking
- **Resources naming**: `banana-date-{resource}-prod`

## Deployment Process

### Infrastructure Deployment (Manual)
1. Update Terraform code in `terraform/`
2. Run `terraform plan` to review changes
3. Run `terraform apply` to deploy

### Application Deployment (Automated)
1. Push code to `main` branch
2. GitHub Actions automatically:
   - Builds Docker image
   - Pushes to ECR
   - Updates ECS service

**Triggers:**
- Frontend: Changes in `frontend/**`
- Backend: Changes in `backend/**`

## Testing Requirements

**IMPORTANT**: Before marking any Jira ticket as complete:

### Frontend Changes
- Use Playwright MCP to verify UI functionality
- Test user interactions and navigation
- Verify API integration with backend

### Backend Changes
- Make actual API calls to verify endpoints
- Run integration tests
- Verify database operations
- Check API responses match specifications

## Git Workflow

### Branch Strategy
```
main                    # Production branch (auto-deploys to AWS)
└── feature/SCRUM-XX    # Feature branch for each Jira ticket
└── fix/SCRUM-XX        # Bug fix branch for each Jira ticket
└── refactor/SCRUM-XX   # Refactoring branch for each Jira ticket
```

### Jira Ticket Workflow

**IMPORTANT**: When working on a Jira ticket, ALWAYS follow this workflow using GitHub CLI (`gh`):

1. **Create Feature Branch**
   ```bash
   # Get latest main
   gh repo sync

   # Create branch from ticket number
   git checkout -b feature/SCRUM-XX
   # or
   git checkout -b fix/SCRUM-XX
   ```

2. **Work on the Ticket**
   - Make changes in the feature branch
   - Commit regularly with clear messages
   - Keep commits atomic and focused
   ```bash
   git add <files>
   git commit -m "feat(scope): description"
   ```

3. **Before Completing Ticket**
   - Run all required tests (Playwright for frontend, API tests for backend)
   - Verify functionality works as expected
   - Ensure code follows project conventions

4. **Push and Create Pull Request**
   ```bash
   # Push branch to remote
   git push -u origin feature/SCRUM-XX

   # Create PR using GitHub CLI
   gh pr create --title "[SCRUM-XX] Feature title" \
     --body "## Summary
   - Changes made

   ## Testing
   - [ ] Playwright tests passed
   - [ ] Manual testing completed

   Closes SCRUM-XX"
   ```

5. **Merge Pull Request**
   ```bash
   # Review PR
   gh pr view

   # Merge PR (squash or merge)
   gh pr merge --squash --delete-branch
   # or
   gh pr merge --merge --delete-branch
   ```

6. **Complete Jira Ticket**
   - ONLY after successful merge to main
   - Transition ticket to "완료" status
   - Add comment with PR link and merge details

### Commit Message Convention

**Format**: `<type>(<scope>): <subject>`

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `style`: Code formatting, missing semicolons, etc.
- `refactor`: Code refactoring without functionality change
- `test`: Adding or updating tests
- `chore`: Build process, dependencies, configs
- `perf`: Performance improvements

**Examples:**
```bash
feat(auth): implement Kakao OAuth2 login
fix(date-picker): resolve timezone issue
docs(readme): update local development setup
refactor(api): restructure recommendation service
test(frontend): add Playwright tests for home page
chore(deps): upgrade Spring Boot to 3.3.5
```

**Scope** (optional): Specify the area affected
- `frontend`, `backend`, `terraform`, `auth`, `api`, `db`, etc.

### Branch Naming Convention

- `feature/SCRUM-XX` - New features
- `fix/SCRUM-XX` - Bug fixes
- `refactor/SCRUM-XX` - Code refactoring
- `docs/SCRUM-XX` - Documentation updates
- `test/SCRUM-XX` - Test additions/updates

**Examples:**
```bash
feature/SCRUM-38  # Next.js 프론트엔드 초기 설정
fix/SCRUM-45      # PostgreSQL connection pool issue
refactor/SCRUM-52 # Restructure recommendation algorithm
```

### Git & GitHub CLI Commands

**Repository Management:**
```bash
# Clone repository
gh repo clone <owner>/<repo>

# Sync with remote (fetch + merge)
gh repo sync

# View repository info
gh repo view

# Set default branch
gh repo edit --default-branch main
```

**Branch Management:**
```bash
# List branches
git branch -a

# Switch branch
git checkout <branch-name>

# Create and switch to new branch
git checkout -b feature/SCRUM-XX

# Delete local branch
git branch -d feature/SCRUM-XX

# Delete remote branch
git push origin --delete feature/SCRUM-XX
```

**Pull Request Workflow:**
```bash
# Create PR
gh pr create --title "Title" --body "Description"

# Create PR with template
gh pr create --web

# List PRs
gh pr list

# View PR details
gh pr view <number>

# Check out PR locally
gh pr checkout <number>

# Review PR
gh pr review --approve
gh pr review --comment --body "LGTM"
gh pr review --request-changes --body "Please fix..."

# Merge PR
gh pr merge <number> --squash --delete-branch
gh pr merge <number> --merge --delete-branch
gh pr merge <number> --rebase --delete-branch

# Close PR
gh pr close <number>
```

**Issue Management:**
```bash
# Create issue
gh issue create --title "Bug: Something broke" --body "Details..."

# List issues
gh issue list

# View issue
gh issue view <number>

# Close issue
gh issue close <number>
```

**Status & Logs:**
```bash
# Check status
git status

# View commit history
git log --oneline
git log --graph --oneline --all

# View changes
git diff
git diff --staged

# View file history
git log -p <file>
```

**Stash Changes:**
```bash
# Stash changes
git stash

# List stashes
git stash list

# Apply stash
git stash pop
git stash apply

# Drop stash
git stash drop
```

## Troubleshooting

### PostgreSQL Connection Issues
```bash
docker-compose ps
docker-compose logs postgres
lsof -i :5432
```

### Backend Build Issues
```bash
cd backend
./gradlew clean
./gradlew build --refresh-dependencies
```

### Frontend Build Issues
```bash
cd frontend
rm -rf node_modules package-lock.json .next
npm install
```

### Port Conflicts
```bash
lsof -i :3000  # Frontend
lsof -i :8080  # Backend
lsof -i :5432  # PostgreSQL
```

## Project Management

- **Jira**: All tasks tracked in Jira (Atlassian MCP integration available)
- **Project**: SCRUM board
- **Epic**: SCRUM-36 (MVP)
- **Workflow**: 해야 할 일 → 진행 중 → 검토 중 → 완료
