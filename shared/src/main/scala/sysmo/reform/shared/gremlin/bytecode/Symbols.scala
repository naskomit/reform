package sysmo.reform.shared.gremlin.bytecode

object Symbols {
  val map = "map"
  val flatMap = "flatMap"
  val id = "id"
  val label = "label"
  val identity = "identity"
  val constant = "constant"
  val V = "V"
  val E = "E"
  val to = "to"
  val out = "out"
  val in = "in"
  val both = "both"
  val toE = "toE"
  val outE = "outE"
  val inE = "inE"
  val bothE = "bothE"
  val toV = "toV"
  val outV = "outV"
  val inV = "inV"
  val bothV = "bothV"
  val otherV = "otherV"
  val order = "order"
  val properties = "properties"
  val values = "values"
  val propertyMap = "propertyMap"
  val valueMap = "valueMap"
  val elementMap = "elementMap"
  val select = "select"
  val key = "key"
  val value = "value"
  val path = "path"
  val match_ = "match"
  val math = "math"
  val sack = "sack"
  val loops = "loops"
  val project = "project"
  val unfold = "unfold"
  val fold = "fold"
  val count = "count"
  val sum = "sum"
  val max = "max"
  val min = "min"
  val mean = "mean"
  val group = "group"
  val groupCount = "groupCount"
  val tree = "tree"
  val addV = "addV"
  val addE = "addE"
  val from = "from"
  val filter = "filter"
  val or = "or"
  val and = "and"
  val inject = "inject"
  val dedup = "dedup"
  val where = "where"
  val has = "has"
  val hasNot = "hasNot"
  val hasLabel = "hasLabel"
  val hasId = "hasId"
  val hasKey = "hasKey"
  val hasValue = "hasValue"
  val is = "is"
  val not = "not"
  val range = "range"
  val limit = "limit"
  val skip = "skip"
  val tail = "tail"
  val coin = "coin"
  val io = "io"
  val read = "read"
  val write = "write"

  val timeLimit = "timeLimit"
  val simplePath = "simplePath"
  val cyclicPath = "cyclicPath"
  val sample = "sample"

  val drop = "drop"

  val sideEffect = "sideEffect"
  val cap = "cap"
  val property = "property"

  val aggregate = "aggregate"
  val subgraph = "subgraph"
  val barrier = "barrier"
  val index = "index"
  val local = "local"
  val emit = "emit"
  val repeat = "repeat"
  val until = "until"
  val branch = "branch"
  val union = "union"
  val coalesce = "coalesce"
  val choose = "choose"
  val optional = "optional"


  val pageRank = "pageRank"
  val peerPressure = "peerPressure"
  val connectedComponent = "connectedComponent"
  val shortestPath = "shortestPath"
  val program = "program"

  val by = "by"
  val with_ = "with"
  val times = "times"
  val as = "as"
  val option = "option"
}
