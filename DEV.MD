# Development related stuff

## TODO

- Rename quiz-flow-client/browser -> quiz-flow-ui
- Acceptance tests - rename GlobalConfig to GlobalContext
- Add static analyzer detekt
- Add formatter klint
- Add version explicit definition on build
- KSP (Kotlin Symbolic Processing) - annotations that allows generating classes before compile task
- Java impl should use the exactly same dynamic libraries that the native target uses

```sh
git submodule add -b tags/version-3.50.4 git@github.com:sqlite/sqlite.git external/sqlite
git submodule add -b future git@github.com:rg3/libbcrypt.git external/libbcrypt

# Clone a Repo with Submodules
git submodule init
git submodule update

git submodule status 

git submodule update --remote external/sqlite
git submodule deinit -f external/sqlite
git rm --cached external/sqlite
rm -rf ./.git/modules/external/

./gradlew wrapper --gradle-version latest
```

## Upload file

```sh
curl \
    -H "Content-Type: multipart/form-data" \
    -F "testid=1" \
    -F "filecomment=This is an text file" \
    -F "file1=@/$PROJECT_ROOT/test.txt" \
    http://localhost:8080/api/upload

curl -X POST http://localhost:8080/api/upload2 \
    -H "Content-Type: multipart/form-data" \
    -F "testid=1" \
    -F "filecomment=This is an text file" \
    -F "file1=@/$PROJECT_ROOT/test.txt" 
    
```

## Requests

```sh
curl -i http://localhost:8080/api/health-check


# questions set endpoints
curl -i -X POST http://localhost:8080/api/question-sets -H "Content-Type: application/json" -d '{"name":"English 1","description":"Some simple english questions"}'

curl -X PUT http://localhost:8080/api/question-sets/8bde044c-0199-1000-c76c-000089860bf7 -H "Content-Type: application/json" -d '{"name":"English Quiz","description":"A simple English language quiz"}' 

curl -i -X GET http://localhost:8080/api/question-sets -H "Content-Type: application/json"

curl -X GET http://localhost:8080/api/question-sets/8bde044c-0199-1000-c76c-000089860bf7 -H "Content-Type: application/json"

curl -X DELETE http://localhost:8080/api/question-sets/8bde044c-0199-1000-c76c-000089860bf7 -H "Content-Type: application/json"

# question-sets/{question_set_id}/questions endpoints
curl -X POST http://localhost:8080/api/question-sets/8bde044c-0199-1000-c76c-000089860bf7/questions \
    -H "Content-Type: multipart/form-data" \
    -F "testid=1" \
    -F "filecomment=This is an text file" \
    -F "file=@/$PROJECT_ROOT/../docubase-documents/Java/OCP21/OCP21.MD"

curl -i -X GET http://localhost:8080/api/question-sets/8bde044c-0199-1000-c76c-000089860bf7/questions/8afb9805-0199-1000-fbe4-00009ce065d8


# users
curl -X POST http://localhost:8080/api/users \
    -H "Authorization: Bearer af2ea80001991000d0ab00005a8a1466af2ea80001991000dbb30000cfeebf7a" \
    -H "Content-Type: application/json" \
    -d '{"username":"test","password":"test"}'

curl -X POST http://localhost:8080/api/users \
    -H "Content-Type: application/json" \
    -d '{"username":"test1","password":"test1"}'

curl -X GET http://localhost:8080/api/users

curl \
    -F "filecomment=This is an text file" \
    -F "file=@/$PROJECT_ROOT/../docubase-documents/Java/OCP21/OCP21.MD"
    

curl -X POST http://localhost:8080/api/question-sets/8bde044c-0199-1000-c76c-000089860bf7/questions \
    -H "Content-Type: multipart/form-data" \
    -F "testid=1" \
    -F "filecomment=This is an text file" \
    -F "file=@/$PROJECT_ROOT/../docubase-documents/Java/OCP21/OCP21.MD"

# authorize as super admin
curl -i -X POST http://localhost:8080/api/authorizations \
    -H "Content-Type: application/json" \
    -d '{"username":"admin1","password":"admin"}'

```

## Run the app

```sh
# reset path to dynamic lib, origin means path to the executable
patchelf --set-rpath '$ORIGIN' ./quiz-flow-0.0.1.kexe
# or
LD_LIBRARY_PATH=".:$LD_LIBRARY_PATH" ./quiz-flow-0.0.1.kexe
```

### Compile Sqlite (outdated)

```sh
# 1. download sqlite-amalgamation-3500400.zip from https://www.sqlite.org/download.html

gcc -fPIC -shared -o libsqlite3.so sqlite3.c -DSQLITE_OMIT_LOAD_EXTENSION -DSQLITE_THREADSAFE=0

gcc -fPIC -shared -o libsqlite3.so sqlite3.c
```
