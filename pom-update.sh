#!/bin/bash

#
# MIT License
#
# Copyright (c) 2023 Qingtian Wang
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#

# Function to display help menu
show_help() {
    cat <<EOF
Usage: $0 [options]

Options:
  -h, --help       Show this help menu
  -c, --check      Check for available stable dependency and plugin updates
  -u, --update     Update dependencies and plugins to the latest stable releases
  -a, --all        Check and update dependencies and plugins
  -n, --no-clean   Do not remove backup files created by the Maven Versions plugin

Example:
  $0 --check      # Only check for updates
  $0 --update     # Only update dependencies/plugins
  $0 --all        # Check and update everything
EOF
    exit 0
}

# Function to check if Maven is installed
check_maven() {
    if ! command -v mvn &>/dev/null; then
        echo "Error: Maven (mvn) is not installed or not found in PATH."
        exit 1
    fi
}

# Function to check for available updates
check_updates() {
    echo "Checking for dependency updates (stable releases only)..."
    mvn versions:display-dependency-updates -DallowSnapshots=false || {
        echo "Error: Failed to check dependency updates."
        exit 1
    }

    echo "Checking for plugin updates (stable releases only)..."
    mvn versions:display-plugin-updates -DallowSnapshots=false || {
        echo "Error: Failed to check plugin updates."
        exit 1
    }
}

# Function to update dependencies and plugins
update_dependencies() {
    echo "Updating dependencies and plugins to the latest stable versions..."

    # Ensure dependencies (including major versions) are updated
#    mvn versions:use-latest-versions -DgenerateBackupPoms=false || {
    mvn versions:use-latest-releases -DgenerateBackupPoms=false || {
        echo "Error: Failed to update dependencies."
        exit 1
    }

    # Update dependencies managed in <properties>
    mvn versions:update-properties -DgenerateBackupPoms=false || {
        echo "Error: Failed to update property-managed dependencies."
        exit 1
    }

    echo "Ensuring the latest plugin versions are set..."
    mvn versions:use-latest-releases -Dincludes="org.apache.maven.plugins:*" -DgenerateBackupPoms=false || {
#    mvn versions:use-latest-versions -Dincludes="org.apache.maven.plugins:*" -DgenerateBackupPoms=false || {
        echo "Error: Failed to update Maven plugins."
        exit 1
    }

    echo "All dependencies and plugins updated successfully."
}


# Function to clean up backup files
cleanup() {
    echo "Cleaning up backup files created by the Maven Versions plugin..."
    find . -name "pom.xml.versionsBackup" -delete
}

# Default flags
CHECK_ONLY=false
UPDATE_ONLY=false
CLEANUP=true

# Parse command-line arguments
while [[ "$#" -gt 0 ]]; do
    case "$1" in
        -h|--help) show_help ;;
        -c|--check) CHECK_ONLY=true ;;
        -u|--update) UPDATE_ONLY=true ;;
        -a|--all) CHECK_ONLY=true; UPDATE_ONLY=true ;;
        -n|--no-clean) CLEANUP=false ;;
        *) echo "Unknown option: $1"; show_help ;;
    esac
    shift
done

# Ensure Maven is installed
check_maven

# Execute actions based on provided options
if [[ "$CHECK_ONLY" == "true" ]]; then
    check_updates
fi

if [[ "$UPDATE_ONLY" == "true" ]]; then
    update_dependencies
fi

if [[ "$UPDATE_ONLY" == "true" && "$CLEANUP" == "true" ]]; then
    cleanup
fi

# If no options provided, show help
if [[ "$CHECK_ONLY" == "false" && "$UPDATE_ONLY" == "false" ]]; then
    show_help
fi

echo "Done."
exit 0
