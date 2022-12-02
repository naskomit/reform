    
class DockerExecAction(Action):
  def __init__(self, container: str, cmd:str):
    self.container = container
    self.cmd = cmd

  def execute(self):
    print("Executing (container: {}): {}".format(self.container, self.cmd))
    subprocess.run(["docker", "compose", "exec", self.container, self.cmd])

class DockerContainerNode:
  def __init__(self, name: str):
    self.name = name

  def exec(self, cmd) -> DockerExecAction:
    return DockerExecAction(self.name, cmd)


      