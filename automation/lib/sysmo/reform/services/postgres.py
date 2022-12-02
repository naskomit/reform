from dataclasses import dataclass

from sysmo.reform.docker_compose.model import DockerService
from .model import ServiceImpl, ServiceConfig, ServiceDef

@dataclass
class PostgresConfig(ServiceConfig):
  admin_user: str
  admin_password: str

class PostgresImpl(ServiceImpl):
    def decode_config(data):
      return PostgresConfig(
        admin_user = data['admin']['user'],
        admin_password = data['admin']['password']
      )
      
    def modify_docker_compose(docker: DockerService, config: PostgresConfig):
      docker.environment["POSTGRES_USER"] = config.admin_user
      docker.environment["POSTGRES_PASSWORD"] = config.admin_password



ServiceDef._register_impl("postgres", PostgresImpl)