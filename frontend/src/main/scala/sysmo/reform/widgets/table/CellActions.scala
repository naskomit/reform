package sysmo.reform.widgets.table

import sysmo.reform.shared.actions.Action
import sysmo.reform.shared.data.Value

case class CellActions(
                        click: Option[Value => Action] = None
                      )
