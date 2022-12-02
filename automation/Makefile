VENV_PATH:= ${PWD}/env-dev
PYTHON_VERSION:=3.10

# External commands
setup: venv/create venv/install_pkgs venv/add-reform

remove:
	rm -rf $(VENV_PATH)

# Internal commands

venv/install_pkgs:
	/bin/bash -c "source $(VENV_PATH)/bin/activate && pip install -r requirements.txt"
	

venv/create:
	python3 -m venv $(VENV_PATH)

venv/add-reform:
	/bin/bash -c "echo ${PWD}/lib > $(VENV_PATH)/lib/python$(PYTHON_VERSION)/site-packages/reform.pth"

#venv/ln-activate:
#	ln -s $(VENV_PATH)/bin/activate start



