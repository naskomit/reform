VERBOSE:=vv

dev/setup:
	ansible-playbook -$(VERBOSE) 05_dev_setup_local.yaml --ask-become-pass

build/config:
	ansible-playbook -$(VERBOSE) 10_configure_variables.yaml

build/dockers:
	ansible-playbook -$(VERBOSE) 11_build_docker_images.yaml

build/files:
	ansible-playbook -$(VERBOSE) 12_build_gen_config.yaml

install_pkgs:
	ansible-playbook -$(VERBOSE) 20_server_install_packages.yaml

stack/deploy: build/files
	ansible-playbook -$(VERBOSE) 30_stack_deploy.yaml

stack/up:
	ansible-playbook -$(VERBOSE) 31_stack_start.yaml

auth/setup:
	ansible-playbook -$(VERBOSE) 50_auth_setup.yaml


