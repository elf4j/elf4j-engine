#!/usr/bin/env bash

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

# Tag and push a new version of the project from the root directory

# Ensure the script is run from the root of the project
[ -f "pom.xml" ] || { echo "Run from the project root with pom.xml"; exit 1; }

# Ensure the working directory is clean
[ -z "$(git status --porcelain)" ] || { echo "Working directory is not clean, please commit or stash your changes"; exit 1; }

# Ensure current POM version is a valid semver
mvn semver:verify-current >/dev/null || { echo "Current POM version is not a valid semver version"; exit 1; }

# Get the current version from the pom.xml file
CURRENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout | tr -d "[:space:]")

# Create and push a new tag with the current version
git tag -a "$CURRENT_VERSION" -m "Release version tag $CURRENT_VERSION"
git push origin "$CURRENT_VERSION"

echo "Tagged and pushed version $CURRENT_VERSION."