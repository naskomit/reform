package sysmo.reform.app

import sysmo.reform.react.ReactComponent

trait Panel extends ReactComponent{
  def apply(app_config: Configuration): Unmounted
}
