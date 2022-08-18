package sysmo.reform.shared

package object runtime {
  type FLocal[+T] = Either[Throwable, T]
}
