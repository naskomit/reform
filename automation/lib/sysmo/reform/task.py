from .action import Action

class Task:
  def __init__(self, name: str):
    self.name = name
    self.actions = []

  def __add__(self, action: Action):
    self.actions.append(action)
    return self

  def execute(self):
    print("Begin task: {}".format(self.name))
    for action in self.actions:
      action.execute()
    print("End task: {}".format(self.name))
