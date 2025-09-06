# Quiz Flow

A web app that allows users to generate personalized quizzes by randomly selecting questions from a predefined set.

## Description

- 📝 **Create Custom Quizzes:** Easily build personalized quizzes and exams.

- 🔄 **Randomized Questions:** Generate unique tests with random question selection.

- 📚 **Large Question Pool:** Access a wide range of curated questions.

- 🌐 **Easy to Use:** Simple, intuitive platform for all users.

## Setup dev env

- **Install:** Intellij + Temurin OpenJDK-21
- **Enter shell session:** `nix-shell --run bash`
- **Build(without frontend):** `./gradlew clean build -PbuildType=RELEASE|DEBUG`
  - `DEBUG` (default) takes ~5s
  - `RELEASE` takes ~60s
- **Build(with frontend):** `./gradlew clean :webapp-ui:runNpmBuild build -PbuildType=DEBUG`

- **Run:**
  - **native:** `./webapp/build/packaged/quiz-flow-0.0.1.kexe`
  - **jvm:** `java -jar ./webapp/build/packaged/quiz-flow-0.0.1.jar`

## Debug

- Run: `./gradlew clean build -PbuildType=DEBUG`

- In Intellij create debug profile for jar and run `quiz-flow/webapp/build/packaged/quiz-flow-0.0.1.jar`
  
  ---

- For the frontend run: `cd webapp-ui/` & `npm run start`

Upload file:

  ```sh
  curl \
    -F "testid=1" \
    -F "filecomment=This is an text file" \
    -F "file1=@/$PROJECT_ROOT/test.txt" \
    http://localhost:8080/api/upload
  ```

## Build / Run native

```sh
cd ./native/simple
gcc -fPIC -shared -o libsimple.so simple.c

# reset path to dynamic lib, origin means path to the executable
patchelf --set-rpath '$ORIGIN' ./webapp/build/bin/native/debugExecutable/quiz-flow-0.0.1.kexe
# or
LD_LIBRARY_PATH=".:$LD_LIBRARY_PATH" ./quiz-flow-0.0.1.kexe
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
