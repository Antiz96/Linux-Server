# Add package

Install a package or a list of packages.

Support for Debian, Arch Linux and Alpine Linux.

## Variables

The following variables can be set at the inventory level, the playbook level or as `--extra-vars` *(at least one of them should not be undefined / empty)*:

- package_debian: name of a package or array of packages to install on Debian hosts (examples: `package` or `"['package1', 'package2', 'package3']"`).
- package_arch: name of a package or array of packages to install on Arch Linux hosts (examples: `package` or `"['package1', 'package2', 'package3']"`).
- package_alpine: name of a package or array of packages to install on Alpine Linux hosts (examples: `package` or `"['package1', 'package2', 'package3']"`).
