package com.commercetools.queue.gcp.pubsub

import cats.effect.Async
import cats.implicits.toFunctorOps
import com.commercetools.queue.{Message, MessageBatch}
import com.google.cloud.pubsub.v1.stub.SubscriberStub
import com.google.pubsub.v1.{AcknowledgeRequest, ModifyAckDeadlineRequest, SubscriptionName}
import fs2.Chunk

private class PubSubMessageBatch[F[_], T](
  payload: Chunk[PubSubMessageContext[F, T]],
  subscriptionName: SubscriptionName,
  subscriber: SubscriberStub
)(implicit F: Async[F])
  extends MessageBatch[F, T] {
  override def messages: Chunk[Message[F, T]] = payload

  override def ackAll: F[Unit] =
    wrapFuture(
      F.delay(
        subscriber
          .acknowledgeCallable()
          .futureCall(
            AcknowledgeRequest
              .newBuilder()
              .setSubscription(subscriptionName.toString)
              .addAllAckIds(payload.map(_.underlying.getAckId).asJava)
              .build()))).void // TODO mladens adapt error

  override def nackAll: F[Unit] = wrapFuture(
    F.delay(
      subscriber
        .modifyAckDeadlineCallable()
        .futureCall(
          ModifyAckDeadlineRequest
            .newBuilder()
            .setSubscription(subscriptionName.toString)
            .setAckDeadlineSeconds(0)
            .addAllAckIds(payload.map(_.underlying.getAckId).asJava)
            .build()))).void // TODO mladens adapt error
}
