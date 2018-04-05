# MIDIBlocks
This is the respository for the Software portion of the MIDIBlocks project [ENGG2800@UQ 2015]

## Getting Started

### Step 1: Download
Clone the repository to your computer.

### Step 2: IDE Files
Generate the configuration files for your IDE of choice

```
cd engg2800g07

# If using eclipse
./gradlew eclipse

# If using intellij idea
./gradlew idea
```

### Step 3: Project Import
Import the project into your IDE in the way you typically would.

--------

## Building/Running The Project
Gradle should be used for all this stuff.

```bash
# build the project
./gradlew build

# run the tests
./gradlew test

# run the app
./gradlew runApp
```

Note that we are using `./gradlew build` rather than `gradle build`. This is because we are using the gradle wrapper. This allows everyone to build the project the same irrespective what system they are running to build the program, and everyone can build the project without having to first install and configure a specific version of Gradle.

