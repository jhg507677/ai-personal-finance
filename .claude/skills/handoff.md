---
name: handoff
description: Create a HANDOFF.md file documenting current progress for the next agent
---

# Handoff Documentation

When the user invokes this skill, create a comprehensive HANDOFF.md file that allows a fresh agent to continue the work seamlessly.

## Instructions

1. **Analyze the current conversation** to understand:
   - What task the user originally requested
   - What has been attempted so far
   - What worked and what didn't work
   - Current state of the codebase
   - Any blockers or issues encountered

2. **Create HANDOFF.md** in the project root with the following structure:

```markdown
# Handoff Document

## Original Task
[Clearly state what the user wanted to accomplish]

## Current Status
[Summary of where things stand now]

## What Has Been Tried

### ✅ What Worked
- [List successful attempts, approaches, and solutions]
- [Include file paths, code changes, commands that worked]

### ❌ What Didn't Work
- [List failed attempts and why they failed]
- [Include error messages, issues encountered]
- [Explain what was learned from failures]

## Current State of Codebase
- [List modified files and their current state]
- [Note any uncommitted changes]
- [Describe the file structure relevant to this task]

## Next Steps
[Clear, actionable steps for the next agent to complete the task]

1. [First step]
2. [Second step]
3. [etc.]

## Important Context
- [Any critical information the next agent needs to know]
- [User preferences or requirements]
- [Technical constraints or dependencies]

## Questions/Blockers
[Any unresolved questions or blockers that need user input]
```

3. **Be thorough but concise** - include enough detail that a fresh agent can pick up exactly where you left off without needing to read the entire conversation history

4. **Save the file** to the project root directory

5. **Confirm to the user** that the handoff document has been created and where it's located
