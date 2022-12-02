import types
from ..util.registrant import registrant
from yaml import load, dump
try:
    from yaml import CLoader as Loader, CDumper as Dumper
except ImportError:
    from yaml import Loader, Dumper

from dataclasses import dataclass, field
from typing import Dict
from ..docker_compose.model import DockerService, DockerCompose
from abc import ABC, ABCMeta, abstractmethod
from copy import deepcopy

class ServiceConfig:
  pass


class ServiceImpl():
  @classmethod
  def decode_config(data: dict):
    raise NotImplementedError

  def modify_docker_compose(docker: DockerService, config: ServiceConfig):
    raise NotImplementedError


@dataclass(slots = True)
@registrant("impl")
class ServiceDef():
  name: str
  docker: DockerService
  config: ServiceConfig

  @classmethod
  def decode(cls, name, data: dict):
    impl: ServiceImpl = cls._get_impl(name)
    config = impl.decode_config(data.get('config', {}))
    return ServiceDef(
      name = name,
      docker = DockerService.decode(name, data['docker']),
      config = config
    )

  def get_impl(self) -> ServiceImpl: 
    return self.__class__._get_impl(self.name)

  def gen_docker_compose(self):
    docker = deepcopy(self.docker)
    self.get_impl().modify_docker_compose(docker, self.config)
    return docker

@dataclass(slots = True)
class ServiceStackDef:
  network: str
  services: list[ServiceDef]
  
  def from_file(file: str):
    with open(file) as f:
      data = load(f, Loader)
    return ServiceStackDef.decode(data)

  def gen_docker_compose(self):
    docker_services = [service.gen_docker_compose() for service in self.services]
    docker_compose = DockerCompose(docker_services, [{self.network: None}])
    return docker_compose

  def decode(data: dict):
    return ServiceStackDef(
      network = data['network'],
      services = [ServiceDef.decode(name, service) for name, service in data['services'].items()]
    )