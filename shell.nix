let 
  pkgs = import <nixpkgs> { config = { allowUnfree = true; }; };
in
pkgs.mkShell {
  name = "app-shell";

  buildInputs = [
    pkgs.jdk17
    pkgs.libxcrypt-legacy # required for .kexe app to run
    
  ];

  LANG = "en_US.UTF-8";
  LC_ALL = "en_US.UTF-8";

  shellHook = ''
        export LD_LIBRARY_PATH="${pkgs.libxcrypt-legacy}/lib:$LD_LIBRARY_PATH"

        export JAVA_HOME=${pkgs.jdk17}
        export PATH=${pkgs.jdk17}/bin:$PATH
  '';
}
