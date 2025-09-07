# Development related stuff

```sh
git submodule add -b version-3.50.4 git@github.com:sqlite/sqlite.git external/sqlite # may need git submodule add --force
git submodule init
git submodule update
git submodule update --remote external/sqlite
git submodule deinit -f external/sqlite
git rm --cached external/sqlite
```
