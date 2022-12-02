def registrant(id: str): #"impl"
  def registrant_fn(cls):
    cls.registry = {}

    def register(name, decode):
      cls.registry[name] = decode

    def get(name):
      return cls.registry[name]

    setattr(cls, "_register_" + id, register)
    setattr(cls, "_get_" + id, get)
    
    return cls
  return registrant_fn

# @registrant("impl")
# class AAA:
#   pass

# class BBB(AAA):
#   def hello():
#     print("Hello from the registered method")
#   AAA.register_impl("Hello", hello)

# aaa = AAA()

# aaa.get_impl("Hello")()