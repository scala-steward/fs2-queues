package com.commercetools.queue.azure.servicebus

import cats.effect.Async
import com.azure.messaging.servicebus.ServiceBusReceiverClient
import com.commercetools.queue.{Message, MessageBatch}
import fs2.Chunk

private class ServiceBusMessageBatch[F[_], T](
  payload: Chunk[ServiceBusMessageContext[F, T]],
  receiver: ServiceBusReceiverClient
)(implicit F: Async[F])
  extends MessageBatch[F, T] {
  override def messages: Chunk[Message[F, T]] = payload

  override def ackAll: F[Unit] = F.blocking {
    payload.foreach(mCtx => receiver.complete(mCtx.underlying))
  }

  override def nackAll: F[Unit] = F.blocking {
    payload.foreach(mCtx => receiver.abandon(mCtx.underlying))
  }
}
