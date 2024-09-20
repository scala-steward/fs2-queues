package com.commercetools.queue.aws.sqs

import cats.effect.Async
import cats.implicits.toFunctorOps
import com.commercetools.queue.{Message, MessageBatch}
import fs2.Chunk
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.{ChangeMessageVisibilityBatchRequest, ChangeMessageVisibilityBatchRequestEntry, DeleteMessageBatchRequest, DeleteMessageBatchRequestEntry}

private class SQSMessageBatch[F[_], T](
  payload: Chunk[SQSMessageContext[F, T]],
  client: SqsAsyncClient,
  queueUrl: String
)(implicit F: Async[F])
  extends MessageBatch[F, T] {

  override def messages: Chunk[Message[F, T]] = payload

  override def ackAll: F[Unit] =
    F.fromCompletableFuture {
      F.delay {
        client.deleteMessageBatch(
          DeleteMessageBatchRequest
            .builder()
            .queueUrl(queueUrl)
            .entries(payload.map(m => DeleteMessageBatchRequestEntry.builder().id(m.receiptHandle).build()).asJava)
            .build()
        )
      }
    }.void
  // TODO adapt error

  override def nackAll: F[Unit] = F.fromCompletableFuture {
    F.delay {
      client.changeMessageVisibilityBatch(
        ChangeMessageVisibilityBatchRequest
          .builder()
          .queueUrl(queueUrl)
          .entries(
            payload
              .map(m =>
                ChangeMessageVisibilityBatchRequestEntry
                  .builder()
                  .receiptHandle(m.receiptHandle)
                  .visibilityTimeout(0)
                  .build())
              .asJava
          )
          .build()
      )
    }
  }.void // TODO adapt error
}
