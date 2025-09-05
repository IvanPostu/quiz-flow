#!/bin/sh

# Define the types of commits (you can add more if needed)
types=("feat" "fix" "docs" "chore" "style" "refactor" "perf" "test")

# Prompt the user to choose a commit type
echo "Select the type of commit:"
select type in "${types[@]}"; do
    if [[ -n "$type" ]]; then
        break
    else
        echo "Invalid selection. Please choose a valid commit type."
    fi
done

# Prompt for the commit message description
echo "Enter the commit description:"
read -r description

commit_message="$type: $description"

# Output the commit message and ask for confirmation
echo "Your commit message is:"
echo "$commit_message"
echo "Command:"
echo "git add . && git commit -m '$commit_message'"
