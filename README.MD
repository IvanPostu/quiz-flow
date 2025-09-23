# Quiz Flow

A web app that allows users to generate personalized quizzes by randomly selecting questions from a predefined set.

## Description

- 📝 **Create Custom Quizzes:** Easily build personalized quizzes

- 🔄 **Randomized Questions:** Generate unique quizzes with random question selection

- 🌐 **Easy to Use:** Simple, intuitive platform for all users

## Setup environment

### Prerequisites

- Intellij
- Openjdk 21 (Temurin OpenJDK-21)
- Nix shell

### Setup

```sh
nix-shell --run bash

git submodule init
git submodule update

npm install --prefix ./webapp-ui

gcc -fPIC -shared -o native/simple/libsimple.so native/simple/simple.c

# build sqlite amalgamation, See reference 13
cd $PROJECT_ROOT/external/sqlite \
  && ./configure && make sqlite3.c \
  && cp -f $PROJECT_ROOT/external/sqlite/sqlite3.c $PROJECT_ROOT/native/sqlite-amalgamation-3500400/sqlite3.c \
  && cp -f $PROJECT_ROOT/external/sqlite/sqlite3.h $PROJECT_ROOT/native/sqlite-amalgamation-3500400/sqlite3.h \
  && git clean -fdx \
  && cd $PROJECT_ROOT

# build sqlite dynamic library
cd $PROJECT_ROOT/native/sqlite-amalgamation-3500400 \
  && gcc -fPIC -shared -o libsqlite3.so sqlite3.c \
  && cd $PROJECT_ROOT

cd $PROJECT_ROOT/external/libbcrypt \
  && ( make EXTRA_CFLAGS="-fPIC" || [ -e ./libbcrypt.so.1.0.0 ] ) \
  && cp -f $PROJECT_ROOT/external/libbcrypt/bcrypt.h $PROJECT_ROOT/native/bcrypt/bcrypt.h \
  && cp -f $PROJECT_ROOT/external/libbcrypt/libbcrypt.so.1.0.0 $PROJECT_ROOT/native/bcrypt/libbcrypt.so.1 \
  && git clean -fdx \
  && cd $PROJECT_ROOT
```

### JVM debug

```sh
./gradlew build -PbuildType=DEBUG
# IntelliJ idea: create debug profile for JAR - quiz-flow/webapp/build/packaged/quiz-flow-0.0.1.jar

# Since DEBUG_APPLICATION_ROOT_FOLDER env variable was specified in shell.nix
# the app can be run directly using Intellij.

# clean and build
./gradlew clean webapp:package build -PbuildType=DEBUG -Pkotlin.native.cacheKind=none
```

### Frontend development

```sh
npm install --prefix ./webapp-ui
npm run start --prefix ./webapp-ui
```

### Release

```sh
./gradlew clean :webapp-ui:runNpmBuild build -PbuildType=RELEASE -Pkotlin.native.cacheKind=none
```

### Run

```sh
# Run native executable 
./webapp/build/packaged/quiz-flow-0.0.1.kexe
# Or
LD_LIBRARY_PATH=".:$LD_LIBRARY_PATH" ./quiz-flow-0.0.1.kexe

# Run jvm bundle
java -jar ./webapp/build/packaged/quiz-flow-0.0.1.jar
```

## References

1. <https://github.com/ktorio/ktor-documentation/blob/3.2.3/codeSnippets/snippets/embedded-server-native/README.md>
2. <https://github.com/Kotlin/kmp-logic-sharing-simple-example/tree/main>
3. <https://regex101.com/>
4. <https://nequalsonelifestyle.com/2020/11/16/kotlin-native-file-io/>
5. <https://stackoverflow.com/questions/59596589/how-to-set-up-a-kotlin-native-project-so-that-it-places-resources-alongside-the>
6. Lilleaas, August (2023). *Pro Kotlin Web Apps from Scratch*.
7. <https://thejoeflow.com/post/handling-multipart-requests-with-ktor>
8. <https://dev.to/programadorthi/kotlin-routing-routing-everything-1f4j>
9. <https://stackoverflow.com/questions/66232760/how-to-implement-in-kotlin-native-fileseek-getfilelength-and-readintobuffer>
10. <https://discourse.nixos.org/t/how-to-refer-to-current-directory-in-shell-nix/9526/2>
11. **BEST** <https://flyingbytes.github.io/programming/android/kotlin/native/c/interop/interoperability/2022/09/10/Kotlin-Native-C-Interop.html>
12. <https://stackoverflow.com/questions/47618276/how-to-use-sqlite-in-kotlin-native>
  12.1. <https://github.com/Kotlin/kotlinconf-spinner/tree/b1e66920ac7216d3dbc4b6b4625fb77efe23cff8/sql>
13. <https://jvns.ca/blog/2019/10/28/sqlite-is-really-easy-to-compile/>
14. <https://www.conventionalcommits.org/en/v1.0.0/>
15. <https://sqlite.org/amalgamation.html>
16. <https://www.w3schools.com/git/git_submodules.asp>
17. <https://stackoverflow.com/questions/1777854/how-can-i-specify-a-branch-tag-when-adding-a-git-submodule>
18. <https://stackoverflow.com/questions/20342058/which-uuid-version-to-use>
19. <https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/>
20. <https://www.youtube.com/watch?v=nKCsIHWircA>
