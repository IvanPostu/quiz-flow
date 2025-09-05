let 
  pkgs = import <nixpkgs> { config = { allowUnfree = true; }; };
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
  ];

  LANG = "en_US.UTF-8";
  LC_ALL = "en_US.UTF-8";

  shellHook = ''
        export PROJECT_ROOT="${PROJECT_ROOT}"
        export LD_LIBRARY_PATH="${pkgs.stdenv.cc.cc.lib}/lib:$LD_LIBRARY_PATH"
        export LD_LIBRARY_PATH="${pkgs.libxcrypt-legacy}/lib:$LD_LIBRARY_PATH"
        export LD_LIBRARY_PATH="${pkgs.zlib}/lib:$LD_LIBRARY_PATH"

        export JAVA_HOME=${pkgs.temurin-bin-21}
        export PATH=${pkgs.temurin-bin-21}/bin:$PATH

        chmod -R +x ${PROJECT_ROOT}/scripts
        export PATH=${PROJECT_ROOT}/scripts:$PATH
  '';
}
