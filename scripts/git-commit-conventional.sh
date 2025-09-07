#!/bin/sh

# see: https://www.conventionalcommits.org/en/v1.0.0/

main() {
    # feat: A new feature or functionality added to the project.
    # fix: A bug fix or a patch for a bug.
    # docs: Documentation changes or improvements.
    # chore: Routine tasks, such as updating dependencies or other non-functional changes.
    # style: Changes that don't affect the functionality of the code but improve its style, such as code formatting, missing semicolons, or whitespace adjustments.
    # refactor: Code changes that neither fix a bug nor add a feature, but make the code structure or readability better.
    # perf: Performance improvements that make the system run faster or use fewer resources.
    # test: Adding or modifying tests, or fixing test-related issues.
    local types=("feat" "fix" "docs" "chore" "style" "refactor" "perf" "test")

    echo "Select the type of commit:"
    select type in "${types[@]}"; do
        if [[ -n "$type" ]]; then
            break
        else
            echo "Invalid selection. Please choose a valid commit type."
        fi
    done

    echo "Enter the commit description:"
    read -r description

    commit_message="$type: $description"

    echo "Your commit message is:"
    echo "$commit_message"
    echo "Command:"
    echo "git add . && git commit -m '$commit_message'"
}

main "$@"
