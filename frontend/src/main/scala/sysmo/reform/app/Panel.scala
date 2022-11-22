package sysmo.reform.app

import sysmo.reform.widgets.ReactComponent

trait Panel extends ReactComponent{
  def apply(): Unmounted
}
