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

# Prompt for an optional scope
echo "Enter the scope (optional, press Enter to skip):"
read -r scope

# Prompt for the commit message description
echo "Enter the commit description:"
read -r description

# Format the commit message
if [[ -n "$scope" ]]; then
    commit_message="$type($scope): $description"
else
    commit_message="$type: $description"
fi

# Output the commit message and ask for confirmation
echo "Your commit message is:"
echo "$commit_message"
echo "git commit -m '$commit_message'"
