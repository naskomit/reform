package sysmo.reform.shared.data

import sysmo.reform.shared.util.Ref

package object graph {
  type VCRef = Ref[VertexSchema]
  type ECRef = Ref[EdgeSchema]
  type OVCRef = Option[Ref[VertexSchema]]
}
