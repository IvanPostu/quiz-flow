# Quiz Flow

A  web app that lets users create custom exams by randomly selecting questions.

## Description

- 📝 **Create Custom Quizzes:** Easily build personalized quizzes and exams.

- 🔄 **Randomized Questions:** Generate unique tests with random question selection.

- 📚 **Large Question Pool:** Access a wide range of curated questions.

- 🌐 **Easy to Use:** Simple, intuitive platform for all users.

## Setup dev env

- **Install:** Intellij + Temurin OpenJDK-21
- **Enter shell session:** `nix-shell --run bash`
- **Build:** `./gradlew clean build -PbuildType=RELEASE|DEBUG`
  - `DEBUG` (default) takes ~5s
  - `RELEASE` takes ~60s
- **Run:** `./webapp/build/bin/native/quiz-flowReleaseExecutable/quiz-flow.kexe`

    ---

- **Debug:** `./gradlew :webapp:jvmRun -DmainClass=com.iv127.quizflow.webapp.MainKt`

## References

1. <https://github.com/ktorio/ktor-documentation/blob/3.2.3/codeSnippets/snippets/embedded-server-native/README.md>
2. <https://github.com/Kotlin/kmp-logic-sharing-simple-example/tree/main>
3. <https://regex101.com/>
4. <https://nequalsonelifestyle.com/2020/11/16/kotlin-native-file-io/>
