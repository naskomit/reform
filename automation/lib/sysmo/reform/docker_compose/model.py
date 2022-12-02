from dataclasses import dataclass, field
from typing import List, Dict, Union

from yaml import load, dump
try:
    from yaml import CLoader as Loader, CDumper as Dumper
except ImportError:
    from yaml import Loader, Dumper

import subprocess

@dataclass(slots = True)
class DockerBuild:
  context: str
  dockerfile: str
  def decode(data: dict):
    return DockerBuild(
      context = data["context"],
      dockerfile = data["dockerfile"]
    )

  def encode(self):
    data = {
      "context": self.context,
      "dockerfile": self.dockerfile
    }
    return data


@dataclass(slots = True)
class DockerService:
  name: str
  image: Union[str, DockerBuild]
  container_name: str = None
  networks: List[str] = field(default_factory = list)
  environment: Dict[str, str] = field(default_factory = dict)
  ports: List[str] = field(default_factory = list)
  volumes: List[str] = field(default_factory = list)
  user: str = None
  command: str = None

  def decode_image(data):
    result = data.get("image", None)
    if result == None:
      result = DockerBuild.decode(data.get("build"))
    return result

  def decode(name, data: dict):
    return DockerService(
      name = name,
      image = DockerService.decode_image(data),
      networks = data.get("networks", []),
      environment = data.get("environment", {}),
      ports = data.get("ports", []),
      volumes = data.get("volumes", []),
    )
  def encode(self) -> dict:
    data = {}
    data['container_name'] = self.container_name or self.name
    if isinstance(self.image, str):
      data['image'] = self.image
    else:
      data['build'] = self.image.encode()
    if (len(self.environment) > 0):
      data['environment'] = self.environment
    if (len(self.networks) > 0):
      data['networks'] = self.networks
    if (len(self.ports) > 0):
      data['ports'] = self.ports
    if (len(self.volumes) > 0):
      data['volumes'] = self.volumes
    if self.user != None:
      data['user'] = self.user
    if self.command != None:
      data['command'] = self.command

    return data


@dataclass(slots = True)
class DockerCompose:
  services: List[DockerService] = field(default_factory = list)
  networks: List[str] = field(default_factory = list)
  volumes: List[str] = field(default_factory = list)
  
  def from_file(file: str):
    with open(file) as f:
      data = load(f, Loader)
    return DockerCompose.decode(data)

  def to_file(self, file: str):
    with open(file, 'w') as f:
      return dump(self.encode(), stream = f, sort_keys=False)


  def decode(data: dict):    
    return DockerCompose(
      services = [DockerService.decode(name, data) for name, data in data['services'].items()],
      networks = data['networks'],
      volumes = data.get('volumes', [])
    )

  def encode(self):
    data = {}
    data["version"] = "3.4"
    data["services"] = {service.name: service.encode() for service in self.services}
    data['networks'] = self.networks
    if len(self.volumes) > 0:
      data['volumes'] = self.volumes
    return data
    
  