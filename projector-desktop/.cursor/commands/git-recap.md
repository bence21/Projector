# Git Changes Recap

## Description
Provides a comprehensive summary of current git changes, including file statistics, detailed diffs, and suggestions for next steps.

## Instructions

When the user requests a git recap or review of changes, follow these steps:

1. **Get Git Status**
   - Run `git status` to identify all modified, staged, and untracked files
   - Parse the output to categorize files by status (modified, staged, untracked)

2. **Get Change Statistics**
   - Run `git diff --stat` to get a summary of changes (files changed, lines added/removed)
   - Parse the output to extract:
     - Total number of files changed
     - Total lines added (+X)
     - Total lines removed (-Y)

3. **Get Detailed Diffs**
   - For each modified file, run `git diff <filepath>` to get detailed changes
   - For staged files, run `git diff --staged <filepath>`
   - Analyze each diff to identify:
     - New methods/classes added
     - Methods/classes modified
     - Methods/classes removed
     - Key logic changes
     - Configuration changes

4. **Format the Output**
   Create a well-formatted markdown recap with the following structure:

   ```markdown
   ## Git Changes Recap

   ### Summary
   - **X files changed**
   - **+Y lines added, -Z lines removed**
   - **Branch**: [current branch name]
   - **Status**: [clean, has uncommitted changes, etc.]

   ### Files Modified

   #### 1. **[File1.java](path/to/File1.java)** (+N lines, -M lines)
   - **Key Changes:**
     - [Brief description of main changes]
     - [New features/methods added]
     - [Bugs fixed or logic modified]
   
   #### 2. **[File2.java](path/to/File2.java)** (+N lines, -M lines)
   - **Key Changes:**
     - [Brief description of main changes]
     - [New features/methods added]
     - [Bugs fixed or logic modified]

   [Continue for all modified files...]

   ### What to Continue With

   Based on the changes, here are suggested next steps:

   - **Testing:**
     - [Specific test suggestions based on changes]
   
   - **Documentation:**
     - [Documentation needs]
   
   - **Code Review:**
     - [Areas that might need review]
   
   - **Follow-up Tasks:**
     - [Related tasks or improvements]
   ```

5. **Generate Contextual Suggestions**
   Analyze the changes to provide relevant suggestions:
   - If network/client code changed → suggest testing connection scenarios
   - If UI/controller code changed → suggest testing user interactions
   - If new features added → suggest adding tests and documentation
   - If bugs fixed → suggest regression testing
   - If configuration changed → suggest verifying settings

6. **Handle Edge Cases**
   - If no changes exist, report "Working directory clean"
   - If only untracked files exist, list them separately
   - If changes are very large (>500 lines), summarize key sections rather than listing all changes
   - If binary files changed, note them but don't attempt to diff

## Example Output Format

```markdown
## Git Changes Recap

### Summary
- **3 files changed**
- **+127 lines added, -1 line removed**
- **Branch**: main
- **Status**: Has uncommitted changes

### Files Modified

#### 1. **[TCPClient.java](src/main/java/projector/network/TCPClient.java)** (+116 lines, -0 lines)
- **Key Changes:**
  - Added auto-connect loop functionality with `startAutoConnectLoop()` and `stopAutoConnectLoop()` methods
  - Implemented connection loss listener to automatically restart connection attempts
  - Added thread management for auto-connect daemon thread
  - Updated `close()` method to properly handle connection state updates

#### 2. **[MyController.java](src/main/java/projector/controller/MyController.java)** (+1 line, -1 line)
- **Key Changes:**
  - Changed from one-time `connectToShared()` call to `startAutoConnectLoop()` when auto-connect is enabled at startup

#### 3. **[SettingsController.java](src/main/java/projector/controller/SettingsController.java)** (+10 lines, -0 lines)
- **Key Changes:**
  - Added listener for auto-connect checkbox to start/stop auto-connect loop
  - Integrated auto-connect functionality with settings UI

### What to Continue With

Based on the changes, here are suggested next steps:

- **Testing:**
  - Test auto-reconnect functionality when connection is lost
  - Verify thread cleanup when auto-connect is stopped
  - Test checkbox toggle behavior in SettingsController
  - Test connection behavior when network is unavailable

- **Documentation:**
  - Add JavaDoc comments for new public methods (`startAutoConnectLoop`, `stopAutoConnectLoop`)
  - Document the auto-connect retry interval and timeout behavior

- **Code Review:**
  - Review thread synchronization in `startAutoConnectLoop()` to ensure no deadlocks
  - Verify proper cleanup of listeners and threads
  - Check error handling in connection loss scenarios

- **Follow-up Tasks:**
  - Consider making retry interval configurable
  - Add UI feedback/status indicator for auto-connect attempts
  - Consider adding connection attempt counter or logging improvements
```

## Notes

- Always use relative file paths in the output
- Focus on meaningful changes (new features, bug fixes) rather than minor formatting
- Provide actionable suggestions based on the actual code changes
- Keep the output concise but informative
- Use proper markdown formatting for readability
