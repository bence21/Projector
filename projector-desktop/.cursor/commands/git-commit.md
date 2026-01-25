# Git Commit

## Description
Provides an interactive git commit workflow that allows users to select files to stage, generates commit messages from changes, and commits with user approval.

## Instructions

When the user requests a git commit, follow these steps:

1. **Check Git Status**
   - Run `git status` to identify all modified, staged, and untracked files
   - Parse the output to categorize files by status:
     - Modified files (M)
     - Staged files (staged for commit)
     - Untracked files (new files)
     - Deleted files (D)
   - Run `git branch --show-current` to get the current branch name
   - If no changes exist, inform the user: "Working directory clean - nothing to commit" and exit

2. **Check for Merge Conflicts**
   - Look for merge conflict markers in `git status` output
   - If conflicts exist, warn the user: "Merge conflicts detected. Please resolve conflicts before committing." and exit

3. **Interactive File Selection**
   - Present all available files to the user with their status:
     - Show modified files (not yet staged)
     - Show untracked files
     - Show deleted files
     - Note: Already staged files will be included automatically
   - Use the `AskQuestion` tool to present file selection options
   - Format the question to allow multiple file selection
   - Group files by status for clarity
   - Example format:
     ```
     Which files would you like to stage for commit?
     
     Modified files:
     - [ ] src/main/java/projector/network/TCPClient.java
     - [ ] src/main/java/projector/controller/MyController.java
     
     Untracked files:
     - [ ] .cursor/commands/git-commit.md
     
     Already staged:
     - [x] src/main/java/projector/controller/SettingsController.java
     ```
   - Allow user to select multiple files
   - If all files are already staged, skip this step and proceed to message generation

4. **Stage Selected Files**
   - For each selected file, run `git add <filepath>`
   - Verify staging with `git status --short` to confirm files are staged
   - Display confirmation: "Staged X files for commit"
   - If no files were selected and nothing is staged, inform user and exit

5. **Generate Commit Message**
   - Analyze staged changes using `git diff --staged`
   - Run `git diff --staged --stat` to get file statistics
   - For each staged file, analyze the diff to identify:
     - New methods/classes added
     - Methods/classes modified
     - Methods/classes removed
     - Key logic changes
     - Bug fixes
     - Configuration changes
   - Generate a suggested commit message following conventional commit format:
     - **feat:** for new features
     - **fix:** for bug fixes
     - **refactor:** for code refactoring
     - **docs:** for documentation changes
     - **style:** for formatting changes
     - **test:** for test additions/modifications
     - **chore:** for build/tooling changes
   - Create a concise subject line (50-72 characters)
   - Optionally add a body with key changes (if multiple significant changes)
   - Format example:
     ```
     feat: add auto-connect loop functionality to TCPClient
     
     - Implemented startAutoConnectLoop() and stopAutoConnectLoop() methods
     - Added connection loss listener for automatic reconnection
     - Updated close() method to handle connection state properly
     ```

6. **User Message Approval/Edit**
   - Display the generated commit message in a clear format
   - Use the `AskQuestion` tool to present options:
     - Accept the generated message
     - Edit the message (provide text input)
     - Provide a completely custom message
   - If user chooses to edit, show the generated message as a starting point
   - If user provides custom message, use it as-is
   - Validate that message is not empty (if empty, prompt again)

7. **Execute Commit**
   - Run `git commit -m "<message>"` with the final approved message
   - Capture the output to get the commit hash
   - Display commit result:
     - Success: "Commit created successfully: [commit hash]"
     - Failure: Show error message and help user resolve

8. **Post-Commit Summary**
   - Show what was committed:
     - Commit hash
     - Commit message
     - Files included in commit
     - Branch name
   - Run `git status` to check for remaining uncommitted changes
   - If uncommitted changes remain, list them
   - Suggest next steps:
     - Push to remote: "You can push with: git push"
     - Continue working: "X files still have uncommitted changes"
     - Clean state: "Working directory clean"

## Example Output Format

### Step 1: File Selection
```markdown
## Git Commit - File Selection

Current branch: main

### Files Available for Staging

**Modified files (not staged):**
- src/main/java/projector/network/TCPClient.java
- src/main/java/projector/controller/MyController.java

**Untracked files:**
- .cursor/commands/git-commit.md

**Already staged:**
- src/main/java/projector/controller/SettingsController.java

Which files would you like to stage?
```

### Step 2: Staging Confirmation
```markdown
✓ Staged 3 files for commit:
  - src/main/java/projector/network/TCPClient.java
  - src/main/java/projector/controller/MyController.java
  - .cursor/commands/git-commit.md
```

### Step 3: Generated Commit Message
```markdown
## Generated Commit Message

```
feat: add auto-connect loop and git commit command

- Added auto-connect loop functionality to TCPClient
- Implemented connection loss listener for automatic reconnection
- Created interactive git commit Cursor command
- Updated controller to use auto-connect loop
```

Would you like to:
1. Accept this message
2. Edit this message
3. Provide a custom message
```

### Step 4: Commit Success
```markdown
✓ Commit created successfully!

**Commit:** a1b2c3d4e5f6
**Message:** feat: add auto-connect loop and git commit command
**Branch:** main
**Files:** 4 files changed, +127 lines added, -1 line removed

### Remaining Changes
Working directory clean - all changes committed.

### Next Steps
You can push your changes with: `git push`
```

## Edge Cases

1. **No changes to commit**
   - Check `git status` - if clean, inform user and exit
   - Message: "Working directory clean - nothing to commit"

2. **All files already staged**
   - Skip file selection step
   - Proceed directly to message generation
   - Note: "All changes are already staged"

3. **Merge conflicts detected**
   - Check for conflict markers in status
   - Warn user: "Merge conflicts detected. Please resolve conflicts before committing."
   - Exit without committing

4. **Empty commit message**
   - If user provides empty message, prompt again
   - Suggest: "Commit message cannot be empty. Please provide a message."

5. **Binary files**
   - Note binary files in the file list
   - Don't attempt to analyze binary file content for message generation
   - Still stage them if user selects them

6. **Very large changes (>500 lines)**
   - Summarize key sections rather than listing all changes
   - Focus on major features/fixes for commit message
   - Note: "Large change set - summarizing key modifications"

7. **Commit fails (e.g., pre-commit hook)**
   - Display error message from git
   - Help user understand what went wrong
   - Suggest solutions if possible

## Notes

- Always use relative file paths in the output
- Focus on meaningful changes (new features, bug fixes) rather than minor formatting
- Generate concise but descriptive commit messages
- Follow conventional commit format when appropriate
- Provide clear feedback at each step
- Use proper markdown formatting for readability
- Respect user's choice for commit message (don't override custom messages)
- If user cancels at any step, exit gracefully without committing
