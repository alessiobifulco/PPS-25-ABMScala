package domain

trait Memory[S]

object Memory:

  def empty[S]: Memory[S] = MemoryImpl(List.empty)

  private case class MemoryImpl[S](events: List[S]) extends Memory[S]
