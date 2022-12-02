from sysmo.reform.docker_compose.model import DockerCompose
from sysmo.reform.services.model import ServiceStackDef
from sysmo.reform.services import postgres

def test_codec_docker_file():
  compose = DockerCompose.from_file("../../docker/dev/docker-compose.yaml")
  compose.to_file("out/docker_compose.yaml")

def test_gen_docker_file():
  service_stack = ServiceStackDef.from_file("in/services.yaml")
  service_stack.gen_docker_compose().to_file("out/docker_compose.yaml")


test_gen_docker_file()