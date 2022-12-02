from abc import ABC, abstractmethod
import subprocess

class Action(ABC):
    @abstractmethod
    def execute(self):
        pass

class ShellAction(Action):
    def __init__(self, cmd: str):
        self.cmd = cmd
        self.shell = "/bin/bash"

    def execute(self):
        print("Executing: {}".format(self.cmd))
        subprocess.run([self.shell, "-c" , self.cmd])
