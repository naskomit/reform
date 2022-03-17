package sysmo.reform.components



trait ReactComponent {
  type Props <: Product
  type State <: Product
  type Backend
}
