package com.commercetools.queue

import fs2.Chunk

//TODO mladens consider renaming to MessageContextBatch
trait MessageBatch[F[_], T] {
  def messages: Chunk[Message[F, T]]
  def ackAll: F[Unit]
  def nackAll: F[Unit]
}
