
```scala
/** Invoked once, only on the client (not on the server), immediately after the initial rendering occurs. At this point
  * in the lifecycle, the component has a DOM representation which you can access via `ReactDOM.findDOMNode(this)`.
  * The `componentDidMount()` method of child components is invoked before that of parent components.
  *
  * If you want to integrate with other JavaScript frameworks, set timers using `setTimeout` or `setInterval`, or send
  * AJAX requests, perform those operations in this method.
  */
def componentDidMount[G[_]](f: ComponentDidMount[P, S, B] => G[Unit])(implicit G: Dispatch[G]): This =
  lcAppendDispatch(LifecycleF.componentDidMount)(f)
```

```scala
object Effect extends EffectFallbacks {
  trait Dispatch[F[_]] extends Effect[F] {
    /** Fire and forget, could be sync or async */
    def dispatch[A](fa: F[A]): Unit
    def dispatchFn[A](fa: => F[A]): js.Function0[Unit]
  }
}
```

```scala
trait Effect[F[_]] extends Monad[F] {
  def delay      [A]         (a: => A)                           : F[A]
  def handleError[A, AA >: A](fa: => F[A])(f: Throwable => F[AA]): F[AA]
  def suspend    [A]         (fa: => F[A])                       : F[A]

  /** Wraps this callback in a `try-finally` block and runs the given callback in the `finally` clause, after the
    * current callback completes, be it in error or success.
    */
  def finallyRun[A, B](fa: => F[A], runFinally: => F[B]): F[A]

  @inline final def throwException[A](t: Throwable): F[A] =
    delay(throw t)
}
```

```scala

```

```scala

```

```scala

```

```scala

```

```scala

```