# Development related stuff

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
```

## Upload file

```sh
curl \
-F "testid=1" \
-F "filecomment=This is an text file" \
-F "file1=@/$PROJECT_ROOT/test.txt" \
http://localhost:8080/api/upload
```

## Requests

```sh
curl -i http://localhost:8080/api/health-check


# questions set endpoints
curl -X POST http://localhost:8080/api/question-sets -H "Content-Type: application/json" -d '{"name":"test","description":"aa111"}'

curl -i -X GET http://localhost:8080/api/question-sets -H "Content-Type: application/json"

curl -X GET http://localhost:8080/api/question-sets/1 -H "Content-Type: application/json"

curl -X DELETE http://localhost:8080/api/question-sets/1 -H "Content-Type: application/json"

curl -X PUT http://localhost:8080/api/question-sets/1 -H "Content-Type: application/json" -d '{"name":"test1","description":"1aa111"}' 

# users
curl -X POST http://localhost:8080/api/users \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin"}'

curl -X GET http://localhost:8080/api/users

curl \
    -F "filecomment=This is an text file" \
    -F "file=@/$PROJECT_ROOT/../docubase-documents/Java/OCP21/OCP21.MD" \
    http://localhost:8080/api/question-sets/1/questions
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
