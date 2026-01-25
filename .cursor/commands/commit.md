# Git Commit Based on Changes

Generate a commit message from staged changes and create the commit.

## Workflow

1. **Check for staged changes**
   - Run `git diff --cached --quiet` to check if there are staged changes
   - If no staged changes exist, inform the user they need to stage files first using `git add`
   - If staged changes exist, proceed to the next step

2. **Analyze staged changes**
   - Get the list of staged files: `git diff --cached --name-only`
   - Get the full diff: `git diff --cached`
   - Review the changes to understand:
     - What files were modified
     - What functionality was added, changed, or removed
     - The scope and impact of the changes

3. **Generate commit message**
   - Create a commit message that:
     - Uses imperative mood ("Add feature" not "Added feature" or "Adds feature")
     - Is clear and concise (ideally under 72 characters for the subject line)
     - Accurately describes what the changes do
     - Follows conventional commit format if the project uses it (e.g., `feat:`, `fix:`, `refactor:`, `docs:`, etc.)
     - If multiple changes are present, consider if they should be separate commits or if a single commit is appropriate
   - Consider any additional context provided by the user after `/commit`

4. **Present commit message for review**
   - Display the generated commit message
   - Show a summary of what files will be committed
   - Ask the user to confirm before proceeding

5. **Execute the commit**
   - Once confirmed, execute: `git commit -m "<generated message>"`
   - If the user wants to edit the message, allow them to provide a modified version
   - Only commit after explicit user confirmation

## Guidelines

- Always check for staged changes before attempting to commit
- Generate meaningful commit messages that clearly describe the changes
- Never commit without user confirmation
- If the user provides additional context (e.g., `/commit fix typo in README`), incorporate that into the message generation
- Respect the project's commit message conventions if they exist
