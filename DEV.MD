# Development related stuff

```sh
git submodule add -b tags/version-3.50.4 git@github.com:sqlite/sqlite.git external/sqlite

# Clone a Repo with Submodules
git submodule init
git submodule update

git submodule status 

git submodule update --remote external/sqlite
git submodule deinit -f external/sqlite
git rm --cached external/sqlite
```
