let 
  pkgs = import <nixpkgs> { config = { allowUnfree = false; }; };
  PROJECT_ROOT = builtins.toString ./.;
in
pkgs.mkShell {
  name = "app-shell";

  buildInputs = [
    pkgs.temurin-bin-21
    pkgs.pkgs.stdenv.cc.cc.lib # libstdc++.so
    pkgs.libxcrypt-legacy # required for .kexe app to run
    pkgs.zlib
    pkgs.nodejs_22
    pkgs.patchelf # is used to override path to dynamic libraries for executables
    pkgs.gcc13
  ];

  LANG = "en_US.UTF-8";
  LC_ALL = "en_US.UTF-8";

  shellHook = ''
        export PROJECT_ROOT="${PROJECT_ROOT}"
        export LD_LIBRARY_PATH="${pkgs.stdenv.cc.cc.lib}/lib:$LD_LIBRARY_PATH"
        export LD_LIBRARY_PATH="${pkgs.libxcrypt-legacy}/lib:$LD_LIBRARY_PATH"
        export LD_LIBRARY_PATH="${pkgs.zlib}/lib:$LD_LIBRARY_PATH"
        
        export LD_LIBRARY_PATH="${PROJECT_ROOT}/native/simple:$LD_LIBRARY_PATH"
        export LD_LIBRARY_PATH="${PROJECT_ROOT}/native/sqlite-amalgamation-3500400:$LD_LIBRARY_PATH"
        export LD_LIBRARY_PATH="${PROJECT_ROOT}/native/bcrypt:$LD_LIBRARY_PATH"

        export JAVA_HOME=${pkgs.temurin-bin-21}
        export PATH=${pkgs.temurin-bin-21}/bin:$PATH

        export DEBUG_APPLICATION_ROOT_FOLDER="$PROJECT_ROOT/webapp/build/packaged"

        chmod -R +x ${PROJECT_ROOT}/scripts
        export PATH=${PROJECT_ROOT}/scripts:$PATH
  '';
}
